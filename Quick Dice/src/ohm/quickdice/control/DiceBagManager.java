package ohm.quickdice.control;

import ohm.quickdice.R;
import ohm.quickdice.entity.Dice;
import ohm.quickdice.entity.DiceBag;
import ohm.quickdice.entity.DiceBagCollection;
import ohm.quickdice.entity.DiceCollection;
import ohm.quickdice.entity.IconCollection;
import ohm.quickdice.entity.ModifierCollection;
import ohm.quickdice.entity.RollModifier;
import ohm.quickdice.entity.Variable;
import ohm.quickdice.entity.Icon.CustomIcon;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.ImageView;
import android.widget.Toast;

public class DiceBagManager {

	protected static final String KEY_CURRENT_BAG = "KEY_CURRENT_BAG";
	protected static final int UNDEFINED_INT = Integer.MIN_VALUE;
	
	protected PersistenceManager persistence;
	protected Context context;
	protected SharedPreferences config = null;
	
	//protected ArrayList<DiceBag> diceBagList = null;
	private DiceBagCollection diceBagCollection = null;
	private IconCollection iconCollection = null;
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
		this.iconCollection= new IconCollection(this, this.context);
		this.isDataSaved = true;
		this.isDataInitialized = false;
	}

	/**
	 * Initialize all the data on the {@link DiceBagManager}.<br />
	 * Must be called prior to call any other public method
	 * otherwise said method will return an error.<br />
	 * No check are made to make the code faster.
	 */
	public void init() {
		init(false);
	}

	/**
	 * Initialize all the data on the {@link DiceBagManager}.<br />
	 * Must be called prior to call any other public method
	 * otherwise said method will return an error.<br />
	 * No check are made to make the code faster.
	 * @param force Tell if an initialization is required even if already made.
	 */
	public void init(boolean force) {
		if (! isDataInitialized || force) {
			readDiceBagManager();
			config = PreferenceManager.getDefaultSharedPreferences(context);
			curDiceBagIndex = config.getInt(KEY_CURRENT_BAG, 0);
			isDataInitialized = true;
			isDataSaved = true;
		}
	}
	
	public void saveAll() {
		if (isDataInitialized && ! isDataSaved) {
			saveDiceBagManager();
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
	
	/* ************ */
	/* Icon methods */
	/* ************ */
	
	/**
	 * Access to the collection of all the icon resources.
	 * @return The collection of all the icon resources.
	 */
	public IconCollection getIconCollection() {
		return iconCollection;
	}
	
	/**
	 * Asynchronously load an icon to the ImageView.
	 * @param imageView Target view.
	 * @param iconId Identifier of the icon.
	 */
	public void setIconDrawable(ImageView imageView, int iconId) {
		//return iconCollection.getDrawable(context, iconId);
		iconCollection.loadDrawable(imageView, iconId);
	}

	/**
	 * Convenience method to get the {@link Drawable} of the icon with the given ID resized to the specified size.<br />
	 * @param iconId Identifier of the icon.
	 * @param width Desired width in {@code dp}.
	 * @param height Desired height in {@code dp}.
	 * @return Resized {@link Drawable} of the icon.
	 */
	public Drawable getIconDrawable(int iconId, int width, int height) {
		return iconCollection.getDrawable(context, iconId, width, height);
	}

	/**
	 * Convenience method to get the mask of the icon with the given ID.<br />
	 * The color of the mask is the one assigned to the icon.
	 * @param ctx Context.
	 * @param iconId Identifier of the icon.
	 * @return {@link Drawable} representing the mask of the icon.
	 */
	public Drawable getIconMask(int iconId) {
		return iconCollection.getMask(context, iconId);
	}

	/**
	 * Get the number of reference for an icon.<br />
	 * This method return a 4 element array:
	 * <ul>
	 * <li>Element at index 0 represents the total number of elements that uses this icon.</li>
	 * <li>Element at index 1 represents the number of dice bags that uses this icon.</li>
	 * <li>Element at index 2 represents the number of dice that uses this icon.</li>
	 * <li>Element at index 3 represents the number of variables that uses this icon.</li>
	 * </ul>
	 * @param iconId Icon Id.
	 * @return A 4 element array. See details.
	 */
	public int[] getIconInstances(int iconId) {
		return getIconInstances(iconId, false);
	}
	
	/**
	 * Remove all the references of an icon.<br />
	 * This method is meant to be used upon and icon deletion.
	 * @param iconId Identifier of the icon to remove.
	 */
	public void resetIconInstances(int iconId) {
		getIconInstances(iconId, true);
		setDataChanged();
	}
	
	/**
	 * Get the number of reference for an icon and optionally remove them.<br />
	 * This method return a 4 element array:
	 * <ul>
	 * <li>Element at index 0 represents the total number of elements that uses this icon.</li>
	 * <li>Element at index 1 represents the number of dice bags that uses this icon.</li>
	 * <li>Element at index 2 represents the number of dice that uses this icon.</li>
	 * <li>Element at index 3 represents the number of variables that uses this icon.</li>
	 * </ul>
	 * @param iconId Icon Id.
	 * @param reset {@code true} to remove all references to this icon.
	 * @return A 4 element array. See details.
	 */
	private int[] getIconInstances(int iconId, boolean reset) {
		int[] retVal = new int[4];
		retVal[0] = 0;
		retVal[1] = 0;
		for (DiceBag diceBag : diceBagCollection) {
			if (diceBag.getResourceIndex() == iconId) {
				retVal[0]++;
				retVal[1]++;
				if (reset) {
					diceBag.setResourceIndex(IconCollection.ID_ICON_DEFAULT);
				}
			}
			retVal[2] = 0;
			for (Dice dice : diceBag.getDice()) {
				if (dice.getResourceIndex() == iconId) {
					retVal[0]++;
					retVal[2]++;
					if (reset) {
						dice.setResourceIndex(IconCollection.ID_ICON_DEFAULT);
					}
				}
			}
			retVal[3] = 0;
			for (Variable variable : diceBag.getVariables()) {
				if (variable.getResourceIndex() == iconId) {
					retVal[0]++;
					retVal[3]++;
					if (reset) {
						variable.setResourceIndex(IconCollection.ID_ICON_DEFAULT);
					}
				}
			}
		}
		return retVal;
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
		bag.setResourceIndex(IconCollection.ID_ICON_DEFAULT);
		bag.setName(context.getString(R.string.def_bag_name));
		bag.setDescription(context.getString(R.string.def_bag_description));
		//bag.setDiceList(initDiceList());
		initDiceCollection(bag.getDice());
		//bag.setModifiers(initBonusBag());
		initModifierCollection(bag.getModifiers());

		return bag;
	}
	
	/**
	 * Load the Dice Bag Manager from the device internal memory to the specified collection.<br />
	 * If an error occur during the memory access, the default dice bag list is loaded.
	 */
	protected void readDiceBagManager() {
		
//		boolean loaded = persistence.loadDiceBagManager(this);
//		
//		if (! loaded) {
//			legacyLoadDiceBagCollection(diceBagCollection);
//		}
		int error = persistence.readDiceBagManager(this, persistence.getSystemArchiveUri());
		
		if (error != PersistenceManager.ERR_NONE) {
			//Show error message (unless the error is File not found, which will happen on first startup).
			if (error != PersistenceManager.ERR_FILE_NOT_FOUND) {
				showErrorMessage(context, R.string.err_cannot_read);
			}
			//Load definition using legacy files
			legacyLoadDiceBagCollection(diceBagCollection);
		}
		
		CustomIcon.removeTempIconFiles(context);
	}
	
	
	/**
	 * Store the Dice Bag Manager in the device internal memory
	 */
	protected void saveDiceBagManager() {
		persistence.writeDiceBagManager(this,
				persistence.getSystemArchiveUri(),
				R.string.err_cannot_update);
	}
	
//	/**
//	 * 
//	 * @param file
//	 * @return
//	 * @deprecated use {@code exportAll(Uri)}
//	 */
//	public boolean exportAll(java.io.File file) {
//		return persistence.exportDiceBagManager(this, file);
//	}
//
//	/**
//	 * 
//	 * @param path
//	 * @return
//	 * @deprecated use {@code exportAll(Uri)}
//	 */
//	public boolean exportAll(String path) {
//		return persistence.exportDiceBagManager(this, path);
//	}
//
//	/**
//	 * 
//	 * @param path
//	 * @return
//	 * @deprecated use {@code importAll(Uri)}
//	 */
//	public boolean importAll(String path) {
//		return importAll(Uri.fromFile(new File(path)));
//	}

	public boolean exportAll(Uri resourceUri) {
		return persistence.writeDiceBagManager(this,
				resourceUri,
				R.string.err_cannot_export) == PersistenceManager.ERR_NONE;
	}
	
	public boolean importAll(Uri resourceUri) {
		boolean retVal = false;
		
		CustomIcon.backupIconFiles(context);
		
		DiceBagManager newManager = new DiceBagManager(persistence);
		//boolean loaded = persistence.importDiceBagManager(newManager, resourceUri);
		int error = persistence.readDiceBagManager(newManager, resourceUri, R.string.err_cannot_import);
		//if (loaded) {
		if (error == PersistenceManager.ERR_NONE) {
			diceBagCollection.clear(); //Help to free resources
			for (DiceBag diceBag : newManager.getDiceBagCollection()) {
				diceBagCollection.add(diceBag);
			}
			iconCollection = newManager.getIconCollection();
			iconCollection.setParent(this);

			isDataSaved = false;
			setCurrentIndex(curDiceBagIndex);
			retVal = true;
		} else {
			//Something went wrong.
			//Restore icon files.
			CustomIcon.restoreIconFiles(context);
		}
		return retVal;
	}

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

	/**
	 * Try to populate a {@link DiceBagCollection} from old (legacy) storage files.<br />
	 * If such attempt fail, the collection will be initialized with default data.
	 * @param collection Collection to populate.
	 */
	private void legacyLoadDiceBagCollection(DiceBagCollection collection) {
		DiceBag bag;
		
		bag = new DiceBag();
		bag.setResourceIndex(IconCollection.ID_ICON_DEFAULT);
		bag.setName(context.getString(R.string.def_bag_name));
		bag.setDescription(context.getString(R.string.def_bag_description));

		legacyLoadModifierCollection(bag.getModifiers());
		legacyLoadDiceCollection(bag.getDice());

		collection.clear();
		collection.add(bag);
	}

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
	
	private void showErrorMessage(final Context ctx, final int messageResId) {
		if (messageResId > 0x00000000) {
			new android.os.Handler(ctx.getMainLooper()).post(new java.lang.Runnable() {
				@Override
				public void run() {
					Toast.makeText(ctx, messageResId, Toast.LENGTH_LONG).show();
				}
			});
		}
	}
}
