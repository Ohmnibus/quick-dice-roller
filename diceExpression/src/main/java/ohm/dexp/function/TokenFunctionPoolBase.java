package ohm.dexp.function;

import ohm.dexp.DContext;
import ohm.dexp.Dice;
import ohm.dexp.exception.DException;
import ohm.dexp.exception.LoopDetected;
import ohm.dexp.exception.ParameterOutOfBound;

public abstract class TokenFunctionPoolBase extends TokenFunction {

	/**
	 * A d10, the standard dice used in dice pool test.<br />
	 */
	protected static Dice standardDice = new Dice(10);
	
	@Override
	protected void evaluateSelf(DContext instance) throws DException {
		int rollRes;
		int poolSize;
		int successes;

		resultValue = 0;
		resultString = SYM_BEGIN; //"[";

		initSequence(instance);

		poolSize = getPoolSize(instance);
		if (poolSize > MAX_TOKEN_ITERATIONS) {
			//poolSize = MAX_TOKEN_ITERATIONS;
			throw new ParameterOutOfBound(getFunctionName(this.getClass()), getPoolIndex());
		}
		
		boolean isRollAgain;
		int totalRollNumber = 0;
		
		for (int i=1; i<=poolSize; i++) {
			isRollAgain = false;
			do {
				if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
					if (isRollAgain) {
						resultString += SYM_EXPLODE; //",";
					} else if (i>1) {
						resultString += SYM_SEP; //",";
					}
				}
				
				// Roll the value
				rollRes = getRoll(instance);
	
				if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
					resultString = resultString + Integer.toString(rollRes);
				}
	
				successes = countSuccesses(instance, rollRes);
				
				//Place a "!" for every success
				for (int j = 0; j<successes; j++) {
					if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
						resultString = resultString + SYM_SUCCESS; //"!";
					}
				}
				//Place a "*" for every failure
				for (int j = 0; j>successes; j--) {
					if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
						resultString = resultString + SYM_FAILURE; //"*";
					}
				}
				
				resultValue += successes;
				
				totalRollNumber++;
				if (totalRollNumber > MAX_TOKEN_ITERATIONS) {
					//throw new ParameterOutOfBound(getFunctionName(this.getClass()), getPoolIndex());
					throw new LoopDetected(getFunctionName(this.getClass()));
				}
				
				isRollAgain = true;
			} while(rollAgain(instance, rollRes, i));
		}
		
		endSequence(instance);
		
		resultValue = resultValue * VALUES_PRECISION_FACTOR;
		resultMaxValue = Math.max(getMaxPoolSize(instance), 1);
		resultMinValue = 0;
		if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
			resultString = resultString + SYM_END; //"]";
		} else {
			//resultString = "[...=" + Long.toString(resultValue / VALUES_PRECISION_FACTOR) + "]";
			resultString = SYM_TRUNK_BEGIN + Long.toString(resultValue / VALUES_PRECISION_FACTOR) + SYM_TRUNK_END;
		}
	}
	
	/**
	 * Tell if another roll is required.
	 * @param instance
	 * @param rollResult
	 * @param poolRollNumber Number of roll according to pool size (zero based)
	 * @return {@code true} if another roll is required.
	 */
	protected boolean rollAgain(DContext instance, int rollResult, int poolRollNumber) {
		return false;
	}
	
	/**
	 * Used to eventually initialize specific values.
	 */
	protected abstract void initSequence(DContext instance) throws DException;

	/**
	 * Return the pool size, or the number of roll to perform.
	 * @return The number of roll to perform.
	 */
	protected abstract int getPoolSize(DContext instance) throws DException;

	/**
	 * Return the index of the parameter that contain the pool size.
	 * @return The index of the parameter that contain the pool size.
	 */
	protected abstract int getPoolIndex();

	/**
	 * Perform a roll and return the result.
	 * @return Result of a single roll.
	 */
	protected abstract int getRoll(DContext instance) throws DException;

	/**
	 * Return the number of successes obtained with this roll.<br />
	 * Can be a negative value.
	 * @return Successes for this roll.
	 */
	protected abstract int countSuccesses(DContext instance, int rollResult) throws DException;

	/**
	 * Used to eventually perform controls after the pool rolls.
	 */
	protected abstract void endSequence(DContext instance) throws DException;

	/**
	 * Return the maximum size of the pool.<br />
	 * This should be equal to {@link #getPoolSize}, but is actually allowed
	 * to specify a variable pool size.
	 * @return The maximum pool size.
	 */
	protected abstract long getMaxPoolSize(DContext instance) throws DException;
}
