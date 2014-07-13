package ohm.quickdice.activity;

import ohm.quickdice.QuickDiceApp;
import ohm.quickdice.R;
import ohm.quickdice.control.PreferenceManager;
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.startup_activity);

		app = QuickDiceApp.getInstance();
		pref = app.getPreferences();

		new Thread(this).start();
	}
	
	@Override
	public void run() {
		
		//Initialize Bag Manager
		app.getBagManager().initBagManager();
		
		//Initialize Result List
		app.getPersistence().preloadResultList();

		//Launch main application
		Intent i = new Intent(this, QuickDiceMainActivity.class);
		startActivity(i);

		//Close self
		this.finish();
	}
}
