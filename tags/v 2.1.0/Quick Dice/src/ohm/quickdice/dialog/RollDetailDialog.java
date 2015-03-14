package ohm.quickdice.dialog;

import java.util.ArrayList;

import ohm.quickdice.QuickDiceApp;
import ohm.quickdice.R;
import ohm.quickdice.adapter.MenuAdapter;
import ohm.quickdice.entity.RollResult;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class RollDetailDialog extends MenuDialog {

	RollResult[] lastResult;
	ArrayList<RollResult[]> resultList;
	int resultIndex;

	RollResult[] result = null;
	RollResult mergedResult = null;
	
	public RollDetailDialog(Activity activity, Menu menu, RollResult[] lastResult, ArrayList<RollResult[]> resultList, int resultIndex) {
		super(activity, menu);
		this.lastResult = lastResult;
		this.resultList = resultList;
		this.resultIndex = resultIndex;
	}
	
	protected RollResult[] getResult() {
		if (result == null) {
			if (resultIndex < 0) {
				result = lastResult;
			} else {
				result = resultList.get(resultIndex); //This seem to never throw ClassCastException!!
			}
		}
		return result;
	}
	
	protected RollResult getMergedResult() {
		if (mergedResult == null) {
			mergedResult = RollResult.mergeResultList(getResult());
			if (mergedResult == null) {
				mergedResult = new RollResult("", "", "", 0, 0, 0, RollResult.DEFAULT_RESULT_ICON);
			}
		}
		return mergedResult;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		setTitle(getMergedResult().getName());

		//setIcon(getDialogIcon(getMergedResult()));
		setIcon(QuickDiceApp.getInstance().getBagManager().getIconCollection().getByID(getMergedResult().getResourceIndex()));

		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected View getHeaderView(LayoutInflater inflater, ListView parent) {
		View view = getLayoutInflater().inflate(R.layout.roll_detail_dialog, parent, false);

		RollResult res = getMergedResult();
		
		view.setSelected(true);

		//((TextView)view.findViewById(R.id.rdName)).setText(res.getName());
		if (res.getDescription() == null || res.getDescription().length() == 0) {
			((TextView)view.findViewById(R.id.rdDescription)).setVisibility(View.GONE);
			((TextView)view.findViewById(R.id.rdDescriptionLabel)).setVisibility(View.GONE);
		} else {
			((TextView)view.findViewById(R.id.rdDescription)).setText(res.getDescription());
		}
		//((TextView)view.findViewById(R.id.rdExpression)).setText(res.);
		((TextView)view.findViewById(R.id.rdResultText)).setText(res.getResultText());
		((TextView)view.findViewById(R.id.rdResultValue)).setText(Long.toString(res.getResultValue()));
		((ImageView)view.findViewById(R.id.rdResultQuality)).setImageResource(res.getResultIconID());
//		((TextView)view.findViewById(R.id.rdRange)).setText(
//				Long.toString(res.getMinResultValue()) + " - " +
//				Long.toString(res.getMaxResultValue()) + " (" +
//				Long.toString(res.getMaxResultValue() - res.getMinResultValue() + 1) + ")");
		long min = res.getMinResultValue();
		long max = res.getMaxResultValue();
		long range = max - min + 1;
		((TextView)view.findViewById(R.id.rdRange)).setText(
				view.getResources().getString(R.string.lblRangeFmt,
						min,
						max,
						range));
		
		return view;
	}

	@Override
	protected boolean onPrepareOptionsMenu(MenuAdapter adapter) {
		RollResult[] rollItem;
		RollResult[] nextItem;
		
		rollItem = getResult();
		
		if (resultIndex + 1 < resultList.size()) {
			try {
				nextItem = resultList.get(resultIndex + 1);
			} catch (ClassCastException ex) {
				//Really don't know why, but sometime
				//a cast exception occur.
				nextItem = null;
			}
		} else {
			nextItem = null;
		}

		if (rollItem.length <= 1) {
			//Cannot split
			//adapter.findItem(R.id.mrSplit).setVisible(false);
			adapter.findItem(R.id.mrSplit).setEnabled(false);
		}
		if (nextItem == null || rollItem.length + nextItem.length > QuickDiceApp.getInstance().getPreferences().getMaxResultLink()) {
			//Cannot link (Last element or too much results)
			//adapter.findItem(R.id.mrMerge).setVisible(false);
			adapter.findItem(R.id.mrMerge).setEnabled(false);
		}
		
		return super.onPrepareOptionsMenu(adapter);
	}
	
//	protected Drawable getDialogIcon(RollResult res) {
//		//return graphicManager.getResizedDiceIcon(res.getResourceIndex(), 32, 32);
//		return QuickDiceApp.getInstance().getBagManager().getIconDrawable(
//				res.getResourceIndex(), 32, 32);
//	}
}
