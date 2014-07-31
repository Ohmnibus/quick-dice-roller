package ohm.quickdice.dialog;

import ohm.dexp.TokenBase;
import ohm.dexp.exception.DException;
import ohm.quickdice.QuickDiceApp;
import ohm.quickdice.R;
import ohm.quickdice.adapter.MenuAdapter;
import ohm.quickdice.entity.Dice;
import ohm.quickdice.entity.DiceBag;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class DiceDetailDialog extends MenuDialog {

	DiceBag diceBag;
	int dieIndex;
	Dice expression;
	
	public DiceDetailDialog(Activity activity, DiceBag diceBag, int dieIndex, Menu menu) {
		super(activity, menu);
		this.diceBag = diceBag;
		this.dieIndex = dieIndex;
		this.expression = diceBag.getDice().get(dieIndex);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		setTitle(expression.getName());

		setIcon(getDialogIcon());

		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected View getHeaderView(LayoutInflater inflater, ListView parent) {
		View view = inflater.inflate(R.layout.dice_detail_dialog, parent, false);

		//((TextView)view.findViewById(R.id.ddName)).setText(expression.getName());
		if (expression.getDescription() == null || expression.getDescription().length() == 0) {
			((TextView)view.findViewById(R.id.ddDescription)).setVisibility(View.GONE);
			((TextView)view.findViewById(R.id.ddDescriptionLabel)).setVisibility(View.GONE);
		} else {
			((TextView)view.findViewById(R.id.ddDescription)).setText(expression.getDescription());
		}
		((TextView)view.findViewById(R.id.ddExpresson)).setText(expression.getExpression());
		try {
			long min = expression.getMinResult() / TokenBase.VALUES_PRECISION_FACTOR;
			long max = expression.getMaxResult() / TokenBase.VALUES_PRECISION_FACTOR;
			long range = max - min + 1;
			((TextView)view.findViewById(R.id.ddRange)).setText(
					Long.toString(min) + " - " +
							Long.toString(max) + " (" +
							Long.toString(range) + ")");
		} catch (DException e) {
			((TextView)view.findViewById(R.id.ddRange)).setText(R.string.lblCannotEvaluate);
		}
		
		return view;
	}

	@Override
	protected boolean onPrepareOptionsMenu(MenuAdapter adapter) {
		
		adapter.findItem(R.id.mdDetails).setVisible(false); //No longer needed.
		adapter.findItem(R.id.mdRoll).setVisible(false); //Not really useful
		
		if (diceBag.getDice().size() == 1) {
			//Only one element
			//adapter.findItem(R.id.mdRemove).setVisible(false);
			//adapter.findItem(R.id.mdMoveTo).setVisible(false);
			adapter.findItem(R.id.mdRemove).setEnabled(false);
			adapter.findItem(R.id.mdMoveTo).setEnabled(false);
		}
		if (diceBag.getDice().size() >= QuickDiceApp.getInstance().getPreferences().getMaxDice()) {
			//Maximum number of allowed dice reached
			//adapter.findItem(R.id.mdAddHere).setVisible(false);
			//adapter.findItem(R.id.mdClone).setVisible(false);
			adapter.findItem(R.id.mdAddHere).setEnabled(false);
			adapter.findItem(R.id.mdClone).setEnabled(false);
		}
		return super.onPrepareOptionsMenu(adapter);
	}

	protected Drawable getDialogIcon() {
		return QuickDiceApp.getInstance().getGraphic().getResizedDiceIcon(
				expression.getResourceIndex(), 32, 32);
	}
}
