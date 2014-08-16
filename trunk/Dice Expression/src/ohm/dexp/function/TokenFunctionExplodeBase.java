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

//	/**
//	 * Evaluate an explode (open) roll.<br />
//	 * If the roll is evaluated as it's maximum value, the die is rolled again and added to the
//	 * main result.<br />
//	 * Optionally, if the roll is evaluated as it's minimum, the die is rolled again and 
//	 * subtracted to the main result.<br />
//	 * The tolerance is used to elaborate a range near the top and the bottom values in order
//	 * to allow tolerance in big sized dice rolls (d30, d100).
//	 * @param instance
//	 * @param roll Expression to evaluate.
//	 * @param tolerance Tolerance to apply, expressed ad NON raw values.
//	 * @param bothDirection True to apply explosion to both direction, false to explode only upward.
//	 * @throws DException
//	 */
//	protected void evaluateExplode(DContext instance, TokenBase roll, long tolerance, boolean bothDirection) throws DException {
//		
//		//[10,10,5] 25
//		//10
//		int iterations = 0;
//		boolean loopSafe = true;
//		int nextSign = 1;
//		long currentResult;
//		long topThreshold;
//		long botThreshold;
//		
//		//I need to evaluate here to get Max and Min values
//		roll.evaluate(instance);
//
//		resultValue = 0;
//
//		resultString = "[";
//
//		resultMaxValue = roll.getMaxResult(); //This is surely false
//		resultMinValue = roll.getMinResult(); //This is surely false
//
//		topThreshold = resultMaxValue - (tolerance - 1) * VALUES_PRECISION_FACTOR;
//		if (bothDirection) {
//			botThreshold = resultMinValue + (tolerance - 1) * VALUES_PRECISION_FACTOR;
//		} else {
//			//The explosion is not in both directions, so didn't consider bottom tolerance.
//			botThreshold = resultMinValue;
//		}
//		
//		if (topThreshold <= botThreshold) {
//			//This will always explode!
//			loopSafe = false;
//		} else {
//			//To avoid loops, at least 1 outcome out of 3 must not explode.
//			loopSafe = ((resultMaxValue - resultMinValue) / (topThreshold - botThreshold) <= 3);
//		}
//		
//		if (! loopSafe) {
//			throw new LoopDetected(getFunctionName(this.getClass()));
//		}
//
//		do {
//			if (iterations>0) {
//				roll.evaluate(instance); //Evaluate here as a slight optimization
//				if (resultString.length() < MAX_TOKEN_STRING_LENGTH)
//					resultString += ",";
//			}
//			if (resultString.length() < MAX_TOKEN_STRING_LENGTH)
//				resultString += Long.toString(roll.getRawResult() / VALUES_PRECISION_FACTOR);
//			
//			currentResult = roll.getRawResult();
//			resultValue += (nextSign * currentResult);
//			
//			if (currentResult >= topThreshold)
//				//Re-roll and add
//				nextSign = 1;
//			else if (bothDirection && currentResult <= botThreshold) {
//				//Re-roll and subtract
//				nextSign = -1;
//			} else {
//				//No more explosions
//				nextSign = 0;
//			}
//			
//			iterations ++;
//		} while (nextSign != 0 && loopSafe && iterations < MAX_TOKEN_ITERATIONS);
//		
//		if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
//			resultString += "]";
//		} else {
//			resultString = "[...=" + Long.toString(resultValue / VALUES_PRECISION_FACTOR) + "]";
//		}
//	}

	/**
	 * Evaluate an exploding/collapsing roll.<br />
	 * The roll explode if it's result is greater or equal to {@code upperTarget}. In this case
	 * a new roll is performed and it's result is added to the main result.<br />
	 * The roll collapse if it's result is lesser or equal to {@code lowerTarget}. In this case
	 * a new roll is performed and it's result is subtracted to the main result.<br />
	 * Maximum number of explosions and collapses is defined by {@code upperRollLimit} and
	 * {@code lowerRollLimit}.
	 * @param instance Instance to use to evaluate the roll.
	 * @param roll Roll to evaluate.
	 * @param upperTarget Explosion target. If equal to {@code UNDEFINED} is set to the maximum of {@code roll}.
	 * @param lowerTarget Collapse target. If equal to {@code UNDEFINED} is set to the minimum of {@code roll}.
	 * @param upperRollLimit Explosions limit. If equal to {@code UNDEFINED} or {@code 0} is set to {@code MAX_TOKEN_ITERATIONS}.
	 * @param lowerRollLimit Collapses limit. If equal to {@code UNDEFINED} or {@code 0} is set to {@code MAX_TOKEN_ITERATIONS}.
	 * @throws DException Thrown if an error occurred during expression tree evaluation.
	 */
	protected void evaluateExplode(
			DContext instance,
			TokenBase roll,
			long upperTarget,
			long lowerTarget,
			long upperRollLimit,
			long lowerRollLimit) throws DException {
		
		evaluateExplode(instance, roll, upperTarget, lowerTarget, upperRollLimit, lowerRollLimit, false);
	}
	
	/**
	 * Evaluate an exploding/collapsing roll.<br />
	 * The roll explode if it's result is greater or equal to {@code upperTarget}. In this case
	 * a new roll is performed and it's result is added to the main result.<br />
	 * The roll collapse if it's result is lesser or equal to {@code lowerTarget}. In this case
	 * a new roll is performed and it's result is subtracted to the main result.<br />
	 * Maximum number of explosions and collapses is defined by {@code upperRollLimit} and
	 * {@code lowerRollLimit}.
	 * @param instance Instance to use to evaluate the roll.
	 * @param roll Roll to evaluate.
	 * @param upperTarget Explosion target. If equal to {@code UNDEFINED} is set to the maximum of {@code roll}.
	 * @param lowerTarget Collapse target. If equal to {@code UNDEFINED} is set to the minimum of {@code roll}.
	 * @param upperRollLimit Explosions limit. If equal to {@code UNDEFINED} or {@code 0} is set to {@code MAX_TOKEN_ITERATIONS}.
	 * @param lowerRollLimit Collapses limit. If equal to {@code UNDEFINED} or {@code 0} is set to {@code MAX_TOKEN_ITERATIONS}.
	 * @param evaluated {@code true} if {@code roll} has already been evaluated once, {@code false} otherwise.
	 * @throws DException Thrown if an error occurred during expression tree evaluation.
	 */
	protected void evaluateExplode(
			DContext instance,
			TokenBase roll,
			long upperTarget,
			long lowerTarget,
			long upperRollLimit,
			long lowerRollLimit,
			boolean evaluated) throws DException {
		
//		boolean upperOpen;
//		boolean lowerOpen;
		int iterations = 0;
		int upperRollAvailable = 0;
		int lowerRollAvailable = 0;
		boolean loopSafe = true;
		int nextSign = 1;
		long currentResult;
		
		//If not done already,
		//I need to evaluate here to get Max and Min values
		if (! evaluated) {
			roll.evaluate(instance);
		}

		resultValue = 0;

		resultString = SYM_BEGIN; // "[";

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
//		upperOpen = (upperTarget <= resultMaxValue);
//		lowerOpen = (lowerTarget >= resultMinValue);

//		if (upperTarget <= lowerTarget) {
//			//This will always explode!
//			loopSafe = false;
//		} else {
//			//To avoid loops, at least 1 outcome out of 3 must not explode.
//			long range;
//			long explodingRange;
//			range = (resultMaxValue - resultMinValue) + VALUES_PRECISION_FACTOR;
//			explodingRange = 0;
//			if (upperOpen) {
//				explodingRange += (resultMaxValue - upperTarget) + VALUES_PRECISION_FACTOR;
//			}
//			if (lowerOpen) {
//				explodingRange += (lowerTarget - resultMinValue) + VALUES_PRECISION_FACTOR;
//			}
//			//loopSafe = explodingRange == 0 || (((range * 2) / explodingRange) >= 3);
//			loopSafe = loopLessExplosion(range, explodingRange);
//		}
		loopSafe = loopLessExplosion(instance, roll, upperTarget, lowerTarget);
		
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
			resultString += SYM_END; // "]";
		} else {
			//resultString = "[...=" + Long.toString(resultValue / VALUES_PRECISION_FACTOR) + "]";
			resultString = SYM_TRUNK_BEGIN + Long.toString(resultValue / VALUES_PRECISION_FACTOR) + SYM_TRUNK_END;
		}
	}
	
	/**
	 * Tell if exploding the specified roll is safe from infinite loop.<br />
	 * An explosion is considered safe if at least one value generated out of 3 
	 * did NOT explode.
	 * @param instance Instance used to evaluate the roll.
	 * @param roll Roll to check. Must be evaluated.
	 * @param target Raw target for explosions.
	 * @return {@code true} if the roll is safe from loop, {@code false} otherwise.
	 * @throws DException Thrown if an error occurred during expression tree evaluation.
	 */
	protected static boolean loopLessExplosion(DContext instance, TokenBase roll, long target) throws DException {
		return loopLessExplosion(instance, roll, target, Long.MIN_VALUE);
	}
	
	/**
	 * Tell if exploding the specified roll is safe from infinite loop.<br />
	 * An explosion is considered safe if at least one value generated out of 3 
	 * did NOT explode.
	 * @param instance Instance used to evaluate the roll.
	 * @param roll Roll to check. Must be evaluated.
	 * @param upperTarget Raw upper target for upward explosions.
	 * @param lowerTarget Raw lower target for downward explosions.
	 * @return {@code true} if the roll is safe from loop, {@code false} otherwise.
	 * @throws DException Thrown if an error occurred during expression tree evaluation.
	 */
	protected static boolean loopLessExplosion(DContext instance, TokenBase roll, long upperTarget, long lowerTarget) throws DException {
		boolean loopSafe;
		
		if (upperTarget <= lowerTarget) {
			//This will always explode!
			loopSafe = false;
		} else {
			//To avoid loops, at least 1 outcome out of 3 must not explode.
			long maxResult;
			long minResult;
			long range; //Number of values generated by the roll
			long explodingRange; //Number of values that make the die explode

			maxResult = roll.getMaxResult();
			minResult = roll.getMinResult();

			range = (maxResult - minResult) + VALUES_PRECISION_FACTOR; //(max - min) + 1
			explodingRange = 0;
			if (upperTarget <= maxResult) {
				explodingRange += (maxResult - upperTarget) + VALUES_PRECISION_FACTOR;
			}
			if (lowerTarget >= minResult) {
				explodingRange += (lowerTarget - minResult) + VALUES_PRECISION_FACTOR;
			}
			loopSafe = explodingRange == 0 || (((range * 2) / explodingRange) >= 3); //is the same as "range/explodingRange >= 1.5"
			//loopSafe = loopLessExplosion(range, explodingRange);
		}

		return loopSafe;
	}
}
