package ohm.quickdice.dialog;

import ohm.quickdice.R;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;
import kankan.wheel.widget.adapters.NumericWheelAdapter;
import kankan.wheel.widget.adapters.WheelViewAdapter;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class NumberPickerDialog extends AlertDialog implements OnClickListener {

	private static final int DEFAULT_VALUE = 0;

	String title;
	String message;
	int defaultValue;
	OnNumberPickedListener onNumberPickedListener;

	WheelView signWheel;
	WheelView tensWheel;
	WheelView unitWheel;

	public interface OnNumberPickedListener {
		public void onNumberPicked(boolean confirmed, int value);
	}

	/**
	 * Initialize a picker with given parameters.
	 * @param context Context
	 * @param onNumberPickedListener Callback listener.
	 */
	public NumberPickerDialog(Context context, OnNumberPickedListener onNumberPickedListener) {
		this(context, DEFAULT_VALUE, onNumberPickedListener);
	}

	/**
	 * Initialize a picker with given parameters.
	 * @param context Context
	 * @param defaultValue Default value.
	 * @param onNumberPickedListener Callback listener.
	 */
	public NumberPickerDialog(Context context, int defaultValue, OnNumberPickedListener onNumberPickedListener) {
		this(context, R.string.lblNumberPicker, R.string.lblSelectValue, defaultValue, onNumberPickedListener);
	}

	/**
	 * Initialize a picker with given parameters.
	 * @param context Context
	 * @param defaultValue Default value.
	 * @param onNumberPickedListener Callback listener.
	 */
	public NumberPickerDialog(Context context, int titleResId, int messageResId, int defaultValue, OnNumberPickedListener onNumberPickedListener) {
		this(context, context.getString(titleResId), context.getString(messageResId), defaultValue, onNumberPickedListener);
	}

	/**
	 * Initialize a number picker with given parameters.
	 * @param context Context
	 * @param title Title
	 * @param message Message
	 * @param defaultValue Default value.
	 * @param onNumberPickedListener Callback listener.
	 */
	public NumberPickerDialog(Context context, String title, String message, int defaultValue, OnNumberPickedListener onNumberPickedListener) {
		super(context);
		
		this.onNumberPickedListener = onNumberPickedListener;
		if (defaultValue > 99) {
			this.defaultValue = 99;
		} else if (defaultValue < -99) {
			this.defaultValue = -99;
		} else {
			this.defaultValue = defaultValue;
		}
		this.title = title;
		this.message = message;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		View mView = getLayoutInflater().inflate(R.layout.dialog_number_picker, null);

		setView(mView);

		setTitle(title);
		((TextView)mView.findViewById(R.id.lblMessage)).setText(message);
		setButton(BUTTON_POSITIVE, this.getContext().getString(R.string.lblOk), this);
		setButton(BUTTON_NEGATIVE, this.getContext().getString(R.string.lblCancel), this);

		super.onCreate(savedInstanceState);

		int curSign;
		int curTens;
		int curUnits;

		if (defaultValue >= 0) {
			curSign = 0;
			curTens = (defaultValue / 10) % 10;
			curUnits = defaultValue % 10;
		} else {
			curSign = 1;
			curTens = ((-defaultValue) / 10) % 10;
			curUnits = (-defaultValue) % 10;
		}

		signWheel = initWheel(R.id.wheelSign, curSign, new ArrayWheelAdapter<String>(getContext(), new String[] {"+", "-"}));
		tensWheel = initWheel(R.id.wheelTens, curTens, new NumericWheelAdapter(getContext(), 0, 9));
		unitWheel = initWheel(R.id.wheelUnits, curUnits, new NumericWheelAdapter(getContext(), 0, 9));

//		getWindow().setLayout(
//				WindowManager.LayoutParams.WRAP_CONTENT,
//				WindowManager.LayoutParams.WRAP_CONTENT);
	}

	/**
	 * Initializes wheel
	 * @param id the wheel widget Id
	 */
	private WheelView initWheel(int id, int current, WheelViewAdapter adapter) {
		WheelView wheel = getWheel(id);
		//wheel.setViewAdapter(new NumericWheelAdapter(getContext(), minValue, maxValue));
		wheel.setViewAdapter(adapter);
		if (this.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			wheel.setVisibleItems(3);
		} else {
			wheel.setVisibleItems(5);
		}
		//wheel.setLabel(this.getContext().getString(label));
		wheel.setCurrentItem(current);

		return wheel;
	}

	/**
	 * Returns wheel by Id
	 * @param id the wheel Id
	 * @return the wheel with passed Id
	 */
	private WheelView getWheel(int id) {
		return (WheelView) findViewById(id);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		int value;
		if (which == DialogInterface.BUTTON_POSITIVE) {
			//The dialog has been confirmed
			int sign = signWheel.getCurrentItem() == 0 ? 1 : -1;
			int tens = tensWheel.getCurrentItem();
			int unit = unitWheel.getCurrentItem();

			value = sign * ((tens * 10) + unit);
		} else {
			value = 0;
		}
		if (onNumberPickedListener != null) {
			onNumberPickedListener.onNumberPicked(which == DialogInterface.BUTTON_POSITIVE, value);
		}
		dismiss();
	}

}
