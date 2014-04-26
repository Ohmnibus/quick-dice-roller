package ohm.dexp;

import java.util.Hashtable;

public class DInstance extends EntityBase {

	/**
	 * Serial version UID used for serialization.
	 */
	private static final long serialVersionUID = 6697919721031329186L;
	
	DContext _parent;
	Hashtable<String, Integer> _values;
	
	public DInstance(DContext parent) {
		this._parent = parent;
	}

	/**
	 * Get the context associated to the expression evaluator.
	 * @return Context used.
	 */
	public DContext getContext() {
		return _parent;
	}

	/**
	 * Set or add a value for a variable.
	 * @param name Name of the variable.
	 * @param value Value of the variable.
	 */
	public void setValue(String name, int value) {
		_values.put(name, value);
	}
	
	/**
	 * Get the value of a variable.
	 * @param name Name of the variable to read.
	 * @return Value of the variable.
	 * @throws IllegalArgumentException Thrown if no variable is defined for the given name.
	 */
	public int getValue(String name) throws IllegalArgumentException {
		Integer retVal;
		retVal = _values.get(name);
		if (retVal == null) {
			DVariable var = _parent.getVariableDefinition(name);
			if (var != null) {
				retVal = var.getDefValue();
			} else {
				throw new IllegalArgumentException();
			}
		}
		return (int)retVal;
	}

}
