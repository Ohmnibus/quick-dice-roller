package ohm.dexp.function;

/**
 * This class implement a function designed to implement Burning Wheel system.<br />
 * @author Ohmnibus
 * 
 */
public class TokenFunctionBWheel extends TokenFunctionShRun5 {

//	private static final int INDEX_ROLL = 1;
//	private static final int INDEX_POOL = 2;
//	private static final int INDEX_TARGET = 3;
//	private static final int INDEX_ROLL_AGAIN = 4;

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
	
	@Override
	protected boolean emitGlitch() {
		return false;
	}

//	private TokenBase roll;
//	private int poolSize;
//	private int target;
//	private int rollAgain;
//
//	protected void initSequence(DContext instance) throws DException {
//		TokenBase tokenPoolSize;
//		TokenBase tokenTarget;
//		TokenBase tokenRollAgain;
//		long maxValue;
//		long minValue;
//		boolean loopSafe = true;
//
//		tokenPoolSize = getChild(INDEX_POOL);
//		tokenTarget = getChild(INDEX_TARGET);
//		tokenRollAgain = getChild(INDEX_ROLL_AGAIN);
//
//		tokenPoolSize.evaluate(instance);
//		tokenTarget.evaluate(instance);
//		tokenRollAgain.evaluate(instance);
//		
//		roll = getChild(INDEX_ROLL);
//		poolSize = (int)tokenPoolSize.getResult();
//		target = (int)tokenTarget.getResult();
//		rollAgain = (int)tokenRollAgain.getResult();
//		
//		roll.evaluate(instance);
//		maxValue = roll.getMaxResult() / VALUES_PRECISION_FACTOR;
//		minValue = roll.getMinResult() / VALUES_PRECISION_FACTOR;
//
//		//Check for loops
//		if (rollAgain > maxValue) {
//			//This will never explode and can lead to a division by zero
//			//Don't know how to handle, but at least is not a loop.
//			loopSafe = true;
//		} else {
//			//To avoid loops, at least 1 outcome out of 3 must not explode.
//			//loopSafe = ((((maxValue - minValue) + 1) * 2) / ((maxValue - rollAgain) + 1) >= 3);
//			long range = (maxValue - minValue) + 1;
//			long explodingRange = (maxValue - rollAgain) + 1;
//			loopSafe = TokenFunctionExplodeBase.loopLessExplosion(range, explodingRange);
//		}
//		
//		if (! loopSafe) {
//			throw new LoopDetected(getFunctionName(this.getClass()));
//		}
//	}
//
//	protected long getMaxPoolSize(DContext instance) throws DException {
//		return getChild(INDEX_POOL).getMaxResult();
//	}
//
//	@Override
//	protected void evaluateSelf(DContext instance) throws DException {
//		int rollRes;
//		int successes;
//		String sep;
//
//		resultValue = 0;
//		resultString = "[";
//
//		initSequence(instance);
//
//		if (poolSize > MAX_TOKEN_ITERATIONS) {
//			//poolSize = MAX_TOKEN_ITERATIONS;
//			throw new LoopDetected(getFunctionName(this.getClass()));
//		}
//
//		for (int i=1; i<=poolSize; i++) {
//			if (resultString.length() < MAX_TOKEN_STRING_LENGTH && i>1) {
//				resultString += ",";
//			}
//			
//			// Roll the value
//			successes = 0;
//			sep = "";
//
//			do {
//				//Since I already evaluate "roll" in "initSequence",
//				//first I read the result...
//				rollRes = (int)roll.getResult();
//				//...and then I evaluate for the next loop
//				roll.evaluate(instance);
//				if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
//					resultString = resultString + sep + Integer.toString(rollRes);
//					sep = "+";
//				}
//				
//				if (rollRes >= target) {
//					successes++;
//					if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
//						resultString = resultString + "!";
//					}
//				}
//				
//				if (rollRes >= rollAgain && extraSuccessOnRollAgain()) {
//					//Count an extra success on "roll again"
//					successes++;
//					if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
//						resultString = resultString + "!";
//					}
//				}
//			} while (rollRes >= rollAgain);
//
//			resultValue += successes;
//		}
//		
//		resultValue = resultValue * VALUES_PRECISION_FACTOR;
//		resultMaxValue = Math.max(getMaxPoolSize(instance), 1);
//		resultMinValue = 0;
//		if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
//			resultString = resultString + "]";
//		} else {
//			resultString = "[...=" + Long.toString(resultValue / VALUES_PRECISION_FACTOR) + "]";
//		}
//	}
//	
//	protected boolean extraSuccessOnRollAgain() {
//		return false;
//	}
}
