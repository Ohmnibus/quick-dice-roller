package ohm.library.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.widget.ListView;

public abstract class ClickableArrayAdapter<T> extends CachedArrayAdapter<T> {

	/**
	 * The click listener base class. Handles click to clickable views in a list element.
	 */
	public abstract class OnClickListener implements View.OnClickListener {

		private ViewCache mViewCache;

		/**
		 * @param viewCache The cache of the clickable list item.
		 */
		public OnClickListener(ViewCache viewCache) {
			mViewCache = viewCache;
		}

		// delegates the click event
		public void onClick(View v) {
			onClick(v, mViewCache);
		}

		/**
		 * Implement your click behavior here
		 * @param v The clicked view.
		 * @param viewCache The cache of the clickable list item.
		 */
		public abstract void onClick(View v, ViewCache viewCache);
	};
	
	/**
	 * Interface definition for a callback to be invoked when Content data changed.
	 * @author Ohmnibus
	 *
	 */
	public interface OnContentChangedListener {
		public abstract void onListViewContentChanged(ListView l);
	}

	/**
	 * Listener to handle change events
	 */
	protected OnContentChangedListener onContentChangedListener = null;

	/**
	 * Allows the user to set an Listener and react to the event
	 * @param listener Listener
	 */
	public void setOnContentChangedListener(OnContentChangedListener listener) {
		onContentChangedListener = listener;
	}
	
	/**
	 * This function is called after the check was complete
	 * @param listView ListView that has just been changed.
	 */
	protected void OnContentChanged(ListView listView){
		if(onContentChangedListener!=null) {
			onContentChangedListener.onListViewContentChanged(listView);
		}
	}
	
	/**
	 * Default constructor.
	 * @param context Current context
	 * @param resourceId Resource ID of the layout defining a {@link ListView} element.
	 * @param objects Object list, or null, if you like to indicate an empty.
	 */
	public ClickableArrayAdapter(Context context, int resourceId, List<T> objects) {
		super(context, resourceId, objects);
	}
}
