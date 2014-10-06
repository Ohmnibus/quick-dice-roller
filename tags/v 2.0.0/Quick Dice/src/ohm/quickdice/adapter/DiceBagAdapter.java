package ohm.quickdice.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import ohm.library.adapter.CachedCollectionAdapter;
import ohm.quickdice.QuickDiceApp;
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

	public DiceBagAdapter(Context context, int resourceId, DiceBagCollection collection) {
		super(context, resourceId, collection);
	}

	@Override
	protected ViewCache<DiceBag> createCache(int position, View convertView) {
		return new ItemViewCache(convertView);
	}

	@Override
	protected void bindData(ViewCache<DiceBag> viewCache) {
		ItemViewCache cache = (ItemViewCache)viewCache;

		DiceBag diceBag = cache.data;

		QuickDiceApp app = (QuickDiceApp)getContext().getApplicationContext();
		cache.diceBagIcon.setImageDrawable(app.getGraphic().getDiceIcon(diceBag.getResourceIndex()));
		cache.name.setText(diceBag.getName());
		cache.description.setText(diceBag.getDescription());

		if (cache.position == app.getBagManager().getCurrentIndex()) {
			cache.root.setBackgroundResource(R.drawable.bg_selected_bag);
		} else {
			cache.root.setBackgroundResource(0);
		}
	}
}
