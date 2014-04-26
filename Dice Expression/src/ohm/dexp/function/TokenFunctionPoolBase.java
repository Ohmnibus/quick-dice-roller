package ohm.dexp.function;

import ohm.dexp.DInstance;
import ohm.dexp.Dice;
import ohm.dexp.exception.DException;

public abstract class TokenFunctionPoolBase extends TokenFunction {

	/**
	 * A d10, the standard dice used in dice pool test.<br />
	 */
	protected static Dice standardDice = new Dice(10);
	
	@Override
	protected void evaluateSelf(DInstance instance) throws DException {
		int rollRes;
		int poolSize;
		int successes;

		resultValue = 0;
		resultString = "[";

		initSequence(instance);

		poolSize = getPoolSize(instance);
		if (poolSize > MAX_TOKEN_ITERATIONS) {
			poolSize = MAX_TOKEN_ITERATIONS;
		} /* else if (poolSize < 1) {
			poolSize = 1;
		} */
		

		for (int i=1; i<=poolSize; i++) {
			if (resultString.length() < MAX_TOKEN_STRING_LENGTH && i>1) {
				resultString += ",";
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
					resultString = resultString + "!";
				}
			}
			//Place a "*" for every failure
			for (int j = 0; j>successes; j--) {
				if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
					resultString = resultString + "*";
				}
			}
			
			resultValue += successes;
		}
		
		endSequence(instance);
		
		resultValue = resultValue * VALUES_PRECISION_FACTOR;
		resultMaxValue = Math.max(getMaxPoolSize(instance), 1);
		resultMinValue = 0;
		if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
			resultString = resultString + "]";
		} else {
			resultString = "[...=" + Long.toString(resultValue / VALUES_PRECISION_FACTOR) + "]";
		}
	}
	
	/**
	 * Used to eventually initialize specific values.
	 */
	protected abstract void initSequence(DInstance instance) throws DException;

	/**
	 * Return the pool size, or the number of roll to perform.
	 * @return The number of roll to perform.
	 */
	protected abstract int getPoolSize(DInstance instance) throws DException;

	/**
	 * Perform a roll and return the result.
	 * @return Result of a single roll.
	 */
	protected abstract int getRoll(DInstance instance) throws DException;

	/**
	 * Return the number of successes obtained with this roll.<br />
	 * Can be a negative value.
	 * @return Successes for this roll.
	 */
	protected abstract int countSuccesses(DInstance instance, int rollResult) throws DException;

	/**
	 * Used to eventually perform controls after the pool rolls.
	 */
	protected abstract void endSequence(DInstance instance) throws DException;

	/**
	 * Return the maximum size of the pool.<br />
	 * This should be equal to {@link getPoolSize()}, but is actually allowed
	 * to specify a variable pool size.
	 * @return The maximum pool size.
	 */
	protected abstract long getMaxPoolSize(DInstance instance) throws DException;
}
