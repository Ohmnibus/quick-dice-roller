package ohm.quickdice.dialog;

import ohm.quickdice.R;
import android.content.Context;

public class ModifierBuilderDialog extends NumberPickerDialog implements NumberPickerDialog.OnNumberPickedListener {

	public static final int POSITION_UNDEFINED = -1;

	int position;
	OnCreatedListener onCreatedListener;

	public interface OnCreatedListener {
		public void onCreated(boolean confirmed, int modifier, int position);
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
		super(context, R.string.lblModifierBuilder, R.string.lblModifierBuilderMessage, 0, null);

		super.onNumberPickedListener = this;
		this.position = position;
		this.onCreatedListener = onCreatedListener;
	}

	@Override
	public void onNumberPicked(boolean confirmed, int value) {
		if (onCreatedListener != null) {
			onCreatedListener.onCreated(confirmed, value, position);
		}
	}
}
