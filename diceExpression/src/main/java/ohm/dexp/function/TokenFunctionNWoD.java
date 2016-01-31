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
	protected int getPoolIndex() {
		return INDEX_POOL;
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
}
