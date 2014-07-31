package ohm.dexp.function;

import ohm.dexp.DContext;
import ohm.dexp.TokenBase;
import ohm.dexp.exception.DException;

public class TokenFunctionNWoD extends TokenFunctionPoolBase {

	private static final int INDEX_POOL = 1;
	private static final int INDEX_TARGET = 2;

	@Override
	protected int initChildNumber() {
		return 2;
	}

	@Override
	public int getType() {
		return 50;
	}

	@Override
	public int getPriority() {
		return 0;
	}

	private int poolSize;
	private int target;
	
	@Override
	protected void initSequence(DContext instance) throws DException {
		TokenBase tokenPoolSize;
		TokenBase tokenTarget;
		
		tokenPoolSize = getChild(INDEX_POOL);
		tokenTarget = getChild(INDEX_TARGET);

		tokenPoolSize.evaluate(instance);
		tokenTarget.evaluate(instance);

		poolSize = (int)tokenPoolSize.getResult();
		target = (int)tokenTarget.getResult();
	}

	@Override
	protected int getPoolSize(DContext instance) throws DException {
		return poolSize;
	}

	@Override
	protected int getRoll(DContext instance) throws DException {
		int retVal;
		
		retVal = 0;
		
		do {
			retVal += standardDice.roll();
		} while (retVal % 10 == 0);
		
		return retVal;
	}

	@Override
	protected int countSuccesses(DContext instance, int rollResult) throws DException {
		int retVal;
		retVal = 0;
		do {
			if (rollResult >= target) {
				retVal++;
			}
			rollResult -= 10;
		} while (rollResult > 0);
		return retVal;
	}

	@Override
	protected void endSequence(DContext instance) throws DException {
		//NOOP
	}

	@Override
	protected long getMaxPoolSize(DContext instance) throws DException {
		return getChild(INDEX_POOL).getMaxResult();
	}

//	@Override
//	protected void evaluateSelf(DInstance instance) throws DException {
//		Dice roll;
//		TokenBase tokenPoolSize;
//		TokenBase tokenTarget;
//
//		int res;
//		int poolSize;
//		int target;
//
//		roll = new Dice(10);
//		tokenPoolSize = getChild(1);
//		tokenTarget = getChild(2);
//		
//		tokenPoolSize.evaluate(instance);
//
//		poolSize = (int)tokenPoolSize.getResult();
//		if (poolSize > MAX_TOKEN_ITERATIONS)
//			poolSize = MAX_TOKEN_ITERATIONS;
//		
//		target = (int)tokenTarget.getResult();
//
//		resultValue = 0;
//		resultString = "[";
//
//		for (int i=1; i<=poolSize; i++) {
//			if (resultString.length() < MAX_TOKEN_STRING_LENGTH && i>1) {
//				resultString += ",";
//			}
//
//			// Roll the value
//			res = 0;
//			do {
//				res += roll.roll();
//			} while (res % 10 == 0);
//
//			if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
//				resultString = resultString + Long.toString(res);
//			}
//
//			do {
//				if (res >= target) {
//					resultValue++;
//					if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
//						resultString = resultString + "!";
//					}
//				}
//				res -= 10;
//			} while (res > 0);
//
//			resultValue = resultValue * VALUES_PRECISION_FACTOR;
//			resultMaxValue = tokenPoolSize.getMaxResult();
//			resultMinValue = 0;
//			if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
//				resultString = resultString + "]";
//			} else {
//				resultString = "[...=" + Long.toString(resultValue / VALUES_PRECISION_FACTOR) + "]";
//			}
//		}
//	}
}
