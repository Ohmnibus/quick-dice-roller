package ohm.quickdice.control;

import ohm.quickdice.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.TypedArray;

public class PreferenceManager {

	public static final int CLIPBOARD_TYPE_NONE = 0;
	public static final int CLIPBOARD_TYPE_VALUE = 1;
	public static final int CLIPBOARD_TYPE_EXT = 2;
	
	public static final int THEME_CLASSIC = 0;
	public static final int THEME_CLEAN = 1;
	public static final int THEME_LIGHT = 2;
	public static final int THEME_DARK = 3;

	protected static final int MAX_DICE = 24;
	protected static final int MAX_VAR = 24;
	protected static final int MAX_DICE_BAGS = 12;
	protected static final int MAX_MODIFIERS = 24;
	protected static final int MAX_LINKED_RESULTS = 8;
	protected static final int MAX_LISTED_RESULTS = 30;
	protected static final int MAX_UNDO_RESULTS = 10;

	//protected static final String SHARED_PREFERENCES_CONFIG = "SHARED_PREFERENCES_CONFIG";
	public static final String KEY_CLIPBOARD = "KEY_CLIPBOARD";
	public static final String KEY_LINK_RESULT = "KEY_LINK_RESULT";
	public static final String KEY_LINK_RESULT_DELAY = "KEY_LINK_RESULT_DELAY";
	public static final String KEY_SHOW_MODIFIERS = "KEY_SHOW_MODIFIERS";
	public static final String KEY_COLUMN_NUM = "KEY_COLUMN_NUM";
	@Deprecated
	protected static final String KEY_PLAIN_BG = "KEY_PLAIN_BG";
	public static final String KEY_THEME = "KEY_THEME";
	public static final String KEY_SHOW_TOAST = "KEY_SHOW_TOAST";
	public static final String KEY_SHOW_ANIMATION = "KEY_SHOW_ANIMATION";
	public static final String KEY_ENABLE_SOUND = "KEY_ENABLE_SOUND";
	public static final String KEY_ENABLE_SPECIAL_SOUND = "KEY_ENABLE_SPECIAL_SOUND";
	public static final String KEY_ENABLE_TTS = "KEY_ENABLE_TTS";
	public static final String KEY_MAX_DICE = "KEY_MAX_DICE";
	public static final String KEY_MAX_DICE_BAGS = "KEY_MAX_DICE_BAGS";
	public static final String KEY_MAX_MODIFIERS = "KEY_MAX_MODIFIERS";
	public static final String KEY_MAX_LINKED_RESULTS = "KEY_MAX_LINKED_RESULTS";
	public static final String KEY_MAX_LISTED_RESULTS = "KEY_MAX_LISTED_RESULTS";
	public static final String KEY_SWAP_NAME_RESULT = "KEY_SWAP_NAME_RESULT";
	public static final String KEY_SPLIT_PANEL_WIDTH = "KEY_SPLIT_PANEL_WIDTH";
	public static final String KEY_SPLIT_PANEL_HEIGHT = "KEY_SPLIT_PANEL_HEIGHT";
	public static final String KEY_WAKELOCK = "KEY_WAKELOCK";
	public static final String KEY_CUSTOM_KEYBOARD = "KEY_CUSTOM_KEYBOARD";

	private boolean initialized = false;
	private int clipboardUsage;
	private boolean autoLinkEnabled;
	private int linkDelay;
	private boolean showModifiers;
	private int gridResultColumn;
	//private boolean plainBackground;
	private int theme;
	private int themeResId;
	private int dialogThemeResId;
	private boolean showToast;
	private boolean showAnimation;
	private boolean soundEnabled;
	private boolean extSoundEnabled;
	private boolean speechEnabled;
	private boolean swapNameResult;
	private int splitPanelWidth;
	private int splitPanelHeight;
	private boolean wakeLock;
	private boolean customKeyboard;
	
	private Context context;
	private SharedPreferences config;
	
	public PreferenceManager(Context context) {
		this.context = context;
		this.config = android.preference.PreferenceManager.getDefaultSharedPreferences(this.context);
	}
	
	public void resetCache() {
		initialized = false;
	}

