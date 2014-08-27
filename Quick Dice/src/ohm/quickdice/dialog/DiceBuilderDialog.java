/**
 * 
 */
package ohm.quickdice.dialog;

import net.londatiga.android.ActionItem;
import net.londatiga.android.PopupMenu;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.NumericWheelAdapter;
import kankan.wheel.widget.adapters.WheelViewAdapter;
import ohm.quickdice.R;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * @author Ohmnibus
 *
 */
public class DiceBuilderDialog extends BuilderDialogBase {
	
	View rootView;
	WheelView timeWheel;
	WheelView faceWheel;
	WheelView modsWheel;
	
	public DiceBuilderDialog(Context context, View view, ReadyListener readyListener) {
		super(context, view, readyListener);
	}

	@SuppressLint("InflateParams")
	@Override
	protected void setupDialog(AlertDialog dialog) {
		Context context = dialog.getContext();
		LayoutInflater inflater = LayoutInflater.from(context);

		rootView = inflater.inflate(R.layout.dice_builder_dialog, null);
		
		dialog.setView(rootView);
		
		dialog.setTitle(R.string.lblDiceBuilder);

		timeWheel = initWheel(context, R.id.dbDiceRollTimes, 0, new NumericWheelAdapter(context, 1, 10));
		faceWheel = initWheel(context, R.id.dbDiceFaces, 0, new DiceWheelAdapter(context));
		modsWheel = initWheel(context, R.id.dbDiceModifiers, 0, new ModifierWheelAdapter(context, -50, 50));
		
		modsWheel.setCurrentItem(50);
		
		dialog.getWindow().setLayout(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT);
	}

	@Override
	protected int getActionType() {
		return BuilderDialogBase.ACTION_EDIT;
	}

	@Override
	protected boolean checkExpression() {
		return true;
	}

	@Override
	protected String getExpression() {
		String diceExpression;

		int times = timeWheel.getCurrentItem() + 1;
		int faces = ((DiceWheelAdapter)faceWheel.getViewAdapter()).getItemValue(faceWheel.getCurrentItem());
		int modifier = ((ModifierWheelAdapter)modsWheel.getViewAdapter()).getItemValue(modsWheel.getCurrentItem());

		diceExpression = Integer.toString(times) +
			"d" + Integer.toString(faces);
		
		if (modifier > 0) {
			diceExpression += "+" + Integer.toString(modifier);
		} else if (modifier < 0) {
			diceExpression += Integer.toString(modifier);
		}
		
		return diceExpression;
	}

//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		View mView = getLayoutInflater().inflate(R.layout.dice_builder_dialog, null);
//		
//		setView(mView);
//		
//		setTitle(R.string.lblDiceBuilder);
//		setButton(BUTTON_POSITIVE, this.getContext().getString(R.string.lblOk), this);
//		setButton(BUTTON_NEGATIVE, this.getContext().getString(R.string.lblCancel), this);
//		
//		super.onCreate(savedInstanceState);
//
//		timeWheel = initWheel(R.id.dbDiceRollTimes, 0, new NumericWheelAdapter(getContext(), 1, 10));
//		faceWheel = initWheel(R.id.dbDiceFaces, 0, new DiceWheelAdapter(getContext()));
//		modsWheel = initWheel(R.id.dbDiceModifiers, 0, new ModifierWheelAdapter(getContext(), -50, 50));
//		
//		modsWheel.setCurrentItem(50);
//		
//		getWindow().setLayout(
//				WindowManager.LayoutParams.WRAP_CONTENT,
//				WindowManager.LayoutParams.WRAP_CONTENT);
//	}

	/**
	 * Initializes wheel
	 * @param id the wheel widget Id
	 */
	private WheelView initWheel(Context context, int id, int label, WheelViewAdapter adapter) {
		WheelView wheel = getWheel(id);
		//wheel.setViewAdapter(new NumericWheelAdapter(getContext(), minValue, maxValue));
		wheel.setViewAdapter(adapter);
		if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			wheel.setVisibleItems(3);
		} else {
			wheel.setVisibleItems(5);
		}
		//wheel.setLabel(this.getContext().getString(label));
		wheel.setCurrentItem(0);
		
