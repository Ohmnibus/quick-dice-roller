package ohm.quickdice.adapter;

import java.util.ArrayList;
import java.util.List;

import ohm.dexp.DExpression;
import ohm.library.adapter.CachedExpandableArrayAdapter;
import ohm.quickdice.QuickDiceApp;
import ohm.quickdice.R;
import ohm.quickdice.entity.DiceBag;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ExpDiceBagAdapter extends CachedExpandableArrayAdapter<DiceBag, DExpression> {
	
	static final QuickDiceApp app = QuickDiceApp.getInstance();
	
    protected class MyChildViewCache extends ChildViewCache  {

		ImageView icon;
		TextView name;
		TextView description;
		
		public MyChildViewCache(View baseView) {
			super(baseView);
		}

		@Override
		protected void findAllViews(View baseView) {
			name = (TextView) baseView.findViewById(R.id.dsiName);
			description = (TextView) baseView.findViewById(R.id.dsiDescription);
			icon = (ImageView) baseView.findViewById(R.id.dsiImage);
		}

		@Override
		public void bindData() {
			DExpression dice = (DExpression)data;

			icon.setImageDrawable(app.getGraphic().getDiceIcon(dice.getResourceIndex()));
			name.setText(dice.getName());
			description.setText(dice.getDescription());
		}

	}

    protected class MyGroupViewCache extends GroupViewCache  {

		ImageView icon;
		ImageView indicator;
		TextView name;
		TextView description;
		
		public MyGroupViewCache(View baseView) {
			super(baseView);
		}

		@Override
		protected void findAllViews(View baseView) {
			name = (TextView) baseView.findViewById(R.id.dsgName);
			description = (TextView) baseView.findViewById(R.id.dsgDescription);
			icon = (ImageView) baseView.findViewById(R.id.dsgImage);
			indicator = (ImageView) baseView.findViewById(R.id.dsgIndicator);
		}

		@Override
		public void bindData(boolean expanded) {
			DiceBag diceBag = (DiceBag)data;

			icon.setImageDrawable(app.getGraphic().getDiceIcon(diceBag.getResourceIndex()));
			name.setText(diceBag.getName());
			description.setText(diceBag.getDescription());
			indicator.setImageResource(
					expanded ? android.R.drawable.arrow_up_float : android.R.drawable.arrow_down_float);
		}

	}
    
	public ExpDiceBagAdapter(Context context, int groupResourceId, int childResourceId, List<DiceBag> diceBags) {
		super(context, groupResourceId, childResourceId, diceBags, getChildLists(diceBags));
	}
	
	private static List<List<DExpression>> getChildLists(List<DiceBag> diceBags) {
		List<List<DExpression>> retVal = new ArrayList<List<DExpression>>();
		for (DiceBag diceBag : diceBags) {
			retVal.add(diceBag.getDice());
		}
		return retVal;
	}

	@Override
	protected GroupViewCache createGroupCache(int group, View convertView) {
		return new MyGroupViewCache(convertView);
	}

	@Override
	protected ChildViewCache createChildCache(int group, int position, View convertView) {
		return new MyChildViewCache(convertView);
	}
	
}
