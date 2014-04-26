package ohm.dexp.exception;

public class DivisionByZero extends DException {

	/**
	 * Serial version UID used for serialization.
	 */
	private static final long serialVersionUID = -2796708950986542966L;

	/**
	 * Constructs a new exception with {@code null} as its detail message. 
	 * The cause is not initialized, and may subsequently be initialized 
	 * by a call to {@link Throwable.initCause(java.lang.Throwable)}.
	 */
	public DivisionByZero() {
		super();
	}

	/**
	 * Constructs a new exception with the specified detail message and cause.
	 * @param message The detail message.
	 * @param cause The cause.
	 */
	public DivisionByZero(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new exception with the specified detail message. 
	 * The cause is not initialized, and may subsequently be initialized by a call 
	 * to {@link Throwable.initCause(java.lang.Throwable)}.
	 * @param message The detail message.
	 */
	public DivisionByZero(String message) {
		super(message);
	}
}
