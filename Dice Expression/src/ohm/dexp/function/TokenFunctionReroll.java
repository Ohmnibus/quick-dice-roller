package ohm.dexp.function;

import ohm.dexp.DContext;
import ohm.dexp.TokenBase;
import ohm.dexp.exception.DException;
import ohm.dexp.exception.ParameterOutOfBound;

public class TokenFunctionReroll extends TokenFunction {

	private static final int INDEX_ROLL = 1;
	private static final int INDEX_BOUNDARY = 2;
	private static final int INDEX_LIMIT = 3;

	@Override
	protected int initChildNumber() {
		return 3;
	}

	@Override
	public int getType() {
		return 24;
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	protected void evaluateSelf(DContext instance) throws DException {
		TokenBase roll;
		long target;
		int limit;
		int iterations;
		
		roll = getChild(INDEX_ROLL);
		
		getChild(INDEX_BOUNDARY).evaluate(instance);
		target = (int)getChild(INDEX_BOUNDARY).getRawResult();
		
		getChild(INDEX_LIMIT).evaluate(instance);
		limit = (int)getChild(INDEX_LIMIT).getResult();
		
		if (limit > MAX_TOKEN_ITERATIONS) {
			throw new ParameterOutOfBound(getFunctionName(this.getClass()), INDEX_LIMIT);
		}
		
		//Check for loop
		roll.evaluate(instance); //To get MAX and MIN
		
		//Not needed, since the number of re-roll is limited
//		boolean loopSafe = true;
//		loopSafe = TokenFunctionExplodeBase.loopLessExplosion(instance, roll, Long.MAX_VALUE, target);
//		if (! loopSafe) {
//			throw new LoopDetected(getFunctionName(this.getClass()));
//		}

		resultValue = 0;
		resultString = SYM_BEGIN; //"[";

		resultMaxValue = roll.getMaxResult();
		resultMinValue = roll.getMinResult();

		iterations = 0;

		do {
			if (iterations > 0) {
				//Don't evaluate on the first roll 'cause already
				//evaluated to obtain max and min
				roll.evaluate(instance);
				
				if (resultString.length() < MAX_TOKEN_STRING_LENGTH)
					resultString += SYM_EXPLODE; // ",";
			}
			
			iterations++;
			
			resultValue = roll.getRawResult();
			if (resultString.length() < MAX_TOKEN_STRING_LENGTH)
				resultString += Long.toString(resultValue / VALUES_PRECISION_FACTOR);
			
		} while(resultValue <= target && iterations <= limit);

		if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
			resultString += SYM_END; // "]";
		} else {
			//resultString = "[...=" + Long.toString(resultValue / VALUES_PRECISION_FACTOR) + "]";
			resultString = SYM_TRUNK_BEGIN + Long.toString(resultValue / VALUES_PRECISION_FACTOR) + SYM_TRUNK_END;
		}
	}

}
