package ohm.quickdice.adapter;

import java.util.ArrayList;
import java.util.List;

import ohm.library.adapter.CachedCollectionAdapter;
import ohm.quickdice.R;
import ohm.quickdice.entity.Icon;
import ohm.quickdice.entity.IconCollection;
import android.content.Context;
import android.view.View;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;

public class IconAdapter extends CachedCollectionAdapter<Icon> implements Filterable {
	public static final int ID_ICON_ADDNEW = -10;
	public static final int ID_ICON_NONE = -11;
	
	public static final CharSequence FILTER_ALL = "ALL";
	public static final CharSequence FILTER_SYSTEM = "SYS";
	public static final CharSequence FILTER_CUSTOM = "CUSTOM";
	
	private List<Icon> mFilteredData = null;
	private IconFilter mFilter = new IconFilter();

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
		if (mFilteredData == null) {
			return super.getCount() + 1;
		} else {
			return mFilteredData.size() + 1;
		}
	}

	@Override
	public Icon getItem(int position) {
		Icon retVal = null;
		if (mFilteredData == null) {
			if (position >= 0 && position < super.getCount()) {
				retVal = super.getItem(position);
			}
		} else {
			if (position >= 0 && position < mFilteredData.size()) {
				retVal = mFilteredData.get(position);
			}
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

	@Override
	public Filter getFilter() {
		return mFilter;
	}

	private int getOriginalCount() {
		return super.getCount();
	}
	
	private Icon getOriginalItem(int position) {
		return super.getItem(position);
	}
	
	private class IconFilter extends Filter {
		private static final int ALL = 0;
		private static final int SYSTEM = 1;
		private static final int CUSTOM = 2;

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();

			int filter = ALL;
			
			if (constraint.equals(FILTER_SYSTEM)) {
				filter = SYSTEM;
			} else if (constraint.equals(FILTER_CUSTOM)) {
				filter = CUSTOM;
			}
			
			if (filter == ALL) {
				//No filter
				results.values = null;
				results.count = getOriginalCount();

				return results;
			}
			
			int count = getOriginalCount();
			final ArrayList<Icon> nlist = new ArrayList<Icon>(count);
			Icon icon;

			for (int i = 0; i < count; i++) {
				icon = getOriginalItem(i);
				if (filter == ALL 
						|| (filter == SYSTEM && ! icon.isCustom())
						|| (filter == CUSTOM && icon.isCustom())) {
					
					nlist.add(icon);
				}
			}

			results.values = nlist;
			results.count = nlist.size();

			return results;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			mFilteredData = (List<Icon>)results.values;
			notifyDataSetChanged();
		}
		
	}
}
