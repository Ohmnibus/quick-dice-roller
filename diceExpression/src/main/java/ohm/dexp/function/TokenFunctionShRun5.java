package ohm.dexp.function;

import ohm.dexp.DContext;
import ohm.dexp.TokenBase;
import ohm.dexp.exception.DException;
import ohm.dexp.exception.LoopDetected;
import ohm.dexp.exception.ParameterOutOfBound;

/**
 * This class implement a function designed to implement Shadowrun 5 system.<br />
 * @author Ohmnibus
 * 
 */
public class TokenFunctionShRun5 extends TokenFunction {

	private static final int INDEX_ROLL = 1;
	private static final int INDEX_POOL = 2;
	private static final int INDEX_TARGET = 3;
	private static final int INDEX_ROLL_AGAIN = 4;
	private static final int INDEX_GLITCH_TARGET = 5;

	@Override
	protected int initChildNumber() {
		return 5;
	}

	@Override
	public int getType() {
		return 77;
	}

	@Override
	public int getPriority() {
		return 0;
	}

	private TokenBase roll;
	private int poolSize;
	private int target;
	private int rollAgain;
	private int glitchTarget;

	protected void initSequence(DContext instance) throws DException {
		TokenBase tokenPoolSize;
		TokenBase tokenTarget;
		TokenBase tokenRollAgain;
		TokenBase tokenGlitchTarget;
//		long maxValue;
//		long minValue;
		boolean loopSafe = true;

		tokenPoolSize = getChild(INDEX_POOL);
		tokenTarget = getChild(INDEX_TARGET);
		tokenRollAgain = getChild(INDEX_ROLL_AGAIN);

		tokenPoolSize.evaluate(instance);
		tokenTarget.evaluate(instance);
		tokenRollAgain.evaluate(instance);
		
		roll = getChild(INDEX_ROLL);
		poolSize = (int)tokenPoolSize.getResult();
		target = (int)tokenTarget.getResult();
		rollAgain = (int)tokenRollAgain.getResult();
		
		roll.evaluate(instance);
//		maxValue = roll.getMaxResult() / VALUES_PRECISION_FACTOR;
//		minValue = roll.getMinResult() / VALUES_PRECISION_FACTOR;
		
		if (emitGlitch()) {
			tokenGlitchTarget = getChild(INDEX_GLITCH_TARGET);
			tokenGlitchTarget.evaluate(instance);
			glitchTarget = (int)tokenGlitchTarget.getResult();
		} else {
			//glitchTarget = (int)minValue - 1; //Never glitch
			//glitchTarget = (int)(roll.getMinResult() / VALUES_PRECISION_FACTOR) - 1; //Never glitch
			glitchTarget = Integer.MIN_VALUE; //Never glitch
		}

		//Check for loops
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
		loopSafe = TokenFunctionExplodeBase.loopLessExplosion(instance, roll, rollAgain * VALUES_PRECISION_FACTOR);
		
		if (! loopSafe) {
			throw new LoopDetected(getFunctionName(this.getClass()));
		}
	}

	protected long getMaxPoolSize(DContext instance) throws DException {
		return getChild(INDEX_POOL).getMaxResult();
	}

	@Override
	protected void evaluateSelf(DContext instance) throws DException {
		int rollRes;
		int successes;
		int glitchCount;
		int rollCount;
		String sep;

		resultValue = 0;
		resultString = SYM_BEGIN; // "[";
		glitchCount = 0;
		rollCount = 0;

		initSequence(instance);

		if (poolSize > MAX_TOKEN_ITERATIONS) {
			//poolSize = MAX_TOKEN_ITERATIONS;
			throw new ParameterOutOfBound(getFunctionName(this.getClass()), INDEX_POOL);
		}

		for (int i=1; i<=poolSize; i++) {
			if (resultString.length() < MAX_TOKEN_STRING_LENGTH && i>1) {
				//resultString += ",";
				resultString += SYM_SEP;
			}
			
			// Roll the value
			successes = 0;
			sep = "";

			do {
				rollCount++;
				//Since I already evaluate "roll" in "initSequence",
				//first I read the result...
				rollRes = (int)roll.getResult();
				//...and then I evaluate for the next loop
				roll.evaluate(instance);
				if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
					resultString = resultString + sep + Integer.toString(rollRes);
					//sep = "+";
					sep = SYM_EXPLODE;
				}
				
				if (rollRes >= target) {
					successes++;
					if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
						resultString = resultString + SYM_SUCCESS; // "!";
					}
				}
				
				if (rollRes <= glitchTarget) {
					glitchCount++;
				}
				
				if (rollRes >= rollAgain && extraSuccessOnRollAgain()) {
					//Count an extra success on "roll again"
					successes++;
					if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
						resultString = resultString + SYM_SUCCESS; // "!";
					}
				}
			} while (rollRes >= rollAgain);

			resultValue += successes;
		}
		
		String glitch = "";
		if (emitGlitch() && isGlitch(glitchCount, rollCount)) {
			if (resultValue > 0) {
				//Glitch
				//glitch = ":G";
				glitch = SYM_SEP_FINAL + "G";
			} else {
				//Critical Glitch
				//glitch = ":C";
				glitch = SYM_SEP_FINAL + "C";
			}
		}
		
		resultValue = resultValue * VALUES_PRECISION_FACTOR;
		resultMaxValue = Math.max(getMaxPoolSize(instance), 1);
		resultMinValue = 0;
		if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
			resultString = resultString + glitch + SYM_END; // "]";
		} else {
			//resultString = "[..." + glitch + "=" + Long.toString(resultValue / VALUES_PRECISION_FACTOR) + "]";
			resultString = SYM_TRUNK_BEGIN + Long.toString(resultValue / VALUES_PRECISION_FACTOR) + glitch + SYM_TRUNK_END;
		}
	}
	
	protected boolean extraSuccessOnRollAgain() {
		return false;
	}
	
	protected boolean emitGlitch() {
		return true;
	}
	
	protected boolean isGlitch(int oneCount, int rollCount) {
		return (oneCount * 2 > rollCount);
	}
}
