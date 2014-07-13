package ohm.dexp;

/**
 * Represent a dice roll result.
 * @author Ohmnibus
 *
 */
public class DResult {
	protected long result;
	protected long maxResult;
	protected long minResult;
	protected String resultText;
	protected DExpression expression;
	
	/** The result is fixed (is a computation, not a roll) */
	public static final int RESULT_FIXED = -2;
	/** The result type is not computable  */
	public static final int RESULT_UNKNOWN = -3;
	/** The result is a fumble (below 0%) */
	public static final int RESULT_FUMBLE = -1;
	/** The result is a critical (above 100%) */
	public static final int RESULT_CRITICAL = 101;

//	/** No error */
//	public static final int ERR_NONE = 0;
//	/** The expression contain an unrecognized function */
//	public static final int ERR_UNKNOWN_FUNCTION = 1;
//	/** Expected end of statement */
//	public static final int ERR_EXPECTED_END_OF_STAT = 2;
//	/** Missing operand */
//	public static final int ERR_MISSING_OPERAND = 3;
//	/** A function has been provided by too many parameters */
//	public static final int ERR_TOO_MANY_PARAMETERS = 4;
//	/** A function has been provided by too few parameters */
//	public static final int ERR_TOO_FEW_PARAMETERS = 5;
//	/** The expression contains unbalanced parenthesis */
//	public static final int ERR_UNBALANCED_PARENTHESYS = 6;
//	/** The expression is empty */
//	public static final int ERR_NOTHING_TO_EVALUATE = 7;
//	/** The expression contains a division by zero */
//	public static final int ERR_DIVISION_BY_ZERO = 8;
//	/** The expression contains an unrecognized variable */
//	public static final int ERR_UNKNOWN_VARIABLE = 9;
//	/** The expression contains an invalid character */
//	public static final int ERR_INVALID_CHARACTER = 10;
//	
//	public static final int ERROR_MESSAGES = ERR_INVALID_CHARACTER;

	/**
	 * Initialize the {@link DResult} object.
	 * @param rawResult Expression result.
	 * @param maxResult Maximum expression result.
	 * @param minResult Minimum expression result.
	 * @param resultText Result text.
	 * @param expression Reference to the expression that generates this result.
	 */
	public DResult(long rawResult, long maxResult, long minResult, String resultText, DExpression expression) {
		this.result = rawResult;
		this.maxResult = maxResult;
		this.minResult = minResult;
		this.resultText = resultText;
		this.expression = expression;
	}

	/**
	 * Get the expression result trunked to it's integer part.
	 * @return The expression numerical result.
	 */
	public long getResult() {
		return getRawResult() / TokenBase.VALUES_PRECISION_FACTOR;
	}
	
	/**
	 * Get the raw expression result.<br />
	 * This is a fixed point value with {@link TokenBase.VALUES_DECIMALS} decimal values
	 * and need do be adjusted to obtain the real expression result.<br />
	 * @return The raw expression result.
	 */
	public long getRawResult() {
		return result;
	}
	
	/**
	 * Get the expression maximum result.
	 * @return Expression maximum result.
	 */
	public long getMaxResult() {
		return getMaxRawResult() / TokenBase.VALUES_PRECISION_FACTOR;
	}
	
	/**
	 * Get the expression minimum result.
	 * @return Expression minimum result.
	 */
	public long getMinResult() {
		return getMinRawResult() / TokenBase.VALUES_PRECISION_FACTOR;
	}
	
	/**
	 * Get the expression raw maximum result.
	 * @return Expression maximum result in "raw" format (like {@link getRawResult}).
	 */
	public long getMaxRawResult() {
		return maxResult;
	}
	
	/**
	 * Get the expression raw minimum result.
	 * @return Expression minimum result in "raw" format (like {@link getRawResult}).
	 */
	public long getMinRawResult() {
		return minResult;
	}
	
	/**
	 * Get a string representing the expression with roll result in place of dices. 
	 * @return String representation of the result.
	 */
	public String getResultText() {
		return resultText;
	}
	
	/**
	 * Get the result as percentile compared to the expression maximum result and minimum result.
	 * @return A value usually ranging from 0 to 100. Other results can be:<br />
	 * {@link RESULT_FIXED}:  The result is fixed (is a computation, not a roll);<br />
	 * {@link RESULT_UNKNOWN}:  The result type is not computable;<br />
	 * {@link RESULT_FUMBLE}:  The result is a fumble (below 0%);<br />
	 * {@link RESULT_CRITICAL}:  The result is a critical (above 100%)
	 */
	public int getPercentResult() {
		int retVal;
		long range;
		long baseResult;

		range = maxResult - minResult;
		baseResult = result - minResult;
		
		if (range < 0) {
			//Negative range. Something went wrong.
			retVal = RESULT_UNKNOWN;
		} else if (range == 0) {
			//No range: this is not a roll, this is a computation.
			retVal = RESULT_FIXED;
		} else {
			retVal = (int)((baseResult * 100) / range);
			if (retVal < 0) {
				retVal = RESULT_FUMBLE;
			} else if (retVal > 100) {
				retVal = RESULT_CRITICAL;
			}
		}

		return retVal;
	}

	/**
	 * Get the expression from which this result was generated.
	 * @return Generating expression.
	 */
	public DExpression getExpression() {
		return expression;
	}
}
