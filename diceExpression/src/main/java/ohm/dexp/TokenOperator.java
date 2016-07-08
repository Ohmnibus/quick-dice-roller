package ohm.dexp;

import ohm.dexp.exception.DException;
import ohm.dexp.exception.DivisionByZero;
import ohm.dexp.exception.InvalidCharacter;

public abstract class TokenOperator extends TokenBase {

	/**
	 * Protected constructor.
	 * @param position Token position.
	 */
	protected TokenOperator(int position) {
		super(position);
	}

	/**
	 * Initialize the right operator token by it's name.
	 * @param name Name of the operator.
	 * @param position Token position.
	 * @return An instance representing the operator, or {@code null} if not found.
	 */
	public static TokenOperator InitToken(String name, int position) throws InvalidCharacter {
		if (name.equals("+")) {
			return new TokenOperatorAdd(position);
		} else if (name.equals("-")) {
			return new TokenOperatorSubtract(position);
		} else if (name.equals("*")) {
			return new TokenOperatorMultiply(position);
		} else if (name.equals("/")) {
			return new TokenOperatorDivide(position);
		} else if (name.equals("d") || name.equals("w") || name.equals("t")) { //"w" is for Germans, "t" for swedish
			return new TokenOperatorDice(position);
		}
		throw new InvalidCharacter(position);
		//return null;
	}
}

/**
 * Interface of operators that can be unary.
 */
interface UnaryOperator {

	/**
	 * Tell if the operator is unary
	 * @return {@code true} if this is a unary operator,
	 * {@code false} otherwise.
	 */
	boolean isUnary();

	/**
	 * Set the unariary of the operator
	 * @param unary {@code true} to set this operator as unary,
	 * {@code false} otherwise.
	 */
	void setUnary(boolean unary);
}

class TokenOperatorAdd extends TokenOperator implements UnaryOperator {

	protected TokenOperatorAdd(int position) {
		super(position);
	}

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
		return unary ? PRIO_UNARY : PRIO_ADDICTIVE;
	}

	@Override
	protected void evaluateSelf(DContext instance) throws DException {
		//int retVal;
		TokenBase lChild;
		TokenBase rChild;
		
		lChild = getLeftChild();
		rChild = getRightChild();

		//if (lChild != null) {
		if (! unary) {
			
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

	private boolean unary = false;

	@Override
	public boolean isUnary() {
		return unary;
	}

	@Override
	public void setUnary(boolean unary) {
		this.unary = unary;
	}
}

class TokenOperatorSubtract extends TokenOperator implements UnaryOperator {

	protected TokenOperatorSubtract(int position) {
		super(position);
	}

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
		return unary ? PRIO_UNARY : PRIO_ADDICTIVE;
	}

	@Override
	protected void evaluateSelf(DContext instance) throws DException {
		TokenBase lChild;
		TokenBase rChild;
		
		lChild = getLeftChild();
		rChild = getRightChild();

		//if (lChild != null) {
		if (! unary) {
				
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

	private boolean unary = false;

	@Override
	public boolean isUnary() {
		return unary;
	}

	@Override
	public void setUnary(boolean unary) {
		this.unary = unary;
	}
}

class TokenOperatorMultiply extends TokenOperator {

	protected TokenOperatorMultiply(int position) {
		super(position);
	}

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
		return PRIO_MULTIPLICATIVE;
	}

	@Override
	protected void evaluateSelf(DContext instance) throws DException {

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

	protected TokenOperatorDivide(int position) {
		super(position);
	}

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
		return PRIO_MULTIPLICATIVE;
	}

	@Override
	protected void evaluateSelf(DContext instance) throws DException {
		
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

class TokenOperatorDice extends TokenOperator implements UnaryOperator {

	protected TokenOperatorDice(int position) {
		super(position);
	}

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
		return PRIO_DICE;
	}

	@Override
	protected void evaluateSelf(DContext instance) throws DException {
		Dice dice;
		int lResult;
		long lMaxResult;
		long lMinResult;
		
		TokenBase lChild = getLeftChild();
		TokenBase rChild = getRightChild();

		//if (lChild != null) {
		if (! unary) {
			lChild.evaluate(instance);
			lResult = (int)lChild.getResult();
			lMaxResult = lChild.resultMaxValue;
			lMinResult = lChild.resultMinValue;
		} else {
			//Unary roll
			lResult = 1;
			lMaxResult = /* 1 * */VALUES_PRECISION_FACTOR;
			lMinResult = /* 1 * */VALUES_PRECISION_FACTOR;
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

	private boolean unary = false;

	@Override
	public boolean isUnary() {
		return unary;
	}

	@Override
	public void setUnary(boolean unary) {
		this.unary = unary;
	}
}

