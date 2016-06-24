package ohm.dexp.function;

import ohm.dexp.DContext;
import ohm.dexp.TokenBase;
import ohm.dexp.exception.DException;

/**
 * Handle Old World of Darkness roll, as described in http://1d4chan.org/wiki/World_of_Darkness
 * @author Ohmnibus
 */
public class TokenFunctionOWoD extends TokenFunctionPoolBase {

	private static final int INDEX_POOL = 1;
	private static final int INDEX_TARGET = 2;

	@Override
	protected int initChildNumber() {
		return 2;
	}

	@Override
	public int getType() {
		return 51;
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
		int retVal;
		retVal = standardDice.roll();
		if (retVal == 10) {
			//A 10 is re-rolled
			retVal += standardDice.roll();
		}
		return retVal;
	}

	@Override
	protected int countSuccesses(DContext instance, int rollResult) throws DException {
		int retVal;
		retVal = 0;
		
		if (rollResult >= 10) {
			//A 10 that was re-rolled (one are not counted)
			retVal++;
			if (rollResult - 10 >= target) {
				retVal++;
			}
		} else if (rollResult == 1) {
			oneCount++;
		} else if (rollResult >= target) {
			retVal++;
		}
		
		return retVal;
	}

	@Override
	protected void endSequence(DContext instance) throws DException {
		if (oneCount > 0) {
			if (resultValue == 0) {
				//No successes and at least one one: botch
				resultValue = -1;
				resultString = resultString + SYM_SEP_SAME + "B"; //B as Botch
			} else if (oneCount > resultValue) {
				//Number of ones is gt the number of successes: 0 successes
				resultValue = 0;
				resultString = resultString + SYM_SEP_SAME + "F"; //F as Fail
			} else if (resultValue > 0) {
				//Got at least one one, subtract 1 from the result
				resultValue--;
				resultString = resultString + SYM_SEP_SAME + "C"; //C as Canceled
			}
		}
	}

	@Override
	protected long getMaxPoolSize(DContext instance) throws DException {
		return getChild(INDEX_POOL).getMaxResult();
	}
}
