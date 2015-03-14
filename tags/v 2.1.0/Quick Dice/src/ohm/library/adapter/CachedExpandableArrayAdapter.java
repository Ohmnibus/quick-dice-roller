package ohm.library.adapter;

import java.security.InvalidParameterException;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

public abstract class CachedExpandableArrayAdapter<T, S> extends BaseExpandableListAdapter {

	//private Context mContext;
	private LayoutInflater mInflater;
	private int mGroupViewId;
	private int mChildViewId;
	private List<T> mGroups;
	private List<List<S>> mChildLists;

	/**
	 * Class to cache a group or child {@link View}s and data.
	 * @author Ohmnibus
	 */
	private abstract class ViewCache {
		
		protected View root;

		/**
		 * 
		 * @param position Element position.
		 * @param baseView Parent view defining the list element.
		 */
		public ViewCache(View baseView) {
			this.root = baseView;
			findAllViews(this.root);
		}
		
		/**
		 * Implementation of this abstract method should perform all needed
		 * baseView.findViewById() and store resulting reference to appropriate
		 * public fields.
		 * @param baseView {@link View} defining a list element.
		 */
		protected abstract void findAllViews(View baseView);

	}
	
	protected abstract class ChildViewCache extends ViewCache{

		protected int childPosition;
		protected int groupPosition;
		protected Object data;

		public ChildViewCache(View baseView) {
			super(baseView);
		}
		
		/**
		 * Binds the data from user's object to the cached {@link View}s.
		 */
		public void bindData(int groupPosition, int childPosition, Object data) {
			this.groupPosition = groupPosition;
			this.childPosition = childPosition;
			this.data = data;
			bindData();
		}

		public abstract void bindData();
	}
	
	protected abstract class GroupViewCache extends ViewCache{

		protected int groupPosition;
		protected Object data;

		public GroupViewCache(View baseView) {
			super(baseView);
		}
		
		/**
		 * Binds the data from user's object to the cached {@link View}s.
		 */
		public void bindData(int groupPosition, boolean expanded, Object data) {
			this.groupPosition = groupPosition;
			this.data = data;
			bindData(expanded);
		}

		public abstract void bindData(boolean expanded);
	}

	/**
	 * Default constructor.
	 * @param context Current context
	 * @param groupResourceId Resource ID of the layout defining a group.
	 * @param childResourceId Resource ID of the layout defining an child.
	 * @param groups List of groups.
	 * @param childLists List of list of children, that is a list of children for each group.
	 */
	public CachedExpandableArrayAdapter(Context context, int groupResourceId, int childResourceId, List<T> groups, List<List<S>> childLists) {
		super();
		
		//mContext = context;
		mInflater = LayoutInflater.from(context);
		mGroupViewId = groupResourceId;
		mChildViewId = childResourceId;
		mGroups = groups;
		mChildLists = childLists;
		if (mGroups.size() != mChildLists.size()){
			throw new InvalidParameterException("Group count and children list count must match.");
		}
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return mChildLists.get(groupPosition).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@SuppressWarnings("unchecked")
	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		ChildViewCache viewCache;

		if (convertView == null) {
			convertView = mInflater.inflate(mChildViewId, null);
			viewCache = createChildCache(groupPosition, childPosition, convertView);
			convertView.setTag(viewCache);
		} else {
			viewCache = (ChildViewCache)convertView.getTag();
		}

		viewCache.bindData(
				groupPosition,
				childPosition,
				getChild(groupPosition, childPosition));

		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return mChildLists.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return mGroups.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return mGroups.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	@SuppressWarnings("unchecked")
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		GroupViewCache viewCache;

		if (convertView == null) {
			convertView = mInflater.inflate(mGroupViewId, null);
			viewCache = createGroupCache(groupPosition, convertView);
			convertView.setTag(viewCache);
		} else {
			viewCache = (GroupViewCache)convertView.getTag();
		}

		viewCache.bindData(
				groupPosition,
				isExpanded,
				getGroup(groupPosition));

		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	/**
	 * Creates your custom cache, that carries reference for e.g. ImageView
	 * and/or TextView. If necessary connect your clickable View object with the
	 * PrivateOnClickListener, or PrivateOnLongClickListener
	 * 
	 * @param convertView The view for the new cache object.
	 */
	protected abstract GroupViewCache createGroupCache(int group, View convertView);

	/**
	 * Creates your custom cache, that carries reference for e.g. ImageView
	 * and/or TextView. If necessary connect your clickable View object with the
	 * PrivateOnClickListener, or PrivateOnLongClickListener
	 * 
	 * @param convertView The view for the new cache object.
	 */
	protected abstract ChildViewCache createChildCache(int group, int position, View convertView);

}
