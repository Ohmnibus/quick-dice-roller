package ohm.dexp;

import java.text.DecimalFormat;

import ohm.dexp.exception.DException;

/**
 * Token base abstract class.
 * @author Ohmnibus
 *
 */
public abstract class TokenBase {
	
	/** Precision used to perform evaluation */
	public static final int VALUES_PRECISION = 3;
	/** Precision factor used to convert raw values to actual ones */
	public static final int VALUES_PRECISION_FACTOR = (int)java.lang.Math.pow(10, VALUES_PRECISION);

	/** Precision used to output values */
	public static final int VALUES_OUTPUT_PRECISION = 2;
	/** Precision factor used to evaluate output */
	public static final int VALUES_OUTPUT_PRECISION_FACTOR = (int)java.lang.Math.pow(10, VALUES_OUTPUT_PRECISION);

	/** Max size of a single token */
	public static final int MAX_TOKEN_STRING_LENGTH = 64;
	/** Max size of an expression */
	public static final int MAX_TOTAL_STRING_LENGTH = 200;

	/** Max iteration number for a token (function) */
	public static final int MAX_TOKEN_ITERATIONS = 500;
	/** Max iteration number for the expression */
	public static final int MAX_TOTAL_ITERATIONS = 5000;

	/** Index of next argument to be assigned (0-based) */
	protected int nextChild;
	/** Number of child allowed by this token */
	protected int numChild;
	/** Child contained by this token */
	protected TokenBase childList[];

	//private int errNumber;
	
	protected long resultValue;
	protected long resultMaxValue;
	protected long resultMinValue;
	protected String resultString;
	
	private static DecimalFormat format = null;

	/**
	 * Initialize the token.
	 */
	public TokenBase() {
		nextChild = 0;
		numChild = initChildNumber();
		childList = new TokenBase[numChild];

		resultValue = 0;
		resultMaxValue = 0;
		resultMinValue = 0;
		resultString = "";
		//errNumber = DResult.ERR_NONE;
		
		if (format == null) {
			//Initialize the decimal formatter.
			format = new DecimalFormat();
			//format.setMinimumFractionDigits(VALUES_OUTPUT_PRECISION);
			format.setMinimumFractionDigits(0);
			format.setMaximumFractionDigits(VALUES_OUTPUT_PRECISION);
			format.setMinimumIntegerDigits(1);
			format.setMaximumIntegerDigits(Integer.MAX_VALUE);
		}
	}

	/**
	 * Return the number of child required by this token.
	 * @return Number of child required by this token.
	 */
	public int getChildNumber() {
		return numChild;
	}

	/**
	 * Set the left child of the token tree.
	 * @param leftChild Token to set as the left child.
	 */
	public void setLeftChild(TokenBase leftChild) {
		setChild(leftChild, 1);
	} 

	/**
	 * Get the left child of the token tree.
	 * @return The left child of the token tree.
	 */
	public TokenBase getLeftChild() {
		return getChild(1);
	} 

	/**
	 * Set the right child of the token tree.
	 * @param rightChild Token to set as the right child.
	 */
	public void setRightChild(TokenBase rightChild) {
		setChild(rightChild, 2);
	} 

	/**
	 * Get the right child of the token tree.
	 * @return The right child of the token tree.
	 */
	public TokenBase getRightChild() {
		return getChild(2);
	} 

	/**
	 * Set the next unassigned child.
	 * @param nChild Child to set.
	 * @throws IndexOutOfBoundsException Thrown if called a number of times 
	 * greater than the number of accepted arguments.
	 */
	public void setNextChild(TokenBase nChild) throws IndexOutOfBoundsException {
		nextChild++;
		setChild(nChild, nextChild);
	} 

	/**
	 * Index of the next unassigned argument ("1" based)
	 * @return Next unassigned argument index ("1" based)
	 */
	public int nextChildNum() {
		return nextChild+1;
	} 
	
	/**
	 * Set a token as function argument.
	 * @param child Argument value
	 * @param index Argument index ("1" based)
	 * @throws IndexOutOfBoundsException Thrown if index is less than 1 
	 * or greater than the number of accepted arguments.
	 */
	public void setChild(TokenBase child, int index) throws IndexOutOfBoundsException {
		if (index>numChild || index < 1) {
			throw new IndexOutOfBoundsException("TokenBase.setChild: Out of bound.");
		}
		childList[index-1]=child;
	}
	
	/**
	 * Get a function argument as a token.
	 * @param index Argument index ("1" based)
	 * @return Argument, as token
	 * @throws IndexOutOfBoundsException Thrown if index is less than 1 
	 * or greater than the number of accepted arguments.
	 */
	public TokenBase getChild(int index) throws IndexOutOfBoundsException {
		if (index>numChild || index < 1) {
			throw new IndexOutOfBoundsException("TokenBase.getChild: Out of bound");
		}
		return childList[index-1];
	}
	
