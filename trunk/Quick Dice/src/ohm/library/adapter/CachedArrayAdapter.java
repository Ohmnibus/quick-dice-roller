package ohm.library.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public abstract class CachedArrayAdapter<T> extends ArrayAdapter<T> {

	private LayoutInflater mInflater;
	private int mViewId;

	/**
	 * Class to cache an item's {@link View}s and data.
	 * @author Ohmnibus
	 */
	protected abstract class ViewCache {
		
		View baseView;
		public int position;
		public Object data;

		/**
		 * 
		 * @param position Element position.
		 * @param baseView Parent view defining the list element.
		 */
		public ViewCache(View baseView) {
			this.baseView = baseView;
			findAllViews(this.baseView);
		}
		
		/**
		 * Implementation of this abstract method should perform all needed
		 * baseView.findViewById() and store resulting reference to appropriate
		 * public fields.
		 * @param baseView {@link View} defining a list element.
		 */
		protected abstract void findAllViews(View baseView);
	}

	/**
	 * Default constructor.
	 * @param context Current context
	 * @param resourceId Resource ID of the layout defining a {@link ListView} element.
	 * @param objects Object list, or null, if you like to indicate an empty.
	 */
	public CachedArrayAdapter(Context context, int resourceId, List<T> objects) {
		super(context, resourceId, objects);
		
		mInflater = LayoutInflater.from(context);
		mViewId = resourceId;
	}

	@SuppressWarnings("unchecked")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewCache viewCache;

		if (convertView == null) {
			convertView = mInflater.inflate(mViewId, null);
			viewCache = createCache(position, convertView);
			convertView.setTag(viewCache);
		} else {
			viewCache = (ViewCache)convertView.getTag();
		}

		viewCache.position = position;
		viewCache.data = getItem(position);

		bindData(viewCache);

		return convertView;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		
		ViewCache viewCache;

		if (convertView == null) {
			convertView = mInflater.inflate(mViewId, null);
			viewCache = createCache(position, convertView);
			convertView.setTag(viewCache);
		} else {
			viewCache = (ViewCache)convertView.getTag();
		}

		viewCache.position = position;
		viewCache.data = getItem(position);

		bindDropDownData(viewCache);

		return convertView;
	}

	/**
	 * Creates your custom cache, that carries reference for e.g. ImageView
	 * and/or TextView. If necessary connect your clickable View object with the
	 * PrivateOnClickListener, or PrivateOnLongClickListener
	 * 
	 * @param convertView The view for the new cache object.
	 */
	protected abstract ViewCache createCache(int position, View convertView);

	/**
	 * Binds the data from user's object to the cached {@link View}s.
	 * @param viewCache Cache of {@link View}s that shall represent the data object.
	 */
	protected abstract void bindData(ViewCache viewCache);

	/**
	 * Binds the data from user's object to the cached {@link View}s.
	 * @param viewCache Cache of {@link View}s that shall represent the data object.
	 */
	protected abstract void bindDropDownData(ViewCache viewCache);

}
