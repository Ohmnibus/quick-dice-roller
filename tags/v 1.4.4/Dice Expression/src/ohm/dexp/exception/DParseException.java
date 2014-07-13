package ohm.dexp.exception;

public abstract class DParseException extends DException {

	/**
	 * Serial version UID used for serialization.
	 */
	private static final long serialVersionUID = -8209796528944791334L;

	protected int fromChar;
	protected int toChar;

	/**
	 * Construct a new exception with specified message, expression bounds and cause.
	 * @param message The detail message.
	 * @param fromChar First char of the expression containing the unrecognized function name.
	 * @param toChar Last char of the expression containing the unrecognized function name.
	 * @param cause The cause.
	 */
	public DParseException(String message, int fromChar, int toChar, Throwable cause) {
		super(message, cause);

		this.fromChar = fromChar;
		this.toChar = toChar;
	}

	/**
	 * Construct a new exception with specified message and expression bounds.
	 * @param message The detail message.
	 * @param fromChar First char of the expression containing the unrecognized function name.
	 * @param toChar Last char of the expression containing the unrecognized function name.
	 */
	public DParseException(String message, int fromChar, int toChar) {
		this(message, fromChar, toChar, null);
	}
	
	/**
	 * Construct a new exception with specified expression bounds.
	 * @param fromChar First char of the expression containing the unrecognized function name.
	 * @param toChar Last char of the expression containing the unrecognized function name.
	 */
	public DParseException(int fromChar, int toChar) {
		this(null, fromChar, toChar, null);
	}

	/**
	 * Get the first char of the expression containing the expression error.
	 * @return First char of the expression containing the expression error.
	 */
	public int getFromChar() {
		return fromChar;
	}

	/**
	 * Get the last char of the expression containing the expression error.
	 * @return Last char of the expression containing the expression error.
	 */
	public int getToChar() {
		return toChar;
	}
}
