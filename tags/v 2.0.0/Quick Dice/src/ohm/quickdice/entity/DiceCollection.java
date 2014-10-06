package ohm.quickdice.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import ohm.quickdice.control.SerializationManager;

public class DiceCollection implements BaseCollection<Dice> {

	private ArrayList<Dice> diceList;
	private DiceBag owner;

	protected DiceCollection(DiceBag owner) {
		this.owner = owner;
		this.diceList = new ArrayList<Dice>();
	}

	/**
	 * Add a new {@link Dice} as the last element.<br />
	 * @param newDice {@link Dice} to add.
	 * @return Position at which the {@link Dice} is added.
	 */
	public int add(Dice newDice) {
		return add(-1, newDice);
	}
	
	/**
	 * Add a new {@link Dice} at the specified position.<br />
	 * If the position is outside the boundary of the {@link DiceBag}, the dice will be added at the end.
	 * @param position Position at which the dice will be added
	 * @param newDice {@link Dice} to add.
	 * @return Position at which the {@link Dice} is added.
	 */
	public int add(int position, Dice newDice) {
		int retVal = position;
		
		if (position >= 0 && position < diceList.size()) {
			//Add at position
			diceList.add(position, newDice);
		} else {
			//Add at the end
			diceList.add(newDice);
			retVal = diceList.size() - 1;
		}
		newDice.setParent(owner);
		setChanged();
		refreshId();

		return retVal;
	}

	/**
	 * Clone specified {@link Dice}.<br />
	 * The new cloned {@link Dice} will be added to the end of the collection.
	 * @param position Position of the source {@link Dice}.
	 */
	public void duplicate(int position) {
		try {
			Dice newDice = SerializationManager.Dice(SerializationManager.Dice(get(position)));
			add(newDice);
		} catch (IOException e) {
			//WTF?
			e.printStackTrace();
		}
	}
	
	/**
	 * Edit a specified dice ({@link Dice}).<br />
	 * This method will substitute the element at {@code position}
	 * with the given dice.<br />
	 * The behavior is pretty the same as calling {@code removeDice(position)}
	 * and then {@code addDice(position, newDiceData)}.
	 * @param position Position of the dice to substitute.
	 * @param newDice New {@code Dice}.
	 * @return A status indicating whether the change was made or not
	 */
	public boolean edit(int position, Dice newDice) {
		boolean retVal = true;
		if (position >= 0 && position < diceList.size()) {
			Dice oldDice = diceList.set(position, newDice);
			oldDice.setParent(null); //Avoid side effects
			newDice.setParent(owner);
			setChanged();
			refreshId();
		} else {
			retVal = false;
		}
		return retVal;
	}
	
	/**
	 * Remove the dice at the give position.
	 * @param position Position of the dice to remove
	 * @return The {@link Dice} removed, or {@code null} if the change was not made
	 */
	public Dice remove(int position) {
		Dice retVal = null;
		if (position >= 0 && position < diceList.size()) {
			retVal = diceList.remove(position);
			retVal.setParent(null); //Avoid side effects
			setChanged();
			refreshId();
		}
		return retVal;
	}
	
	/**
	 * Move a dice inside current {@link DiceBag}.
	 * @param fromPosition Starting position of the dice to move
	 * @param toPosition Ending position of the dice to move
	 * @return A status indicating whether the change was made or not
	 */
	public boolean move(int fromPosition, int toPosition) {
		int curDiceBagIndex = owner.getParent().getDiceBagCollection().getCurrentIndex(); /* owner.getParent().getCurrentDiceBag(); */
		return move(curDiceBagIndex, fromPosition, curDiceBagIndex, toPosition);
	}
	
	/**
	 * Move a dice, even to a different {@link DiceBag}s.
	 * @param fromDiceBagIndex Index of the starting {@link DiceBag}
	 * @param fromPosition Starting position of the dice to move
	 * @param toDiceBagIndex Index of the destination {@link DiceBag}
	 * @param toPosition Ending position of the dice to move
	 * @return A status indicating whether the change was made or not
	 */
	public boolean move(int fromDiceBagIndex, int fromPosition, int toDiceBagIndex, int toPosition) {
		boolean retVal = false;
		DiceCollection collectionFrom = null;
		DiceCollection collectionTo = null;
		
		if (fromDiceBagIndex == toDiceBagIndex) {
			if (toPosition > fromPosition) {
				toPosition--;
			}
		}
		if (fromDiceBagIndex != toDiceBagIndex || fromPosition != toPosition) {
			DiceBagCollection dBags = owner.getParent().getDiceBagCollection();
			if (fromDiceBagIndex >= 0 && fromDiceBagIndex < dBags.size()) {
				collectionFrom = dBags.get(fromDiceBagIndex).getDice();
			}
			if (toDiceBagIndex >= 0 && toDiceBagIndex < dBags.size()) {
				collectionTo = dBags.get(toDiceBagIndex).getDice();
			}
			if (collectionFrom != null && collectionTo != null) {
				if (fromPosition >= 0 && fromPosition < collectionFrom.size()
						&& toPosition >= 0 && toPosition <= collectionTo.size()) {
					
					Dice dice = collectionFrom.remove(fromPosition);
					collectionTo.add(toPosition, dice);
					
					collectionFrom.refreshId();
					if (fromDiceBagIndex != toDiceBagIndex)
						collectionTo.refreshId();
					
					setChanged();
					retVal = true;
				}
			}
		}
		return retVal;
	}
	
	public Dice get(int position) {
		return diceList.get(position);
	}
	
	public int size() {
		return diceList.size();
	}

	public void clear() {
		for (Dice dice : diceList) {
			dice.setParent(null);
		}
		diceList.clear();
	}
	
	/**
	 * Set the identifier of each dice equal to it's index in the dice bag.
	 * @param diceList
	 */
	protected void refreshId() {
		for (int i = 0; i < diceList.size(); i++) {
			diceList.get(i).setID(i);
		}
	}
	
	protected void setChanged() {
		owner.setChanged();
	}

	@Override
	public Iterator<Dice> iterator() {
		return diceList.iterator();
	}
}
