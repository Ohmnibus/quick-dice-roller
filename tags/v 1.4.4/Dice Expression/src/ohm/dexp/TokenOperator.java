package ohm.dexp;

import ohm.dexp.exception.DException;
import ohm.dexp.exception.DivisionByZero;

public abstract class TokenOperator extends TokenBase {
	
	/**
	 * Initialize the right operator token by it's name.
	 * @param name Name of the operator.
	 * @return An instance representing the operator, or {@code null} if not found.
	 */
	public static TokenOperator InitToken(String name) {
		if (name.equals("+")) {
			return new TokenOperatorAdd();
		} else if (name.equals("-")) {
			return new TokenOperatorSubtract();
		} else if (name.equals("*")) {
			return new TokenOperatorMultiply();
		} else if (name.equals("/")) {
			return new TokenOperatorDivide();
		} else if (name.equals("d") || name.equals("w")) { //"w" is for Germans
			return new TokenOperatorDice();
		}
		return null;
	}
}

class TokenOperatorAdd extends TokenOperator {

	@Override
	public int initChildNumber() {
		return 2;
	}

	@Override
	public int getType() {
		return 3;
	}

	@Override
	public int getPriority() {
		return 1;
	}

	@Override
	protected void evaluateSelf(DInstance instance) throws DException {
		//int retVal;
		TokenBase lChild;
		TokenBase rChild;
		
		lChild = getLeftChild();
		rChild = getRightChild();
		
		if (lChild != null) {
			
			lChild.evaluate(instance);
			rChild.evaluate(instance);
	
			//if (retVal == DResult.ERR_NONE) {
			resultValue = lChild.resultValue + rChild.resultValue;
			resultMaxValue = lChild.resultMaxValue + rChild.resultMaxValue;
			resultMinValue = lChild.resultMinValue + rChild.resultMinValue;
			reorderMaxMinValues();
			resultString = lChild.resultString + "+" + rChild.resultString;
			//} else {
			//	resultValue = 0;
			//	resultMaxValue = 0;
			//	resultMinValue = 0;
			//	resultString = "0";
			//}
		} else {
			//Unary addiction
			
			rChild.evaluate(instance);
	
			//if (retVal == DResult.ERR_NONE) {
			resultValue = rChild.resultValue;
			resultMaxValue = rChild.resultMaxValue;
			resultMinValue = rChild.resultMinValue;
			reorderMaxMinValues();
			resultString = rChild.resultString;
			//} else {
			//	resultValue = 0;
			//	resultMaxValue = 0;
			//	resultMinValue = 0;
			//	resultString = "0";
			//}
		}
		
		//return retVal;
	}
}

class TokenOperatorSubtract extends TokenOperator {

	@Override
	protected int initChildNumber() {
		return 2;
	}

	@Override
	public int getType() {
		return 4;
	}

	@Override
	public int getPriority() {
		return 1;
	}

	@Override
	protected void evaluateSelf(DInstance instance) throws DException {
		TokenBase lChild;
		TokenBase rChild;
		
		lChild = getLeftChild();
		rChild = getRightChild();

		if (lChild != null) {
				
			lChild.evaluate(instance);
			rChild.evaluate(instance);
	
			resultValue = lChild.resultValue - rChild.resultValue;
			resultMaxValue = lChild.resultMaxValue - rChild.resultMinValue; //Max-Min
			resultMinValue = lChild.resultMinValue - rChild.resultMaxValue; //Min-Max
			reorderMaxMinValues();
			resultString = lChild.resultString + "-" + rChild.resultString;
		} else {
			//Unary subtraction (get negative value)

			rChild.evaluate(instance);
	
			resultValue = - rChild.resultValue;
			resultMaxValue = - rChild.resultMaxValue;
			resultMinValue = - rChild.resultMinValue;
			reorderMaxMinValues();
			resultString = "-" + rChild.resultString; //This can lead to "--value"
		}
	}
}

class TokenOperatorMultiply extends TokenOperator {

	@Override
	protected int initChildNumber() {
		return 2;
	}

	@Override
	public int getType() {
		return 5;
	}

	@Override
	public int getPriority() {
		return 2;
	}

	@Override
	protected void evaluateSelf(DInstance instance) throws DException {

		TokenBase lChild = getLeftChild();
		TokenBase rChild = getRightChild();

		lChild.evaluate(instance);

		// Check for left token. If is an operator with lower precedence,
		// enclose it's result expression within parenthesis
		if (lChild.getPriority() > 0
				&& lChild.getPriority() < getPriority())
			lChild.resultString = "(" + lChild.resultString + ")";

		rChild.evaluate(instance);

		// Check for right token. If is an operator with lower precedence,
		// enclose it's result expression within parenthesis
		if (rChild.getPriority() > 0
				&& rChild.getPriority() < getPriority())
			rChild.resultString = "(" + rChild.resultString + ")";

		resultValue = (lChild.resultValue * rChild.resultValue) / VALUES_PRECISION_FACTOR;
		resultMaxValue = (lChild.resultMaxValue * rChild.resultMaxValue) / VALUES_PRECISION_FACTOR;
		resultMinValue = (lChild.resultMinValue * rChild.resultMinValue) / VALUES_PRECISION_FACTOR;
		reorderMaxMinValues();
		resultString = "(" + lChild.resultString + "*" + rChild.resultString + ")";
	}
}

