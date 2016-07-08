package ohm.dexp.function;

import ohm.dexp.DContext;
import ohm.dexp.TokenBase;
import ohm.dexp.exception.DException;
import ohm.dexp.exception.LoopDetected;
import ohm.dexp.exception.ParameterOutOfBound;

public class TokenFunctionBASH extends TokenFunction {

	@Override
	protected int initChildNumber() {
		return 2;
	}

	@Override
	public int getType() {
		return 80;
	}

	/**
	 * BAHS (like) system.<br />
	 * Uses 2 parameters: initial rolls number and expression.<br />
	 * Expression is evaluated a number of time equal to initial roll number.<br />
	 * If Expression is always evaluated with the same value, and as long as it is, is
	 * evaluated again. The result is equal to the sum of all outcome.
	 */
	@Override
	protected void evaluateSelf(DContext instance) throws DException {
		TokenBase tokenRollNum = getChild(1);
		TokenBase roll = getChild(2);
		long topThreshold;
		long botThreshold;
		boolean loopSafe = true;

		long rollNum;
		long result;
		long firstResult;
		boolean exploding;
		int iterations;
		
		tokenRollNum.evaluate(instance);
		rollNum = tokenRollNum.getResult();
		
		loopSafe = rollNum > 1; //At least 2 rolls or it will explode forever
		
		if (loopSafe) {
			roll.evaluate(instance);
			botThreshold = roll.getMinResult() / VALUES_PRECISION_FACTOR;
			topThreshold = roll.getMaxResult() / VALUES_PRECISION_FACTOR;
			//To avoid loops, the roll must generate at least 4 different values.
			loopSafe = (topThreshold - botThreshold) >= 3;
		}
		
		if (! loopSafe) {
			throw new LoopDetected(getFunctionName(this.getClass()));
		}
		
		if (rollNum > MAX_TOKEN_ITERATIONS) {
			//rollNum = MAX_TOKEN_ITERATIONS;
			throw new ParameterOutOfBound(getFunctionName(this.getClass()), 1);
		}

		resultValue = 0;

		resultString = SYM_BEGIN; // "[";

		resultMaxValue = roll.getMaxResult() * rollNum; //This is surely false
		resultMinValue = roll.getMinResult() * rollNum;

		exploding = true;
		firstResult = 0;
		iterations = 0;

		for (int i = 0; i < rollNum; i++) {
			if (i > 0) {
				//Don't evaluate on the first roll 'cause already
				//evaluated to obtain max and min
				roll.evaluate(instance);
				
				if (resultString.length() < MAX_TOKEN_STRING_LENGTH)
					resultString += SYM_SEP; // ",";
			}
			result = roll.getRawResult();
			resultValue += result;
			iterations++;
			if (resultString.length() < MAX_TOKEN_STRING_LENGTH)
				resultString += Long.toString(result / VALUES_PRECISION_FACTOR);
			
			if (i == 0) {
				firstResult = result;
				exploding = true;
			} else {
				exploding = exploding && (result == firstResult);
			}
		}
		
		while(exploding && iterations < MAX_TOKEN_ITERATIONS) {
			//Explosions!
			
			roll.evaluate(instance);
			result = roll.getRawResult();
			resultValue += result;
			iterations++;
			if (resultString.length() < MAX_TOKEN_STRING_LENGTH)
				resultString = resultString + SYM_SEP + Long.toString(result / VALUES_PRECISION_FACTOR) + SYM_EXTRA;
				//resultString = resultString + "," + Long.toString(result / VALUES_PRECISION_FACTOR) + "!";
			
			exploding = exploding && (result == firstResult);
		}

		if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
			resultString += SYM_END; // "]";
		} else {
			//resultString = "[...=" + Long.toString(resultValue / VALUES_PRECISION_FACTOR) + "]";
			resultString = SYM_TRUNK_BEGIN + Long.toString(resultValue / VALUES_PRECISION_FACTOR) + SYM_TRUNK_END;
		}

	}

}
