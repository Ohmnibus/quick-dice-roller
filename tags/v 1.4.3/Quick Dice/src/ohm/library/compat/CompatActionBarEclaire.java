package ohm.library.compat;

import android.app.Activity;

public class CompatActionBarEclaire extends CompatActionBar {

	Activity mActivity;
	
	public CompatActionBarEclaire(Activity activity) {
		mActivity = activity;
	}

	@Override
	public void setTitle(int titleId) {
		mActivity.setTitle(titleId);
	}

	@Override
	public void setTitle(CharSequence title) {
		mActivity.setTitle(title);
	}

	@Override
	public void setDisplayHomeAsUpEnabled(boolean showHomeAsUp) {
	}

	@Override
	public void setHomeButtonEnabled(boolean enabled) {
	}

}
