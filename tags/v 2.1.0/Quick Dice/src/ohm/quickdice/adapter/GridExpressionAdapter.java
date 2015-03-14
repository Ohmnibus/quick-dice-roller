package ohm.quickdice.adapter;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import ohm.library.adapter.CachedCollectionAdapter;
import ohm.quickdice.QuickDiceApp;
import ohm.quickdice.R;
import ohm.quickdice.entity.Dice;
import ohm.quickdice.entity.DiceCollection;

public class GridExpressionAdapter extends CachedCollectionAdapter<Dice> {

	/**
	 * Max number of char that fit in one line with standard font size.
	 */
	static int MAX_CHAR_FIT = 6;

	/**
	 * This class contain all the variable view of a single item in the GridActivity.
	 * @author Ohmnibus
	 *
	 */
	private class ItemViewCache extends ViewCache<Dice>  {

		TextView name;
		ImageView icon;

		public ItemViewCache(View baseView) {
			super(baseView);
		}

		@Override
		protected void findAllViews(View baseView) {
			name = (TextView) baseView.findViewById(R.id.diName);
			icon = (ImageView) baseView.findViewById(R.id.diIcon);
		}
	}

	/**
	 * Default constructor.
	 * @param context The current context.
	 * @param resourceId The resource ID for a layout file containing appropriate Views to use when instantiating views.
	 * @param objects The objects to represent in the {@link ListView}.
	 */
	public GridExpressionAdapter(Context context, int resourceId, DiceCollection collection) {
		super(context, resourceId, collection);
	}

	@Override
	protected ViewCache<Dice> createCache(int position, View convertView) {
		return new ItemViewCache(convertView);
	}

	@Override
	protected void bindData(ViewCache<Dice> viewCache) {
		ItemViewCache cache = (ItemViewCache)viewCache;
		Dice exp = (Dice)cache.data;

		QuickDiceApp app = QuickDiceApp.getInstance();

		cache.name.setText(exp.getName());
		if (exp.getName().length() <= MAX_CHAR_FIT) {
			//Single line font size
			cache.name.setTextSize(
					TypedValue.COMPLEX_UNIT_PX,
					app.getResources().getDimension(R.dimen.dice_name_single_line_size));
		} else {
			//Double line font size
			cache.name.setTextSize(
					TypedValue.COMPLEX_UNIT_PX,
					app.getResources().getDimension(R.dimen.dice_name_multi_line_size));
		}
		//cache.icon.setImageDrawable(app.getBagManager().getIconDrawable(exp.getResourceIndex()));
		app.getBagManager().setIconDrawable(cache.icon, exp.getResourceIndex());
	}
}
