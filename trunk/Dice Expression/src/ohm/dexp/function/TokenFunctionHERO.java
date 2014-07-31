package ohm.dexp.function;

import ohm.dexp.DContext;
import ohm.dexp.Dice;
import ohm.dexp.TokenBase;
import ohm.dexp.exception.DException;

public class TokenFunctionHERO extends TokenFunction {

	/**
	 * A d6, the dice used in the test.<br />
	 */
	protected static Dice standardDice = new Dice(6);

	@Override
	protected int initChildNumber() {
		return 1;
	}

	@Override
	public int getType() {
		return 70;
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	protected void evaluateSelf(DContext instance) throws DException {
		int rollRes;
		int poolSize;
		boolean halfDice;
		int rollBody;
		int resultBody;

		TokenBase poolSizeToken = getChild(1);
		
		poolSizeToken.evaluate(instance);
		
		halfDice = (poolSizeToken.getRawResult() % VALUES_PRECISION_FACTOR >= 500);
		poolSize = (int)poolSizeToken.getResult();
		
		if (poolSize > MAX_TOKEN_ITERATIONS) {
			poolSize = MAX_TOKEN_ITERATIONS;
			halfDice = false;
		}

		resultValue = 0;
		resultBody = 0;
		resultString = "[";

		for (int i=1; i<=poolSize; i++) {
			if (resultString.length() < MAX_TOKEN_STRING_LENGTH && i>1) {
				resultString += ",";
			}
			
			// Roll the value
			rollRes = standardDice.roll();

			if (rollRes == 1) {
				//No body damage
				rollBody = 0;
			} else if(rollRes == 6) {
				//Full body damage				
				rollBody = 2;
			} else {
				//Half body damage
				rollBody = 1;
			}

			if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
				resultString = resultString + Integer.toString(rollRes) +
					":" + Integer.toString(rollBody);
			}
			
			resultValue += rollRes;
			resultBody += rollBody;
		}
		
		if (halfDice) {
			if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
				resultString += ",";
			}
			
			// Roll the value
			rollRes = standardDice.roll();

			if (rollRes < 4) {
				//No body damage
				rollBody = 0;
			} else {
				//Half body damage
				rollBody = 1;
			}
			
			rollRes = (rollRes + 1) / 2;

			if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
				resultString = resultString + "{" + Integer.toString(rollRes) +
					":" + Integer.toString(rollBody) + "}";
			}
			
			resultValue += rollRes;
			resultBody += rollBody;
		}
		
		resultValue = resultValue * VALUES_PRECISION_FACTOR;
		resultMaxValue = poolSizeToken.getMaxResult() * 6;
		resultMinValue = poolSizeToken.getMinResult();
		if (resultMinValue % VALUES_PRECISION_FACTOR >= 500) {
			//The minimum of an half die is 1, not 0.5
			resultMinValue += 500;
		}
		if (resultString.length() >= MAX_TOKEN_STRING_LENGTH) {
			//Output is too long
			resultString = "[...";
		}
		resultString += "=" + Long.toString(resultValue / VALUES_PRECISION_FACTOR) + ":" + resultBody + "]";
	}
}
