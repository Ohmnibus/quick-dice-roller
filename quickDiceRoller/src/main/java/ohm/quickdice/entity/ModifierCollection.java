package ohm.quickdice.entity;

import java.util.ArrayList;
import java.util.Iterator;

public class ModifierCollection implements Iterable<RollModifier> {

	protected ArrayList<RollModifier> modifierList;
	private DiceBag owner;

	protected ModifierCollection(DiceBag owner) {
		this.owner = owner;
		this.modifierList = new ArrayList<RollModifier>();
	}

	/**
	 * Add a new {@link RollModifier} as the last element.<br />
	 * @param newModifier {@link RollModifier} to add.
	 * @return Position at which the {@link RollModifier} is added.
	 */
	public int add(RollModifier newModifier) {
		return add(-1, newModifier);
	}

	/**
	 * Add a new {@link RollModifier} at the specified position.<br />
	 * If the position is outside the boundary of the {@link DiceBag}, the dice will be added at the end.
	 * @param position Position at which the dice will be added
	 * @param newModifier {@link RollModifier} to add.
	 * @return Position at which the {@link RollModifier} is added.
	 */
	public int add(int position, RollModifier newModifier) {
		int retVal = position;
		
		if (position >= 0 && position < modifierList.size()) {
			//Add at position
			modifierList.add(position, newModifier);
		} else {
			//Add at the end
			modifierList.add(newModifier);
			retVal = modifierList.size() - 1;
		}
		newModifier.setParent(owner);
		setChanged();

		return retVal;
	}
	

	/**
	 * Remove the modifier at the give position.
	 * @param position Position of the modifier to remove
	 * @return The {@link RollModifier} removed, or {@code null} if the change was not made
	 */
	public RollModifier remove(int position) {
		RollModifier retVal = null;
		
		if (position >= 0 && position < modifierList.size()) {
			retVal = modifierList.remove(position);
			retVal.setParent(null); //Avoid side effects
			setChanged();
		}

		return retVal;
	}
	
	/**
	 * Move a modifier inside current {@link DiceBag}.
	 * @param fromPosition Starting position of the modifier to move
	 * @param toPosition Ending position of the modifier to move
	 * @return A status indicating whether the change was made or not
	 */
	public boolean move(int fromPosition, int toPosition) {
		int curDiceBagIndex = owner.getParent().getDiceBagCollection().getCurrentIndex(); /* owner.getParent().getCurrentDiceBag(); */
		return move(curDiceBagIndex, fromPosition, curDiceBagIndex, toPosition);
	}
	
	/**
	 * Move a modifier, even to a different {@link DiceBag}s.
	 * @param fromDiceBagIndex Index of the starting {@link DiceBag}
	 * @param fromPosition Starting position of the modifier to move
	 * @param toDiceBagIndex Index of the destination {@link DiceBag}
	 * @param toPosition Ending position of the modifier to move
	 * @return A status indicating whether the change was made or not
	 */
	public boolean move(int fromDiceBagIndex, int fromPosition, int toDiceBagIndex, int toPosition) {
		boolean retVal = false;
		ModifierCollection collectionFrom = null;
		ModifierCollection collectionTo = null;
		
		if (fromDiceBagIndex == toDiceBagIndex) {
			if (toPosition > fromPosition) {
				toPosition--;
			}
		}
		if (fromDiceBagIndex != toDiceBagIndex || fromPosition != toPosition) {
			DiceBagCollection dBags = owner.getParent().getDiceBagCollection();
			if (fromDiceBagIndex >= 0 && fromDiceBagIndex < dBags.size()) {
				collectionFrom = dBags.get(fromDiceBagIndex).getModifiers();
			}
			if (toDiceBagIndex >= 0 && toDiceBagIndex < dBags.size()) {
				collectionTo = dBags.get(toDiceBagIndex).getModifiers();
			}
			if (collectionFrom != null && collectionTo != null) {
				if (fromPosition >= 0 && fromPosition < collectionFrom.size()
						&& toPosition >= 0 && toPosition <= collectionTo.size()) {
					
					RollModifier dice = collectionFrom.remove(fromPosition);
					collectionTo.add(toPosition, dice);
					
					setChanged();
					retVal = true;
				}
			}
		}

		return retVal;
	}
	
	public RollModifier get(int position) {
		return modifierList.get(position);
	}

	public int size() {
		return modifierList.size();
	}
	
	public void clear() {
		for (RollModifier mod : modifierList) {
			mod.setParent(null);
		}
		modifierList.clear();
	}

	protected void setChanged() {
		owner.setChanged();
	}

	@Override
	public Iterator<RollModifier> iterator() {
		return modifierList.iterator();
	}
}
