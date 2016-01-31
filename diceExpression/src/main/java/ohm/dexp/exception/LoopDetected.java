package ohm.dexp.exception;

/**
 * This exception indicates the presence of a (possible) loop.
 * @author Ohmnibus
 *
 */
public class LoopDetected extends DException {

	/**
	 * Serial version UID used for serialization.
	 */
	private static final long serialVersionUID = -8598515574652436481L;
	
	protected String fncName;
	
	/**
	 * Construct a new exception with the specified detail message, function name and cause.
	 * @param message Error message.
	 * @param functionName Function that raised the error.
	 * @param cause The cause.
	 */
	public LoopDetected(String message, String functionName, Throwable cause) {
		super(message, cause);
		
		this.fncName = functionName;
	}

	/**
	 * Construct a new exception with the specified detail message and function name.
	 * @param message Error message.
	 * @param functionName Function that raised the error.
	 */
	public LoopDetected(String message, String functionName) {
		this(message, functionName, null);
	}

	/**
	 * Construct a new exception with the specified function name.
	 * @param functionName Function that raised the error.
	 */
	public LoopDetected(String functionName) {
		this(null, functionName, null);
	}
	
	/**
	 * Get the name of the function that raised the error.
	 * @return Function that raised the error.
	 */
	public String getFunctionName() {
		return fncName;
	}
}
