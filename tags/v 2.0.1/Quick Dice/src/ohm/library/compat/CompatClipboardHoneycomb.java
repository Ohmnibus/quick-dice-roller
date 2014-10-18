package ohm.library.compat;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;

public class CompatClipboardHoneycomb extends CompatClipboard {

	Context mContext;
	ClipboardManager mClipboardManager;
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public CompatClipboardHoneycomb(Context context) {
		mContext = context;
		mClipboardManager = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void setText(CharSequence label, CharSequence text) {
		ClipData clip = ClipData.newPlainText(label, text);
		mClipboardManager.setPrimaryClip(clip);
	}

}
