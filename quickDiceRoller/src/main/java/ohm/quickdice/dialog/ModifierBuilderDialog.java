package ohm.quickdice.dialog;

import ohm.quickdice.QuickDiceApp;
import ohm.quickdice.R;
import ohm.quickdice.adapter.VariableAdapter;
import ohm.quickdice.control.DiceBagManager;
import ohm.quickdice.entity.Variable;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

public class ModifierBuilderDialog extends NumberPickerDialog implements NumberPickerDialog.OnNumberPickedListener, RadioGroup.OnCheckedChangeListener {

	public static final int POSITION_UNDEFINED = -1;

	View panelMod;
	View panelVar;
	RadioGroup radioGroup;

	int curIndex; // = OnItemSelectedListener.ITEM_UNDEFINED;
	Variable curItem;
	ListView listView;
	VariableAdapter adapter;
	//OnItemSelectedListener<Variable> onItemSelectedListener;

	int position;
	OnCreatedListener onCreatedListener;

	public interface OnCreatedListener {
		void onCreated(boolean confirmed, int position, int modifier, String label);
	}

//	/**
//	 * Listener for item selection on picker.
//	 * @author Ohmnibus
//	 *
//	 */
//	public interface OnItemSelectedListener<T> {
//
//		/** Index of undefined item */
//		int ITEM_UNDEFINED = -1;
//
//		/**
//		 * Invoked upon an item picker selection.
//		 * @param confirmed {@code true} is the selection was confirmed, {@code false} otherwise.
//		 * @param itemId Index of the selected item, or {@code ITEM_UNDEFINED} if {@code confirm} is {@code false}.
//		 * @param item Selected item, or {@code null} if {@code confirm} is {@code false}.
//		 */
//		void onItemSelected(boolean confirmed, int itemId, T item);
//	}
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

//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		//super.onCreate(savedInstanceState);
//		View mView = getLayoutInflater().inflate(R.layout.dialog_add_modifier, null);
//
//		setView(mView);
//
//		setTitle(title);
//		((TextView)mView.findViewById(R.id.lblMessage)).setText(message);
//		setButton(BUTTON_POSITIVE, this.getContext().getString(R.string.lblOk), this);
//		setButton(BUTTON_NEGATIVE, this.getContext().getString(R.string.lblCancel), this);
//
//		super.onCreate(savedInstanceState);
//
//		initViews();
//	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (radioGroup.getCheckedRadioButtonId() == R.id.rbModifier) {
			//Modifier page
			super.onClick(dialog, which);
		} else {
			//Variable page
			//if (onItemSelectedListener != null) {
				if (which == DialogInterface.BUTTON_POSITIVE && curItem != null) {
					//onItemSelectedListener.onItemSelected(true, curIndex, curItem);
					onVariablePicked(true, curItem.getLabel());
				} else {
					//onItemSelectedListener.onItemSelected(false, OnItemSelectedListener.ITEM_UNDEFINED, null);
					onVariablePicked(false, "");
				}
			//}
			dismiss();
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup radioGroup, int i) {
		if (i == R.id.rbModifier) {
			panelMod.setVisibility(View.VISIBLE);
			panelVar.setVisibility(View.GONE);
		} else {
			panelMod.setVisibility(View.GONE);
			panelVar.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onNumberPicked(boolean confirmed, int value) {
		if (onCreatedListener != null) {
			onCreatedListener.onCreated(confirmed, position, value, null);
		}
	}

	public void onVariablePicked(boolean confirmed, String label) {
		if (onCreatedListener != null) {
			onCreatedListener.onCreated(confirmed, position, 0, label);
		}
	}

	@Override
	protected void initHead() {
		//super.initHead();
		View mView = getLayoutInflater().inflate(R.layout.dialog_add_modifier, null);

		setView(mView);

		setTitle(title);
		((TextView)mView.findViewById(R.id.lblMessage)).setText(message);
		setButton(BUTTON_POSITIVE, this.getContext().getString(R.string.lblOk), this);
		setButton(BUTTON_NEGATIVE, this.getContext().getString(R.string.lblCancel), this);
	}

	@Override
	protected void initViews() {

		super.initViews();

		panelMod = findViewById(R.id.panelModifiers);
		panelVar = findViewById(R.id.panelVariables);

		radioGroup = (RadioGroup)findViewById(R.id.rbgMain);

		radioGroup.setOnCheckedChangeListener(this);

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

		//listView = (ListView)findViewById(R.id.lvVariables);
		listView = (ListView)findViewById(R.id.panelVariables);

		//if (requestType == DIALOG_SELECT_VARIABLE) {
		listView.setAdapter(adapter);
		//}
		listView.setOnItemClickListener(listItemClickListener);

		getWindow().setLayout(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT);
	}

	AdapterView.OnItemClickListener listItemClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
			curIndex = position;
			curItem = adapter.getItem(curIndex);
			//v.setSelected(true);
			//selectItem((ListView)parent, curItem);
			((VariableAdapter)parent.getAdapter()).setSelected(position);
		}
	};


}
