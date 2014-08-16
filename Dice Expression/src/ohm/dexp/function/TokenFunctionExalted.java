package ohm.dexp.function;

import ohm.dexp.DContext;
import ohm.dexp.TokenBase;
import ohm.dexp.exception.DException;

public class TokenFunctionExalted extends TokenFunctionPoolBase {

	private static final int INDEX_POOL = 1;
	private static final int INDEX_TARGET = 2;

	@Override
	protected int initChildNumber() {
		return 2;
	}

	@Override
	public int getType() {
		return 52;
	}

	@Override
	public int getPriority() {
		return 0;
	}

	private int target;
	private int oneCount;

	@Override
	protected void initSequence(DContext instance) throws DException {
		TokenBase tokenTarget;
		
		tokenTarget = getChild(INDEX_TARGET);
		tokenTarget.evaluate(instance);

		target = (int)tokenTarget.getResult();
		oneCount = 0;
	}

	@Override
	protected int getPoolSize(DContext instance) throws DException {
		getChild(INDEX_POOL).evaluate(instance);
		return (int)getChild(INDEX_POOL).getResult();
	}
	
	@Override
	protected int getPoolIndex() {
		return INDEX_POOL;
	}

	@Override
	protected int getRoll(DContext instance) throws DException {
		return standardDice.roll();
	}

	@Override
	protected int countSuccesses(DContext instance, int rollResult) throws DException {
		int retVal;
		
		retVal = 0;
		
		if (rollResult == 10) {
			//A 10 is an extra success
			retVal++;
		}
		
		if (rollResult >= target) {
			//Standard success
			retVal++;
		}
		
		if (rollResult == 1) {
			oneCount++;
		}

		return retVal;
	}

	@Override
	protected void endSequence(DContext instance) throws DException {
		if (resultValue == 0 && oneCount > 0) {
			//Botch!
			resultValue = -1;
			//resultString = resultString + ":B"; //B as Botch
			resultString = resultString + SYM_SEP_FINAL + "B"; //B as Botch
		}
	}

	@Override
	protected long getMaxPoolSize(DContext instance) throws DException {
		return getChild(INDEX_POOL).getMaxResult();
	}

}
