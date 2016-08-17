package ohm.quickdice.dialog;

import ohm.quickdice.QuickDiceApp;
import ohm.quickdice.R;
import ohm.quickdice.adapter.MenuAdapter;
import ohm.quickdice.entity.DiceBag;
import ohm.quickdice.entity.Variable;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

public class VariableDetailDialog extends MenuDialog
		implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {

	DiceBag diceBag;
	int varIndex;
	Variable variable;
	
	int newVal;
	TextView lvlValue;
	SeekBar sbValue;
	ImageButton cmdIncrease;
	ImageButton cmdDecrease;
	boolean asModifier;
	SparseBooleanArray hiddenMenuItems;

//	public VariableDetailDialog(Activity activity, Menu menu, DiceBag diceBag, int variableIndex) {
//		this(activity, menu, diceBag, variableIndex, false);
//	}

	public VariableDetailDialog(Activity activity, Menu menu, DiceBag diceBag, int variableIndex, boolean treatAsModifier, SparseBooleanArray hiddenMenuItems) {
		super(activity, menu);
		this.diceBag = diceBag;
		this.varIndex = variableIndex;
		this.variable = diceBag.getVariables().get(varIndex);
		this.asModifier = treatAsModifier;
		this.hiddenMenuItems = hiddenMenuItems;
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
			view.findViewById(R.id.lblDescription).setVisibility(View.GONE);
			view.findViewById(R.id.lblDescriptionLabel).setVisibility(View.GONE);
		} else {
			((TextView)view.findViewById(R.id.lblDescription)).setText(variable.getDescription());
		}
		//newVal = variable.getCurVal();
		lvlValue = (TextView)view.findViewById(R.id.lblLabel);
		//refreshValue();
		sbValue = (SeekBar)view.findViewById(R.id.sbValue);
		
		sbValue.setMax(variable.getMaxVal() - variable.getMinVal());
		//sbValue.setProgress(variable.getCurVal() - variable.getMinVal());
		sbValue.setOnSeekBarChangeListener(this);

		setValue(variable.getCurVal());

		cmdIncrease = (ImageButton)view.findViewById(R.id.cmdIncrease);
		cmdIncrease.setOnClickListener(this);

		cmdDecrease = (ImageButton)view.findViewById(R.id.cmdDecrease);
		cmdDecrease.setOnClickListener(this);
		
		return view;
	}
	
	private void setValue(int value) {
		newVal = value;

		sbValue.setProgress(newVal - variable.getMinVal());

		refreshValue();
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
		if (asModifier) {
			//menu_modifier.xml

			MenuItem item;
			int key;
			for (int i=0; i < hiddenMenuItems.size(); i++) {
				key = hiddenMenuItems.keyAt(i);
				item = adapter.findItem(key);
				if (item != null) {
					item.setVisible(hiddenMenuItems.get(key));
				}
			}
		} else {
			//menu_variable.xml
			if (!QuickDiceApp.getInstance().canAddVariable()) {
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
		}
		return super.onPrepareOptionsMenu(adapter);
	}
	
//	protected Drawable getDialogIcon() {
//		return QuickDiceApp.getInstance().getBagManager().getIconDrawable(
//				variable.getResourceIndex(), 32, 32);
//	}


	@Override
	public void onItemClick(MenuAdapter parent, View view, int row, int column, long id) {
		if (asModifier) {
			//Update variable value because it can be used by menu item.
			variable.setCurVal(newVal);
		}
		super.onItemClick(parent, view, row, column, id);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.cmdIncrease) {
			if (newVal < variable.getMaxVal()) {
				setValue(newVal + 1);
			}
		} else if (view.getId() == R.id.cmdDecrease) {
			if (newVal > variable.getMinVal()) {
				setValue(newVal - 1);
			}
		}
	}

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
