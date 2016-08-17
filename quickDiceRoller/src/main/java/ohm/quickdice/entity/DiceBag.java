package ohm.quickdice.entity;

import ohm.quickdice.control.DiceBagManager;

/**
 * Represent a profile defining collections of Dices and Modifiers.
 * @author Ohmnibus
 *
 */
public class DiceBag {

	private int resourceIndex;
	private String title;
	private String description;
	private DiceBagManager parent = null;
	private DiceCollection diceCollection;
	private ModifierCollection modifierCollection;
	private VariableCollection variableCollection;
	
	public DiceBag() {
		diceCollection = new DiceCollection(this);
		modifierCollection = new ModifierCollection(this);
		variableCollection = new VariableCollection(this);
	}
	
	/**
	 * @return the resourceIndex
	 */
	public int getResourceIndex() {
		return resourceIndex;
	}
	/**
	 * @param resourceIndex the resourceIndex to set
	 */
	public void setResourceIndex(int resourceIndex) {
		this.resourceIndex = resourceIndex;
	}
	/**
	 * @return the title
	 */
	public String getName() {
		return title;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.title = name;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	public DiceCollection getDice() {
		return diceCollection;
	}
	
	public ModifierCollection getModifiers() {
		return modifierCollection;
	}
	
	public VariableCollection getVariables() {
		return variableCollection;
	}

	public DiceBagManager getParent() {
		return parent;
	}

	protected void setParent(DiceBagManager parent) {
		this.parent = parent;
	}
	
	public boolean isChanged() {
		return parent == null ? false : parent.isDataChanged();
	}

	protected void setChanged() {
		if (parent != null) {
			parent.setDataChanged();
		}
	}
}
