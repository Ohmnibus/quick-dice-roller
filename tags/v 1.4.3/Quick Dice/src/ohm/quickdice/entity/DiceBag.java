package ohm.quickdice.entity;

import java.io.Serializable;
import java.util.ArrayList;

import ohm.dexp.DExpression;

/**
 * Represent a profile defining collections of Dices and Modifiers.
 * @author Ohmnibus
 *
 */
public class DiceBag implements Serializable {

	/**
	 * Serial version UID used for serialization.
	 */
	private static final long serialVersionUID = 9108921301875089304L;

	protected int resourceIndex;
	protected String title;
	protected String description;
	protected ArrayList<DExpression> dice;
	protected ArrayList<RollModifier> modifiers;
	
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
	/**
	 * @return the dice
	 */
	public ArrayList<DExpression> getDice() {
		return dice;
	}
	/**
	 * @param dice the dice to set
	 */
	public void setDice(ArrayList<DExpression> dice) {
		this.dice = dice;
	}
	/**
	 * @return the modifiers
	 */
	public ArrayList<RollModifier> getModifiers() {
		return modifiers;
	}
	/**
	 * @param modifiers the modifiers to set
	 */
	public void setModifiers(ArrayList<RollModifier> modifiers) {
		this.modifiers = modifiers;
	}
	
//	public DiceBag clone() {
//		DiceBag retVal = new DiceBag();
//
//		retVal.resourceIndex = this.resourceIndex;
//		retVal.title = new String(this.title);
//		retVal.description = new String(this.description);
//		retVal.dice = new ArrayList<DExpression>(this.dice.size());
//		for (DExpression dexp : this.dice) {
//			retVal.dice.add(dexp.clone());
//		}
//		retVal.modifiers = new ;
//
//		return retVal;
//	}

}
