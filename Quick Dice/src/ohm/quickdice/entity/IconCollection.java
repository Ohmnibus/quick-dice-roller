package ohm.quickdice.entity;

import java.util.ArrayList;
import java.util.Iterator;

import ohm.quickdice.R;
import ohm.quickdice.control.IIconManager;
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
	/** Used to keep track of unused custom icon ID.<br>Maps id (key) and position (value) */
	private SparseIntArray iconIds; //Used to keep track of unused custom icon ID.
	private IIconManager owner;
	private int firstCustomIconPos;
	
	static {
		ICON_DEFAULT.setId(ID_ICON_DEFAULT);
		ICON_BONUS.setId(ID_ICON_BONUS);
		ICON_MALUS.setId(ID_ICON_MALUS);
	}

	public IconCollection(IIconManager owner, Context ctx) {
		this.owner = owner;
		this.iconList = new ArrayList<Icon>();
		this.iconIds = new SparseIntArray();
		initSystemIcons(ctx);
	}
	
	public void setParent(IIconManager parent) {
		owner = parent;
		for (Icon icon : iconList) {
			icon.setParent(owner);
		}
	}
	
//	public IIconManager getParent() {
//		return owner;
//	}
	
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
			icon.setParent(owner);
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
					Icon removed = iconList.remove(retVal);
					removed.setParent(null);
					
					item.setParent(owner);
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
		item.setParent(owner);
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
//		Icon retVal = null;
//		if (iconList.get(position).isCustom()) {
//			//Remove icon
//			retVal = iconList.remove(position);
//			retVal.setParent(null);
//			//Remove id from identifiers list
//			int indexOfId = iconIds.indexOfKey(retVal.getId());
//			iconIds.removeAt(indexOfId);
//			//Reduce by 1 all indexes above "position"
//			for (int i = 0; i < iconIds.size(); ++i) {
//				if (iconIds.valueAt(i) > position) {
//					iconIds.put(
//							iconIds.keyAt(i),
//							iconIds.valueAt(i) - 1);
//				}
//			}
//			owner.resetIconInstances(retVal.getId());
//			setChanged();
//		}
//		return retVal;
		return remove(position, true);
	}
	
	/**
	 * Remove an Icon from the collection.
	 * @param position Position of the Icon to remove.
	 * @param resetReferences If references to this icon should be removed.
	 * @return Removed object.
	 */
	public Icon remove(int position, boolean resetReferences) {
		Icon retVal = null;
		if (iconList.get(position).isCustom()) {
			//Remove icon
			retVal = iconList.remove(position);
			retVal.setParent(null);
			//Remove id from identifiers list
			int indexOfId = iconIds.indexOfKey(retVal.getId());
			iconIds.removeAt(indexOfId);
			//Reduce by 1 all indexes above "position"
			for (int i = 0; i < iconIds.size(); ++i) {
				if (iconIds.valueAt(i) > position) {
					iconIds.put(
							iconIds.keyAt(i),
							iconIds.valueAt(i) - 1);
				}
			}
			if (resetReferences) {
				owner.resetIconInstances(retVal.getId());
			}
			setChanged();
		}
		return retVal;
	}

	@Override
	public Icon get(int position) {
		return iconList.get(position);
	}
	
	@Override
	public int indexOf(Icon item) {
		return iconList.indexOf(item);
	}

	/**
	 * Get the icon with the given ID.
	 * @param iconId Identifier of the icon.
	 * @return The icon with the given identifier, or the default icon.
	 */
	public Icon getByID(int iconId) {
		Icon retVal = null;
//		if (iconId >= 0 && iconId < firstCustomIconPos) {
//			//System icon. ID and Index are the same.
//			retVal = get(iconId);
//		} else {
//			if (iconId == ID_ICON_BONUS) {
//				retVal = ICON_BONUS;
//			} else if (iconId == ID_ICON_MALUS) {
//				retVal = ICON_MALUS;
//			} else {
//				retVal = iconList.get(iconIds.get(iconId, ID_ICON_DEFAULT)); //Default Icon ID and Index are equal
//			}
//		}
		if (iconId == ID_ICON_BONUS) {
			retVal = ICON_BONUS;
		} else if (iconId == ID_ICON_MALUS) {
			retVal = ICON_MALUS;
		} else {
			int iconPos = getPositionByID(iconId);
			if (iconPos >= 0) {
				retVal = iconList.get(iconPos);
			} else {
				retVal = iconList.get(ID_ICON_DEFAULT);
			}
		}

		return retVal;
	}

	/**
	 * Get the icon position given its ID.
	 * @param iconId Identifier of the icon.
	 * @return The position of the icon with the given identifier, or {@code -1} if not found.
	 */
	public int getPositionByID(int iconId) {
		int retVal = -1;
		if (iconId >= 0 && iconId < firstCustomIconPos) {
			//System icon. ID and Index are the same.
			retVal = iconId;
		} else {
			if (iconId == ID_ICON_BONUS) {
				retVal = -1;
			} else if (iconId == ID_ICON_MALUS) {
				retVal = -1;
			} else {
				retVal = iconIds.get(iconId, -1);
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
			Icon removed = iconList.remove(iconList.size() - 1);
			removed.setParent(null);
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
	
	/**
	 * Notify the change of the icon folder and move all files to the right position.
	 * @return
	 */
	public boolean folderChanged() {
		for (Icon icon : iconList) {
			icon.folderChanged();
		}
		return true;
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
	 * @param widthId Reference to the dimension containing the desired width.
	 * @param heightId Reference to the dimension containing the desired height.
	 * @return Resized {@link Drawable} of the icon.
	 */
	public Drawable getDrawable(Context ctx, int iconId, int widthId, int heightId) {
		return Helper.resizeDrawable(ctx, getByID(iconId).getDrawable(ctx), widthId, heightId);
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
	
	@Deprecated
	public void loadDrawable(ImageView imageView, int iconId, int widthId, int heightId) {
		AsyncDrawable.setDrawable(
				imageView,
				getDefaultDrawable(imageView.getResources()),
				new IconResizedLoader(getByID(iconId), widthId, heightId));
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
		private int widthId;
		private int heightId;
		
		public IconResizedLoader(Icon icon, int widthId, int heightId) {
			this.icon = icon;
			this.widthId = widthId;
			this.heightId = heightId;
		}
		
		@Override
		public Drawable getDrawable(Context context) {
			return Helper.resizeDrawable(
					context,
					icon.getDrawable(context),
					widthId,
					heightId);
		}

		@Override
		public String getHash() {
			return "IconResizedLoader." + icon.getId() +
					"." + widthId +
					"." + heightId;
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
