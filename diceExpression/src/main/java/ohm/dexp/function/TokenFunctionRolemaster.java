package ohm.dexp.function;

import ohm.dexp.DContext;
import ohm.dexp.TokenBase;
import ohm.dexp.exception.DException;
import ohm.dexp.exception.LoopDetected;

public class TokenFunctionRolemaster extends TokenFunctionExplodeBase {

	private static final int INDEX_ROLL = 1;
	private static final int INDEX_EXPLODE_TARGET = 2;
	private static final int INDEX_INVERT_TARGET = 3;
	private static final int INDEX_LIMIT = 4;

	@Override
	protected int initChildNumber() {
		return 1;
	}

	@Override
	protected int initOptionalChildNumber() {
		return 3;
	};

	@Override
	public int getType() {
		return 26;
	}

	@Override
	protected void evaluateSelf(DContext instance) throws DException {
		TokenBase roll = getChild(INDEX_ROLL);
		
		roll.evaluate(instance);
		
		long target = getOptionalChildRawResult(instance, INDEX_EXPLODE_TARGET, roll.getMaxResult());
		long invert = getOptionalChildRawResult(instance, INDEX_INVERT_TARGET, roll.getMinResult());
		long limit = getOptionalChildRawResult(instance, INDEX_LIMIT, UNDEFINED);
		
		resultValue = 0;

		resultString = SYM_BEGIN; // "[";

		int iterations = 0;
		int extraRolls = 0;
		boolean loopSafe = true;
		int sign = 1;
		long currentResult;

		if (limit == UNDEFINED || limit == 0) {
			extraRolls = MAX_TOKEN_ITERATIONS;
		} else {
			extraRolls = (int) (limit / VALUES_PRECISION_FACTOR);
		}

		loopSafe = loopLessExplosion(instance, roll, target, invert);

		if (! loopSafe) {
			throw new LoopDetected(getFunctionName(this.getClass()));
		}

		do {
			if (iterations>0) {
				roll.evaluate(instance); //Evaluate here as a slight optimization
				if (resultString.length() < MAX_TOKEN_STRING_LENGTH)
					//resultString += ",";
					resultString += SYM_EXPLODE;
			}
			
			currentResult = roll.getRawResult();
			resultValue += sign * currentResult;
			
			if (resultString.length() < MAX_TOKEN_STRING_LENGTH)
				resultString += Long.toString(sign * currentResult / VALUES_PRECISION_FACTOR);
			
			if (currentResult >= target && extraRolls > 0) {
				//Re-roll
				extraRolls--;
			} else if (sign > 0 && currentResult <= invert /* && extraRollAvailable > 0 */) {
				//Invert sign
				sign = -1;
				//extraRollAvailable--;
			} else {
				//No more explosions
				sign = 0;
			}
			
			iterations ++;
		} while (sign != 0 && iterations < MAX_TOKEN_ITERATIONS);
		
		if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
			resultString += SYM_END; // "]";
		} else {
			//resultString = "[...=" + Long.toString(resultValue / VALUES_PRECISION_FACTOR) + "]";
			resultString = SYM_TRUNK_BEGIN + Long.toString(resultValue / VALUES_PRECISION_FACTOR) + SYM_TRUNK_END;
		}
	}

}
