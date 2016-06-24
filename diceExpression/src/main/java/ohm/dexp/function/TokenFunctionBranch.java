package ohm.dexp.function;

import ohm.dexp.DContext;
import ohm.dexp.TokenBase;
import ohm.dexp.exception.DException;
import ohm.dexp.exception.LoopDetected;
import ohm.dexp.exception.ParameterOutOfBound;

public class TokenFunctionBranch extends TokenFunctionExplodeBase {

	/** Dice to roll */
	private static final int INDEX_ROLL = 1;
	/** Number of roll to do */
	private static final int INDEX_POOL = 2;
	/** Target to branch */
	private static final int INDEX_TARGET = 3;
	/** Bloom branches */
	private static final int INDEX_BRANCH = 4; //Optional, default 2
	
	@Override
	protected int initChildNumber() {
		return 3;
	}

	@Override
	protected int initOptionalChildNumber() {
		return 1;
	}
	
	@Override
	public int getType() {
		return 25;
	}

	private TokenBase tokenRoll;
	private boolean tokenRollEvaluated;
	private long target;
	private int branchNum;
	private int totalRollNumber;
	
	@Override
	protected void evaluateSelf(DContext instance) throws DException {
		TokenBase tokenPoolSize;
		TokenBase tokenTarget;
		int poolSize;

		tokenPoolSize = getChild(INDEX_POOL);
		tokenTarget = getChild(INDEX_TARGET);

		tokenPoolSize.evaluate(instance);
		tokenTarget.evaluate(instance);
		
		poolSize = (int) tokenPoolSize.getResult();
		target = tokenTarget.getRawResult();
		
		tokenRoll = getChild(INDEX_ROLL);
		
		long rawBranchNum = getOptionalChildRawResult(instance, INDEX_BRANCH, UNDEFINED);
		if (rawBranchNum == UNDEFINED) {
			branchNum = 2;
		} else {
			branchNum = (int) (rawBranchNum / VALUES_PRECISION_FACTOR);
		}
		
		if (poolSize > MAX_TOKEN_ITERATIONS) {
			//poolSize = MAX_TOKEN_ITERATIONS;
			throw new ParameterOutOfBound(getFunctionName(this.getClass()), INDEX_POOL);
		}
		
		tokenRoll.evaluate(instance); //Need to evaluate prior to loopLessExplosion
		tokenRollEvaluated = true;
		
		if (! loopLessExplosion(instance, tokenRoll, target)) {
			throw new LoopDetected(getFunctionName(this.getClass()));
		}
		
		resultValue = 0;
		resultString = SYM_BEGIN; //"[";
		
		totalRollNumber = 0;
		
		for (int i = 1; i <= poolSize; ++i) {
			if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
				if (i > 1) {
					resultString += SYM_SEP; //",";
				}
			}
			
			singleRoll(instance);
		}
		
		//resultValue = resultValue * VALUES_PRECISION_FACTOR;
		resultMaxValue = (tokenPoolSize.getMaxResult() / VALUES_PRECISION_FACTOR)
				* (tokenRoll.getMaxResult() / VALUES_PRECISION_FACTOR)
				* VALUES_PRECISION_FACTOR;
		resultMinValue = (tokenPoolSize.getMinResult() / VALUES_PRECISION_FACTOR)
				* (tokenRoll.getMinResult() / VALUES_PRECISION_FACTOR)
				* VALUES_PRECISION_FACTOR;
		if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
			resultString = resultString + SYM_END; //"]";
		} else {
			//resultString = "[...=" + Long.toString(resultValue / VALUES_PRECISION_FACTOR) + "]";
			resultString = SYM_TRUNK_BEGIN + Long.toString(resultValue / VALUES_PRECISION_FACTOR) + SYM_TRUNK_END;
		}

	}
	
	protected void singleRoll(DContext instance) throws DException {
		
		totalRollNumber++;
		if (totalRollNumber > MAX_TOKEN_ITERATIONS) {
			//throw new ParameterOutOfBound(getFunctionName(this.getClass()), getPoolIndex());
			throw new LoopDetected(getFunctionName(this.getClass()));
		}
		
		//Following check is to evaluate from second roll onward,
		//since "tokenRoll" was already evaluated once
		//to verify if prone to infinite loop
		if (! tokenRollEvaluated) {
			tokenRoll.evaluate(instance);
		} else {
			tokenRollEvaluated = false;
		}
		
		if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
			resultString += Long.toString(tokenRoll.getResult());
		}
		
		if (tokenRoll.getRawResult() >= target) {
			//Branch
			if (branchNum > 0) {
				if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
					resultString += SYM_EXPLODE + SYM_BEGIN_ALT;
				}
				
				for (int i = 1; i <= branchNum; ++i) {
					if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
						if (i > 1) {
							resultString += SYM_SEP; //",";
						}
					}
					singleRoll(instance);
				}
				
				if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
					resultString += SYM_END_ALT;
				}
			} else {
				//No branches?
				if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
					resultString += SYM_EXPLODE + SYM_BEGIN_ALT + "0" + SYM_END_ALT;
				}
			}
		} else {
			//Sum
			resultValue += tokenRoll.getRawResult();
		}
	}
}