class TokenOperatorDivide extends TokenOperator {

	@Override
	protected int initChildNumber() {
		return 2;
	}

	@Override
	public int getType() {
		return 6;
	}

	@Override
	public int getPriority() {
		return 2;
	}

	@Override
	protected void evaluateSelf(DInstance instance) throws DException {
		
		TokenBase lChild = getLeftChild();
		TokenBase rChild = getRightChild();

		lChild.evaluate(instance);

		// Check for left token. If is an operator with lower precedence,
		// enclose it's result expression within parenthesis
		if (lChild.getPriority() > 0
				&& lChild.getPriority() < getPriority())
			lChild.resultString = "(" + lChild.resultString + ")";
		
		rChild.evaluate(instance);

		// Check for division by zero
		if (rChild.resultValue == 0)
			throw new DivisionByZero();

		// Check for right token. If is an operator with lower precedence,
		// enclose it's result expression within parenthesis
		if (rChild.getPriority() > 0
				&& rChild.getPriority() < getPriority())
			rChild.resultString = "(" + rChild.resultString + ")";

		resultValue = ((lChild.resultValue * VALUES_PRECISION_FACTOR) / rChild.resultValue);
		if (rChild.resultMaxValue == 0) {
			//Division by 0 for high values.
			//Round up dividing by -0,5
			//resultMaxValue = (lChild.resultMaxValue / (-VALUES_PRECISION_FACTOR / 2)) * VALUES_PRECISION_FACTOR;
			resultMaxValue = -lChild.resultMaxValue * 2;
		} else {
			resultMaxValue = ((lChild.resultMaxValue * VALUES_PRECISION_FACTOR) / rChild.resultMaxValue);
		}
		if (rChild.resultMinValue == 0) {
			//Division by 0 for low values.
			//Round up dividing by 0,5
			//resultMinValue = (lChild.resultMinValue / (VALUES_PRECISION_FACTOR / 2)) * VALUES_PRECISION_FACTOR;
			resultMinValue = lChild.resultMinValue * 2;
		} else {
			resultMinValue = ((lChild.resultMinValue * VALUES_PRECISION_FACTOR) / rChild.resultMinValue);
		}
		reorderMaxMinValues();
		resultString = "(" + lChild.resultString + "/" + rChild.resultString + ")";
	}
}

class TokenOperatorDice extends TokenOperator {

	@Override
	protected int initChildNumber() {
		return 2;
	}

	@Override
	public int getType() {
		return 7;
	}

	@Override
	public int getPriority() {
		return 3;
	}

	@Override
	protected void evaluateSelf(DInstance instance) throws DException {
		Dice dice;
		int lResult;
		long lMaxResult;
		long lMinResult;
		
		TokenBase lChild = getLeftChild();
		TokenBase rChild = getRightChild();
		
		if (lChild != null) {
			lChild.evaluate(instance);
			lResult = (int)lChild.getResult();
			lMaxResult = lChild.resultMaxValue;
			lMinResult = lChild.resultMinValue;
		} else {
			//Unary roll
			lResult = 1;
			lMaxResult = 1 * VALUES_PRECISION_FACTOR;
			lMinResult = 1 * VALUES_PRECISION_FACTOR;
		}
		rChild.evaluate(instance);

		//Used a dice object to handle dice corrections (i.e. a dice cannot have less than 1 face).
		dice = new Dice(
				lResult, //(int)lChild.getResult(),
				(int)rChild.getResult());
		resultValue = dice.roll() * VALUES_PRECISION_FACTOR;

		//Max result: max of dice faces multiplied max of dice numbers.
		//dice.setTimes((int)(lChild.resultMaxValue / TokenBase.VALUES_PRECISION_FACTOR));
		dice.setTimes((int)(lMaxResult / TokenBase.VALUES_PRECISION_FACTOR));
		dice.setFaces((int)(rChild.resultMaxValue / TokenBase.VALUES_PRECISION_FACTOR));
		resultMaxValue = (dice.getTimes() * dice.getFaces()) * VALUES_PRECISION_FACTOR;

		//Min result: min of dice faces multiplied min of dice numbers.
		//dice.setTimes((int)(lChild.resultMinValue / TokenBase.VALUES_PRECISION_FACTOR));
		dice.setTimes((int)(lMinResult / TokenBase.VALUES_PRECISION_FACTOR));
		dice.setFaces((int)(rChild.resultMinValue / TokenBase.VALUES_PRECISION_FACTOR));
		resultMinValue = dice.getTimes() * VALUES_PRECISION_FACTOR;
		//reorderMaxMinValues(); //Not needed here.
		resultString = "[" + Long.toString(resultValue / VALUES_PRECISION_FACTOR) + "]";
	}
}

