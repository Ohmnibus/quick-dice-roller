package ohm.quickdice.dialog;

import net.londatiga.android.ActionItem;
import net.londatiga.android.PopupMenu;
import ohm.quickdice.QuickDiceApp;
import ohm.quickdice.R;
import ohm.quickdice.adapter.VariableAdapter;
import ohm.quickdice.control.DiceBagManager;
import ohm.quickdice.dialog.BuilderDialogBase.OnDiceBuiltListener;
import ohm.quickdice.entity.Variable;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

public class VariablePickerDialog extends AlertDialog implements DialogInterface.OnClickListener {

	/**
	 * Open the activity to select an existing dice.
	 */
	public static final int DIALOG_SELECT_VARIABLE = 0x00FA7E0A;

	//public static final int ITEM_UNDEFINED = -1;

	Context context;
	int titleId;
	int curIndex;
	Variable curItem;
	int requestType;
	OnItemSelectedListener<Variable> onItemSelectedListener;

	ListView listView;
	VariableAdapter adapter;

	/**
	 * Listener for item selection on picker.
	 * @author Ohmnibus
	 *
	 */
	public interface OnItemSelectedListener<T> {
		
		/** Index of undefined item */
		public static final int ITEM_UNDEFINED = -1;
		
		/**
		 * Invoked upon an item picker selection.
		 * @param confirmed {@code true} is the selection was confirmed, {@code false} otherwise.
		 * @param itemId Index of the selected item, or {@code ITEM_UNDEFINED} if {@code confirm} is {@code false}.
		 * @param item Selected item, or {@code null} if {@code confirm} is {@code false}.
		 */
		public void onItemSelected(boolean confirmed, int itemId, T item);
	}

	public VariablePickerDialog(Context context, OnItemSelectedListener<Variable> listener) {
		this(context, DIALOG_SELECT_VARIABLE, listener);
	}

	public VariablePickerDialog(Context context, int requestType, OnItemSelectedListener<Variable> listener) {
		this(context, R.string.lblSelectVariable, OnItemSelectedListener.ITEM_UNDEFINED, requestType, listener);
	}

	public VariablePickerDialog(Context context, int titleId, int currentItem, int requestType, OnItemSelectedListener<Variable> listener) {
		super(context);

		this.curIndex = currentItem;
		this.context = context;
		this.titleId = titleId;
		this.requestType = requestType;
		this.onItemSelectedListener = listener;
	}

	@SuppressLint("InflateParams")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		View mView = getLayoutInflater().inflate(R.layout.dialog_variable_picker, null);

		setView(mView);

		setTitle(this.titleId);
		setButton(BUTTON_POSITIVE, this.getContext().getString(R.string.lblOk), this);
		setButton(BUTTON_NEGATIVE, this.getContext().getString(R.string.lblCancel), this);

		super.onCreate(savedInstanceState);

		initViews();
	}

	private void initViews() {

		DiceBagManager diceBagManager = QuickDiceApp.getInstance().getBagManager();

		adapter = new VariableAdapter(
				context,
				R.layout.item_variable,
				diceBagManager.getCurrent().getVariables());

		if (curIndex != OnItemSelectedListener.ITEM_UNDEFINED) {
			//selectItem(listView, curItem);
			adapter.setSelected(curIndex);
			curItem = adapter.getItem(curIndex);
		}

		listView = (ListView)findViewById(R.id.lvVariables);
		
		//if (requestType == DIALOG_SELECT_VARIABLE) {
		listView.setAdapter(adapter);
		//}
		listView.setOnItemClickListener(listItemClickListener);

		getWindow().setLayout(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT);
	}


	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (onItemSelectedListener != null) {
			if (which == DialogInterface.BUTTON_POSITIVE && curItem != null) {
				onItemSelectedListener.onItemSelected(true, curIndex, curItem);
			} else {
				onItemSelectedListener.onItemSelected(false, OnItemSelectedListener.ITEM_UNDEFINED, null);
			}
		}
		dismiss();
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

//	protected void selectItem(ListView listView, int item) {
////		long packedPosition = ExpandableListView.getPackedPositionForChild(group, item);
////		int position = expList.getFlatListPosition(packedPosition);
////		expList.setItemChecked(position, true);
//		//listView.setItemChecked(item, true);
//		
////		listView.setSelection(item);
////		listView.post(new Runnable() {
////			public void run() {
////				listView.requestFocus();
////				listView.setSelection(item);
////			}
////		});
//		adapter.setSelected(item);
//		adapter.notifyDataSetChanged();
//	}

	/**
	 * Get an {@link ActionItem} that can be used to populate a QuickAction element.<br />
	 * The {@link ActionItem}, if clicked, open the {@link VariablePickerDialog} and then
	 * invoke the specified {@link ReadyListener} when the dialog is dismissed.
	 * @param context Context
	 * @param parent Reference to the container.
	 * @param diceBuiltListener Listener to be invoked when the dialog is dismissed.
	 * @return An {@link ActionItem}
	 */
	public static ActionItem getActionItem(Context context, PopupMenu parent, OnDiceBuiltListener diceBuiltListener){
		ActionItem retVal;
		
		if (QuickDiceApp.getInstance().getBagManager().getCurrent().getVariables().size() > 0) {
			retVal = new ActionItem();
			retVal.setTitle(context.getResources().getString(R.string.lblVariables));
			retVal.setIcon(context.getResources().getDrawable(R.drawable.ic_var));
			retVal.setOnClickListener(new VariablePickerActionItemClickListener(parent, diceBuiltListener));
		} else {
			retVal = null;
		}

		return retVal;
	}
	
	protected static class VariablePickerActionItemClickListener implements View.OnClickListener, OnItemSelectedListener<Variable> {
		
		private PopupMenu parent;
		private OnDiceBuiltListener diceBuiltListener;
		private View refView;
		
		public VariablePickerActionItemClickListener(PopupMenu parent, OnDiceBuiltListener diceBuiltListener) {
			this.parent = parent;
			this.diceBuiltListener = diceBuiltListener;
		}

		@Override
		public void onClick(View v) {
			refView = parent != null ? parent.getAnchor() : v;
			
			new VariablePickerDialog(refView.getContext(), this).show();
			
			if (parent != null) {
				parent.dismiss();
			}
		}

		@Override
		public void onItemSelected(boolean confirmed, int itemId, Variable item) {
			diceBuiltListener.onDiceBuilt(
					refView,
					confirmed,
					BuilderDialogBase.ACTION_EDIT,
					item == null ? null : item.getLabel());
		}
	}
}
