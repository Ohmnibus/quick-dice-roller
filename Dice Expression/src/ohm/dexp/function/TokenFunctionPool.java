package ohm.dexp.function;

import ohm.dexp.DInstance;
import ohm.dexp.TokenBase;
import ohm.dexp.exception.DException;

public class TokenFunctionPool extends TokenFunctionPoolBase {

	private static final int INDEX_ROLL = 1;
	private static final int INDEX_POOL = 2;
	private static final int INDEX_TARGET = 3;
	
	@Override
	protected int initChildNumber() {
		return 3;
	}

	@Override
	public int getType() {
		return 40;
	}

	@Override
	public int getPriority() {
		return 0;
	}

	private TokenBase roll;
	private int poolSize;
	private int target;

	@Override
	protected void initSequence(DInstance instance) throws DException {
		TokenBase tokenPoolSize;
		TokenBase tokenTarget;

		tokenPoolSize = getChild(INDEX_POOL);
		tokenTarget = getChild(INDEX_TARGET);

		tokenPoolSize.evaluate(instance);
		tokenTarget.evaluate(instance);
		
		roll = getChild(INDEX_ROLL);
		poolSize = (int)tokenPoolSize.getResult();
		target = (int)tokenTarget.getResult();
	}

	@Override
	protected int getPoolSize(DInstance instance) throws DException {
		return poolSize;
	}

	@Override
	protected int getRoll(DInstance instance) throws DException {
		roll.evaluate(instance);
		return (int)roll.getResult();
	}

	@Override
	protected int countSuccesses(DInstance instance, int rollResult) throws DException {
		return rollResult >= target ? 1 : 0;
	}

	@Override
	protected void endSequence(DInstance instance) throws DException {
		//NOOP
	}

	@Override
	protected long getMaxPoolSize(DInstance instance) throws DException {
		return getChild(INDEX_POOL).getMaxResult();
	}

//	@Override
//	protected void evaluateSelf(DInstance instance) throws DException {
//		TokenBase roll;
//		TokenBase tokenPoolSize;
//		TokenBase tokenTarget;
//
//		int poolSize;
//		long target;
//
//		roll = getChild(1);
//		tokenPoolSize = getChild(2);
//		tokenTarget = getChild(3);
//		
//		tokenPoolSize.evaluate(instance);
//		
//		poolSize = (int)tokenPoolSize.getResult();
//		if (poolSize > MAX_TOKEN_ITERATIONS)
//			poolSize = MAX_TOKEN_ITERATIONS;
//		target = tokenTarget.getRawResult();
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
//			roll.evaluate(instance);
//
//			if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
//				resultString = resultString + Long.toString(roll.getResult());
//			}
//
//			if (roll.getRawResult() >= target) {
//				resultValue++;
//				if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
//					resultString = resultString + "!";
//				}
//			}
//		}
//		
//		resultValue = resultValue * VALUES_PRECISION_FACTOR;
//		resultMaxValue = tokenPoolSize.getMaxResult();
//		resultMinValue = 0;
//		if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
//			resultString = resultString + "]";
//		} else {
//			resultString = "[...=" + Long.toString(resultValue / VALUES_PRECISION_FACTOR) + "]";
//		}
//	}
	
}
