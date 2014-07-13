package ohm.quickdice.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import ohm.library.adapter.CachedArrayAdapter;
import ohm.quickdice.QuickDiceApp;
import ohm.quickdice.R;
import ohm.quickdice.entity.DiceBag;

public class DiceBagAdapter extends CachedArrayAdapter<DiceBag> {

	/**
	 * This class contain all the variable view of a single item in a result list item.
	 * @author Ohmnibus
	 *
	 */
    private class ItemViewCache extends ViewCache  {

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
    
	public DiceBagAdapter(Context context, int resourceId, List<DiceBag> objects) {
		super(context, resourceId, objects);
	}

	@Override
	protected ViewCache createCache(int position, View convertView) {
		return new ItemViewCache(convertView);
	}

	@Override
	protected void bindData(ViewCache viewCache) {
		ItemViewCache cache = (ItemViewCache)viewCache;
		
		DiceBag diceBag = (DiceBag)cache.data;

		QuickDiceApp app = (QuickDiceApp)getContext().getApplicationContext();
		cache.diceBagIcon.setImageDrawable(app.getGraphic().getDiceIcon(diceBag.getResourceIndex()));
		cache.name.setText(diceBag.getName());
		cache.description.setText(diceBag.getDescription());
		//if (cache.position == app.getCurrentDiceBagIndex()) {
		if (cache.position == app.getBagManager().getCurrentDiceBag()) {
			cache.root.setBackgroundResource(R.drawable.bg_selected_bag);
		} else {
			cache.root.setBackgroundResource(0);
		}
	}

	@Override
	protected void bindDropDownData(ViewCache viewCache) {
		bindData(viewCache);
	}
	
	

}
