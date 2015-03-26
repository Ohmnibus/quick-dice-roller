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
	public static final int VALUES_PRECISION_DIGITS = 3;
	/** Precision factor used to convert raw values to actual ones */
	public static final int VALUES_PRECISION_FACTOR = (int)java.lang.Math.pow(10, VALUES_PRECISION_DIGITS);

	/** Precision used to output values */
	public static final int VALUES_OUTPUT_PRECISION_DIGITS = 2;
	/** Precision factor used to evaluate output */
	public static final int VALUES_OUTPUT_PRECISION_FACTOR = (int)java.lang.Math.pow(10, VALUES_OUTPUT_PRECISION_DIGITS);

	/** Max size of a single token */
	public static final int MAX_TOKEN_STRING_LENGTH = 64;
	/** Max size of an expression */
	public static final int MAX_TOTAL_STRING_LENGTH = 200;

	/** Max iteration number for a token (function) */
	public static final int MAX_TOKEN_ITERATIONS = 500;
	/** Max iteration number for the expression */
	public static final int MAX_TOTAL_ITERATIONS = 5000;
	
	/** Generation to use to get the root with {@link getParent} */
	protected static final int ROOT_GENERATION = Integer.MAX_VALUE;

	/** Child contained by this token */
	private TokenBase parent;

	/** Index of next argument to be assigned (0-based) */
	private int nextChild;
	/** Number of child required by this token */
	private int numChild;
	/** Number of child allowed by this token */
	private int maxNumChild;
	/** Child contained by this token */
	private TokenBase[] childList;

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
		maxNumChild = numChild + initOptionalChildNumber();
		childList = new TokenBase[maxNumChild];

		resultValue = 0;
		resultMaxValue = 0;
		resultMinValue = 0;
		resultString = "";
		
		if (format == null) {
			//Initialize the decimal formatter.
			format = new DecimalFormat();
			//format.setMinimumFractionDigits(VALUES_OUTPUT_PRECISION);
			format.setMinimumFractionDigits(0);
			format.setMaximumFractionDigits(VALUES_OUTPUT_PRECISION_DIGITS);
			format.setMinimumIntegerDigits(1);
			format.setMaximumIntegerDigits(Integer.MAX_VALUE);
		}
	}

	/**
	 * Return the number of mandatory child required by this token.
	 * @return Number of mandatory child required by this token.
	 */
	public int getChildNumber() {
		return numChild;
	}

	/**
	 * Return the maximum number of child allowed by this token.
	 * @return Maximum number of child allowed by this token.
	 */
	public int getMaxChildNumber() {
		return maxNumChild;
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
		return nextChild + 1;
	} 
	
	/**
	 * Set a token as function argument.
	 * @param child Argument value
	 * @param index Argument index ("1" based)
	 * @throws IndexOutOfBoundsException Thrown if index is less than 1 
	 * or greater than the number of accepted arguments.
	 */
	public void setChild(TokenBase child, int index) throws IndexOutOfBoundsException {
		if (index > maxNumChild || index < 1) {
			throw new IndexOutOfBoundsException("TokenBase.setChild: Out of bound.");
		}
		childList[index-1] = child;
		if (childList[index-1] != null) {
			childList[index-1].setParent(this);
		}
	}
	
	/**
	 * Get a function argument as a token.
	 * @param index Argument index ("1" based)
	 * @return Argument, as token
	 * @throws IndexOutOfBoundsException Thrown if index is less than 1 
	 * or greater than the number of accepted arguments.
	 */
	public TokenBase getChild(int index) throws IndexOutOfBoundsException {
		if (index > maxNumChild || index < 1) {
			throw new IndexOutOfBoundsException("TokenBase.getChild: Out of bound");
		}
		return childList[index-1];
	}
	
	/**
	 * Evaluate current token tree.
	 * @param instance Instance to use to evaluate variables.
	 * @throws DException Thrown if an error occurred during expression tree evaluation.
	 */
	public void evaluate(DContext instance) throws DException {
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
		return resultValue;
	}

	/**
	 * Return the token tree maximum result value.<br />
	 * This value is different from standard result only if the expression contains dice. 
	 * @return The token tree maximum value. This is a fixed point value, same as 
	 * {@link getRawResult}.
	 */
	public long getMaxResult() {
		return resultMaxValue;
	}
	
	/**
	 * Return the token tree minimum result value.<br />
	 * This value is different from standard result only if the expression contains dice. 
	 * @return The token tree minimum value. This is a fixed point value, same as 
	 * {@link getRawResult}.
	 */
	public long getMinResult() {
		return resultMinValue;
	}

	/**
	 * Get the token tree string representing it's expression.
	 * @return The token tree expression.
	 */
	public String getResultString() {
		return resultString;
	}
	
	/**
	 * Convert a raw value into it's {@link String} expression.
	 * @param rawValue Raw numeric value.
	 * @return String representation of the value.
	 */
	public static String rawValueToString(long rawValue) {
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
	
	/**
	 * Set the parent of this instance.
	 * @param parent
	 */
	protected void setParent(TokenBase parent) {
		this.parent = parent;
	}

	/**
	 * Return the ancestor of this instance.<br />
	 * The parameter {@code generation} tell which ancestor to get: {@code 0} mean 
	 * the instance itself, {@code 1} is the parent, {@code 2} is the grand parent and so on.<br />
	 * The function will stop and return the root token if the given {@code generation} is too high
	 * or if is equal to {@link ROOT_GENERATION}.
	 * @param generation Generation to get.
	 * @return Ancestor of the token.
	 */
	protected TokenBase getParent(int generation) {
		TokenBase retVal;
		int cnt = 0;
		retVal = this;
		while (retVal.parent != null && cnt < generation) {
			retVal = retVal.parent;
			cnt++;
		}
		return retVal;
	}
	
	// ================
	// Abstract methods
	// ================

	/**
	 * Initializes the number of mandatory child tokens required by this token.
	 * @return The number of mandatory child tokens required.
	 */
	abstract protected int initChildNumber();

	/**
	 * Initializes the number of optional child tokens allowed by this token.
	 * @return The number of optional child tokens allowed.
	 */
	protected int initOptionalChildNumber() {
		return 0;
	}

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
	abstract protected void evaluateSelf(DContext instance) throws DException;
}

class TokenRoot extends TokenBase {

	public TokenRoot(TokenBase root) {
		setChild(root, 1);
	}
	
	@Override
	protected int initChildNumber() {
		return 1;
	}

	@Override
	public int getType() {
		return -1;
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	protected void evaluateSelf(DContext instance) throws DException {
		TokenBase child = getChild(1);
		child.evaluate(instance);
		resultValue = child.resultValue;
		resultMaxValue = child.resultMaxValue;
		resultMinValue = child.resultMinValue;
		resultString = child.resultString;
	}
	
	@Override
	protected void setParent(TokenBase parent) {
		super.setParent(null); //Root can't got a parent!
	}
	
//	public TokenBase getRoot() {
//		return getChild(1);
//	}
//	
//	public void setRoot(TokenBase root) {
//		setChild(root, 1);
//	}

}
