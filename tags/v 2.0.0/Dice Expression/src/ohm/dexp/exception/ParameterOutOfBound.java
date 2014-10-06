package ohm.dexp.exception;

/**
 * This exception is thrown when a parameter is out of bound.
 * @author Ohmnibus
 *
 */
public class ParameterOutOfBound extends DException {

	/**
	 * Serial version UID used for serialization.
	 */
	private static final long serialVersionUID = 5641695987589184786L;
	
	protected String fncName;
	protected int parIndex;
	
	/**
	 * Construct a new exception with the specified detail message, function name and cause.
	 * @param message Error message.
	 * @param functionName Function that raised the error.
	 * @param paramIndex Index (1-based) of the parameter that lead to the error.
	 * @param cause The cause.
	 */
	public ParameterOutOfBound(String message, String functionName, int paramIndex, Throwable cause) {
		super(message, cause);
		
		this.fncName = functionName;
		this.parIndex = paramIndex;
	}

	/**
	 * Construct a new exception with the specified detail message and function name.
	 * @param message Error message.
	 * @param functionName Function that raised the error.
	 * @param paramIndex Index (1-based) of the parameter that lead to the error.
	 */
	public ParameterOutOfBound(String message, String functionName, int paramIndex) {
		this(message, functionName, paramIndex, null);
	}

	/**
	 * Construct a new exception with the specified function name.
	 * @param functionName Function that raised the error.
	 * @param paramIndex Index (1-based) of the parameter that lead to the error.
	 */
	public ParameterOutOfBound(String functionName, int paramIndex) {
		this(null, functionName, paramIndex, null);
	}
	
	/**
	 * Get the name of the function that raised the error.
	 * @return Function that raised the error.
	 */
	public String getFunctionName() {
		return fncName;
	}
	
	/**
	 * Get the index (1-based) of the parameter that lead to this exception.
	 * @return Index of the parameter that lead to this exception.
	 */
	public int getParamIndex() {
		return parIndex;
	}
}
