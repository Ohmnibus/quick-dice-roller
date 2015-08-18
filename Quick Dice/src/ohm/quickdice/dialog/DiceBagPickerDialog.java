package ohm.quickdice.dialog;

import java.util.ArrayList;

import ohm.quickdice.R;
import ohm.quickdice.adapter.DiceBagAdapter;
import ohm.quickdice.adapter.DiceBagAdapter.DiceBagSelectorAdapter;
import ohm.quickdice.control.DiceBagManager;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;

public class DiceBagPickerDialog extends AlertDialog implements DialogInterface.OnClickListener {

	public interface OnItemPickedListener {
		public void onItemPicked(boolean confirmed, DiceBagManager data, SparseBooleanArray selected, boolean override);
	}
	
	OnItemPickedListener listener;
	DiceBagManager data;
	ListView listView;
	DiceBagSelectorAdapter diceBagAdapter;
	CheckBox checkBox;

	public DiceBagPickerDialog(Context context, DiceBagManager data, OnItemPickedListener listener) {
		super(context);
		
		this.data = data;
		this.listener = listener;
	}
	
	@SuppressLint("InflateParams")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		View mView = getLayoutInflater().inflate(R.layout.dice_bag_picker_dialog, null);

		setView(mView);

		setTitle(R.string.lblSelectBags);
		setButton(BUTTON_POSITIVE, this.getContext().getString(R.string.lblOk), this);
		setButton(BUTTON_NEGATIVE, this.getContext().getString(R.string.lblCancel), this);

		super.onCreate(savedInstanceState);

		initViews();
	}

	private void initViews() {
		listView = (ListView)findViewById(R.id.lvDiceBag);
		diceBagAdapter = new DiceBagSelectorAdapter(getContext(), R.layout.dice_bag_item, data.getDiceBagCollection());
		listView.setAdapter(diceBagAdapter);
		listView.setOnItemClickListener(diceBagAdapter);
		
		checkBox = (CheckBox)findViewById(R.id.cbOverride);
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (listener != null) {
			if (which == DialogInterface.BUTTON_POSITIVE) {
				SparseBooleanArray selected = diceBagAdapter.getSelected(); // listView.getCheckedItemPositions();
//				selections.
//				ArrayList<Integer> indexes = new ArrayList<Integer>();
//				//int[] indexes = new int[data.getDiceBagCollection().size()];
//				for (int i = 0; i < res.size(); i++) {
//					if (res.valueAt(i) == true) {
//						indexes.add(res.keyAt(i));
//					}
//				}
//				int[] asd = new int[indexes.size()];
//				for (int i = 0; i<indexes.size(); i++){
//					asd[i] = indexes.get(i);
//				}
				listener.onItemPicked(true, data, selected, checkBox.isChecked());
			} else {
				listener.onItemPicked(false, data, null, false);
			}
		}
		dismiss();
	}
}
