package ohm.library.compat;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

/**
 * Derived class to be used with {@link Build.VERSION_CODES.DONUT} (API 4).
 * @author Ohmnibus
 */
@SuppressWarnings("deprecation")
public class CompatMiscDonut extends CompatMisc {

	@Override
	public void setBackgroundDrawable(View v, Drawable d) {
		v.setBackgroundDrawable(d);
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
