package ohm.library.adapter;

import ohm.quickdice.entity.BaseCollection;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class CachedCollectionAdapter<T> extends BaseAdapter {

	private BaseCollection<T> mCollection;
	private Context mContext;
	private LayoutInflater mInflater;
	private int mResourceId;

	/**
	 * Class to cache an item's {@link View}s and data.
	 * @author Ohmnibus
	 */
	protected abstract class ViewCache<Y> {
		
		View baseView;
		public int position;
		public Y data;

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
	 * @param collection Collection of object to display, or {@code null} for an empty list
	 */
	public CachedCollectionAdapter(Context context, int resourceId, BaseCollection<T> collection) {
		super();
		//super(context, resourceId, objects);
		
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mResourceId = resourceId;
		mCollection = collection;
	}

	@Override
	@SuppressWarnings("unchecked")
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewCache<T> viewCache;

		if (convertView == null) {
			convertView = mInflater.inflate(mResourceId, null);
			viewCache = createCache(position, convertView);
			convertView.setTag(viewCache);
		} else {
			viewCache = (ViewCache<T>)convertView.getTag();
		}

		viewCache.position = position;
		viewCache.data = getItem(position);

		bindData(viewCache);

		return convertView;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		ViewCache<T> viewCache;

		if (convertView == null) {
			convertView = mInflater.inflate(mResourceId, null);
			viewCache = createCache(position, convertView);
			convertView.setTag(viewCache);
		} else {
			viewCache = (ViewCache<T>)convertView.getTag();
		}

		viewCache.position = position;
		viewCache.data = getItem(position);

		bindDropDownData(viewCache);

		return convertView;
	}

	@Override
	public T getItem(int position) {
		return mCollection.get(position);
	}

	@Override
	public int getCount() {
		return mCollection.size();
	}
	
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public boolean isEmpty() {
		return mCollection == null || mCollection.size() == 0;
	}
	
	public Context getContext() {
		return mContext;
	}

	/**
	 * Creates your custom cache, that carries reference for e.g. ImageView
	 * and/or TextView. If necessary connect your clickable View object with the
	 * PrivateOnClickListener, or PrivateOnLongClickListener
	 * 
	 * @param convertView The view for the new cache object.
	 */
	protected abstract ViewCache<T> createCache(int position, View convertView);

	/**
	 * Binds the data from user's object to the cached {@link View}s.
	 * @param viewCache Cache of {@link View}s that shall represent the data object.
	 */
	protected abstract void bindData(ViewCache<T> viewCache);

	/**
	 * Binds the data from user's object to the cached {@link View}s.
	 * @param viewCache Cache of {@link View}s that shall represent the data object.
	 */
	protected void bindDropDownData(ViewCache<T> viewCache) {
		bindData(viewCache);
	}

}