	/**
	 * Evaluate current token tree.
	 * @param instance Instance to use to evaluate variables.
	 * @throws DException Thrown if an error occurred during expression tree evaluation.
	 */
	public void evaluate(DInstance instance) throws DException {
//		DException retVal;
//		
//		retVal = null;
//		
//		errNumber = evaluateSelf(instance);
//		
//		switch (errNumber) {
//		case DResult.ERR_NONE:
//			retVal = null;
//			break;
//		case DResult.ERR_DIVISION_BY_ZERO:
//			retVal = new DivisionByZero();
//			break;
//		default:
//			retVal = new UnexpectedError();
//			break;
//		}
//		
//		return retVal;
		evaluateSelf(instance);
	}

	/**
	 * Get the token tree numeric integer value.
	 * @return The token tree numeric integer value.
	 */
	public long getResult() {
		return getRawResult() / TokenBase.VALUES_PRECISION_FACTOR;
	}
	
	/**
	 * Get the token last evaluation result.
	 * @return The token tree numeric value. This value is a fixed point value with
	 * VALUES_DECIMALS decimal, so to obtain the current value must be divided 
	 * by 10 ^ VALUES_DECIMALS.
	 */
	public long getRawResult() {
		//evaluate();
		return resultValue;
	}

	/**
	 * Return the token tree maximum result value.<br />
	 * This value is different from standard result only if the expression contains dice. 
	 * @return The token tree maximum value. This is a fixed point value, same as 
	 * {@link getRawResult}.
	 */
	public long getMaxResult() {
		//This is always a constant value, so just one evaluation is required.
		//if (! evaluatedOnce) {
		//	evaluate();
		//}
		return resultMaxValue;
	}
	
	/**
	 * Return the token tree minimum result value.<br />
	 * This value is different from standard result only if the expression contains dice. 
	 * @return The token tree minimum value. This is a fixed point value, same as 
	 * {@link getRawResult}.
	 */
	public long getMinResult() {
		//This is always a constant value, so just one evaluation is required.
		//if (! evaluatedOnce) {
		//	evaluate();
		//}
		return resultMinValue;
	}

	/**
	 * Get the token tree string representing it's expression.
	 * @return The token tree expression.
	 */
	public String getResultString() {
		//evaluate();
		return resultString;
	}
	
	///**
	// * Get the token error code.
	// * @return Error code.
	// */
	//public int getError() {
	//	return errNumber;
	//}
	
	/**
	 * Convert a raw value into it's {@link String} expression.
	 * @param rawValue Raw numeric value.
	 * @return String representation of the value.
	 */
	public static String rawValueToString(long rawValue) {
		//return Long.toString(rawValue / VALUES_PRECISION_FACTOR) + 
		//    "." + 
		//    Long.toString((rawValue % VALUES_PRECISION_FACTOR) / VALUES_OUTPUT_PRECISION_FACTOR);
		return format.format(rawValue / VALUES_PRECISION_FACTOR);
	}
	
	/**
	 * Switch {@link resultMaxValue} and {@link resultMinValue} if their values aren't correct.
	 */
	protected void reorderMaxMinValues() {
		if (resultMaxValue < resultMinValue) {
			long tmp = resultMaxValue;
			resultMaxValue = resultMinValue;
			resultMinValue = tmp;
		}
	}
	
	// ================
	// Abstract methods
	// ================

	/**
	 * Get the required token number for initialization purpose.
	 * @return The number of child required by the token.
	 */
	abstract protected int initChildNumber();

	/**
	 * Get the token type. Each token class must return a different type.
	 * @return An integer. Must be different for each different token type.
	 */
	abstract public int getType();
	
	/**
	 * Get the token evaluation priority.
	 * @return The token evaluation priority. Allowed values are:<br />
	 * 0 for functions and values;<br />
	 * 1 for "+" and "-" operators;<br />
	 * 2 for "*" and "/" operators;<br />
	 * 3 for "d" operator.<br />
	 * Higher priority values must be evaluated first.
	 */
	abstract public int getPriority();

	/**
	 * Evaluate current token tree.<br />
	 * This method must evaluate the token subtree and assign proper values to:<br />
	 * resultValue<br />
	 * resultMaxValue<br />
	 * resultMinValue<br />
	 * resultString
	 * @param instance Instance to use to evaluate variables.
	 * @throws DException Thrown if an error occurred during expression tree evaluation.
	 */
	abstract protected void evaluateSelf(DInstance instance) throws DException;
}
