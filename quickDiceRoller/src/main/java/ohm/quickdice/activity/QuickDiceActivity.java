package ohm.quickdice.activity;

import ohm.quickdice.QuickDiceApp;
import ohm.quickdice.R;
import ohm.quickdice.control.PreferenceManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/**
 * {@link QuickDiceActivity} has become a splash screen to initialize 
 * all the dice data avoiding any ANR error.<br />
 * This class was meant to be named {@code StartupActivity}, but since renaming
 * the main activity in the manifest has lead to the removal of all the shortcut 
 * to the application in the launcher, I ended up renaming the original 
 * {@code QuickDiceActivity} to {@link QuickDiceMainActivity}.
 * @author Ohmnibus
 *
 */
public class QuickDiceActivity extends BaseActivity implements Runnable {

	QuickDiceApp app;
	PreferenceManager pref;
	Handler handler;
	ProgressDialog progressDialog = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//setContentView(R.layout.startup_activity);
		
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage(getText(R.string.msgInitApp));
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setIndeterminate(true);
		progressDialog.setCancelable(true);
		progressDialog.setCanceledOnTouchOutside(false);
		//progressDialog.setOnCancelListener(listener)
		progressDialog.show();

		app = QuickDiceApp.getInstance();
		pref = app.getPreferences();

		new Thread(this).start();
	}
	
	@Override
	public void run() {
		
		//Initialize Bag Manager
		app.getBagManager().init();
		
		//Initialize Result List
		app.getPersistence().preloadResultList();

		if (progressDialog.isShowing()) {

			//Launch main application
			Intent i = new Intent(this, QuickDiceMainActivity.class);
			startActivity(i);

			//Close progress dialog
			progressDialog.dismiss();
		}
		
		//Close self
		this.finish();
	}
}
