package ohm.quickdice.dialog;

import ohm.quickdice.QuickDiceApp;
import ohm.quickdice.R;
import ohm.quickdice.adapter.ExpDiceBagAdapter;
import ohm.quickdice.adapter.ExpDiceBagAdapterDest;
import ohm.quickdice.control.DiceBagManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;

public class DicePickerDialog extends AlertDialog implements DialogInterface.OnClickListener {

	/**
	 * Open the activity to select an existing dice.
	 */
	public static final int DIALOG_SELECT_DICE = 0x00D1CE0A;
	/**
	 * Open the activity to select a dice position.<br />
	 * This option add the option "move dice to the end".
	 */
	public static final int DIALOG_SELECT_DESTINATION = 0x00D1CE0B;

	public static final int GROUP_UNDEFINED = -1;
	public static final int ITEM_UNDEFINED = -1;

	Context context;
	int titleId;
	int curGroup;
	int curItem;
	int requestType;
	ReadyListener readyListener;
	
	ExpandableListView expListView;

	public interface ReadyListener {
        public void ready(boolean confirmed, int groupId, int itemId);
    }

	public DicePickerDialog(Context context, ReadyListener readyListener) {
		this(context, DIALOG_SELECT_DICE, readyListener);
    }

	public DicePickerDialog(Context context, int requestType, ReadyListener readyListener) {
		this(context, R.string.lblSelectDice, GROUP_UNDEFINED, ITEM_UNDEFINED, requestType, readyListener);
    }

	public DicePickerDialog(Context context, int titleId, int currentGroup, int currentItem, int requestType, ReadyListener readyListener) {
		super(context);

		this.curGroup = currentGroup;
		this.curItem = currentItem;
		this.context = context;
		this.titleId = titleId;
		this.requestType = requestType;
		this.readyListener = readyListener;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        View mView = getLayoutInflater().inflate(R.layout.dice_picker_dialog, null);
        
        setView(mView);
		
        setTitle(this.titleId);
		setButton(BUTTON_POSITIVE, this.getContext().getString(R.string.lblOk), this);
        setButton(BUTTON_NEGATIVE, this.getContext().getString(R.string.lblCancel), this);
        
        super.onCreate(savedInstanceState);

        initViews();
	}

	private void initViews() {

        DiceBagManager diceBagManager = QuickDiceApp.getInstance().getBagManager();
        
        expListView = (ExpandableListView)findViewById(R.id.expListView);
        if (requestType == DIALOG_SELECT_DESTINATION) {
	        expListView.setAdapter(new ExpDiceBagAdapterDest(
	        		context,
	        		R.layout.dice_picker_group,
	        		R.layout.dice_picker_item,
	        		diceBagManager.getDiceBags()));
        } else {
	        expListView.setAdapter(new ExpDiceBagAdapter(
	        		context,
	        		R.layout.dice_picker_group,
	        		R.layout.dice_picker_item,
	        		diceBagManager.getDiceBags()));
        }
        expListView.setGroupIndicator(null);
        if (curGroup != GROUP_UNDEFINED) {
        	expListView.expandGroup(curGroup);
        	if (curItem != ITEM_UNDEFINED) {
            	//expListView.setSelectedChild(curGroup, curItem, true);
    		    selectItem(expListView, curGroup, curItem);
        	}
        } else {
        	expListView.expandGroup(diceBagManager.getCurrentDiceBag());
        	//expListView.setSelectedChild(curGroup, curItem, true);
        }
        expListView.setOnChildClickListener(expListItemClickListener);
		
        getWindow().setLayout(
        		WindowManager.LayoutParams.WRAP_CONTENT,
        		WindowManager.LayoutParams.WRAP_CONTENT);
	}

	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (readyListener != null) {
			if (which == DialogInterface.BUTTON_POSITIVE) {
				readyListener.ready(true, curGroup, curItem);
			} else {
				readyListener.ready(false, GROUP_UNDEFINED, ITEM_UNDEFINED);
			}
		}
		dismiss();
	}
	
	ExpandableListView.OnChildClickListener expListItemClickListener = new OnChildClickListener() {
		
		@Override
		public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
			curGroup = groupPosition;
			curItem = childPosition;
			//parent.setSelectedChild(curGroup, curItem, true);
		    selectItem(parent, curGroup, curItem);
			return true;
		}
	};
	
	protected void selectItem(ExpandableListView expList, int group, int item) {
		long packedPosition = ExpandableListView.getPackedPositionForChild(group, item);
		int position = expList.getFlatListPosition(packedPosition);
		expList.setItemChecked(position, true);
	}
}
