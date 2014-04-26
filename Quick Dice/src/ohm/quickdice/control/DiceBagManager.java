package ohm.quickdice.control;

import java.io.IOException;
import java.util.ArrayList;

import ohm.dexp.DExpression;
import ohm.quickdice.R;
import ohm.quickdice.entity.DiceBag;
import ohm.quickdice.entity.RollModifier;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

public class DiceBagManager {

	protected static final String KEY_CURRENT_BAG = "KEY_CURRENT_BAG";
	protected static final int UNDEFINED_INT = Integer.MIN_VALUE;
	
	protected PersistenceManager persistence;
	protected Context context;
	protected SharedPreferences config = null;
	protected ArrayList<DiceBag> diceBagList = null;
	protected int curDiceBagIndex = 0;
	protected boolean isDataSaved = true;
	protected boolean isDataInitialized = false;

	/**
	 * Initialize a new instance of {@link DiceBagManager}.<br />
	 * No more than one instance should be used.
	 * @param persistence The persistence manager to use.
	 */
	public DiceBagManager(PersistenceManager persistence) {
		this.persistence = persistence;
		this.context = this.persistence.getContext();
		this.isDataSaved = true;
		this.isDataInitialized = false;
	}

	/**
	 * Initialize all the data on the {@link DiceBagManager}.<br />
	 * Must be called prior to call any other public method
	 * otherwise said method will return an error.<br />
	 * No check are made to make the code faster.
	 */
	public void initBagManager() {
		initBagManager(false);
	}

	/**
	 * Initialize all the data on the {@link DiceBagManager}.<br />
	 * Must be called prior to call any other public method
	 * otherwise said method will return an error.<br />
	 * No check are made to make the code faster.
	 * @param force Tell if an initialization is required even if already made.
	 */
	public void initBagManager(boolean force) {
		if (! isDataInitialized) {
			diceBagList = loadDiceBags();
			config = PreferenceManager.getDefaultSharedPreferences(context);
			curDiceBagIndex = config.getInt(KEY_CURRENT_BAG, 0);
			isDataInitialized = true;
			isDataSaved = true;
		}
	}
	
	public void saveAll() {
		if (isDataInitialized && ! isDataSaved) {
			saveDiceBags(diceBagList);
			isDataSaved = true;
		}
	}
	
	public boolean isDataChanged() {
		return ! isDataSaved;
	}
	
	/*
	 * DiceBag manipulation
	 */

	/**
	 * Add given {@link DiceBag} at the end of the dice bag list.
	 * @param newBag New {@link DiceBag} to be add to the list. 
	 * @return Position at which the bag was added.
	 */
	public int addDiceBag(DiceBag newBag) {
		return addDiceBag(-1, newBag);
	}
	
	/**
	 * Add given {@link DiceBag} to the dice bag list ad the specified position.<br />
	 * If position lies outside of the possible values, the {@link DiceBag} will be added to the end.
	 * @param position Position where to add the new bag.
	 * @param newBag New {@link DiceBag} to be add to the list. 
	 * @return Position at which the bag was added.
	 */
	public int addDiceBag(int position, DiceBag newBag) {
		int retVal = position;
		if (position >= 0 && position < diceBagList.size()) {
			//Add bag at position
			diceBagList.add(position, newBag);
		} else {
			//Add bag at end
			diceBagList.add(newBag);
			retVal = diceBagList.size() - 1;
		}
		isDataSaved = false;
		return retVal;
	}

