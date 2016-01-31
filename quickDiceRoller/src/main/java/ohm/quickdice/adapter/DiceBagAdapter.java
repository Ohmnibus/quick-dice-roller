package ohm.quickdice.adapter;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import ohm.library.adapter.CachedCollectionAdapter;
import ohm.quickdice.R;
import ohm.quickdice.entity.DiceBag;
import ohm.quickdice.entity.DiceBagCollection;

public class DiceBagAdapter extends CachedCollectionAdapter<DiceBag> {

	/**
	 * This class contain all the variable view of a single item in a result list item.
	 * @author Ohmnibus
	 *
	 */
	private class ItemViewCache extends ViewCache<DiceBag>  {

		ImageView diceBagIcon;
		View root;
		TextView name;
		TextView description;

		public ItemViewCache(View baseView) {
			super(baseView);
		}

		@Override
		protected void findAllViews(View baseView) {
			root = baseView;
			name = (TextView) baseView.findViewById(R.id.dbiName);
			description = (TextView) baseView.findViewById(R.id.dbiDescription);
			diceBagIcon = (ImageView) baseView.findViewById(R.id.dbiImage);
		}
	}

	DiceBagCollection collection;
	
	public DiceBagAdapter(Context context, int resourceId, DiceBagCollection collection) {
		super(context, resourceId, collection);
		this.collection = collection;
	}

	@Override
	protected ViewCache<DiceBag> createCache(int position, View convertView) {
		return new ItemViewCache(convertView);
	}

	@Override
	protected void bindData(ViewCache<DiceBag> viewCache) {
		ItemViewCache cache = (ItemViewCache)viewCache;

		DiceBag diceBag = cache.data;

		//QuickDiceApp app = (QuickDiceApp)getContext().getApplicationContext();
		////cache.diceBagIcon.setImageDrawable(app.getGraphic().getDiceIcon(diceBag.getResourceIndex()));
		////cache.diceBagIcon.setImageDrawable(app.getBagManager().getIconDrawable(diceBag.getResourceIndex()));
		//app.getBagManager().setIconDrawable(cache.diceBagIcon, diceBag.getResourceIndex());
		collection.getManager().setIconDrawable(cache.diceBagIcon, diceBag.getResourceIndex());
		
		cache.name.setText(diceBag.getName());
		cache.description.setText(diceBag.getDescription());

//		if (cache.position == collection.getCurrentIndex()) {
//			cache.root.setBackgroundResource(R.drawable.bg_selected_bag);
//		} else {
//			cache.root.setBackgroundResource(0);
//		}
		setSelection(cache);
	}
	
	protected void setSelection(ItemViewCache cache) {
		if (cache.position == collection.getCurrentIndex()) {
			cache.root.setBackgroundResource(R.drawable.bg_selected_bag);
		} else {
			cache.root.setBackgroundResource(0);
		}
	}

	public static class DiceBagSelectorAdapter extends DiceBagAdapter implements OnItemClickListener {
		SparseBooleanArray selected;
		
		public DiceBagSelectorAdapter(Context context, int resourceId, DiceBagCollection collection) {
			super(context, resourceId, collection);
			selected = new SparseBooleanArray(collection.size());
		}
	
		@Override
		protected void setSelection(ItemViewCache cache) {
			super.setSelection(cache);
			if (selected.get(cache.position)) {
				cache.root.setBackgroundResource(R.drawable.bg_selected_bag);
			} else {
				cache.root.setBackgroundResource(0);
			}
		}
		
		public SparseBooleanArray getSelected() {
			return selected;
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			selected.put(position, ! selected.get(position));
			notifyDataSetChanged();
		}
		
	}
}