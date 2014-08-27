package ohm.quickdice.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import ohm.library.adapter.CachedCollectionAdapter;
import ohm.quickdice.QuickDiceApp;
import ohm.quickdice.R;
import ohm.quickdice.entity.Variable;
import ohm.quickdice.entity.VariableCollection;


public class VariableAdapter extends CachedCollectionAdapter<Variable> {

	public static final int NO_SELECTION = -1;
	
	private int curPos = NO_SELECTION;

	/**
	 * This class contain all the variable view of a single item in a result list item.
	 * @author Ohmnibus
	 *
	 */
	private class ItemViewCache extends ViewCache<Variable>  {

		ImageView variableIcon;
		View root;
		TextView name;
		TextView description;
		TextView value;

		public ItemViewCache(View baseView) {
			super(baseView);
		}

		@Override
		protected void findAllViews(View baseView) {
			root = baseView;
			name = (TextView) baseView.findViewById(R.id.lblName);
			description = (TextView) baseView.findViewById(R.id.lblDescription);
			variableIcon = (ImageView) baseView.findViewById(R.id.imgIcon);
			value = (TextView) baseView.findViewById(R.id.lblValue);
		}
	}

	public VariableAdapter(Context context, int resourceId, VariableCollection collection) {
		super(context, resourceId, collection);
	}

	@Override
	protected ViewCache<Variable> createCache(int position, View convertView) {
		return new ItemViewCache(convertView);
	}

	@Override
	protected void bindData(ViewCache<Variable> viewCache) {
		ItemViewCache cache = (ItemViewCache)viewCache;

		Variable variable = cache.data;

		QuickDiceApp app = QuickDiceApp.getInstance();
		cache.variableIcon.setImageDrawable(app.getGraphic().getDiceIcon(variable.getResourceIndex()));
		cache.name.setText(variable.getName());
		cache.description.setText(variable.getDescription());
		cache.value.setText(Integer.toString(variable.getCurVal()));
		
		if (cache.position == curPos) {
			cache.root.setBackgroundResource(R.drawable.bg_selector_state_selected);
		} else {
			cache.root.setBackgroundResource(0);
		}
	}
	
	public void setSelected(int position) {
		curPos = position;
		notifyDataSetChanged();
	}
	
	public int getSelected() {
		return curPos;
	}
}
