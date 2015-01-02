package ohm.quickdice.entity;

import java.util.ArrayList;
import java.util.Iterator;

import ohm.quickdice.R;
import ohm.quickdice.control.DiceBagManager;
import ohm.quickdice.util.AsyncDrawable;
import ohm.quickdice.util.Helper;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.SparseIntArray;
import android.widget.ImageView;

public class IconCollection implements BaseCollection<Icon> {

	public static final int ID_ICON_MALUS = -2;
	public static final int ID_ICON_BONUS = -1;
	public static final int ID_ICON_DEFAULT = 0;

	public static final int COLOR_DEFAULT = Icon.DEFAULT_COLOR;
	public static final Icon ICON_DEFAULT = Icon.newIcon(Icon.DEFAULT_ICON_RES_ID, COLOR_DEFAULT);
	public static final Icon ICON_MALUS = Icon.newIcon(R.drawable.ic_mod_malus, COLOR_DEFAULT);
	public static final Icon ICON_BONUS = Icon.newIcon(R.drawable.ic_mod_bonus, COLOR_DEFAULT);
	
	private static final int FIRST_CUSTOM_ICON_ID = 1000;

	private ArrayList<Icon> iconList;
	private SparseIntArray iconIds; //Used to keep track of unused custom icon ID.
	private DiceBagManager owner;
	private int firstCustomIconPos;
	
	static {
		ICON_DEFAULT.setId(ID_ICON_DEFAULT);
		ICON_BONUS.setId(ID_ICON_BONUS);
		ICON_MALUS.setId(ID_ICON_MALUS);
	}

	public IconCollection(DiceBagManager owner, Context ctx) {
		this.owner = owner;
		this.iconList = new ArrayList<Icon>();
		this.iconIds = new SparseIntArray();
		initSystemIcons(ctx);
	}
	
	public void setParent(DiceBagManager parent) {
		owner = parent;
	}
	
	private void initSystemIcons(Context ctx) {
		TypedArray myDiceIcons;
		TypedArray myDiceColors;
		Icon icon;
		
		Resources res = ctx.getResources();

		myDiceIcons = res.obtainTypedArray(R.array.diceIcons);
		myDiceColors = res.obtainTypedArray(R.array.diceColors);
		
		for (int i = 0; i < myDiceIcons.length(); i++) {
			icon = Icon.newIcon(
					myDiceIcons.getResourceId(i, Icon.DEFAULT_ICON_RES_ID),
					myDiceColors.getColor(i, COLOR_DEFAULT));
			icon.setId(i);
			iconList.add(icon);
		}
		
		myDiceIcons.recycle();
		myDiceColors.recycle();
		
		firstCustomIconPos = iconList.size();
	}

	@Override
	public Iterator<Icon> iterator() {
		return iconList.iterator();
	}

	@Override
	public int add(Icon item) {
		int retVal = -1;

		if (item.isCustom()) {
			if (item.getId() >= FIRST_CUSTOM_ICON_ID) {
				//ID already assigned
				retVal = iconIds.get(item.getId(), ID_ICON_DEFAULT);
				if (retVal != ID_ICON_DEFAULT) {
					//ID already in list: replace
					iconList.remove(retVal);
					iconList.add(retVal, item);
				} else {
					retVal = privateAdd(item);
				}
			} else {
				//Check for duplicates and get first icon ID
				int newId = getNextID(item);
				if (newId > -1) {
					item.setId(newId);
					retVal = privateAdd(item);
				}
			}
			if (retVal > -1) {
				setChanged();
			}
		}
		return retVal;
	}
	
	private int privateAdd(Icon item) {
		int retVal = iconList.size();
		iconList.add(item);
		iconIds.put(item.getId(), retVal);
		return retVal;
	}
	
	/**
	 * Check if icon already exists and give next icon ID.
	 * @param item Icon to test.
	 * @return New icon ID if it doesn't already exists, -1 otherwise.
	 */
	private int getNextID(Icon item){
		int retVal = -1;
		boolean exists = false;
		int nextId = FIRST_CUSTOM_ICON_ID;
		
		for (int i = firstCustomIconPos; i < iconList.size(); i++) {
			if (item.equals(iconList.get(i))) {
				//Already exists
				exists = true;
				break;
			}
			if (iconIds.get(nextId, ID_ICON_DEFAULT) == ID_ICON_DEFAULT) {
				//Free slot
				retVal = nextId;
			}
			nextId++;
		}
		if (exists) {
			retVal = -1;
		} else if (retVal == -1) {
			retVal = nextId;
		}
		
		return retVal;
	}

	/**
	 * Currently not supported!
	 */
	@Override
	public int add(int position, Icon item) {
		return add(item);
	}

	/**
	 * Currently not supported!
	 */
	@Override
	public boolean edit(int position, Icon item) {
		return false;
	}

