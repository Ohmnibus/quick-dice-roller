package ohm.quickdice.entity;

import java.util.ArrayList;
import java.util.Locale;

import ohm.dexp.exception.DException;

public class Variable {

	private int id;
	private String name;
	private String description;
	private String label;
	private String privateLabel = "";
	private int resIdx;
	private int minVal;
	private int maxVal;
	private int curVal;
	protected DiceBag parent = null;

	public Variable() {
		super();
	}

	/**
	 * Set an unique identifier for this variable.
	 * @param id Unique identifier.
	 */
	public void setID(int id) {
		this.id = id;
	}

	/**
	 * Get the unique identifier of this variable.
	 * @return Unique identifier.
	 */
	public int getID() {
		return id;
	}

	/**
	 * Set the name of this variable.
	 * @param name Name of the variable.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get the name of this variable.
	 * @return Name of the variable.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the description of this variable.
	 * @param description Description of this variable.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Get the description of this variable.
	 * @return Description of this variable.
	 */
	public String getDescription() {
		return this.description;
	}
	
	/**
	 * Set the index of the graphic resource to be used for this variable.<br />
	 * This is NOT a reference to a resource.
	 * @param resourceIndex Resource index.
	 */
	public void setResourceIndex(int resourceIndex) {
		this.resIdx = resourceIndex;
	}

	/**
	 * Get the index of the resource to be used for this variable.<br />
	 * This is NOT a reference to a resource.
	 * @return Resource index.
	 */
	public int getResourceIndex() {
		return this.resIdx;
	}

	/**
	 * Set the label of this variable.<br />
	 * The label will be used in expressions.
	 * @param label Label of this variable.
	 */
	public void setLabel(String label) {
		this.label = label;
		this.privateLabel = label == null ? "" : label.toLowerCase(Locale.getDefault());
	}

	/**
	 * Get the label of this variable.<br />
	 * The label will be used in expressions.<br />
	 * Use {@link #getCleanLabel()} in place of this method
	 * in comparisons.
	 * @return Label of this variable.
	 */
	public String getLabel() {
		return this.label;
	}
	
	/**
	 * Get a "clean" version of the label<br />
	 * This method should be used in place of {@link #getLabel()}
	 * to check the variable's label.
	 * @return Label of this variable.
	 */
	protected String getCleanLabel() {
		return this.privateLabel;
	}
	
	/**
	 * Tell if specified label match to the one defined for this instance.
	 * @param labelToCompare Label to check.
	 * @return {@code true} if given label is equal to the one defined for this instance.
	 */
	public boolean matchLabel(String labelToCompare) {
		return privateLabel.equalsIgnoreCase(labelToCompare);
	}

	/**
	 * Set the minimum allowed value for this variable.
	 * @param minVal Minimum allowed value
	 */
	public void setMinVal(int minVal) {
		this.minVal = minVal;
	}

	/**
	 * Get the minimum allowed value for this variable.
	 * @return Minimum allowed value
	 */
	public int getMinVal() {
		return minVal;
	}

	/**
	 * Set the maximum allowed value for this variable.
	 * @param maxVal Maximum allowed value
	 */
	public void setMaxVal(int maxVal) {
		this.maxVal = maxVal;
	}

	/**
	 * Get the maximum allowed value for this variable.
	 * @return Maximum allowed value
	 */
	public int getMaxVal() {
		return maxVal;
	}

	/**
	 * Set the current value of this variable.
	 * @param curVal Current value.
	 */
	public void setCurVal(int curVal) {
		if (curVal != this.curVal) {
			this.curVal = curVal;
			if (parent != null) {
				//parent.getVariables().changedContent();
				parent.getVariables().setValueChanged();
			}
		}
	}
	
	/**
	 * Get the current value of this variable.
	 * @return Current value.
	 */
	public int getCurVal() {
		return curVal;
	}

	public DiceBag getParent() {
		return parent;
	}

	protected void setParent(DiceBag parent) {
		this.parent = parent;
	}
	
	/**
	 * Array of dice that require this variable.
	 * @return Array of dice that require this variable.
	 */
	public Dice[] requiredBy() {
		Dice[] retVal = new Dice[0];
		if (parent != null) {
			ArrayList<Dice> req = new ArrayList<Dice>();
			for (Dice dice : parent.getDice()) {
				try {
					String[] labels = dice.getRequiredVariables();
					for (String label : labels) {
						if (matchLabel(label)) {
							req.add(dice);
							break;
						}
					}
				} catch (DException e) {
					//Somehow the dice expression is invalid.
					//Cannot take it in account.
					e.printStackTrace();
				}
			}
			if (req.size() > 0) {
				retVal = req.toArray(retVal);
			}
		}
		return retVal;
	}
}
