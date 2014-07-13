package ohm.quickdice.activity;

import ohm.quickdice.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PrefDiceActivity extends PreferenceActivity {
	
	/**
	 * Open the activity to edit an existing dice expression.
	 */
	public static final int ACTIVITY_EDIT_PREF = 0x00020001;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.preferences);
	}

}