	@Override
	public Icon remove(int position) {
		Icon retVal = null;
		if (iconList.get(position).isCustom()) {
			retVal = iconList.remove(position);
			int indexOfId = iconIds.indexOfKey(retVal.getId());
			//int removedIndex = iconIds.valueAt(indexOfId);
			iconIds.removeAt(indexOfId);
			//Reduce by 1 all values above "position"
			for (int i = 0; i < iconIds.size(); ++i) {
				if (iconIds.valueAt(i) > position) {
					iconIds.put(
							iconIds.keyAt(i),
							iconIds.valueAt(i) - 1);
				}
			}
			owner.resetIconInstances(retVal.getId());
			setChanged();
		}
		return retVal;
	}

	@Override
	public Icon get(int position) {
		return iconList.get(position);
	}

	/**
	 * Get the icon with the given ID.
	 * @param iconId Identifier of the icon.
	 * @return The icon with the given identifier, or the default icon.
	 */
	public Icon getByID(int iconId) {
		Icon retVal = null;
		if (iconId >= 0 && iconId < firstCustomIconPos) {
			//System icon. ID and Index are the same.
			retVal = get(iconId);
		} else {
			if (iconId == ID_ICON_BONUS) {
				retVal = ICON_BONUS;
			} else if (iconId == ID_ICON_MALUS) {
				retVal = ICON_MALUS;
			} else {
				retVal = iconList.get(iconIds.get(iconId, ID_ICON_DEFAULT)); //Default Icon ID and Index are equal
			}
		}

		return retVal;
	}

	@Override
	public int size() {
		return iconList.size();
	}

	@Override
	public void clear() {
		while (iconList.get(iconList.size() - 1).isCustom()) {
			iconList.remove(iconList.size() - 1);
		}
		iconIds.clear();
		setChanged();
	}
	
	public boolean isChanged() {
		return owner.isDataChanged();
	}
	
	protected void setChanged() {
		owner.setDataChanged();
	}

	/* ******************* */
	/* Convenience methods */
	/* ******************* */
	
	/**
	 * Convenience method to get the {@link Drawable} of the icon with the given ID.<br />
	 * @param ctx Context.
	 * @param iconId Identifier of the icon.
	 * @return {@link Drawable} of an icon.
	 */
	public Drawable getDrawable(Context ctx, int iconId) {
		return getByID(iconId).getDrawable(ctx);
	}

	/**
	 * Convenience method to get the {@link Drawable} of the icon with the given ID resized to the specified size.<br />
	 * @param ctx Context.
	 * @param iconId Identifier of the icon.
	 * @param width Desired width in {@code dp}.
	 * @param height Desired height in {@code dp}.
	 * @return Resized {@link Drawable} of the icon.
	 */
	public Drawable getDrawable(Context ctx, int iconId, int width, int height) {
		return Helper.resizeDrawable(ctx, getByID(iconId).getDrawable(ctx), width, height);
	}

	/**
	 * Convenience method to get the mask of the icon with the given ID.<br />
	 * The color of the mask is the one assigned to the icon.
	 * @param ctx Context.
	 * @param iconId Identifier of the icon.
	 * @return {@link Drawable} representing the mask of the icon.
	 */
	public Drawable getMask(Context ctx, int iconId) {
		Icon icon = getByID(iconId);
		return Helper.getMask(ctx, icon.getDrawable(ctx), icon.getColor(ctx));
	}

	/* ******************** */
	/* Asynchronous loading */
	/* ******************** */

	public void loadDrawable(ImageView imageView, int iconId) {
		getByID(iconId).setDrawable(imageView);
	}
	
	public void loadDrawable(ImageView imageView, int iconId, int width, int height) {
		AsyncDrawable.setDrawable(
				imageView,
				getDefaultDrawable(imageView.getResources()),
				new IconResizedLoader(getByID(iconId), width, height));
	}

	public void loadMask(ImageView imageView, int iconId) {
		AsyncDrawable.setDrawable(
				imageView,
				getDefaultDrawable(imageView.getResources()),
				new IconMaskLoader(getByID(iconId)));
	}

	private Drawable defaultDrawable = null;
	
	private Drawable getDefaultDrawable(Resources res) {
		if (defaultDrawable == null) {
			defaultDrawable = res.getDrawable(Icon.DEFAULT_ICON_RES_ID);
		}
		return defaultDrawable;
	}

	private class IconResizedLoader implements AsyncDrawable.DrawableProvider {
		
		private Icon icon;
		private int width;
		private int height;
		
		public IconResizedLoader(Icon icon, int width, int height) {
			this.icon = icon;
			this.width = width;
			this.height = height;
		}
		
		@Override
		public Drawable getDrawable(Context context) {
			return Helper.resizeDrawable(
					context,
					icon.getDrawable(context),
					width,
					height);
		}

		@Override
		public String getHash() {
			return "IconResizedLoader." + icon.getId() +
					"." + width +
					"." + height;
		}
		
	}
	
	private class IconMaskLoader implements AsyncDrawable.DrawableProvider {
		
		private Icon icon;
		
		public IconMaskLoader(Icon icon) {
			this.icon = icon;
		}
		
		@Override
		public Drawable getDrawable(Context context) {
			return Helper.getMask(
					context,
					icon.getDrawable(context),
					icon.getColor(context));
		}

		@Override
		public String getHash() {
			return "IconLoader" + icon.getId();
		}
		
	}
}
