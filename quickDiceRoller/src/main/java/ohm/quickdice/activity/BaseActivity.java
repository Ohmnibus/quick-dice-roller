package ohm.quickdice.activity;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

public abstract class BaseActivity extends Activity {

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		unbindDrawables(findViewById(android.R.id.content));
		
		System.gc();
	}
	
	private void unbindDrawables(View view) {
		if (view != null) {
			if (view.getBackground() != null) {
				view.getBackground().setCallback(null);
			}
			if (view instanceof ViewGroup && !(view instanceof AdapterView)) {
				for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
					unbindDrawables(((ViewGroup) view).getChildAt(i));
				}
				((ViewGroup) view).removeAllViews();
			}
		}
	}
}
