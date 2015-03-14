package ohm.library.preference;

import android.content.Context;
import android.preference.EditTextPreference;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

public class IntEditTextPreference extends EditTextPreference {

	public IntEditTextPreference(Context context) {
		super(context);
	}

	public IntEditTextPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public IntEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onAddEditTextToDialogView(View dialogView, EditText editText) {
		super.onAddEditTextToDialogView(dialogView, editText);
		editText.setInputType(InputType.TYPE_CLASS_NUMBER);
	}

	@Override
	protected String getPersistedString(String defaultReturnValue) {
		return String.valueOf(getPersistedInt(0));
	}

	@Override
	protected boolean persistString(String value) {
		boolean retVal;
		try {
			retVal = persistInt(Integer.parseInt(value));
		} catch (NumberFormatException ex) {
			retVal = false;
		}
		return retVal;
	}
}