	/**
	 * Clone specified {@link DiceBag}.<br />
	 * The new cloned {@link DiceBag} will be added to the end.
	 * @param position Position of the source {@link DiceBag}.
	 */
	public void cloneDiceBag(int position) {
		try {
			DiceBag newBag = SerializationManager.DiceBag(SerializationManager.DiceBag(getDiceBag(position)));
			addDiceBag(newBag);
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
	public boolean editDiceBag(int position, DiceBag newBagData) {
		boolean retVal = true;
		if (position >= 0 && position < diceBagList.size()) {
        	DiceBag bag = diceBagList.get(position);
        	bag.setName(newBagData.getName());
        	bag.setDescription(newBagData.getDescription());
        	bag.setResourceIndex(newBagData.getResourceIndex());
    		isDataSaved = false;
		} else {
			//WTF?
			retVal = false;
		}
		return retVal;
	}

	public boolean removeDiceBag(int position) {
		boolean retVal = true;
		if (position >= 0 && position < diceBagList.size()) {
			diceBagList.remove(position);
			isDataSaved = false;
			if (position < curDiceBagIndex) {
				//Index of current selected bag is decreased by one.
				setCurrentDiceBag(curDiceBagIndex - 1);
			} else if (position == curDiceBagIndex) {
				//If current bag is deleted, select next (noop)
				if (curDiceBagIndex >= diceBagList.size()) {
					//Next doesn't exist. Select previous.
					setCurrentDiceBag(curDiceBagIndex - 1);
				}
			}
		} else {
			//WTF?
			retVal = false;
		}
		return retVal;
	}
	
	public boolean moveDiceBag(int fromPosition, int toPosition) {
		boolean retVal = true;
		
		if (fromPosition >= 0 && fromPosition < diceBagList.size()
				&& toPosition >= 0 && toPosition < diceBagList.size()) {
			
			DiceBag bag = diceBagList.remove(fromPosition);
	    	diceBagList.add(toPosition, bag);
	    	
	    	//Change current dice bag if one of the two was the current
	    	if (curDiceBagIndex == fromPosition) {
	    		setCurrentDiceBag(toPosition);
	    	} else if (curDiceBagIndex == toPosition) {
	    		setCurrentDiceBag(fromPosition);
	    	}
	    	
	    	isDataSaved = false;
		} else {
			//WTF?
			retVal = false;
		}
		
		return retVal;
	}
	
	public int getCurrentDiceBag() {
		return curDiceBagIndex;
	}
	
	public void setCurrentDiceBag(int position) {
		if (position < 0) {
			curDiceBagIndex = 0;
		} else if (position >= diceBagList.size()) {
			curDiceBagIndex = diceBagList.size() - 1;
		} else {
			curDiceBagIndex = position;
		}
		SharedPreferences.Editor edit;
		edit = config.edit();
		edit.putInt(KEY_CURRENT_BAG, curDiceBagIndex);
		edit.commit();
	}
	
	public DiceBag getDiceBag() {
		return getDiceBag(curDiceBagIndex);
	}
	
	public DiceBag getDiceBag(int position) {
		return diceBagList.get(position);
	}
	
	public ArrayList<DiceBag> getDiceBags() {
		return diceBagList;
	}
	

	/*
	 * Dice manipulation
	 */

	/**
	 * Add a new {@link DExpression} to the current {@link DiceBag} as the last element.<br />
	 * @param newDie {@link DExpression} to add.
	 * @return Position at which the {@link DExpression} is added.
	 */
	public int addDie(DExpression newDie) {
		return addDie(curDiceBagIndex, -1, newDie);
	}
	
	/**
	 * Add a new {@link DExpression} to the current {@link DiceBag} at the specified position.<br />
	 * If the position is outside the boundary of the specified {@link DiceBag}, the die will be added at the end.
	 * @param position Position at which the die will be added
	 * @param newDie {@link DExpression} to add.
	 * @return Position at which the {@link DExpression} is added.
	 */
	public int addDie(int position, DExpression newDie) {
		return addDie(curDiceBagIndex, position, newDie);
	}
	
	/**
	 * Add a new {@link DExpression} to the specified {@link DiceBag} at the specified position.<br />
	 * If the position is outside the boundary of the specified {@link DiceBag}, the die will be added at the end.
	 * @param diceBagIndex Index of the {@link DiceBag}
	 * @param position Position at which the die will be added
	 * @param newDie {@link DExpression} to add.
	 * @return Position at which the {@link DExpression} is added.
	 */
	public int addDie(int diceBagIndex, int position, DExpression newDie) {
		int retVal = position;
		
		if (diceBagIndex >= 0 && diceBagIndex < diceBagList.size()) {
			ArrayList<DExpression> dice = diceBagList.get(diceBagIndex).getDice();
			if (position >= 0 && position < dice.size()) {
				//Add at position
				dice.add(position, newDie);
			} else {
				//Add at the end
				dice.add(newDie);
				retVal = dice.size() - 1;
			}
			refreshDiceId(dice);
			isDataSaved = false;
		} else {
			//WTF?
			retVal = -1;
		}
		return retVal;
	}

	/**
	 * Clone specified {@link DExpression} from current {@link DiceBag}.<br />
	 * The new cloned {@link DExpression} will be added to the end
	 * of the same {@link DiceBag} of the source {@link DExpression}.
	 * @param position Position of the source {@link DExpression}.
	 */
	public void cloneDie(int position) {
		cloneDie(curDiceBagIndex, position);
	}

	/**
	 * Clone specified {@link DExpression}.<br />
	 * The new cloned {@link DExpression} will be added to the end
	 * of the same {@link DiceBag} of the source {@link DExpression}.
	 * @param diceBagIndex Index of the source {@link DiceBag}.
	 * @param position Position of the source {@link DExpression}.
	 */
	public void cloneDie(int diceBagIndex, int position) {
		try {
			DExpression newDie = SerializationManager.Die(SerializationManager.Die(getDie(diceBagIndex, position)));
			addDie(newDie);
		} catch (IOException e) {
			//WTF?
			e.printStackTrace();
		}
	}
	
	/**
	 * Edit a specified die ({@link DExpression}) from to the current {@link DiceBag}.<br />
	 * This method will substitute the given die to the one specified.
	 * @param position Position of the die to substitute
	 * @param newDieData New die to substitute
	 * @return A status indicating whether the change was made or not
	 */
	public boolean editDie(int position, DExpression newDieData) {
		return editDie(curDiceBagIndex, position, newDieData);
	}

	/**
	 * Edit a specified die ({@link DExpression}).<br />
	 * This method will substitute the given die to the one specified.
	 * @param diceBagIndex Index of the {@link DiceBag}
	 * @param position Position of the die to substitute
	 * @param newDieData New die to substitute
	 * @return A status indicating whether the change was made or not
	 */
	public boolean editDie(int diceBagIndex, int position, DExpression newDieData) {
		boolean retVal = true;
		ArrayList<DExpression> dice;
		
		if ((dice = checkDice(diceBagIndex)) != null) {
			if (position >= 0 && position < dice.size()) {
				dice.set(position, newDieData);
				refreshDiceId(dice);
				isDataSaved = false;
			} else {
				retVal = false;
			}
		} else {
			retVal = false;
		}
		return retVal;
	}
	
	/**
	 * Remove the die at the give position from the current {@link DiceBag}.
	 * @param diceBagIndex Index of the {@link DiceBag}
	 * @param position Position of the die to remove
	 * @return A status indicating whether the change was made or not
	 */
	public boolean removeDie(int position) {
		return removeDie(curDiceBagIndex, position);
	}
	
	/**
	 * Remove the die at the give position from the given {@link DiceBag}.
	 * @param diceBagIndex Index of the {@link DiceBag}
	 * @param position Position of the die to remove
	 * @return A status indicating whether the change was made or not
	 */
	public boolean removeDie(int diceBagIndex, int position) {
		boolean retVal = true;
		ArrayList<DExpression> dice;
		
		if ((dice = checkDice(diceBagIndex)) != null) {
			if (position >= 0 && position < dice.size()) {
				dice.remove(position);
				refreshDiceId(dice);
				isDataSaved = false;
			} else {
				retVal = false;
			}
		} else {
			retVal = false;
		}
		return retVal;
	}
	
	/**
	 * Move a dice inside current {@link DiceBag}s.
	 * @param fromPosition Starting position of the die to move
	 * @param toPosition Ending position of the die to move
	 * @return A status indicating whether the change was made or not
	 */
	public boolean moveDie(int fromPosition, int toPosition) {
		return moveDie(curDiceBagIndex, fromPosition, curDiceBagIndex, toPosition);
	}
	
	/**
	 * Move a die, even to a different {@link DiceBag}s.
	 * @param fromDiceBagIndex Index of the starting {@link DiceBag}
	 * @param fromPosition Starting position of the die to move
	 * @param toDiceBagIndex Index of the destination {@link DiceBag}
	 * @param toPosition Ending position of the die to move
	 * @return A status indicating whether the change was made or not
	 */
	public boolean moveDie(int fromDiceBagIndex, int fromPosition, int toDiceBagIndex, int toPosition) {
		boolean retVal = false;
		ArrayList<DExpression> fromDice;
		ArrayList<DExpression> toDice;
		
		if (fromDiceBagIndex == toDiceBagIndex) {
			if (toPosition > fromPosition) {
				toPosition--;
			}
		}
		if (fromDiceBagIndex != toDiceBagIndex || fromPosition != toPosition) {
			if ((fromDice = checkDice(fromDiceBagIndex)) != null
					&& (toDice = checkDice(toDiceBagIndex)) != null) {
				if (fromPosition >= 0 && fromPosition < fromDice.size()
						&& toPosition >= 0 && toPosition <= toDice.size()) {
					
					DExpression die = fromDice.remove(fromPosition);
					toDice.add(toPosition, die);
					
					refreshDiceId(fromDice);
					if (fromDiceBagIndex != toDiceBagIndex)
						refreshDiceId(toDice);
					
					isDataSaved = false;
					retVal = true;
				}
			}
		}
		return retVal;
	}
	
	public DExpression getDie(int diceBagIndex, int position) {
		return diceBagList.get(diceBagIndex).getDice().get(position);
	}

	public ArrayList<DExpression> getDice() {
		return getDice(curDiceBagIndex);
	}

	public ArrayList<DExpression> getDice(int diceBagIndex) {
		return diceBagList.get(diceBagIndex).getDice();
	}

	/**
	 * Check if a given {@link DiceBag} exists, and return it's dice list.
	 * @param diceBagIndex {@link DiceBag} to check
	 * @return A dice list id the {@link DiceBag} was found, null otherwise.
	 */
	protected ArrayList<DExpression> checkDice(int diceBagIndex) {
		ArrayList<DExpression> retVal;
		
		if (diceBagIndex >= 0 && diceBagIndex < diceBagList.size()) {
			retVal = diceBagList.get(diceBagIndex).getDice();
		} else {
			retVal = null;
		}
		return retVal;
	}

	protected void refreshDiceId(ArrayList<DExpression> dice) {
		for (int i = 0; i < dice.size(); i++) {
			dice.get(i).setID(i);
		}
	}
	

	/*
	 * Modifier manipulation
	 */
	
	public int addModifier(int position, RollModifier newModifier) {
		return addModifier(curDiceBagIndex, position, newModifier);
	}
	
	public int addModifier(int diceBagIndex, int position, RollModifier newModifier) {
		int retVal = position;
		
		if (diceBagIndex >= 0 && diceBagIndex < diceBagList.size()) {
			ArrayList<RollModifier> mods = diceBagList.get(diceBagIndex).getModifiers();
			if (position >= 0 && position < mods.size()) {
				//Add at position
				mods.add(position, newModifier);
			} else {
				//Add at the end
				mods.add(newModifier);
				retVal = mods.size() - 1;
			}
			isDataSaved = false;
		} else {
			//WTF?
			retVal = -1;
		}
		return retVal;
	}
	
//	public boolean editModifier(int diceBagIndex, int position, RollModifier newModifierData) {
//		return false;
//	}
	
	public boolean removeModifier(int position) {
		return removeModifier(curDiceBagIndex, position);
	}
	
	public boolean removeModifier(int diceBagIndex, int position) {
		boolean retVal = true;
		ArrayList<RollModifier> mods;
		
		if ((mods = checkModifier(diceBagIndex)) != null) {
			if (position >= 0 && position < mods.size()) {
				mods.remove(position);
				isDataSaved = false;
			} else {
				retVal = false;
			}
		} else {
			retVal = false;
		}
		return retVal;
	}
	
	public boolean moveModifier(int fromPosition, int toPosition) {
		return moveModifier(curDiceBagIndex, fromPosition, curDiceBagIndex, toPosition);
	}
	
	public boolean moveModifier(int fromDiceBagIndex, int fromPosition, int toDiceBagIndex, int toPosition) {
		boolean retVal = true;
		ArrayList<RollModifier> fromMods;
		ArrayList<RollModifier> toMods;
		
		if ((fromMods = checkModifier(fromDiceBagIndex)) != null
				&& (toMods = checkModifier(toDiceBagIndex)) != null) {
			if (fromPosition >= 0 && fromPosition < fromMods.size()
					&& toPosition >= 0 && toPosition < toMods.size()) {
				
				RollModifier mod = fromMods.remove(fromPosition);
				toMods.add(toPosition, mod);
				
				isDataSaved = false;
			} else {
				retVal = false;
			}
		} else {
			retVal = false;
		}
		return retVal;
	}
	
	public RollModifier getModifier(int diceBagIndex, int position) {
		return diceBagList.get(diceBagIndex).getModifiers().get(position);
	}

	public ArrayList<RollModifier> getModifiers() {
		return getModifiers(curDiceBagIndex);
	}

	public ArrayList<RollModifier> getModifiers(int diceBagIndex) {
		return diceBagList.get(diceBagIndex).getModifiers();
	}

	/**
	 * Check if a given {@link DiceBag} exists, and return it's dice list.
	 * @param diceBagIndex {@link DiceBag} to check
	 * @return A dice list id the {@link DiceBag} was found, null otherwise.
	 */
	protected ArrayList<RollModifier> checkModifier(int diceBagIndex) {
		ArrayList<RollModifier> retVal;
		
		if (diceBagIndex >= 0 && diceBagIndex < diceBagList.size()) {
			retVal = diceBagList.get(diceBagIndex).getModifiers();
		} else {
			retVal = null;
		}
		return retVal;
	}


	/**
	 * Initialize a default {@link DiceBag} with standard values.
	 * @param context Context to use to load default name and description.
	 * @return A default instance of {@link DiceBag}.
	 */
	public DiceBag getNewDiceBag() {
		DiceBag bag;
		
		bag = new DiceBag();
		bag.setResourceIndex(GraphicManager.INDEX_DICE_ICON_DEFAULT);
		bag.setName(context.getString(R.string.def_bag_name));
		bag.setDescription(context.getString(R.string.def_bag_description));
		bag.setDice(initDices());
		bag.setModifiers(initBonusBag());

		return bag;
	}
	
	/**
	 * Load all the Dice Bags from the device internal memory.<br />
	 * If an error occur during the memory access, the default dice bag list is loaded.
	 * @param context Context.
	 * @return An ArrayList<DiceBag> representing all the dice bags.
	 */
	protected ArrayList<DiceBag> loadDiceBags() {
		ArrayList<DiceBag> retVal;
		
		retVal = persistence.loadDiceBags();
		
		if (retVal == null || retVal.size() == 0) {
			retVal = initDiceBags();
		}

		return retVal;
	}
	
	/**
	 * Store all the Dice Bags in the device internal memory
	 * @param context Context.
	 * @param diceBags An ArrayList<DiceBag> representing all the Dice Bags.
	 */
	protected void saveDiceBags(ArrayList<DiceBag> diceBags) {
		persistence.saveDiceBags(diceBags);
	}
	
	/**
	 * Load a bonus bag from the device internal memory.<br />
	 * If an error occur during the memory access, the default bonus bag is loaded.
	 * @param context Context.
	 * @return An ArrayList<RollModifier> representing a dice bag.
	 */
	private ArrayList<RollModifier> loadBonusBag() {
		ArrayList<RollModifier> retVal;
		
		retVal = persistence.loadBonusBag();
		
		if (retVal == null || retVal.size() == 0) {
			retVal = initBonusBag();
		}

		return retVal;
	}
	
	/**
	 * Load a dice bag from the device internal memory.<br />
	 * If an error occur during the memory access, the default dice bag is loaded.
	 * @param context Context.
	 * @return An ArrayList<DExpression> representing a dice bag.
	 */
	private ArrayList<DExpression> loadDiceBag() {
		ArrayList<DExpression> retVal;
		
		retVal = persistence.loadDiceBag();
		
		if (retVal == null || retVal.size() == 0) {
			//if (DEBUG) {Log.i(TAG, "loadDiceBag:noData");}
			retVal = initDices();
		}

		return retVal;
	}

	public boolean exportAll(String path) {
		return persistence.exportDiceBags(diceBagList, path);
	}

	public boolean importAll(String path) {
		boolean retVal = false;
		ArrayList<DiceBag> newDef = persistence.importDiceBags(path);
		if (newDef != null) {
			diceBagList = newDef;
			isDataSaved = false;
			setCurrentDiceBag(curDiceBagIndex);
			retVal = true;
		}
		return retVal;
	}

	/*
	 * Default bags
	 */
//	private static final int RES_IDX_D4 = 1;
//	private static final int RES_IDX_D6 = 2;
//	private static final int RES_IDX_D8 = 3;
//	private static final int RES_IDX_D10 = 4;
//	private static final int RES_IDX_D12 = 5;
//	private static final int RES_IDX_D20 = 6;
//	private static final int RES_IDX_D100 = 7;
//	
//	private static final int[][] defaultBag = new int[][] {
//		{R.string.def_d4_name, R.string.def_d4_desc, RES_IDX_D4, 4},
//		{R.string.def_d6_name, R.string.def_d6_desc, RES_IDX_D6, 6},
//		{R.string.def_d8_name, R.string.def_d8_desc, RES_IDX_D8, 8},
//		{R.string.def_d10_name, R.string.def_d10_desc, RES_IDX_D10, 10},
//		{R.string.def_d12_name, R.string.def_d12_desc, RES_IDX_D12, 12},
//		{R.string.def_d20_name, R.string.def_d20_desc, RES_IDX_D20, 20},
//		{R.string.def_d100_name, R.string.def_d100_desc, RES_IDX_D100, 100}
//	};

	private ArrayList<DExpression> initDices() {
		ArrayList<DExpression> retVal;
        DExpression exp;
		Resources res;
		String[] defaultDiceName;
		String[] defaultDiceDesc;
		int[] defaultDiceIcon;
		String[] defaultDiceExpr;
		
		res = context.getResources();
		defaultDiceName = res.getStringArray(R.array.default_dice_name);
		defaultDiceDesc = res.getStringArray(R.array.default_dice_desc);
		defaultDiceIcon = res.getIntArray(R.array.default_dice_icon);
		defaultDiceExpr = res.getStringArray(R.array.default_dice_expr);

		retVal = new ArrayList<DExpression>();

		for (int i = 0; i < defaultDiceExpr.length; i++) {
	        exp = new DExpression();
	        exp.setID(i);
        	exp.setName(defaultDiceName.length > i ? defaultDiceName[i] : res.getString(R.string.def_die_name));
        	exp.setDescription(defaultDiceDesc.length > i ? defaultDiceDesc[i] : res.getString(R.string.def_die_desc));
        	exp.setResourceIndex(defaultDiceIcon.length > i ? defaultDiceIcon[i] : 0);
	        exp.setExpression(defaultDiceExpr[i]);
	        retVal.add(exp);
		}

		return retVal;
	}
	
	private ArrayList<RollModifier> initBonusBag() {
		ArrayList<RollModifier> retVal;
		int[] defaultBonusBag;
		Resources res;
		
		res = context.getResources();
		
		defaultBonusBag = res.getIntArray(R.array.default_modifiers);
		retVal = new ArrayList<RollModifier>();

		for (int i = 0; i < defaultBonusBag.length; i++) {
			retVal.add(new RollModifier(context, defaultBonusBag[i]));
		}

		return retVal;
	}

	private ArrayList<DiceBag> initDiceBags() {
		ArrayList<DiceBag> retVal;
		DiceBag bag;
		
		retVal = new ArrayList<DiceBag>();
		
		bag = new DiceBag();
		bag.setResourceIndex(GraphicManager.INDEX_DICE_ICON_DEFAULT);
		bag.setName(context.getString(R.string.def_bag_name));
		bag.setDescription(context.getString(R.string.def_bag_description));
		bag.setDice(loadDiceBag());
		bag.setModifiers(loadBonusBag());

		retVal.add(bag);
		
		return retVal;
	}
}
