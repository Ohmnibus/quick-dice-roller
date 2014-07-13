package ohm.dexp.exception;

/**
 * Base exception for the dice expression evaluator.
 * @author Ohmnibus
 *
 */
public abstract class DException extends Exception {

	/**
	 * Serial version UID used for serialization.
	 */
	private static final long serialVersionUID = -8205500679586127845L;
	
	/**
	 * Constructs a new exception with {@code null} as its detail message. 
	 * The cause is not initialized, and may subsequently be initialized 
	 * by a call to {@link Throwable.initCause(java.lang.Throwable)}.
	 */
	public DException() {
		super();
	}

	/**
	 * Constructs a new exception with the specified detail message and cause.
	 * @param message The detail message.
	 * @param cause The cause.
	 */
	public DException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new exception with the specified detail message. 
	 * The cause is not initialized, and may subsequently be initialized by a call 
	 * to {@link Throwable.initCause(java.lang.Throwable)}.
	 * @param message The detail message.
	 */
	public DException(String message) {
		this(message, null);
	}

	/**
	 * Constructs a new exception with the specified cause 
	 * and a detail message of {@code (cause==null ? null : cause.toString())} 
	 * (which typically contains the class and detail message of cause).
	 * @param cause The cause.
	 */
	public DException(Throwable cause) {
		this(cause==null ? null : cause.toString(), cause);
	}

}
