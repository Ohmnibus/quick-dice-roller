package ohm.quickdice.activity;

import ohm.quickdice.R;
import ohm.quickdice.control.PreferenceManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class PrefDiceActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
	/**
	 * Open the activity to edit an existing dice expression.
	 */
	public static final int ACTIVITY_EDIT_PREF = 0x00020001;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.preferences);
		
		//Clipboard
		setSummary(
				PreferenceManager.KEY_CLIPBOARD,
				PreferenceManager.CLIPBOARD_TYPE_NONE,
				R.string.pref_clipboard_dyn_desc,
				R.array.entryvalues_clipboard,
				R.array.entries_clipboard);
		
		//Link sensitivity
		setSummary(
				PreferenceManager.KEY_LINK_RESULT_DELAY,
				1500,
				R.string.pref_link_sens_dyn_desc,
				R.array.entryvalues_sensitivity,
				R.array.entries_sensitivity);
		
		//Result columns
		setSummary(
				PreferenceManager.KEY_COLUMN_NUM,
				1,
				R.string.pref_aspect_col_dyn_desc,
				R.array.entryvalues_columns,
				R.array.entries_columns);

		//Themes
		setSummary(
				PreferenceManager.KEY_THEME,
				PreferenceManager.THEME_CLASSIC,
				R.string.pref_aspect_theme_dyn_desc,
				R.array.entryvalues_themes,
				R.array.entries_themes);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}
	
	/**
	 * Set the right summary for the preference identified by {@code key}.<br />
	 * This method change the summary for those settings based on a selection,
	 * reflecting the value selected.
	 * @param key Identifier for the setting.
	 */
	private void setSummary(String key) {
		if (key.equals(PreferenceManager.KEY_CLIPBOARD)) {
			//Clipboard
			setSummary(
					PreferenceManager.KEY_CLIPBOARD,
					PreferenceManager.CLIPBOARD_TYPE_NONE,
					R.string.pref_clipboard_dyn_desc,
					R.array.entryvalues_clipboard,
					R.array.entries_clipboard);
		} else if (key.equals(PreferenceManager.KEY_LINK_RESULT_DELAY)) {
			//Link sensitivity
			setSummary(
					PreferenceManager.KEY_LINK_RESULT_DELAY,
					1500,
					R.string.pref_link_sens_dyn_desc,
					R.array.entryvalues_sensitivity,
					R.array.entries_sensitivity);
		} else if (key.equals(PreferenceManager.KEY_COLUMN_NUM)) {
			//Result columns
			setSummary(
					PreferenceManager.KEY_COLUMN_NUM,
					1,
					R.string.pref_aspect_col_dyn_desc,
					R.array.entryvalues_columns,
					R.array.entries_columns);
		} else if (key.equals(PreferenceManager.KEY_THEME)) {
			//Themes
			setSummary(
					PreferenceManager.KEY_THEME,
					PreferenceManager.THEME_CLASSIC,
					R.string.pref_aspect_theme_dyn_desc,
					R.array.entryvalues_themes,
					R.array.entries_themes);
		}
	}
	
	@SuppressWarnings("deprecation")
	private void setSummary(
			String key,
			int defValue,
			int summaryResId,
			int valuesResId,
			int namesResId) {

		Preference pref = findPreference(key);
		
		SharedPreferences sharedPreferences = pref.getSharedPreferences();

		int value = sharedPreferences.getInt(key, defValue);
		
		pref.setSummary(getString(
				summaryResId,
				getSelectedName(value, valuesResId, namesResId)));
	}
	
	private String getSelectedName(int selected, int valuesResId, int namesResId) {
		String retVal;
		
		String strSel = String.valueOf(selected);
		String[] values = getResources().getStringArray(valuesResId);
		int index = 0; //Default
		for (int i = 0; i < values.length; i++) {
			if (values[i].equals(strSel)) {
				index = i;
				break;
			}
		}
		
		retVal = getResources().getStringArray(namesResId)[index];
		
		return retVal;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		setSummary(key);
	}
}
