package ohm.dexp.function;

import ohm.dexp.DContext;
import ohm.dexp.TokenBase;
import ohm.dexp.exception.DException;
import ohm.dexp.exception.ParameterOutOfBound;

public class TokenFunctionRollAndKeep extends TokenFunction {

	private static final int INDEX_ROLL = 1;
	private static final int INDEX_POOL = 2;
	private static final int INDEX_KEEP = 3;

	private static final int MAX_POOL_SIZE = 50;
	
	private long[] rollValues = null; // = new long[MAX_POOL_SIZE];

	private boolean[] validValues = null; // = new boolean[MAX_POOL_SIZE];
	
	@Override
	protected int initChildNumber() {
		return 2;
	}
	
	@Override
	protected int initOptionalChildNumber() {
		return 1;
	}

	@Override
	public int getType() {
		return 60;
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	protected void evaluateSelf(DContext instance) throws DException {
		TokenBase roll;
		int poolSize;
		int keepSize;
		
		roll = getChild(INDEX_ROLL);
		
		getChild(INDEX_POOL).evaluate(instance);
		poolSize = (int)getChild(INDEX_POOL).getResult();
		if (poolSize > MAX_POOL_SIZE) {
			//poolSize = MAX_POOL_SIZE;
			throw new ParameterOutOfBound(getFunctionName(this.getClass()), INDEX_POOL);
		}
		if (poolSize < 0) {
			poolSize = 0;
		}

		if (getChild(INDEX_KEEP) != null) {
			getChild(INDEX_KEEP).evaluate(instance);
			keepSize = (int)getChild(INDEX_KEEP).getResult();
		} else {
			keepSize = poolSize;
		}
		
		if (keepSize > poolSize) {
			keepSize = poolSize;
		}
		
		long rollResult;
		long minKept;
		int minKeptIndex;
		
		minKept = Long.MAX_VALUE;
		minKeptIndex = 0;
		
		if (rollValues == null || rollValues.length < poolSize) {
			rollValues = new long[poolSize];
			validValues = new boolean[poolSize];
		}
		
		for (int i = 0; i<poolSize; i++) {
			roll.evaluate(instance);
			rollResult = roll.getRawResult();

			rollValues[i] = rollResult;
			validValues[i] = true;

			if (i < keepSize) {
				//Simply keep and detect minimum kept value
				if (minKept > rollResult) {
					minKept = rollResult;
					minKeptIndex = i;
				}
			} else {
				//Chose what to do.
				if (rollResult >= minKept) {
					
					//Discards previous minimum value
					validValues[minKeptIndex] = false;

					//Search new minimum value
					minKept = Long.MAX_VALUE;
					minKeptIndex = 0;
					for (int j = 0; j <= i; j++) {
						if (validValues[j]) {
							if (minKept > rollValues[j]) {
								minKept = rollValues[j];
								minKeptIndex = j;
							}
						}
					}
				} else {
					//This roll is equal or less than the minimum kept value.
					validValues[i] = false;
				}
			}
		}
		
		resultValue = 0;
		resultString = SYM_BEGIN; //"[";

		for (int i = 0; i<poolSize; i++) {
			if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
				if (i > 0) {
					resultString += SYM_SEP; //",";
				}
				resultString += Long.toString(rollValues[i] / VALUES_PRECISION_FACTOR);
				if (validValues[i] && keepSize != poolSize) {
					resultString += SYM_SELECTED; //"!";
				}
			}
			if (validValues[i]) {
				resultValue += rollValues[i];
			}
		}

		resultMaxValue = keepSize * roll.getMaxResult();
		resultMinValue = keepSize * roll.getMinResult();
		if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
			resultString += SYM_END; //"]";
		} else {
			//resultString = "[...=" + Long.toString(resultValue / VALUES_PRECISION_FACTOR) + "]";
			resultString = SYM_TRUNK_BEGIN + Long.toString(resultValue / VALUES_PRECISION_FACTOR) + SYM_TRUNK_END;
		}
	}
}
