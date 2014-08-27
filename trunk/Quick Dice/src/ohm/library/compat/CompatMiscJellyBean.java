package ohm.library.compat;

import android.annotation.TargetApi;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;

/**
 * Derived class to be used with {@link Build.VERSION_CODES.JELLY_BEAN} (API 16).
 * @author Ohmnibus
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class CompatMiscJellyBean extends CompatMisc {

	@Override
	public void setBackgroundDrawable(View v, Drawable d) {
		v.setBackground(d);
	}

	@Override
	public void setInputTypeNoKeyboard(EditText editText) {
		editText.setInputType(InputType.TYPE_NULL);

		editText.setRawInputType(InputType.TYPE_CLASS_TEXT);
		editText.setTextIsSelectable(true);
	}
	
	@Override
	protected int getLayoutMatchParent() {
		return LayoutParams.MATCH_PARENT;
	}

	@Override
	protected int getLayoutWrapContent() {
		return LayoutParams.WRAP_CONTENT;
	}

}
