package ohm.library.compat;

import android.content.Context;
import android.text.ClipboardManager;

@SuppressWarnings("deprecation")
public class CompatClipboardEclaire extends CompatClipboard {

	Context mContext;
	ClipboardManager mClipboardManager;

	public CompatClipboardEclaire(Context context) {
		mContext = context;
		mClipboardManager = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
	}

	@Override
	public void setText(CharSequence label, CharSequence text) {
		mClipboardManager.setText(text);
	}

}
