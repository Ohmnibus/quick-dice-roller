package ohm.quickdice.adapter;

import java.util.List;

import ohm.quickdice.QuickDiceApp;
import ohm.quickdice.R;
import ohm.quickdice.entity.DiceBag;
import android.content.Context;
import android.view.View;

public class ExpDiceBagAdapterDest extends ExpDiceBagAdapter {
	
	static final QuickDiceApp app = QuickDiceApp.getInstance();
	
    protected class MyChildViewCacheDest extends MyChildViewCache  {

		public MyChildViewCacheDest(View baseView) {
			super(baseView);
		}

		@Override
		public void bindData() {
			if (data == null) {
				icon.setVisibility(View.INVISIBLE);
				name.setText(R.string.lblLastPositionName);
				description.setText(R.string.lblLastPositionDesc);
			} else {
				icon.setVisibility(View.VISIBLE);
				super.bindData();
			}
		}

	}

	public ExpDiceBagAdapterDest(Context context, int groupResourceId, int childResourceId, List<DiceBag> diceBags) {
		super(context, groupResourceId, childResourceId, diceBags);
	}
	
	@Override
	public int getChildrenCount(int groupPosition) {
		return super.getChildrenCount(groupPosition) + 1;
	}
	
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		if (childPosition >= super.getChildrenCount(groupPosition)) {
			return null;			
		}
		return super.getChild(groupPosition, childPosition);
	}
	
	@Override
	protected GroupViewCache createGroupCache(int group, View convertView) {
		return new MyGroupViewCache(convertView);
	}

	@Override
	protected ChildViewCache createChildCache(int group, int position, View convertView) {
		return new MyChildViewCacheDest(convertView);
	}
	
}
