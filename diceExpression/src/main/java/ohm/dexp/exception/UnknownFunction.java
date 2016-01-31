package ohm.dexp.exception;

public class UnknownFunction extends DParseException {

	/**
	 * Serial version UID used for serialization.
	 */
	private static final long serialVersionUID = -7145316795738528396L;
	
	protected String name;

	/**
	 * Construct a new exception with specified message, unrecognized function name,
	 * expression bounds and cause.
	 * @param message The detail message.
	 * @param name The unrecognized function name.
	 * @param fromChar First char of the expression containing the unrecognized function name.
	 * @param toChar Last char of the expression containing the unrecognized function name.
	 * @param cause The cause.
	 */
	public UnknownFunction(String message, String name, int fromChar, int toChar, Throwable cause) {
		super(message, fromChar, toChar, cause);

		this.name = name;
	}

	/**
	 * Construct a new exception with specified message, unrecognized function name
	 * and expression bounds.
	 * @param message The detail message.
	 * @param name The unrecognized function name.
	 * @param fromChar First char of the expression containing the unrecognized function name.
	 * @param toChar Last char of the expression containing the unrecognized function name.
	 */
	public UnknownFunction(String message, String name, int fromChar, int toChar) {
		this(message, name, fromChar, toChar, null);
	}
	
	/**
	 * Construct a new exception with specified unrecognized function name and expression bounds.
	 * @param name The unrecognized function name.
	 * @param fromChar First char of the expression containing the unrecognized function name.
	 * @param toChar Last char of the expression containing the unrecognized function name.
	 */
	public UnknownFunction(String name, int fromChar, int toChar) {
		this(null, name, fromChar, toChar, null);
	}

	/**
	 * Get the unrecognized function name.
	 * @return The unrecognized function name.
	 */
	public String getName() {
		return name;
	}
}
