package ohm.dexp.exception;

public class UnexpectedError extends DException {

	/**
	 * Serial version UID used for serialization.
	 */
	private static final long serialVersionUID = -1600830313441412468L;

	/**
	 * Constructs a new exception with {@code null} as its detail message. 
	 * The cause is not initialized, and may subsequently be initialized 
	 * by a call to {@link Throwable.initCause(java.lang.Throwable)}.
	 */
	public UnexpectedError() {
		super();
	}

	/**
	 * Constructs a new exception with the specified detail message and cause.
	 * @param message The detail message.
	 * @param cause The cause.
	 */
	public UnexpectedError(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new exception with the specified detail message. 
	 * The cause is not initialized, and may subsequently be initialized by a call 
	 * to {@link Throwable.initCause(java.lang.Throwable)}.
	 * @param message The detail message.
	 */
	public UnexpectedError(String message) {
		super(message);
	}
}
