package ohm.quickdice.dialog;

import ohm.quickdice.QuickDiceApp;
import ohm.quickdice.R;
import ohm.quickdice.adapter.MenuAdapter;
import ohm.quickdice.entity.DiceBag;
import ohm.quickdice.entity.Variable;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

public class VariableDetailDialog extends MenuDialog implements SeekBar.OnSeekBarChangeListener {

	DiceBag diceBag;
	int varIndex;
	Variable variable;
	
	int newVal;
	TextView lvlValue;
	SeekBar sbValue;
	
	public VariableDetailDialog(Activity activity, Menu menu, DiceBag diceBag, int variableIndex) {
		super(activity, menu);
		this.diceBag = diceBag;
		this.varIndex = variableIndex;
		this.variable = diceBag.getVariables().get(varIndex);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		setTitle(variable.getName());

		//setIcon(getDialogIcon());
		setIcon(QuickDiceApp.getInstance().getBagManager().getIconCollection().getByID(variable.getResourceIndex()));

		super.onCreate(savedInstanceState);
	}
	

	@Override
	protected View getHeaderView(LayoutInflater inflater, ListView parent) {
		View view = inflater.inflate(R.layout.dialog_variable_detail, parent, false);

		if (variable.getDescription() == null || variable.getDescription().length() == 0) {
			((TextView)view.findViewById(R.id.lblDescription)).setVisibility(View.GONE);
			((TextView)view.findViewById(R.id.lblDescriptionLabel)).setVisibility(View.GONE);
		} else {
			((TextView)view.findViewById(R.id.lblDescription)).setText(variable.getDescription());
		}
		newVal = variable.getCurVal();
		lvlValue = (TextView)view.findViewById(R.id.lblLabel);
		refreshValue();
		sbValue = (SeekBar)view.findViewById(R.id.sbValue);
		
		sbValue.setMax(variable.getMaxVal() - variable.getMinVal());
		sbValue.setProgress(variable.getCurVal() - variable.getMinVal());
		sbValue.setOnSeekBarChangeListener(this);
		
		return view;
	}
	
	private void refreshValue() {
		String lbl;
		lbl = lvlValue.getContext().getString(R.string.lblLabelValueVarFmt,
				variable.getLabel(),
				newVal, //variable.getCurVal(),
				variable.getMinVal(),
				variable.getMaxVal());
		lvlValue.setText(lbl);
	}
	
	@Override
	protected boolean onPrepareOptionsMenu(MenuAdapter adapter) {
		
		if (! QuickDiceApp.getInstance().canAddVariable()) {
			//Maximum number of allowed variables reached
			adapter.findItem(R.id.mvAddHere).setEnabled(false);
			//adapter.findItem(R.id.mdClone).setEnabled(false);
		}
		if (varIndex == 0) {
			adapter.findItem(R.id.mvSwitchPrev).setEnabled(false);
		}
		if (varIndex == diceBag.getVariables().size() - 1) {
			adapter.findItem(R.id.mvSwitchNext).setEnabled(false);
		}
		return super.onPrepareOptionsMenu(adapter);
	}
	
//	protected Drawable getDialogIcon() {
//		return QuickDiceApp.getInstance().getBagManager().getIconDrawable(
//				variable.getResourceIndex(), 32, 32);
//	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (fromUser) {
			newVal = progress + variable.getMinVal();
			refreshValue();
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		//if (newVal != variable.getCurVal()) {
		//QuickDiceApp.getInstance().getBagManager().setVariableValue(varIndex, newVal);
		variable.setCurVal(newVal);
		//}
		super.onDismiss(dialog);
	}
}
