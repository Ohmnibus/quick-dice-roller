package ohm.dexp.function;

import ohm.dexp.DContext;
import ohm.dexp.TokenBase;
import ohm.dexp.exception.DException;
import ohm.dexp.exception.LoopDetected;

public class TokenFunctionExp extends TokenFunctionExplodeBase {

	@Override
	protected int initChildNumber() {
		return 1;
	}
	
	@Override
	protected int initOptionalChildNumber() {
		return 4;
	};

	@Override
	public int getType() {
		return 20;
	}

	@Override
	protected void evaluateSelf(DContext instance) throws DException {
		//Mandatory Parameters:
		//1 - Die to roll
		//Optional Parameters:
		//2 - Upper target. If none is equal to die.max
		//3 - Lower target. If none is equal to die.min
		//4 - Upper re-roll limit (max number of re-roll to do) - 0 to no limit. If none is 0
		//5 - Lower re-roll limit (max number of re-roll to do) - 0 to no limit. If none is 0
		
		long upperTarget;
		long lowerTarget;
		long upperRollLimit;
		long lowerRollLimit;
		
		upperTarget = getOptionalChildRawResult(instance, 2);
		lowerTarget = getOptionalChildRawResult(instance, 3);
		upperRollLimit = getOptionalChildRawResult(instance, 4);
		lowerRollLimit = getOptionalChildRawResult(instance, 5);

		evaluateExplode(instance, getChild(1), upperTarget, lowerTarget, upperRollLimit, lowerRollLimit);
	}
	
	private long getOptionalChildRawResult(DContext instance, int index) throws DException {
		long retVal;
		TokenBase tmpRoll = getChild(index);
		if (tmpRoll != null) {
			tmpRoll.evaluate(instance);
			retVal = tmpRoll.getRawResult();
		} else {
			retVal = UNDEFINED;
		}
		return retVal;
	}
	
	private static final long UNDEFINED = Long.MIN_VALUE;
	
	protected void evaluateExplode(
			DContext instance,
			TokenBase roll,
			long upperTarget,
			long lowerTarget,
			long upperRollLimit,
			long lowerRollLimit) throws DException {
		
		boolean upperOpen;
		boolean lowerOpen;
		int iterations = 0;
		int upperRollAvailable = 0;
		int lowerRollAvailable = 0;
		boolean loopSafe = true;
		int nextSign = 1;
		long currentResult;
		
		//I need to evaluate here to get Max and Min values
		roll.evaluate(instance);

		resultValue = 0;

		resultString = "[";

		resultMaxValue = roll.getMaxResult(); //This is surely false
		resultMinValue = roll.getMinResult(); //This is surely false

		if (upperTarget == UNDEFINED) {
			upperTarget = resultMaxValue;
		}
		if (lowerTarget == UNDEFINED) {
			lowerTarget = resultMinValue;
		}
		if (upperRollLimit == UNDEFINED || upperRollLimit == 0) {
			upperRollAvailable = MAX_TOKEN_ITERATIONS;
		} else {
			upperRollAvailable = (int) (upperRollLimit / VALUES_PRECISION_FACTOR);
		}
		if (lowerRollLimit == UNDEFINED || lowerRollLimit == 0) {
			lowerRollAvailable = MAX_TOKEN_ITERATIONS;
		} else {
			lowerRollAvailable = (int) (lowerRollLimit / VALUES_PRECISION_FACTOR);
		}
		upperOpen = (upperTarget <= resultMaxValue);
		lowerOpen = (lowerTarget >= resultMinValue);

		if (upperTarget <= lowerTarget) {
			//This will always explode!
			loopSafe = false;
		} else {
			//To avoid loops, at least 1 outcome out of 3 must not explode.
			long range;
			long explodingRange;
			range = (resultMaxValue - resultMinValue) + VALUES_PRECISION_FACTOR;
			explodingRange = 0;
			if (upperOpen) {
				explodingRange += (resultMaxValue - upperTarget) + VALUES_PRECISION_FACTOR;
			}
			if (lowerOpen) {
				explodingRange += (lowerTarget - resultMinValue) + VALUES_PRECISION_FACTOR;
			}
			loopSafe = explodingRange == 0 || (((range * 2) / explodingRange) >= 3);
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
			
			if (currentResult >= upperTarget && upperRollAvailable > 0) {
				//Re-roll and add
				nextSign = 1;
				upperRollAvailable--;
			} else if (currentResult <= lowerTarget && lowerRollAvailable > 0) {
				//Re-roll and subtract
				nextSign = -1;
				lowerRollAvailable--;
			} else {
				//No more explosions
				nextSign = 0;
			}
			
			iterations ++;
		} while (nextSign != 0 && iterations < MAX_TOKEN_ITERATIONS);
		
		if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
			resultString += "]";
		} else {
			resultString = "[...=" + Long.toString(resultValue / VALUES_PRECISION_FACTOR) + "]";
		}
	}
}
