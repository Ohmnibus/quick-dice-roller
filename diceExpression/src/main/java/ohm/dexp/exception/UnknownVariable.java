package ohm.dexp.exception;

public class UnknownVariable extends DException {
	
	/**
	 * Serial version UID used for serialization. 
	 */
	private static final long serialVersionUID = -1791356385635112098L;
	
	protected String name;
	protected int position;

	/**
	 * Construct a new exception with specified message, unrecognized variable name,
	 * expression bounds and cause.
	 * @param message The detail message.
	 * @param name The unrecognized variable name.
	 * @param position Position of the variable inside of the expression.
	 * @param cause The cause.
	 */
	public UnknownVariable(String message, String name, int position, Throwable cause) {
		super(message, cause);

		this.name = name;
		this.position = position;
	}

	/**
	 * Construct a new exception with specified message, unrecognized variable name
	 * and expression bounds.
	 * @param message The detail message.
	 * @param name The unrecognized variable name.
	 * @param position Position of the variable inside of the expression.
	 */
	public UnknownVariable(String message, String name, int position) {
		this(message, name, position, null);
	}
	
	/**
	 * Construct a new exception with specified unrecognized variable name and expression bounds.
	 * @param name The unrecognized variable name.
	 * @param position Position of the variable inside of the expression.
	 */
	public UnknownVariable(String name, int position) {
		this(null, name, position, null);
	}

	/**
	 * Get the unrecognized variable name.
	 * @return The unrecognized variable name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Position of the variable inside of the expression.
	 * @return
	 */
	public int getPosition() {
		return position;
	}

}
