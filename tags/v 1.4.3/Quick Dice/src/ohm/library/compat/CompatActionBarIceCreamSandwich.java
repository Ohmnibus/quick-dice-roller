package ohm.library.compat;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.os.Build;

public class CompatActionBarIceCreamSandwich extends CompatActionBar {

	ActionBar mActionBar;
	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public CompatActionBarIceCreamSandwich(Activity activity) {
		mActionBar = activity.getActionBar();
	}
	
	@Override
	public void setTitle(int titleId) {
		mActionBar.setTitle(titleId);
	}

	@Override
	public void setTitle(CharSequence title) {
		mActionBar.setTitle(title);

	}

	@Override
	public void setDisplayHomeAsUpEnabled(boolean showHomeAsUp) {
		mActionBar.setDisplayHomeAsUpEnabled(showHomeAsUp);
	}

	@Override
	public void setHomeButtonEnabled(boolean enabled) {
		mActionBar.setHomeButtonEnabled(enabled);
	}

}