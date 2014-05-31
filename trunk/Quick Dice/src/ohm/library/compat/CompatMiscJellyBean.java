package ohm.library.compat;

import android.annotation.TargetApi;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

/**
 * Derived class to be used with {@link Build.VERSION_CODES.JELLY_BEAN} (API 16).
 * @author Ohmnibus
 */
public class CompatMiscJellyBean extends CompatMisc {

	@Override
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void setBackgroundDrawable(View v, Drawable d) {
		v.setBackground(d);
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
