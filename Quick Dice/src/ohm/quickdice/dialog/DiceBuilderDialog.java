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
	
	public DiceBuilderDialog(Context context, View view, OnDiceBuiltListener diceBuiltListener) {
		super(context, view, diceBuiltListener);
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
		
//		dialog.getWindow().setLayout(
//				WindowManager.LayoutParams.WRAP_CONTENT,
//				WindowManager.LayoutParams.WRAP_CONTENT);
	}

	@Override
	protected int getActionType() {
		return BuilderDialogBase.ACTION_EDIT;
	}

//	@Override
//	protected boolean checkExpression() {
//		return true;
//	}

	@Override
	protected void checkExpression(OnExpressionCheckedListener expressionCheckedListener) {
		expressionCheckedListener.onExpressionChecked(true);
	}
	
	@Override
	protected String getExpression() {
		String diceExpression;

		int times = timeWheel.getCurrentItem() + 1;
		int faces = ((DiceWheelAdapter)faceWheel.getViewAdapter()).getItemValue(faceWheel.getCurrentItem());
		int modifier = ((ModifierWheelAdapter)modsWheel.getViewAdapter()).getItemValue(modsWheel.getCurrentItem());

//		diceExpression = Integer.toString(times) +
//			"d" + Integer.toString(faces);
		diceExpression = Integer.toString(times) +
				rootView.getContext().getString(R.string.lblD) +
				Integer.toString(faces);
		
		if (modifier > 0) {
			diceExpression += "+" + Integer.toString(modifier);
		} else if (modifier < 0) {
			diceExpression += Integer.toString(modifier);
		}
		
		return diceExpression;
	}

	/**
	 * Initializes wheel
	 * @param id the wheel widget Id
	 */
	private WheelView initWheel(Context context, int id, int label, WheelViewAdapter adapter) {
		WheelView wheel = getWheel(id);
		wheel.setViewAdapter(adapter);
		wheel.setVisibleItems(getVisibleItems(context));
		wheel.setCurrentItem(0);
		
		return wheel;
	}
	
	private int getVisibleItems(Context context) {
		int visibleItems = 5;
		if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			//Screen is landscape. Available height may not be enough for 5 items.
			int screenSize = context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
			if (screenSize == Configuration.SCREENLAYOUT_SIZE_UNDEFINED
					|| screenSize == Configuration.SCREENLAYOUT_SIZE_SMALL
					|| screenSize == Configuration.SCREENLAYOUT_SIZE_NORMAL) {
				
				//Screen height is too small for 5 items.
				visibleItems = 3;
			}
		}
		return visibleItems;
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
	 * @param diceBuiltListener Listener to be invoked when the dialog is dismissed.
	 * @return An {@link ActionItem}
	 */
	public static ActionItem getActionItem(Context context, PopupMenu parent, OnDiceBuiltListener diceBuiltListener){
		ActionItem retVal;
		
		retVal = new ActionItem();
		retVal.setTitle(context.getResources().getString(R.string.lblDiceBuilder));
		retVal.setIcon(context.getResources().getDrawable(R.drawable.ic_dice_builder));
		retVal.setOnClickListener(new DiceBuilderActionItemClickListener(parent, diceBuiltListener));

		return retVal;
	}
	
	protected static class DiceBuilderActionItemClickListener implements View.OnClickListener {
		
		PopupMenu parent;
		OnDiceBuiltListener diceBuiltListener;
		
		public DiceBuilderActionItemClickListener(PopupMenu parent, OnDiceBuiltListener diceBuiltListener) {
			this.parent = parent;
			this.diceBuiltListener = diceBuiltListener;
		}

		@Override
		public void onClick(View v) {
			View refView = parent != null ? parent.getAnchor() : v;
			new DiceBuilderDialog(
					refView.getContext(), 
					refView, 
					diceBuiltListener).show();
			if (parent != null) {
				parent.dismiss();
			}
		}
	}
}
