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
	private static final int DEFAULT_DIGITS = 2;

	String title;
	String message;
	int defaultValue;
	int digits;
	OnNumberPickedListener onNumberPickedListener;

	WheelView signWheel;
	WheelView hundWheel;
	WheelView tensWheel;
	WheelView unitWheel;

	public interface OnNumberPickedListener {
		void onNumberPicked(boolean confirmed, int value);
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
		this(context, R.string.lblNumberPicker, R.string.lblSelectValue, defaultValue, DEFAULT_DIGITS, onNumberPickedListener);
	}

	/**
	 * Initialize a picker with given parameters.
	 * @param context Context
	 * @param titleResId Title resource id.
	 * @param messageResId Message resource id.
	 * @param defaultValue Default value.
	 * @param digits Number of digits to show.
	 * @param onNumberPickedListener Callback listener.
	 */
	public NumberPickerDialog(Context context, int titleResId, int messageResId, int defaultValue, int digits, OnNumberPickedListener onNumberPickedListener) {
		this(context, context.getString(titleResId), context.getString(messageResId), defaultValue, digits, onNumberPickedListener);
	}

	/**
	 * Initialize a number picker with given parameters.
	 * @param context Context
	 * @param title Title
	 * @param message Message
	 * @param defaultValue Default value.
	 * @param digits Number of digits to show.
	 * @param onNumberPickedListener Callback listener.
	 */
	public NumberPickerDialog(Context context, String title, String message, int defaultValue, int digits, OnNumberPickedListener onNumberPickedListener) {
		super(context);
		
		this.onNumberPickedListener = onNumberPickedListener;
		if (digits > 3) {
			this.digits = 3;
		} else if (digits < 1) {
			this.digits = 1;
		} else {
			this.digits = digits;
		}
		
		long max = pow(10, this.digits) - 1;
		
		if (defaultValue > max) {
			this.defaultValue = (int) max;
		} else if (defaultValue < -max) {
			this.defaultValue = (int) -max;
		} else {
			this.defaultValue = defaultValue;
		}
		this.title = title;
		this.message = message;
	}

	long pow (long a, int b) {
		if ( b == 0)        return 1;
		if ( b == 1)        return a;
		if (isEven( b ))    return     pow ( a * a, b/2); //even a=(a^2)^b/2
		else                return a * pow ( a * a, b/2); //odd  a=a*(a^2)^b/2
	}
	
	boolean isEven(int b) {
		return (b / 2) == ((b + 1) / 2);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		initHead();

		super.onCreate(savedInstanceState);

		initViews();
	}

	protected void initHead() {
		View mView = getLayoutInflater().inflate(R.layout.dialog_number_picker, null);

		setView(mView);

		setTitle(title);
		((TextView)mView.findViewById(R.id.lblMessage)).setText(message);
		setButton(BUTTON_POSITIVE, this.getContext().getString(R.string.lblOk), this);
		setButton(BUTTON_NEGATIVE, this.getContext().getString(R.string.lblCancel), this);
	}

	protected void initViews() {
		int curSign;
		int cur100s;
		int curTens;
		int curUnits;

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

		signWheel = initWheel(R.id.wheelSign, curSign, new ArrayWheelAdapter<String>(getContext(), new String[] {"+", "-"}));
		hundWheel = initWheel(R.id.wheelHundreds, cur100s, new NumericWheelAdapter(getContext(), 0, 9));
		tensWheel = initWheel(R.id.wheelTens, curTens, new NumericWheelAdapter(getContext(), 0, 9));
		unitWheel = initWheel(R.id.wheelUnits, curUnits, new NumericWheelAdapter(getContext(), 0, 9));

		if (digits < 3) {
			hundWheel.setVisibility(View.GONE);
		}
		if (digits < 2) {
			tensWheel.setVisibility(View.GONE);
		}
	}

	/**
	 * Initializes wheel
	 * @param id the wheel widget Id
	 */
	private WheelView initWheel(int id, int current, WheelViewAdapter adapter) {
		WheelView wheel = getWheel(id);
		wheel.setViewAdapter(adapter);
		if (this.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			wheel.setVisibleItems(3);
		} else {
			wheel.setVisibleItems(5);
		}
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
			int hund = hundWheel.getCurrentItem();
			int tens = tensWheel.getCurrentItem();
			int unit = unitWheel.getCurrentItem();

			value = sign * ((hund * 100) + (tens * 10) + unit);
		} else {
			value = 0;
		}
		if (onNumberPickedListener != null) {
			onNumberPickedListener.onNumberPicked(which == DialogInterface.BUTTON_POSITIVE, value);
		}
		dismiss();
	}

}
