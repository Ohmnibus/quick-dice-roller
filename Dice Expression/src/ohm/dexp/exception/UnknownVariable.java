package ohm.dexp.exception;

public class UnknownVariable extends DParseException {
	
	/**
	 * Serial version UID used for serialization. 
	 */
	private static final long serialVersionUID = -1791356385635112098L;
	
	protected String name;

	/**
	 * Construct a new exception with specified message, unrecognized variable name,
	 * expression bounds and cause.
	 * @param message The detail message.
	 * @param name The unrecognized variable name.
	 * @param fromChar First char of the expression containing the unrecognized variable name.
	 * @param toChar Last char of the expression containing the unrecognized variable name.
	 * @param cause The cause.
	 */
	public UnknownVariable(String message, String name, int fromChar, int toChar, Throwable cause) {
		super(message, fromChar, toChar, cause);

		this.name = name;
	}

	/**
	 * Construct a new exception with specified message, unrecognized variable name
	 * and expression bounds.
	 * @param message The detail message.
	 * @param name The unrecognized variable name.
	 * @param fromChar First char of the expression containing the unrecognized variable name.
	 * @param toChar Last char of the expression containing the unrecognized variable name.
	 */
	public UnknownVariable(String message, String name, int fromChar, int toChar) {
		this(message, name, fromChar, toChar, null);
	}
	
	/**
	 * Construct a new exception with specified unrecognized variable name and expression bounds.
	 * @param name The unrecognized variable name.
	 * @param fromChar First char of the expression containing the unrecognized variable name.
	 * @param toChar Last char of the expression containing the unrecognized variable name.
	 */
	public UnknownVariable(String name, int fromChar, int toChar) {
		this(null, name, fromChar, toChar, null);
	}

	/**
	 * Get the unrecognized variable name.
	 * @return The unrecognized variable name.
	 */
	public String getName() {
		return name;
	}

}
