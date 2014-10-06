package ohm.quickdice.control;

import ohm.quickdice.R;
import ohm.quickdice.entity.Dice;
import ohm.quickdice.entity.DiceBag;
import ohm.quickdice.entity.DiceBagCollection;
import ohm.quickdice.entity.DiceCollection;
import ohm.quickdice.entity.ModifierCollection;
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
	
	//protected ArrayList<DiceBag> diceBagList = null;
	private DiceBagCollection diceBagCollection = null;
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
		this.diceBagCollection = new DiceBagCollection(this); 
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
			//diceBagList = loadDiceBags();
			loadDiceBagCollection(diceBagCollection);
			config = PreferenceManager.getDefaultSharedPreferences(context);
			curDiceBagIndex = config.getInt(KEY_CURRENT_BAG, 0);
			isDataInitialized = true;
			isDataSaved = true;
		}
	}
	
	public void saveAll() {
		if (isDataInitialized && ! isDataSaved) {
			//saveDiceBags(diceBagList);
			saveDiceBagCollection(diceBagCollection);
			isDataSaved = true;
		}
	}
	
	public boolean isDataChanged() {
		return ! isDataSaved;
	}
	
	public void setDataChanged() {
		isDataSaved = false;
	}
	
	/**
	 * Access to the collection of all the dice bags.
	 * @return The collection of all the dice bags.
	 */
	public DiceBagCollection getDiceBagCollection() {
		return diceBagCollection;
	}

	/**
	 * Return the currently selected collection.
	 * @return
	 */
	public DiceBag getCurrent() {
		return diceBagCollection.getCurrent();
	}
	
	/**
	 * Return the index of currently selected collection.
	 * @return
	 */
	public int getCurrentIndex() {
		return curDiceBagIndex;
	}
	
	/**
	 * Set the collection at given position as the one currently selected.
	 * @param position Position of the collection to select.
	 */
	public void setCurrentIndex(int position) {
		if (position < 0) {
			curDiceBagIndex = 0;
		} else if (position >= diceBagCollection.size()) {
			curDiceBagIndex = diceBagCollection.size() - 1;
		} else {
			curDiceBagIndex = position;
		}
		SharedPreferences.Editor edit;
		edit = config.edit();
		edit.putInt(KEY_CURRENT_BAG, curDiceBagIndex);
		edit.commit();
	}
	
	/* ************* */
	/* Other methods */
	/* ************* */

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
		//bag.setDiceList(initDiceList());
		initDiceCollection(bag.getDice());
		//bag.setModifiers(initBonusBag());
		initModifierCollection(bag.getModifiers());

		return bag;
	}
	
	/**
	 * Load all the Dice Bags from the device internal memory to the specified collection.<br />
	 * If an error occur during the memory access, the default dice bag list is loaded.
	 * @param diceBags Collection of dice bags to populate.
	 * @return An ArrayList<DiceBag> representing all the dice bags.
	 */
	protected void loadDiceBagCollection(DiceBagCollection diceBags) {
		
		diceBags.clear();

		boolean loaded = persistence.loadDiceBagCollection(diceBags);
		
		if (! loaded) {
			legacyLoadDiceBagCollection(diceBags);
		}
	}
	
	
	/**
	 * Store all the Dice Bags in the device internal memory
	 * @param context Context.
	 * @param diceBags A {@link DiceBagCollection} representing all the Dice Bags.
	 */
	protected void saveDiceBagCollection(DiceBagCollection diceBags) {
		persistence.saveDiceBagCollection(diceBags);
	}
	
	public boolean exportAll(String path) {
		//return persistence.exportDiceBags(diceBagList, path);
		return persistence.exportDiceBagCollection(getDiceBagCollection(), path);
	}

	public boolean importAll(String path) {
		boolean retVal = false;
//		ArrayList<DiceBag> newDef = persistence.importDiceBags(path);
//		if (newDef != null) {
//			diceBagList = newDef;
//			isDataSaved = false;
//			setCurrentDiceBag(curDiceBagIndex);
//			retVal = true;
//		}
		DiceBagCollection newCollection = new DiceBagCollection(this);
		boolean loaded = persistence.importDiceBagCollection(newCollection, path);
		if (loaded) {
			diceBagCollection.clear(); //Help to free resources
			diceBagCollection = newCollection;
			isDataSaved = false;
			setCurrentIndex(curDiceBagIndex);
			retVal = true;
		}
		return retVal;
	}

//	/*
//	 * Initialize the dice list for the default bag.
//	 */
//	private ArrayList<Dice> initDiceList() {
//		ArrayList<Dice> retVal;
//		Dice exp;
//		Resources res;
//		String[] defaultDiceName;
//		String[] defaultDiceDesc;
//		int[] defaultDiceIcon;
//		String[] defaultDiceExpr;
//		
//		res = context.getResources();
//		defaultDiceName = res.getStringArray(R.array.default_dice_name);
//		defaultDiceDesc = res.getStringArray(R.array.default_dice_desc);
//		defaultDiceIcon = res.getIntArray(R.array.default_dice_icon);
//		defaultDiceExpr = res.getStringArray(R.array.default_dice_expr);
//
//		retVal = new ArrayList<Dice>();
//
//		for (int i = 0; i < defaultDiceExpr.length; i++) {
//			exp = new Dice();
//			exp.setID(i);
//			exp.setName(defaultDiceName.length > i ? defaultDiceName[i] : res.getString(R.string.def_die_name));
//			exp.setDescription(defaultDiceDesc.length > i ? defaultDiceDesc[i] : res.getString(R.string.def_die_desc));
//			exp.setResourceIndex(defaultDiceIcon.length > i ? defaultDiceIcon[i] : 0);
//			exp.setExpression(defaultDiceExpr[i]);
//			retVal.add(exp);
//		}
//
//		return retVal;
//	}
	
	/*
	 * Initialize the dice list for the default bag.
	 */
	private void initDiceCollection(DiceCollection collection) {
		Dice exp;
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

		collection.clear();

		for (int i = 0; i < defaultDiceExpr.length; i++) {
			exp = new Dice();
			exp.setID(i);
			exp.setName(defaultDiceName.length > i ? defaultDiceName[i] : res.getString(R.string.def_die_name));
			exp.setDescription(defaultDiceDesc.length > i ? defaultDiceDesc[i] : res.getString(R.string.def_die_desc));
			exp.setResourceIndex(defaultDiceIcon.length > i ? defaultDiceIcon[i] : 0);
			exp.setExpression(defaultDiceExpr[i]);
			collection.add(exp);
		}
	}
	
