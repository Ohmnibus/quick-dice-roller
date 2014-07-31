package ohm.dexp.function;

import ohm.dexp.DContext;
import ohm.dexp.TokenBase;
import ohm.dexp.exception.DException;
import ohm.dexp.exception.LoopDetected;

public abstract class TokenFunctionExplodeBase extends TokenFunction {

	@Override
	public int getPriority() {
		return 0;
	}

	/**
	 * Evaluate an explode (open) roll.<br />
	 * If the roll is evaluated as it's maximum value, the die is rolled again and added to the
	 * main result.<br />
	 * Optionally, if the roll is evaluated as it's minimum, the die is rolled again and 
	 * subtracted to the main result.<br />
	 * The tolerance is used to elaborate a range near the top and the bottom values in order
	 * to allow tolerance in big sized dice rolls (d30, d100).
	 * @param instance
	 * @param roll Expression to evaluate.
	 * @param tolerance Tolerance to apply, expressed ad NON raw values.
	 * @param bothDirection True to apply explosion to both direction, false to explode only upward.
	 * @throws DException
	 */
	protected void evaluateExplode(DContext instance, TokenBase roll, long tolerance, boolean bothDirection) throws DException {
		
		//[10,10,5] 25
		//10
		int iterations = 0;
		boolean loopSafe = true;
		int nextSign = 1;
		long currentResult;
		long topThreshold;
		long botThreshold;
		
		//I need to evaluate here to get Max and Min values
		roll.evaluate(instance);

		resultValue = 0;

		resultString = "[";

		resultMaxValue = roll.getMaxResult(); //This is surely false
		resultMinValue = roll.getMinResult(); //This is surely false

		topThreshold = resultMaxValue - (tolerance - 1) * VALUES_PRECISION_FACTOR;
		if (bothDirection) {
			botThreshold = resultMinValue + (tolerance - 1) * VALUES_PRECISION_FACTOR;
		} else {
			//The explosion is not in both directions, so didn't consider bottom tolerance.
			botThreshold = resultMinValue;
		}
		
		if (topThreshold <= botThreshold) {
			//This will always explode!
			loopSafe = false;
		} else {
			//To avoid loops, at least 1 outcome out of 3 must not explode.
			loopSafe = ((resultMaxValue - resultMinValue) / (topThreshold - botThreshold) <= 3);
		}
		
		if (! loopSafe) {
			throw new LoopDetected(getFunctionName(this.getClass()));
		}

		do {
			if (iterations>0) {
				roll.evaluate(instance); //Evaluate here as a slight optimization
				if (resultString.length() < MAX_TOKEN_STRING_LENGTH)
					resultString += ",";
			}
			if (resultString.length() < MAX_TOKEN_STRING_LENGTH)
				resultString += Long.toString(roll.getRawResult() / VALUES_PRECISION_FACTOR);
			
			currentResult = roll.getRawResult();
			resultValue += (nextSign * currentResult);
			
			if (currentResult >= topThreshold)
				//Re-roll and add
				nextSign = 1;
			else if (bothDirection && currentResult <= botThreshold) {
				//Re-roll and subtract
				nextSign = -1;
			} else {
				//No more explosions
				nextSign = 0;
			}
			
			iterations ++;
		} while (nextSign != 0 && loopSafe && iterations < MAX_TOKEN_ITERATIONS);
		
		if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
			resultString += "]";
		} else {
			resultString = "[...=" + Long.toString(resultValue / VALUES_PRECISION_FACTOR) + "]";
		}
	}
}
