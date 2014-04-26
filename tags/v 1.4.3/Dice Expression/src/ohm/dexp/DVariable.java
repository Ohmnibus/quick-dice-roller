package ohm.dexp;

public class DVariable extends EntityBase {
	
	/**
	 * Serial version UID used for serialization.
	 */
	private static final long serialVersionUID = 3366177497439222806L;
	
	String _label;
	private int defValue;
	private int maxValue;
	private int minValue;
	
	/**
	 * Set the label identifying the variable.
	 * @param label Label of the variable.
	 */
	public void setLabel(String label) {
		_label = label;
	}

	/**
	 * Get the label identifying the variable.
	 * @return Label identifying the variable.
	 */
	public String getLabel() {
		return _label;
	}

	/**
	 * Instantiate a new {@link VariableDefinition} from the given parameters.
	 * @param def Default value for new {@link DInstance}s variable.
	 * @param max Maximum value for the variable.
	 * @param min Minimum value for the variable.
	 */
	public DVariable(String label, int def, int max, int min) {
		_label = label;
		defValue = def;
		maxValue = max;
		minValue = min;
	}
	
	/**
	 * Set the default value for the variable.
	 * @param defValue the default value to set
	 */
	public void setDefValue(int defValue) {
		this.defValue = defValue;
	}
	/**
	 * Get the default value for the variable.
	 * @return the default value
	 */
	public int getDefValue() {
		return defValue;
	}
	/**
	 * Set the maximum value for the variable.
	 * @param maxValue the maximum value to set
	 */
	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
	}
	/**
	 * Get the maximum value for the variable.
	 * @return the maximum value
	 */
	public int getMaxValue() {
		return maxValue;
	}
	/**
	 * Set the minimum value for the variable.
	 * @param minValue the minimum value to set
	 */
	public void setMinValue(int minValue) {
		this.minValue = minValue;
	}
	/**
	 * Get the minimum value for the variable.
	 * @return the minimum value
	 */
	public int getMinValue() {
		return minValue;
	}
}