//	private ArrayList<RollModifier> initBonusBag() {
//		ArrayList<RollModifier> retVal;
//		int[] defaultBonusBag;
//		Resources res;
//		
//		res = context.getResources();
//		
//		defaultBonusBag = res.getIntArray(R.array.default_modifiers);
//		retVal = new ArrayList<RollModifier>();
//
//		for (int i = 0; i < defaultBonusBag.length; i++) {
//			retVal.add(new RollModifier(context, defaultBonusBag[i]));
//		}
//
//		return retVal;
//	}

	private void initModifierCollection(ModifierCollection collection) {
		int[] defaultBonusBag;
		Resources res;
		
		res = context.getResources();
		
		defaultBonusBag = res.getIntArray(R.array.default_modifiers);
		
		collection.clear();

		for (int i = 0; i < defaultBonusBag.length; i++) {
			collection.add(new RollModifier(context, defaultBonusBag[i]));
		}
	}

//	private ArrayList<DiceBag> initDiceBags() {
//		ArrayList<DiceBag> retVal;
//		DiceBag bag;
//		
//		retVal = new ArrayList<DiceBag>();
//		
//		bag = new DiceBag();
//		bag.setResourceIndex(GraphicManager.INDEX_DICE_ICON_DEFAULT);
//		bag.setName(context.getString(R.string.def_bag_name));
//		bag.setDescription(context.getString(R.string.def_bag_description));
//		bag.setDiceList(loadDiceBag());
//		bag.setModifiers(loadBonusBag());
//
//		retVal.add(bag);
//		
//		return retVal;
//	}
	
	/**
	 * Try to populate a {@link DiceBagCollection} from old (legacy) storage files.<br />
	 * If such attempt fail, the collection will be initialized with default data.
	 * @param collection Collection to populate.
	 */
	private void legacyLoadDiceBagCollection(DiceBagCollection collection) {
		DiceBag bag;
		
		bag = new DiceBag();
		bag.setResourceIndex(GraphicManager.INDEX_DICE_ICON_DEFAULT);
		bag.setName(context.getString(R.string.def_bag_name));
		bag.setDescription(context.getString(R.string.def_bag_description));

		legacyLoadModifierCollection(bag.getModifiers());
		legacyLoadDiceCollection(bag.getDice());

		collection.clear();
		collection.add(bag);
	}

//	/**
//	 * Load a bonus bag from the device internal memory.<br />
//	 * If an error occur during the memory access, the default bonus bag is loaded.
//	 * @param context Context.
//	 * @return An ArrayList<RollModifier> representing a dice bag.
//	 */
//	@SuppressWarnings("deprecation")
//	private ArrayList<RollModifier> loadBonusBag() {
//		ArrayList<RollModifier> retVal;
//		
//		retVal = persistence.loadBonusBag();
//		
//		if (retVal == null || retVal.size() == 0) {
//			retVal = initBonusBag();
//		}
//
//		return retVal;
//	}

	/**
	 * Load a bonus bag from the device internal memory.<br />
	 * It access to the old modifier storage file.<br />
	 * If an error occur during the memory access, the default bonus bag is loaded.
	 * @param context Context.
	 * @return An ArrayList<RollModifier> representing a dice bag.
	 */
	private void legacyLoadModifierCollection(ModifierCollection collection) {

		persistence.loadModifierCollection(collection);
		
		if (collection.size() == 0) {
			initModifierCollection(collection);
		}
	}

//	/**
//	 * Load a dice bag from the device internal memory.<br />
//	 * It access to the old dice storage file.<br />
//	 * If an error occur during the memory access, the default dice bag is loaded.
//	 * @param context Context.
//	 * @return An {@code ArrayList<Dice>} representing a dice bag.
//	 */
//	@SuppressWarnings("deprecation")
//	private ArrayList<Dice> loadDiceBag() {
//		ArrayList<Dice> retVal;
//		
//		retVal = persistence.loadDiceBag();
//		
//		if (retVal == null || retVal.size() == 0) {
//			//if (DEBUG) {Log.i(TAG, "loadDiceBag:noData");}
//			retVal = initDiceList();
//		}
//
//		return retVal;
//	}
	
	/**
	 * Load a dice bag from the device internal memory.<br />
	 * It access to the old dice storage file.<br />
	 * If an error occur during the memory access, the default dice bag is loaded.
	 * @param context Context.
	 * @return An {@code ArrayList<Dice>} representing a dice bag.
	 */
	private void legacyLoadDiceCollection(DiceCollection collection) {
		
		persistence.loadDiceCollection(collection);
		
		if (collection.size() == 0) {
			initDiceCollection(collection);
		}
	}
}
