package ohm.library.compat;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.Context;
import android.os.Build;

public abstract class CompatClipboard {

	private static CompatClipboard mClipboard = null;
	
	public static CompatClipboard getInstance(Context context) {
		if (mClipboard == null) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				mClipboard = new CompatClipboardHoneycomb(context);
			} else {
				mClipboard = new CompatClipboardEclair(context);
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
	
	
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.ECLAIR)
	private static class CompatClipboardEclair extends CompatClipboard {

		//Context mContext;
		android.text.ClipboardManager mClipboardManager;

		public CompatClipboardEclair(Context context) {
			//mContext = context;
			mClipboardManager = (android.text.ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
		}

		@Override
		public void setText(CharSequence label, CharSequence text) {
			mClipboardManager.setText(text);
		}

	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static class CompatClipboardHoneycomb extends CompatClipboard {

		//Context mContext;
		android.content.ClipboardManager mClipboardManager;
		
		public CompatClipboardHoneycomb(Context context) {
			//mContext = context;
			mClipboardManager = (android.content.ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
		}

		@Override
		public void setText(CharSequence label, CharSequence text) {
			ClipData clip = ClipData.newPlainText(label, text);
			mClipboardManager.setPrimaryClip(clip);
		}

	}

}
