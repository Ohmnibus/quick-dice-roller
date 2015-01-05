package ohm.dexp.function;

import ohm.dexp.DContext;
import ohm.dexp.exception.DException;

public class TokenFunctionExp extends TokenFunctionExplodeBase {

	private static final int INDEX_ROLL = 1;
	private static final int INDEX_UPPER_TARGET = 2;
	private static final int INDEX_LOWER_TARGET = 3;
	private static final int INDEX_UPPER_LIMIT = 4;
	private static final int INDEX_LOWER_LIMIT = 5;

	@Override
	protected int initChildNumber() {
		return 1;
	}
	
	@Override
	protected int initOptionalChildNumber() {
		return 4;
	};

	@Override
	public int getType() {
		return 20;
	}

	@Override
	protected void evaluateSelf(DContext instance) throws DException {
		//Mandatory Parameters:
		//1 - Die to roll
		//Optional Parameters:
		//2 - Upper target. If none is equal to die.max
		//3 - Lower target. If none is equal to die.min
		//4 - Upper re-roll limit (max number of re-roll to do) - 0 to no limit. If none is 0
		//5 - Lower re-roll limit (max number of re-roll to do) - 0 to no limit. If none is 0
		
		long upperTarget;
		long lowerTarget;
		long upperRollLimit;
		long lowerRollLimit;
		
		upperTarget = getOptionalChildRawResult(instance, INDEX_UPPER_TARGET, UNDEFINED);
		lowerTarget = getOptionalChildRawResult(instance, INDEX_LOWER_TARGET, UNDEFINED);
		upperRollLimit = getOptionalChildRawResult(instance, INDEX_UPPER_LIMIT, UNDEFINED);
		lowerRollLimit = getOptionalChildRawResult(instance, INDEX_LOWER_LIMIT, UNDEFINED);

		evaluateExplode(instance, getChild(INDEX_ROLL), upperTarget, lowerTarget, upperRollLimit, lowerRollLimit);
	}
}
