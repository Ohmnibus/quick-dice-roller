package ohm.dexp.function;

import ohm.dexp.DContext;
import ohm.dexp.TokenBase;
import ohm.dexp.exception.DException;

/**
 * Simple Dice Pool handling.<br />
 * Parameters of the function are:<br />
 * {@code pool(dice, poolSize, target, doubleTarget, failTarget, rollAgain, limit)}
 * @author Ohmnibus
 *
 */
public class TokenFunctionPool extends TokenFunctionPoolBase {

	private static final int INDEX_ROLL = 1;
	private static final int INDEX_POOL = 2;
	private static final int INDEX_TARGET = 3;
	
	private static final int INDEX_DOUBLE = 4;
	private static final int INDEX_FAIL = 5;
	private static final int INDEX_ROLL_AGAIN = 6;
	private static final int INDEX_ROLL_LIMIT = 7;
	
	@Override
	protected int initChildNumber() {
		return 3;
	}
	
	@Override
	protected int initOptionalChildNumber() {
		return 4;
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
	
	private boolean isDouble;
	private int doubleTarget;

	private boolean isFail;
	private int failTarget;

	private boolean isReroll;
	private int reRollTarget;

	private boolean isRollLimit;
	private int maxRollLimit;

	@Override
	protected void initSequence(DContext instance) throws DException {
		TokenBase tokenPoolSize;
		TokenBase tokenTarget;

		tokenPoolSize = getChild(INDEX_POOL);
		tokenTarget = getChild(INDEX_TARGET);

		tokenPoolSize.evaluate(instance);
		tokenTarget.evaluate(instance);
		
		roll = getChild(INDEX_ROLL);
		poolSize = (int)tokenPoolSize.getResult();
		target = (int)tokenTarget.getResult();
		
		long temp;
		temp = getOptionalChildRawResult(instance, INDEX_DOUBLE, UNDEFINED);
		isDouble = temp != UNDEFINED;
		if (isDouble) doubleTarget = (int) (temp / TokenBase.VALUES_PRECISION_FACTOR);

		temp = getOptionalChildRawResult(instance, INDEX_FAIL, UNDEFINED);
		isFail = temp != UNDEFINED;
		if (isFail) failTarget = (int) (temp / TokenBase.VALUES_PRECISION_FACTOR);

		temp = getOptionalChildRawResult(instance, INDEX_ROLL_AGAIN, UNDEFINED);
		isReroll = temp != UNDEFINED;
		if (isReroll) reRollTarget = (int) (temp / TokenBase.VALUES_PRECISION_FACTOR);

		temp = getOptionalChildRawResult(instance, INDEX_ROLL_LIMIT, UNDEFINED);
		isRollLimit = temp != UNDEFINED && temp != 0;
		if (isRollLimit) maxRollLimit = (int) (temp / TokenBase.VALUES_PRECISION_FACTOR);
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
		roll.evaluate(instance);
		return (int)roll.getResult();
	}

	@Override
	protected int countSuccesses(DContext instance, int rollResult) throws DException {
		int retVal = 0;
		if (rollResult >= target) {
			retVal++;
			if (isDouble && rollResult >= doubleTarget) {
				retVal++;
			}
		} else {
			if (isFail && rollResult <= failTarget) {
				retVal--;
			}
		}
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
	
	private int lastPoolRollNumber = -1;
	private int lastPoolRollExplosions = 0;
	
	@Override
	protected boolean rollAgain(DContext instance, int rollResult, int poolRollNumber) {
		boolean retVal = false;
		
		if (lastPoolRollNumber != poolRollNumber) {
			lastPoolRollExplosions = 0;
			lastPoolRollNumber = poolRollNumber;
		} else if (isRollLimit && lastPoolRollExplosions >= maxRollLimit) {
			//Roll limit exists and it is exceeded
			return false;
		}
		
		retVal = isReroll && rollResult >= reRollTarget;
		lastPoolRollExplosions += retVal ? 1 : 0;
		
		return retVal;
	}

}
