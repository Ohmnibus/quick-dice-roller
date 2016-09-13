package ohm.quickdice.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import ohm.quickdice.control.DiceBagManager;
import ohm.quickdice.control.SerializationManager;

/**
 * This class represent a collection of {@link DiceBag}, corresponding to all
 * the data needed by the main application.<br />
 * @author Ohmnibus
 *
 */
public class DiceBagCollection implements BaseCollection<DiceBag> {

	private ArrayList<DiceBag> diceBagList = new ArrayList<DiceBag>();
	private DiceBagManager owner;

	/**
	 * Initialize a new instance of {@link DiceBagCollection}.<br />
	 * No more than one instance should be used.
	 */
	public DiceBagCollection(DiceBagManager owner) {
		this.owner = owner;
		diceBagList = new ArrayList<DiceBag>();
	}

	/**
	 * Add given {@link DiceBag} at the end of the dice bag list.
	 * @param newBag New {@link DiceBag} to be add to the list. 
	 * @return Position at which the bag was added.
	 */
	public int add(DiceBag newBag) {
		return add(-1, newBag);
	}
	
	/**
	 * Add given {@link DiceBag} to the dice bag list ad the specified position.<br />
	 * If position lies outside of the possible values, the {@link DiceBag} will be added to the end.
	 * @param position Position where to add the new bag.
	 * @param newBag New {@link DiceBag} to be add to the list. 
	 * @return Position at which the bag was added.
	 */
	public int add(int position, DiceBag newBag) {
		int retVal = position;
		if (position >= 0 && position < diceBagList.size()) {
			//Add bag at position
			diceBagList.add(position, newBag);
		} else {
			//Add bag at end
			diceBagList.add(newBag);
			retVal = diceBagList.size() - 1;
		}
		newBag.setParent(owner);
		setChanged();
		return retVal;
	}

	/**
	 * Clone specified {@link DiceBag}.<br />
	 * The new cloned {@link DiceBag} will be added to the end.
	 * @param position Position of the source {@link DiceBag}.
	 */
	public void duplicate(int position) {
		try {
			DiceBag newBag = SerializationManager.DiceBag(SerializationManager.DiceBag(get(position)));
			add(newBag);
		} catch (IOException e) {
			//WTF?
			e.printStackTrace();
		}
	}
	
	/**
	 * Edit specified {@link DiceBag} with provided data.<br />
	 * Only bag information are affected, not inner collections.
	 * @param position
	 * @param newBagData
	 * @return true if data changes where possible.
	 */
	public boolean edit(int position, DiceBag newBagData) {
		boolean retVal = true;
		if (position >= 0 && position < diceBagList.size()) {
			DiceBag bag = diceBagList.get(position);
			bag.setName(newBagData.getName());
			bag.setDescription(newBagData.getDescription());
			bag.setResourceIndex(newBagData.getResourceIndex());
			setChanged();
		} else {
			//WTF?
			retVal = false;
		}
		return retVal;
	}

	/**
	 * Remove collection at given position.
	 * @param position Position of collection to remove.
	 * @return {@code true} if collection was removed, {@code false} otherwise.
	 */
	public DiceBag remove(int position) {
		DiceBag retVal = null;
		if (position >= 0 && position < diceBagList.size()) {
			
			int curDiceBagIndex = getCurrentIndex();
			retVal = diceBagList.remove(position);
			retVal.setParent(null); //Avoid side effects
			setChanged();
			
			if (position < curDiceBagIndex) {
				//Index of current selected bag is decreased by one.
				setCurrentIndex(curDiceBagIndex - 1);
			} else if (position == curDiceBagIndex) {
				//If current bag is deleted, select next (noop)
				if (curDiceBagIndex >= diceBagList.size()) {
					//Next doesn't exist. Select previous.
					setCurrentIndex(curDiceBagIndex - 1);
				}
			}
		} else {
			//WTF?
			retVal = null;
		}
		return retVal;
	}
	
	/**
	 * Swap two collections.
	 * @param fromPosition Position of the first collection to move.
	 * @param toPosition Position of the second collection to move.
	 * @return {@code true} if operation has completed, {@code false} 
	 * if the switch wasn't possible.
	 */
	public boolean move(int fromPosition, int toPosition) {
		boolean retVal = true;
		
		if (fromPosition >= 0 && fromPosition < diceBagList.size()
				&& toPosition >= 0 && toPosition < diceBagList.size()) {
			
			int curDiceBagIndex = getCurrentIndex();
			DiceBag bag = diceBagList.remove(fromPosition);
			diceBagList.add(toPosition, bag);

			//Change current dice bag if one of the two was the current
			if (curDiceBagIndex == fromPosition) {
				setCurrentIndex(toPosition);
			} else if (curDiceBagIndex == toPosition) {
				setCurrentIndex(fromPosition);
			}

			setChanged();
		} else {
			//WTF?
			retVal = false;
		}
		
		return retVal;
	}
	
	/**
	 * Get a reference to the {@link DiceBagManager} containing this instance.
	 * @return
	 */
	public DiceBagManager getManager() {
		return owner;
	}
	
	/**
	 * Return the index of currently selected collection.
	 * @return
	 */
	public int getCurrentIndex() {
		int position = owner.getCurrentIndex();
		if (position < 0) {
			position = 0;
		} else if (position >= size()) {
			position = size() - 1;
		}
		return position;
	}
	
	/**
	 * Set the collection at given position as the one currently selected.
	 * @param position Position of the collection to select.
	 */
	public void setCurrentIndex(int position) {
		owner.setCurrentIndex(position);
	}
	
	/**
	 * Return the currently selected collection.
	 * @return Currently selected {@link DiceBag}.
	 */
	public DiceBag getCurrent() {
		return get(getCurrentIndex());
	}
	
	/**
	 * Return specified collection.
	 * @param position Position of the collection in the list (zero based).
	 * @return
	 */
	public DiceBag get(int position) {
		return diceBagList.get(position);
	}
	
	@Override
	public int indexOf(DiceBag item) {
		return diceBagList.indexOf(item);
	}
	
	@Override
	public int size() {
		return diceBagList.size();
	}
	
	public void clear() {
		for (DiceBag dBag : diceBagList) {
			dBag.getDice().clear();
			dBag.getModifiers().clear();
			dBag.getVariables().clear();
			dBag.setParent(null);
		}
		diceBagList.clear();
	}
	
	public boolean isChanged() {
		return owner.isDataChanged();
	}
	
	protected void setChanged() {
		owner.setDataChanged();
	}

	@Override
	public Iterator<DiceBag> iterator() {
		return diceBagList.iterator();
	}
	
}
