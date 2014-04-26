package ohm.quickdice.adapter;

import java.util.List;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import ohm.dexp.DExpression;
import ohm.library.adapter.ClickableArrayAdapter;
import ohm.quickdice.QuickDiceApp;
import ohm.quickdice.R;

public class GridExpressionAdapter extends ClickableArrayAdapter<DExpression> {

	/**
	 * Max number of char that fit in one line with standard font size.
	 */
	static int MAX_CHAR_FIT = 6;
	
	/**
	 * This class contain all the variable view of a single item in the GridActivity.
	 * @author Ohmnibus
	 *
	 */
    private class ExpViewCache extends ViewCache  {

		TextView name;
		ImageView icon;
		
		public ExpViewCache(View baseView) {
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
	public GridExpressionAdapter(Context context, int resourceId, List<DExpression> objects) {
		super(context, resourceId, objects);
	}

	@Override
	protected ViewCache createCache(int position, View convertView) {
		ExpViewCache cache = new ExpViewCache(convertView);

		//Handle here eventual click listeners
//		convertView.setOnClickListener(new OnClickListener(cache) {
//			
//			public void onClick(View v, ViewCache viewCache) {
//				DExpression exp = (DExpression)viewCache.data;
//			}
//		});

		return cache;
	}

	@Override
	protected void bindData(ViewCache viewCache) {
		ExpViewCache cache = (ExpViewCache)viewCache;
		DExpression exp = (DExpression)cache.data;

		//QuickDiceApp app = (QuickDiceApp)getContext().getApplicationContext();
		QuickDiceApp app = QuickDiceApp.getInstance();
		
		cache.name.setText(exp.getName());
		if (exp.getName().length() <= MAX_CHAR_FIT) {
			//Single line font size
			//cache.name.setTextSize(TypedValue.COMPLEX_UNIT_SP, cache.name.getResources().getDimension(R.dimen.dice_name_single_line_size));
			//cache.name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
			cache.name.setTextSize(
					TypedValue.COMPLEX_UNIT_PX,
					app.getResources().getDimension(R.dimen.dice_name_single_line_size));
		} else {
			//Double line font size
			//cache.name.setTextSize(TypedValue.COMPLEX_UNIT_SP, cache.name.getResources().getDimension(R.dimen.dice_name_multi_line_size));
			//cache.name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
			cache.name.setTextSize(
					TypedValue.COMPLEX_UNIT_PX,
					app.getResources().getDimension(R.dimen.dice_name_multi_line_size));
		}
		//cache.icon.setImageResource( exp.getResource());
		cache.icon.setImageDrawable(app.getGraphic().getDiceIcon(exp.getResourceIndex()));
		//cache.description.setText(exp.getDescription());
	}
	
	@Override
	protected void bindDropDownData(ViewCache viewCache) {
		bindData(viewCache);
	}

	/**
	 * Interface definition for a call-back to be invoked when an item is clicked.
	 * @author Ohmnibus
	 *
	 */
	public interface OnItemClickListener {
		public abstract void onGridViewItemClick(GridView gridView, View view, DExpression expression, int position, long id);
	}

	/**
	 * Listener to handle change events
	 */
	protected OnItemClickListener onItemClickListener = null;

	/**
	 * Allows the user to set an Listener and react to the event
	 * @param listener Listener
	 */
	public void setOnItemClickListener(OnItemClickListener listener) {
		onItemClickListener = listener;
	}
	
	/**
	 * This function is called after the check was complete
	 * @param listView ListView that has just been changed.
	 */
	protected void OnItemClick(GridView gridView, View view, DExpression expression, int position, long id){
		if(onItemClickListener!=null) {
			onItemClickListener.onGridViewItemClick(gridView, view, expression, position, id);
		}
	}
}
