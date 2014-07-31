package ohm.quickdice.adapter;

import java.util.ArrayList;
import java.util.List;

import ohm.library.adapter.CachedExpandableArrayAdapter;
import ohm.quickdice.QuickDiceApp;
import ohm.quickdice.R;
import ohm.quickdice.entity.Dice;
import ohm.quickdice.entity.DiceBag;
import ohm.quickdice.entity.DiceBagCollection;
import ohm.quickdice.entity.DiceCollection;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ExpDiceBagAdapter extends CachedExpandableArrayAdapter<DiceBag, Dice> {

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
			Dice dice = (Dice)data;

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

	public ExpDiceBagAdapter(Context context, int groupResourceId, int childResourceId, DiceBagCollection diceBagCollection) {
		super(context, groupResourceId, childResourceId, getParentList(diceBagCollection), getChildLists(diceBagCollection));
	}

	private static List<DiceBag> getParentList(DiceBagCollection diceBagCollection) {
		List<DiceBag> retVal = new ArrayList<DiceBag>();
		for (DiceBag diceBag : diceBagCollection) {
			retVal.add(diceBag);
		}
		return retVal;
	}
	
	private static List<List<Dice>> getChildLists(DiceBagCollection diceBagCollection) {
		List<List<Dice>> retVal = new ArrayList<List<Dice>>();
		for (DiceBag diceBag : diceBagCollection) {
			retVal.add(getChildList(diceBag.getDice()));
		}
		return retVal;
	}
	
	private static List<Dice> getChildList(DiceCollection diceCollection) {
		List<Dice> retVal = new ArrayList<Dice>();
		for (Dice dice: diceCollection) {
			retVal.add(dice);
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
