package ohm.quickdice.activity;

import ohm.quickdice.R;
import ohm.quickdice.control.PreferenceManager;
import ohm.quickdice.util.Helper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

@SuppressWarnings("deprecation")
public class PrefDiceActivity extends PreferenceActivity implements 
OnSharedPreferenceChangeListener,
OnPreferenceClickListener {
	
	/**
	 * Open the activity to edit an existing dice expression.
	 */
	public static final int ACTIVITY_EDIT_PREF = 0x02000100;
	
	private static final int PICK_BACKGROUND_IMAGE = ACTIVITY_EDIT_PREF | 0xFF;
	private static final String KEY_CUSTOM_BACKGROUND_PICK = PreferenceManager.KEY_CUSTOM_BACKGROUND + "_PICK";

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
		
		//Background image
		if (! Helper.BackgroundManager.exists(this)) {
			//BG image does not exists, clean the flag
			CheckBoxPreference pref = (CheckBoxPreference) findPreference(PreferenceManager.KEY_CUSTOM_BACKGROUND);
			pref.setChecked(false);
		}

		Preference pref = findPreference(KEY_CUSTOM_BACKGROUND_PICK);
		pref.setOnPreferenceClickListener(this);
	}
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		startActivityForResult(preference.getIntent(), PICK_BACKGROUND_IMAGE);
		return true;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

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
		} else if (key.equals(PreferenceManager.KEY_CUSTOM_BACKGROUND)) {
			//Background
			CheckBoxPreference pref = (CheckBoxPreference)findPreference(key);
//			SharedPreferences sharedPreferences = pref.getSharedPreferences();
//			boolean newVal = sharedPreferences.getBoolean(key, false);
			boolean newVal = pref.isChecked();
			if (newVal && !Helper.BackgroundManager.exists(this)) {
				//Flag set to true but no image available.
				//Open image piker
				//pref = findPreference(KEY_CUSTOM_BACKGROUND_PICK);
				//startActivityForResult(pref.getIntent(), PICK_BACKGROUND_IMAGE);
				onPreferenceClick(findPreference(KEY_CUSTOM_BACKGROUND_PICK));
			}
		}
	}
	
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == PICK_BACKGROUND_IMAGE) {
			if (resultCode == RESULT_OK && data != null) {
//				DisplayMetrics metrics = new DisplayMetrics();
//				getWindowManager().getDefaultDisplay().getMetrics(metrics);
				Helper.BackgroundManager.setBackgroundImage(
						this,
						data.getData(),
						1); //(float)metrics.widthPixels / (float)metrics.heightPixels);
			}
			if (! Helper.BackgroundManager.exists(this)) {
				//BG image does not exists, clean the flag
				CheckBoxPreference pref = (CheckBoxPreference) findPreference(PreferenceManager.KEY_CUSTOM_BACKGROUND);
				pref.setChecked(false);
			}
		}
	}
	
}