		return wheel;
	}

	/**
	 * Returns wheel by Id
	 * @param id the wheel Id
	 * @return the wheel with passed Id
	 */
	private WheelView getWheel(int id) {
		return (WheelView)rootView.findViewById(id);
	}

	protected class DiceWheelAdapter extends NumericWheelAdapter {
		
		protected int[] diceFaces = new int[] {
				2,
				3,
				4,
				5,
				6,
				8,
				10,
				12,
				20,
				30,
				100,
				1000
		};

		public DiceWheelAdapter(Context context) {
			super(context);
		}

		/* (non-Javadoc)
		 * @see kankan.wheel.widget.adapters.NumericWheelAdapter#getItemText(int)
		 */
		@Override
		public CharSequence getItemText(int index) {
			if (index >= 0 && index < getItemsCount()) {
				if (diceFaces[index] == 100) {
					return "%";
				} else if (diceFaces[index] == 1000) {
					return "‰";
				} else {
					return Integer.toString(diceFaces[index]);
				}
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see kankan.wheel.widget.adapters.NumericWheelAdapter#getItemsCount()
		 */
		@Override
		public int getItemsCount() {
			return diceFaces.length;
		}
		
		public int getItemValue(int index) {
			int retVal;
			if (index >= 0 && index < getItemsCount()) {
				retVal = diceFaces[index];
			} else {
				retVal = 0;
			}
			return retVal;
		}
	}
	
	protected class ModifierWheelAdapter extends NumericWheelAdapter {
		// Index of current item
		int currentItem;
		int minValue;

		public ModifierWheelAdapter(Context context, int minValue, int maxValue) {
			super(context, minValue, maxValue);
			this.minValue = minValue;
		}


		@Override
		protected void configureTextView(TextView view) {
			super.configureTextView(view);
			if (getItemValue(currentItem) == 0) {
				view.setTextColor(0xFF888888);
			}
			//view.setTypeface(Typeface.SANS_SERIF);
		}

		/* (non-Javadoc)
		 * @see kankan.wheel.widget.adapters.NumericWheelAdapter#getItemText(int)
		 */
		@Override
		public CharSequence getItemText(int index) {
			if (index >= 0 && index < getItemsCount()) {
				int value = getItemValue(index);
				if (value == 0) {
					return "0";
				} else if (value > 0) {
					return "+" + Integer.toString(value);
				} else {
					return Integer.toString(value);
				}
			}
			return null;
		}
		
		@Override
		public View getItem(int index, View cachedView, ViewGroup parent) {
			currentItem = index;
			return super.getItem(index, cachedView, parent);
		}
		
		public int getItemValue(int index) {
			int retVal;
			if (index >= 0 && index < getItemsCount()) {
				retVal = minValue + index;
			} else {
				retVal = 0;
			}
			return retVal;
		}
	}
	
	/**
	 * Get an {@link ActionItem} that can be used to populate a QuickAction element.<br />
	 * The {@link ActionItem}, if clicked, open the {@link DiceBuilderDialog} and then
	 * invoke the specified {@link ReadyListener} when the dialog is dismissed.
	 * @param context Context
	 * @param parent Reference to the container.
	 * @param readyListener Listener to be invoked when the dialog is dismissed.
	 * @return An {@link ActionItem}
	 */
	public static ActionItem getActionItem(Context context, PopupMenu parent, ReadyListener readyListener){
		ActionItem retVal;
		
		retVal = new ActionItem();
		retVal.setTitle(context.getResources().getString(R.string.lblDiceBuilder));
		retVal.setIcon(context.getResources().getDrawable(R.drawable.ic_dice_builder));
		retVal.setOnClickListener(new DiceBuilderActionItemClickListener(parent, readyListener));

		return retVal;
	}
	
	protected static class DiceBuilderActionItemClickListener implements View.OnClickListener {
		
		PopupMenu parent;
		ReadyListener readyListener;
		
		public DiceBuilderActionItemClickListener(PopupMenu parent, ReadyListener readyListener) {
			this.parent = parent;
			this.readyListener = readyListener;
		}

		@Override
		public void onClick(View v) {
			View refView = parent != null ? parent.getAnchor() : v;
			new DiceBuilderDialog(
					refView.getContext(), 
					refView, 
					readyListener).show(); //.getDialog().show();
			if (parent != null) {
				parent.dismiss();
			}
		}
	}
}
