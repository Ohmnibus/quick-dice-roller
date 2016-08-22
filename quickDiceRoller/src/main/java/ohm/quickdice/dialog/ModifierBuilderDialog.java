package ohm.quickdice.dialog;

import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;
import kankan.wheel.widget.adapters.NumericWheelAdapter;
import ohm.quickdice.QuickDiceApp;
import ohm.quickdice.R;
import ohm.quickdice.adapter.VariableAdapter;
import ohm.quickdice.control.DiceBagManager;
import ohm.quickdice.entity.PercentModifier;
import ohm.quickdice.entity.RollModifier;
import ohm.quickdice.entity.VarModifier;
import ohm.quickdice.entity.Variable;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;


public class ModifierBuilderDialog extends NumberPickerDialog
		implements NumberPickerDialog.OnNumberPickedListener,
		AdapterView.OnItemSelectedListener,
		AdapterView.OnItemClickListener {

	public static final int POSITION_UNDEFINED = -1;

	public static final int TYPE_MODIFIER = RollModifier.TYPE_ID;
	public static final int TYPE_VARIABLE = VarModifier.TYPE_ID;
	public static final int TYPE_PERCENTAGE = PercentModifier.TYPE_ID;

	View panelMod;
	View panelVar;
	View panelPer;
	//RadioGroup radioGroup;
	Spinner spinner;
	int type = TYPE_MODIFIER;

	int curIndex; // = OnItemSelectedListener.ITEM_UNDEFINED;
	Variable curItem;
	ListView listView;
	VariableAdapter adapter;
	//OnItemSelectedListener<Variable> onItemSelectedListener;

	int position;
	OnCreatedListener onCreatedListener;

	public interface OnCreatedListener {
		void onCreated(boolean confirmed, int position, int type, int value, String label);
	}

	/**
	 * Initialize a builder with given parameters.
	 * @param context Context
	 * @param readyListener Callback listener.
	 */
	public ModifierBuilderDialog(Context context, OnCreatedListener readyListener) {
		this(context, POSITION_UNDEFINED, readyListener);
	}

	/**
	 * Initialize a builder with given parameters.
	 * @param context Context
	 * @param position Position on which the item should be added. This value is used by the caller.
	 * @param onCreatedListener Callback listener.
	 */
	public ModifierBuilderDialog(Context context, int position, OnCreatedListener onCreatedListener) {
		super(context, R.string.lblModifierBuilder, R.string.lblModifierBuilderMessage, 0, 2, null);

		super.onNumberPickedListener = this;
		this.position = position;
		this.onCreatedListener = onCreatedListener;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (type == TYPE_VARIABLE) {
			//Variable page
			variablePicked(which);
		} else if (type == TYPE_PERCENTAGE) {
			//Percentage page
			percentagePicked(which);
		} else {
			//Modifier page
			super.onClick(dialog, which);
		}
	}

	public void variablePicked(int whichButton) {
		if (whichButton == DialogInterface.BUTTON_POSITIVE && curItem != null) {
			onVariablePicked(true, curItem.getLabel());
		} else {
			onVariablePicked(false, "");
		}
		dismiss();
	}

	public void percentagePicked(int whichButton) {
		int value;
		if (whichButton == DialogInterface.BUTTON_POSITIVE) {
			//The dialog has been confirmed
			int sign = signPercWheel.getCurrentItem() == 0 ? 1 : -1;
			int hund = hundPercWheel.getCurrentItem();
			int tens = tensPercWheel.getCurrentItem();
			int unit = unitPercWheel.getCurrentItem();

			value = sign * ((hund * 100) + (tens * 10) + unit);
		} else {
			value = 0;
		}
		if (onNumberPickedListener != null) {
			onNumberPickedListener.onNumberPicked(whichButton == DialogInterface.BUTTON_POSITIVE, value);
		}
		dismiss();
	}

//	@Override
//	public void onCheckedChanged(RadioGroup radioGroup, int i) {
//		if (i == R.id.rbModifier) {
//			panelMod.setVisibility(View.VISIBLE);
//			panelVar.setVisibility(View.GONE);
//		} else {
//			panelMod.setVisibility(View.GONE);
//			panelVar.setVisibility(View.VISIBLE);
//		}
//	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		curIndex = position;
		curItem = adapter.getItem(curIndex);
		//v.setSelected(true);
		//selectItem((ListView)parent, curItem);
		//((VariableAdapter)parent.getAdapter()).setSelected(position);
		adapter.setSelected(position);
	}

	@Override
	public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
		switch (i) {
			case 1:
				type = TYPE_VARIABLE;
				break;
			case 2:
				type = TYPE_PERCENTAGE;
				break;
			default:
				type = TYPE_MODIFIER;
				break;
		}
		panelMod.setVisibility(type == TYPE_MODIFIER ? View.VISIBLE : View.GONE);
		panelVar.setVisibility(type == TYPE_VARIABLE ? View.VISIBLE : View.GONE);
		panelPer.setVisibility(type == TYPE_PERCENTAGE ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onNothingSelected(AdapterView<?> adapterView) {

	}

	@Override
	public void onNumberPicked(boolean confirmed, int value) {
		if (onCreatedListener != null) {
			onCreatedListener.onCreated(confirmed, position, type, value, null);
		}
	}

	public void onVariablePicked(boolean confirmed, String label) {
		if (onCreatedListener != null) {
			onCreatedListener.onCreated(confirmed, position, type, 0, label);
		}
	}

	@Override
	protected View initHead() {
		//super.initHead();
		View mView = getLayoutInflater().inflate(R.layout.dialog_add_modifier, null);

		setView(mView);

		setTitle(title);
		//((TextView)mView.findViewById(R.id.lblMessage)).setText(message);
		setButton(BUTTON_POSITIVE, this.getContext().getString(R.string.lblOk), this);
		setButton(BUTTON_NEGATIVE, this.getContext().getString(R.string.lblCancel), this);

		return mView;
	}

	@Override
	protected void initViews(View root) {

		panelMod = findViewById(R.id.panelModifiers);
		panelVar = findViewById(R.id.panelVariables);
		panelPer = findViewById(R.id.panelPercentage);

		super.initViews(panelMod);
		((TextView)panelMod.findViewById(R.id.lblMessage)).setText(R.string.lblModifierBuilderMessage);

		initVarViews(panelVar);

		initPercViews(panelPer, 0);

		spinner = (Spinner) findViewById(R.id.spnType);

		ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(
				getContext(),
				R.array.mod_type_array,
				android.R.layout.simple_spinner_item);

		typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spinner.setAdapter(typeAdapter);
		spinner.setOnItemSelectedListener(this);
		spinner.setSelection(0, false);
//		radioGroup = (RadioGroup)findViewById(R.id.rbgMain);
//
//		radioGroup.setOnCheckedChangeListener(this);

		getWindow().setLayout(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT);
	}

	protected void initVarViews(View root) {
		TextView lbl;
		DiceBagManager diceBagManager = QuickDiceApp.getInstance().getBagManager();

		adapter = new VariableAdapter(
				getContext(),
				R.layout.item_variable,
				diceBagManager.getCurrent().getVariables());

//		if (curIndex != OnItemSelectedListener.ITEM_UNDEFINED) {
//			//selectItem(listView, curItem);
//			adapter.setSelected(curIndex);
//			curItem = adapter.getItem(curIndex);
//		}

		listView = (ListView)root.findViewById(R.id.lvVariables);
		//listView = (ListView)findViewById(R.id.panelVariables);

		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);

		lbl = (TextView)root.findViewById(R.id.lblMessage);
		lbl.setText(R.string.lblVarModifierBuilderMessage);
	}

	WheelView signPercWheel;
	WheelView hundPercWheel;
	WheelView tensPercWheel;
	WheelView unitPercWheel;

	protected void initPercViews(View root, int defaultValue) {
		int curSign;
		int cur100s;
		int curTens;
		int curUnits;
		TextView lbl;

		if (defaultValue >= 0) {
			curSign = 0;
			cur100s = (defaultValue / 100) % 10;
			curTens = (defaultValue / 10) % 10;
			curUnits = defaultValue % 10;
		} else {
			curSign = 1;
			cur100s = ((-defaultValue) / 100) % 10;
			curTens = ((-defaultValue) / 10) % 10;
			curUnits = (-defaultValue) % 10;
		}

		signPercWheel = initWheel(root, R.id.wheelSign, curSign, new ArrayWheelAdapter<String>(getContext(), new String[] {"+", "-"}));
		hundPercWheel = initWheel(root, R.id.wheelHundreds, cur100s, new NumericWheelAdapter(getContext(), 0, 9));
		tensPercWheel = initWheel(root, R.id.wheelTens, curTens, new NumericWheelAdapter(getContext(), 0, 9));
		unitPercWheel = initWheel(root, R.id.wheelUnits, curUnits, new NumericWheelAdapter(getContext(), 0, 9));

		setWheelSize(signPercWheel);
		setWheelSize(hundPercWheel);
		setWheelSize(tensPercWheel);
		setWheelSize(unitPercWheel);

		lbl = (TextView)root.findViewById(R.id.lblMessage);
		lbl.setText(R.string.lblPercModifierBuilderMessage);

		lbl = (TextView) root.findViewById(R.id.lblType);
		lbl.setText("%");
		lbl.setVisibility(View.VISIBLE);
	}

	protected void setWheelSize(WheelView wheel) {
		ViewGroup.LayoutParams lp = wheel.getLayoutParams();
		if (lp.width > 0) {
			lp.width = (lp.width * 7) / 8;
		}
		wheel.setLayoutParams(lp);
	}

}
