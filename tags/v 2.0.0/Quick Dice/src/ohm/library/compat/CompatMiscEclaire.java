package ohm.library.compat;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;

/**
 * Derived class to be used with {@link Build.VERSION_CODES.ECLAIR} (API 5).
 * @author Ohmnibus
 */
@SuppressWarnings("deprecation")
public class CompatMiscEclaire extends CompatMisc {

	@Override
	public void setBackgroundDrawable(View v, Drawable d) {
		v.setBackgroundDrawable(d);
	}

	@Override
	public void setInputTypeNoKeyboard(EditText editText) {
		editText.setInputType(InputType.TYPE_NULL);
	}
	
	@Override
	protected int getLayoutMatchParent() {
		return LayoutParams.FILL_PARENT;
	}

	@Override
	protected int getLayoutWrapContent() {
		return LayoutParams.WRAP_CONTENT;
	}
}
