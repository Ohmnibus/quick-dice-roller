package ohm.library.compat;

import java.lang.reflect.Field;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.ViewConfiguration;

public abstract class CompatActionBar {
	
	public static CompatActionBar createInstance(Activity activity) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			return new CompatActionBarIceCreamSandwich(activity);
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			return new CompatActionBarHoneycomb(activity);
		} else {
			return new CompatActionBarEclair(activity);
		}
	}

	public abstract void setTitle(int titleId);
	
	public abstract void setTitle(CharSequence title);
	
	public abstract void setDisplayHomeAsUpEnabled(boolean showHomeAsUp);
	
	public abstract void setHomeButtonEnabled(boolean enabled);
	
	protected void forceOverflowMenu(Context ctx) {
		try {
			ViewConfiguration config = ViewConfiguration.get(ctx);
			Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");

			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception e) {
			Log.w("forceOverflowMenu", "Cannot force Overflow Menu");
		}
	}
	
	@TargetApi(Build.VERSION_CODES.ECLAIR)
	private static class CompatActionBarEclair extends CompatActionBar {

		Activity mActivity;
		
		public CompatActionBarEclair(Activity activity) {
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
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static class CompatActionBarHoneycomb extends CompatActionBar {
		
		ActionBar mActionBar;
		
		public CompatActionBarHoneycomb(Activity activity) {
			mActionBar = activity.getActionBar();
			forceOverflowMenu(activity);
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
		}

	}
	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private static class CompatActionBarIceCreamSandwich extends CompatActionBar {

		ActionBar mActionBar;
		
		public CompatActionBarIceCreamSandwich(Activity activity) {
			mActionBar = activity.getActionBar();
			forceOverflowMenu(activity);
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
}
