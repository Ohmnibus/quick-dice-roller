package ohm.dexp.exception;

public class ExpectedParameter extends DParseException {

	/**
	 * Serial version UID used for serialization.
	 */
	private static final long serialVersionUID = -6303774073479938814L;

	/**
	 * Construct a new exception with specified error position.
	 * @param atChar The position in the expression at which the error occurred.
	 */
	public ExpectedParameter(int atChar) {
		super(atChar, atChar);
	}

	/**
	 * Construct a new exception with specified message and error position.
	 * @param message The detail message.
	 * @param atChar The position in the expression at which the error occurred.
	 */
	public ExpectedParameter(String message, int atChar, Throwable cause) {
		super(message, atChar, atChar, cause);
	}

	/**
	 * Construct a new exception with specified message, error position and cause.
	 * @param message The detail message.
	 * @param atChar The position in the expression at which the error occurred.
	 * @param cause The cause.
	 */
	public ExpectedParameter(String message, int atChar) {
		super(message, atChar, atChar);
	}

	/**
	 * Get the position in the expression at which the error occurred.
	 * @return The position in the expression at which the error occurred.
	 */
	public int getPosition() {
		return fromChar;
	}
}