	public void initCache() {
		if (! initialized) {
			clipboardUsage = config.getInt(KEY_CLIPBOARD, CLIPBOARD_TYPE_NONE);
			autoLinkEnabled = config.getBoolean(KEY_LINK_RESULT, true);
			linkDelay = config.getInt(KEY_LINK_RESULT_DELAY, 1500);
			showModifiers = config.getBoolean(KEY_SHOW_MODIFIERS, true);
			gridResultColumn = config.getInt(KEY_COLUMN_NUM, 1);
			theme = config.getInt(KEY_THEME, -1);
			if (theme == -1) {
				boolean plainBackground = config.getBoolean(KEY_PLAIN_BG, false);
				theme = plainBackground ? THEME_CLEAN : THEME_CLASSIC;
				//If I'm here it is due to an app update.
				//I have to write the theme in order to set the 
				//correct default value in the preference activity.
				Editor edit = config.edit();
				edit.putInt(KEY_THEME, theme);
				edit.commit();
			}
			fillThemeResources(theme);
			showToast = config.getBoolean(KEY_SHOW_TOAST, true);
			showAnimation = config.getBoolean(KEY_SHOW_ANIMATION, true);
			soundEnabled = config.getBoolean(KEY_ENABLE_SOUND, true);
			extSoundEnabled = config.getBoolean(KEY_ENABLE_SPECIAL_SOUND, true);
			speechEnabled = config.getBoolean(KEY_ENABLE_TTS, false);
			swapNameResult = config.getBoolean(KEY_SWAP_NAME_RESULT, false);
			splitPanelWidth = config.getInt(KEY_SPLIT_PANEL_WIDTH, -1);
			splitPanelHeight = config.getInt(KEY_SPLIT_PANEL_HEIGHT, -1);
			wakeLock = config.getBoolean(KEY_WAKELOCK, true);
			customKeyboard = config.getBoolean(KEY_CUSTOM_KEYBOARD, true);
			initialized = true;
		}
	}
	
	/**
	 * Set the correct value to actual theme resource ID.<br />
	 * This method set the correct value to {@link #themeResId} and {@link #dialogThemeResId}.
	 * @param theme Identifier of the theme to apply, as stored in preferences file.
	 */
	private void fillThemeResources(int theme) {
		//Set the correct resource for the main theme.
		switch (theme) {
			case THEME_CLASSIC:
				themeResId = R.style.QDR_Theme_Classic;
				break;
			case THEME_CLEAN:
				themeResId = R.style.QDR_Theme_Clean;
				break;
			case THEME_LIGHT:
				themeResId = R.style.QDR_Theme_Light;
				break;
			case THEME_DARK:
				themeResId = R.style.QDR_Theme_Dark;
				break;
			default:
				themeResId = R.style.QDR_Theme_Classic;
				break;
		}
		
		//Set the correct resource for the dialog theme.
		TypedArray ta = context.obtainStyledAttributes(themeResId, new int[] {R.attr.ohmDialogTheme});

		dialogThemeResId = ta.getResourceId(0, R.style.DialogBaseTheme);

		ta.recycle();
	}
	
	/**
	 * Return an integer indicating the type of usage of the clipboard required.
	 * @return An integer value. The value indicates the usage of the clipboard:<br />
	 * {@code CLIPBOARD_TYPE_NONE}: Do not store data to the clipboard.<br />
	 * {@code CLIPBOARD_TYPE_VALUE}: Store the numeric result of the roll to the clipboard.<br />
	 * {@code CLIPBOARD_TYPE_EXT}: Store the entire expression to the clipboard.<br />
	 */
	public int getClipboardUsage() {
		initCache();
		return clipboardUsage;
	}
	
	/**
	 * Tell if automatic linking of roll results is enabled.
	 */
	public boolean getAutoLinkEnabled() {
		initCache();
		return autoLinkEnabled;
	}

	/**
	 * Return the time (in milliseconds) within which two rolls are added.
	 * @return Time (in milliseconds) within which two rolls are added.
	 */
	public int getLinkDelay() {
		initCache();
		return linkDelay;
	}

	/**
	 * Tell if show modifiers bar.
	 * @return
	 */
	public boolean getShowModifiers() {
		initCache();
		return showModifiers;
	}

	/**
	 * Return the number of columns on which the result are shown.
	 * @return Number of columns to display.
	 */
	public int getGridResultColumn() {
		initCache();
		return gridResultColumn;
	}

//	/**
//	 * Indicate if the use of plain background is required.
//	 * @return Boolean indicating if the use of plain background is required.
//	 */
//	public boolean getPlainBackground() {
//		initCache();
//		return plainBackground;
//	}
	
	/**
	 * Return the value representing the theme to apply.<br />
	 * It is not a reference to a theme. Is one of the values
	 * defined in the constants {@code THEME_*}.
	 * @return
	 * @see #getThemeResId()
	 */
	public int getTheme() {
		initCache();
		return theme;
	}
	
