package ohm.library.compat;

import android.content.Context;
import android.os.Build;

public abstract class CompatClipboard {

	private static CompatClipboard mClipboard = null;
	
	public static CompatClipboard getInstance(Context context) {
		if (mClipboard == null) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				mClipboard = new CompatClipboardHoneycomb(context);
			} else {
				mClipboard = new CompatClipboardEclaire(context);
			}
		}
		return mClipboard;
	}

	/**
	 * Sets the contents of the clipboard to the specified text.
	 * @param label User-visible label for the clipboard data.
	 * @param text The actual text to be set to the clipboard.
	 */
	public abstract void setText(CharSequence label, CharSequence text);
}
