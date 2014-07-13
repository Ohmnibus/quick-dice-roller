package ohm.library.compat;

import android.app.Activity;
import android.os.Build;

public abstract class CompatActionBar {
	
	public static CompatActionBar createInstance(Activity activity) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			return new CompatActionBarIceCreamSandwich(activity);
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			return new CompatActionBarHoneycomb(activity);
		} else {
			return new CompatActionBarEclaire(activity);
		}
	}

	public abstract void setTitle(int titleId);
	
	public abstract void setTitle(CharSequence title);
	
	public abstract void setDisplayHomeAsUpEnabled(boolean showHomeAsUp);
	
	public abstract void setHomeButtonEnabled(boolean enabled);
}
