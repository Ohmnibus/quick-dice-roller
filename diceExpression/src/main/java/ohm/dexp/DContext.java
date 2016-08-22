package ohm.dexp;

import java.util.Hashtable;

/**
 * Defines a context that can be used to evaluate a {@link DExpression}.<br />
 * The context contains a set of names and their values. Such names represents
 * variables of {@link DExpression}s.
 * @author Ohmnibus
 *
 */
public class DContext {

	/**
	 * Represent the values of a variable.
	 * @author Ohmnibus
	 *
	 */
	public static class DVariable {
		public long minVal;
		public long maxVal;
		public long curVal;

		public DVariable() {
			this(0, 0, 0);
		}

		/**
		 * Copy constructor.
		 * @param dVariable Instance to copy from.
		 */
		public DVariable(DVariable dVariable) {
			this(dVariable.minVal, dVariable.maxVal, dVariable.curVal);
		}
		
		public DVariable(long minVal, long maxVal, long curVal) {
			this.minVal = minVal;
			this.maxVal = maxVal;
			this.curVal = curVal;
		}
		
		public boolean equals(DVariable dVariable) {
			return dVariable != null
					&& dVariable.minVal == this.minVal
					&& dVariable.maxVal == this.maxVal
					&& dVariable.curVal == this.curVal;
		}
		
		public boolean equals(long minVal, long maxVal, long curVal) {
			return minVal == this.minVal
					&& maxVal == this.maxVal
					&& curVal == this.curVal;
		}
	}
	
	boolean _changed;
	Hashtable<String, DVariable> _values;
	
	public DContext() {
		_changed = false;
		_values = new Hashtable<String, DVariable>();
	}

	/**
	 * Set or add a variable.
	 * @param name Name of the variable.
	 * @param minVal Minimum allowed value for the variable.
	 * @param maxVal Maximum allowed value for the variable.
	 * @param curVal Actual value of the variable.
	 */
	public void setValue(String name, long minVal, long maxVal, long curVal) {
		name = name.toLowerCase();
		DVariable original = getVariableInternal(name);
		if (original == null || ! original.equals(minVal, maxVal, curVal)) {
			_values.put(name, new DVariable(minVal, maxVal, curVal));
			_changed = true;
		}
	}
	
	/**
	 * Get the minimum value of a variable.
	 * @param name Name of the variable to read.
	 * @return Min. value of the variable.
	 * @throws IllegalArgumentException Thrown if the variable {@code name} is not defined.
	 */
	public long getMinValue(String name) throws IllegalArgumentException {
		return getVariable(name).minVal;
	}
	
	/**
	 * Get the maximum value of a variable.
	 * @param name Name of the variable to read.
	 * @return Max. value of the variable.
	 * @throws IllegalArgumentException Thrown if the variable {@code name} is not defined.
	 */
	public long getMaxValue(String name) throws IllegalArgumentException {
		return getVariable(name).maxVal;
	}
	
	/**
	 * Get the actual value of a variable.
	 * @param name Name of the variable to read.
	 * @return Value of the variable.
	 * @throws IllegalArgumentException Thrown if the variable {@code name} is not defined.
	 */
	public long getValue(String name) throws IllegalArgumentException {
		return getVariable(name).curVal;
	}
	
	/**
	 * Get the values of a variable.
	 * @param name Name of the variable to read.
	 * @return Values of the variable.
	 * @throws IllegalArgumentException Thrown if the variable {@code name} is not defined.
	 */
	public DVariable getVariable(String name) throws IllegalArgumentException {
		DVariable retVal;
		retVal = getVariableInternal(name);
		if (retVal == null) {
			throw new IllegalArgumentException();
		}
		return retVal;
	}

	/**
	 * Get the values of a variable.
	 * @param name Name of the variable to read.
	 * @return Values of the variable.
	 * @throws IllegalArgumentException Thrown if the variable {@code name} is not defined.
	 */
	private DVariable getVariableInternal(String name) {
		return _values.get(name.toLowerCase());
	}
	
	/**
	 * Tell if given name is defined for this context.
	 * @param name Name of the variable to check.
	 * @return {@code true} id the variable is defined, {@code false} otherwise.
	 */
	public boolean checkName(String name) {
		return _values.containsKey(name.toLowerCase());
	}
	
	/**
	 * Tell if data contained on this instance are changed since last reset.
	 * @return
	 */
	public boolean isChanged() {
		return _changed;
	}
	
	protected void reset() {
		_changed = false;
	}
}