	/**
	 * Return the resource ID representing the theme to apply.<br />
	 * This is not the value in the preference file.<br />
	 * @return The theme resource ID to apply.
	 * @see #getTheme()
	 */
	public int getThemeResId() {
		initCache();
		return themeResId;
	}
	
	/**
	 * Return the resource ID representing the theme to apply to a dialog.<br />
	 * This is not the value in the preference file.<br />
	 * @return The theme resource ID to apply.
	 * @see #getTheme()
	 */
	public int getDialogThemeResId() {
		initCache();
		return dialogThemeResId;
	}
	
	/**
	 * Return a boolean telling if the toast showing the roll result is required.
	 * @return {@code true} if toast is required, {@code false} otherwise.
	 */
	public boolean getShowToast() {
		initCache();
		return showToast;
	}

	/**
	 * Return a boolean telling if the toast animation is required.<br />
	 * This value is ignored if {@link getShowToast} is {@code false}.
	 * @return {@code true} if animation is required, {@code false} otherwise.
	 */
	public boolean getShowAnimation() {
		initCache();
		return showAnimation;
	}

	public boolean getSoundEnabled() {
		initCache();
		return soundEnabled;
	}

	public boolean getExtSoundEnabled() {
		initCache();
		return extSoundEnabled;
	}
	
	public boolean getSpeechEnabled() {
		initCache();
		return speechEnabled;
	}

	/**
	 * Tell if the wake-lock (keep screen on) should be enabled.
	 * @return {@code true} is wake-lock is enabled, {@code false} otherwise.
	 */
	public boolean getWakeLock() {
		initCache();
		return wakeLock;
	}
	
	public boolean getCustomKeyboard() {
		initCache();
		return customKeyboard;
	}

	public boolean getSwapNameResult() {
		initCache();
		return swapNameResult;
	}

	public int getSplitPanelWidth() {
		initCache();
		return splitPanelWidth;
	}

	public void setSplitPanelWidth(int width) {
		initCache();
		splitPanelWidth = width;
		Editor edit = config.edit();
		edit.putInt(KEY_SPLIT_PANEL_WIDTH, splitPanelWidth);
		edit.commit();
	}

	public int getSplitPanelHeight() {
		initCache();
		return splitPanelHeight;
	}

	public void setSplitPanelHeight(int height) {
		initCache();
		splitPanelHeight = height;
		Editor edit = config.edit();
		edit.putInt(KEY_SPLIT_PANEL_HEIGHT, splitPanelHeight);
		edit.commit();
	}

	/**
	 * Return the max number of dice bags allowed.
	 * @return Maximum number of bags allowed.
	 */
	public int getMaxDiceBags() {
		//return config.getInt(KEY_MAX_DICE_BAGS, 8);
		return MAX_DICE_BAGS;
	}

	/**
	 * Return the max number of variables allowed in the dice bag.
	 * @return Maximum number of variables allowed in the dice bag.
	 */
	public int getMaxVariables() {
		//return config.getInt(KEY_MAX_VAR, 16);
		return MAX_VAR;
	}
	
	/**
	 * Return the max number of dice allowed in the dicebag.
	 * @return Maximum number of dice allowed in the dicebag.
	 */
	public int getMaxDice() {
		//return config.getInt(KEY_MAX_DICE, 16);
		return MAX_DICE;
	}

	/**
	 * Return the max number of modifiers allowed.
	 * @return Maximum number of modifiers allowed.
	 */
	public int getMaxModifiers() {
		//return config.getInt(KEY_MAX_MODIFIERS, 16);
		return MAX_MODIFIERS;
	}

	/**
	 * Return the number of linked results allowed in a single result item.
	 * @return Maximum number of linked results allowed in a single result item.
	 */
	public int getMaxResultLink() {
		//return config.getInt(KEY_MAX_LINKED_RESULTS, 8);
		return MAX_LINKED_RESULTS;
	}

	/**
	 * Return the number of results allowed in the result list.
	 * @return Maximum number of result in result list.
	 */
	public int getMaxResultList() {
		//return config.getInt(KEY_MAX_LISTED_RESULTS, 30);
		return MAX_LISTED_RESULTS;
	}
	
	/**
	 * Return the max length of the undo stack for deleted results.
	 * @return Max length of the undo stack for deleted results.
	 */
	public int getMaxResultUndo() {
		return MAX_UNDO_RESULTS;		
	}
}
