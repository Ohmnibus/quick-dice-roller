package ohm.dexp.function;

import ohm.dexp.DInstance;
import ohm.dexp.TokenBase;
import ohm.dexp.exception.DException;
import ohm.dexp.exception.LoopDetected;

/**
 * This class implement a function designed to implement Burning Wheel system.<br />
 * @author Ohmnibus
 * 
 */
public class TokenFunctionBWheel extends TokenFunction {

	private static final int INDEX_ROLL = 1;
	private static final int INDEX_POOL = 2;
	private static final int INDEX_TARGET = 3;
	private static final int INDEX_ROLL_AGAIN = 4;

	@Override
	protected int initChildNumber() {
		return 4;
	}

	@Override
	public int getType() {
		return 75;
	}

	@Override
	public int getPriority() {
		return 0;
	}

	private TokenBase roll;
	private int poolSize;
	private int target;
	private int rollAgain;

	protected void initSequence(DInstance instance) throws DException {
		TokenBase tokenPoolSize;
		TokenBase tokenTarget;
		TokenBase tokenRollAgain;
		long topThreshold;
		long botThreshold;
		boolean loopSafe = true;

		tokenPoolSize = getChild(INDEX_POOL);
		tokenTarget = getChild(INDEX_TARGET);
		tokenRollAgain = getChild(INDEX_ROLL_AGAIN);

		tokenPoolSize.evaluate(instance);
		tokenTarget.evaluate(instance);
		tokenRollAgain.evaluate(instance);
		
		roll = getChild(INDEX_ROLL);
		poolSize = (int)tokenPoolSize.getResult();
		target = (int)tokenTarget.getResult();
		rollAgain = (int)tokenRollAgain.getResult();
		
		roll.evaluate(instance);
		botThreshold = roll.getMinResult() / VALUES_PRECISION_FACTOR;
		topThreshold = roll.getMaxResult() / VALUES_PRECISION_FACTOR;

		//Check for loops
		if (rollAgain > topThreshold) {
			//This will never explode and can lead to a division by zero
			//Don't know how to handle, but at least is not a loop.
			loopSafe = true;
		} else {
			//To avoid loops, at least 1 outcome out of 3 must not explode.
			loopSafe = (((topThreshold - botThreshold) + 1) / ((topThreshold - rollAgain) + 1) >= 3);
		}
		
		if (! loopSafe) {
			throw new LoopDetected(getFunctionName(this.getClass()));
		}
	}

	protected long getMaxPoolSize(DInstance instance) throws DException {
		return getChild(INDEX_POOL).getMaxResult();
	}

	/* (non-Javadoc)
	 * @see ohm.dexp.function.TokenFunctionPoolBase#evaluateSelf(ohm.dexp.DInstance)
	 */
	@Override
	protected void evaluateSelf(DInstance instance) throws DException {
		int rollRes;
		int successes;
		String sep;

		resultValue = 0;
		resultString = "[";

		initSequence(instance);

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
			successes = 0;
			sep = "";

			do {
				//Since I already evaluate "roll" in "initSequence",
				//first I read the result...
				rollRes = (int)roll.getResult();
				//...and then I evaluate for the next loop
				roll.evaluate(instance);
				if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
					resultString = resultString + sep + Integer.toString(rollRes);
					sep = "+";
				}
				
				if (rollRes >= target) {
					successes++;
					if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
						resultString = resultString + "!";
					}
				}
				
				if (rollRes >= rollAgain && extraSuccessOnRollAgain()) {
					//Count an extra success on "roll again"
					successes++;
					if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
						resultString = resultString + "!";
					}
				}
			} while (rollRes >= rollAgain);

			resultValue += successes;
		}
		
		resultValue = resultValue * VALUES_PRECISION_FACTOR;
		resultMaxValue = Math.max(getMaxPoolSize(instance), 1);
		resultMinValue = 0;
		if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
			resultString = resultString + "]";
		} else {
			resultString = "[...=" + Long.toString(resultValue / VALUES_PRECISION_FACTOR) + "]";
		}
	}
	
	protected boolean extraSuccessOnRollAgain() {
		return false;
	}
}
