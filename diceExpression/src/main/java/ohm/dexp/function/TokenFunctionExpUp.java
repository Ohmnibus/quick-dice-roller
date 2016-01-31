package ohm.dexp.function;

import ohm.dexp.DContext;
import ohm.dexp.TokenBase;
import ohm.dexp.exception.DException;
import ohm.dexp.exception.LoopDetected;

public class TokenFunctionExpUp extends TokenFunctionExplodeBase {

	private static final int INDEX_ROLL = 1;
	private static final int INDEX_TARGET = 2;
	private static final int INDEX_NEW_ROLL = 3;
	private static final int INDEX_NEW_TARGET = 4;
	private static final int INDEX_LIMIT = 5;

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
		return 21;
	}

//	@Override
//	protected void evaluateSelf(DContext instance) throws DException {
//		evaluateExplode(instance, getChild(1), 1, false);
//	}

	@Override
	protected void evaluateSelf(DContext instance) throws DException {
		//Mandatory Parameters:
		//1 - Die to roll
		//Optional Parameters:
		//2 - Target. If none is equal to die.max
		//3 - New roll. If none is equal to die
		//4 - New target. If none is equal to newDie.max
		//5 - Re-roll limit (max number of re-roll to do) - 0 to no limit. If none is 0
		
		long target;
		long newTarget;
		long rollLimit;
		
		target = getOptionalChildRawResult(instance, INDEX_TARGET, UNDEFINED);
		newTarget = getOptionalChildRawResult(instance, INDEX_NEW_TARGET, UNDEFINED);
		rollLimit = getOptionalChildRawResult(instance, INDEX_LIMIT, UNDEFINED);

		evaluateExplode(instance, getChild(INDEX_ROLL), target, getChild(INDEX_NEW_ROLL), newTarget, rollLimit);
	}

	protected void evaluateExplode(
			DContext instance,
			TokenBase roll,
			long target,
			TokenBase newRoll,
			long newTarget,
			long rollLimit) throws DException {
		
		int iterations = 0;
		int rollAvailable = 0;
		boolean loopSafe = true;
		boolean exploded;
		long currentResult;
		long currentTarget;
		
		//I need to evaluate here to get Max and Min values
		roll.evaluate(instance);

		resultValue = 0;

		resultString = SYM_BEGIN; // "[";

		resultMaxValue = roll.getMaxResult(); //This is surely false
		resultMinValue = roll.getMinResult(); //This is surely false

		if (target == UNDEFINED) {
			target = resultMaxValue;
		}
		
		//loopSafe = loopLessExplosion(instance, roll, target);
		
		if (newRoll == null) {
			newRoll = roll;
			newTarget = target;
		} else {
			newRoll.evaluate(instance);
			if (newTarget == UNDEFINED) {
				newTarget = newRoll.getMaxResult();
			}
			//loopSafe = loopSafe && loopLessExplosion(instance, newRoll, newTarget);
		}
		
		if (rollLimit == UNDEFINED || rollLimit == 0) {
			rollAvailable = MAX_TOKEN_ITERATIONS;
		} else {
			rollAvailable = (int) (rollLimit / VALUES_PRECISION_FACTOR);
		}

		//The function is safe from explosions if the _second_ roll is.
		loopSafe = loopLessExplosion(instance, newRoll, newTarget);

		if (! loopSafe) {
			throw new LoopDetected(getFunctionName(this.getClass()));
		}

		do {
			if (iterations>0) {
				//TODO: Optimize here
				//This is fine it roll == newRoll, but if they are different instances 
				//newRoll is already evaluated and should be evaluated again on the next iteration.
				newRoll.evaluate(instance); //Evaluate here as a slight optimization
				if (resultString.length() < MAX_TOKEN_STRING_LENGTH)
					resultString += SYM_EXPLODE; // ",";
			}
			
			if (iterations == 0) {
				currentResult = roll.getRawResult();
				currentTarget = target;
			} else {
				currentResult = newRoll.getRawResult();
				currentTarget = newTarget;
			}

			if (resultString.length() < MAX_TOKEN_STRING_LENGTH)
				resultString += Long.toString(currentResult / VALUES_PRECISION_FACTOR);
			
			resultValue += currentResult;
			
			if (currentResult >= currentTarget && rollAvailable > 0) {
				//An explosion
				exploded = true;
				rollAvailable--;
			} else {
				//No more explosions
				exploded = false;
			}
			
			iterations ++;
		} while (exploded && iterations < MAX_TOKEN_ITERATIONS);
		
		if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
			resultString += SYM_END; // "]";
		} else {
			//resultString = "[...=" + Long.toString(resultValue / VALUES_PRECISION_FACTOR) + "]";
			resultString = SYM_TRUNK_BEGIN + Long.toString(resultValue / VALUES_PRECISION_FACTOR) + SYM_TRUNK_END;
		}
	}
	

}
