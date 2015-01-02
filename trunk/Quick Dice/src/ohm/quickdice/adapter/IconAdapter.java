package ohm.quickdice.adapter;

import ohm.library.adapter.CachedCollectionAdapter;
import ohm.quickdice.R;
import ohm.quickdice.entity.Icon;
import ohm.quickdice.entity.IconCollection;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;

public class IconAdapter extends CachedCollectionAdapter<Icon> {
	public static final int ID_ICON_ADDNEW = -10;
	public static final int ID_ICON_NONE = -11;
	
	private int selectedIconId;
	
	private class ItemViewCache extends ViewCache<Icon>  {

		View root;
		ImageView icon;
		ImageView lock;

		public ItemViewCache(View baseView) {
			super(baseView);
		}

		@Override
		protected void findAllViews(View baseView) {
			root = baseView;
			icon = (ImageView) baseView.findViewById(R.id.imgIcon);
			lock = (ImageView) baseView.findViewById(R.id.imgLock);
		}
	}
	
	public IconAdapter(Context context, int resourceId, IconCollection collection) {
		this(context, resourceId, collection, ID_ICON_NONE);
	}

	public IconAdapter(Context context, int resourceId, IconCollection collection, int selectedIconId) {
		super(context, resourceId, collection);
		this.selectedIconId = selectedIconId;
	}

	@Override
	protected ViewCache<Icon> createCache(int position, View convertView) {
		return new ItemViewCache(convertView);
	}

	@Override
	protected void bindData(ViewCache<Icon> viewCache) {
		ItemViewCache cache = (ItemViewCache)viewCache;

		Icon icon = cache.data;

		if (icon == null) {
			//Add icon
			cache.icon.setImageResource(android.R.drawable.ic_menu_add);
			cache.lock.setVisibility(View.GONE);
			cache.root.setBackgroundResource(0);
		} else {
			//cache.icon.setImageDrawable(icon.getDrawable(getContext()));
			icon.setDrawable(cache.icon);
			cache.lock.setVisibility(icon.isCustom() ? View.GONE : View.VISIBLE);
			if (icon.getId() == selectedIconId) {
				//Selected icon
				cache.root.setBackgroundResource(R.drawable.bg_selector_state_focus);
			} else {
				//Unselected icon
				cache.root.setBackgroundResource(0);
			}
		}
	};

	@Override
	public int getCount() {
		//return app.getGraphic().getDiceIconCount();
		return super.getCount() + 1;
	}

	@Override
	public Icon getItem(int position) {
		Icon retVal = null;
		if (position >= 0 && position < super.getCount()) {
			retVal = super.getItem(position);
		}
		return retVal;
	}

	@Override
	public long getItemId(int position) {
		Icon icon = getItem(position);
		return icon == null ? ID_ICON_ADDNEW : icon.getId();
	}
	
	public void setSelectedId(int iconId) {
		selectedIconId = iconId;
	}
	
	public int getSelectedId() {
		return selectedIconId;
	}

}
