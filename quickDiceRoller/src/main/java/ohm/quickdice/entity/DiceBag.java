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
	 * @param title the title to set
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

	/* ***************** */
	/* Methods to delete */
	/* ***************** */
//	protected ArrayList<Dice> diceList;
//	protected ArrayList<RollModifier> modifiers;
//	protected ArrayList<Variable> variables;
//
//	/**
//	 * @return the dice list
//	 */
//	@Deprecated
//	public ArrayList<Dice> getDiceList() {
//		return diceList;
//	}
//	/**
//	 * @param diceList the dice list to set
//	 */
//	@Deprecated
//	public void setDiceList(ArrayList<Dice> diceList) {
//		this.diceList = diceList;
//	}
//	/**
//	 * @return the modifiers
//	 */
//	@Deprecated
//	public ArrayList<RollModifier> getModifiers() {
//		return modifiers;
//	}
//	/**
//	 * @param modifiers the modifiers to set
//	 */
//	@Deprecated
//	public void setModifiers(ArrayList<RollModifier> modifiers) {
//		this.modifiers = modifiers;
//	}
//	/**
//	 * @return the variables
//	 */
//	@Deprecated
//	public ArrayList<Variable> getVariables() {
//		return variables;
//	}
//	/**
//	 * @param variables the variables to set
//	 */
//	@Deprecated
//	public void setVariables(ArrayList<Variable> variables) {
//		this.variables = variables;
//	}
}
