package ohm.dexp.exception;

public class NothingToEvaluate extends DParseException {

	/**
	 * Serial version UID used for serialization.
	 */
	private static final long serialVersionUID = 1053996748101231436L;

	/**
	 * Construct a new exception.
	 */
	public NothingToEvaluate() {
		super(0, 0);
	}

	/**
	 * Construct a new exception with specified message and cause.
	 * @param message The detail message.
	 * @param cause The cause.
	 */
	public NothingToEvaluate(String message, Throwable cause) {
		super(message, 0, 0, cause);
	}

	/**
	 * Construct a new exception with specified message.
	 * @param message The detail message.
	 */
	public NothingToEvaluate(String message) {
		super(message, 0, 0);
	}
}
