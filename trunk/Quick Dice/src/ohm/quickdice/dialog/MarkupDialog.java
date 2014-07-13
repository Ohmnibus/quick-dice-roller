package ohm.quickdice.dialog;

import ohm.quickdice.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MarkupDialog extends AlertDialog implements OnClickListener, ImageGetter {

	Resources mRes;
	DisplayMetrics mMetrics;
	ReadyListener mReadyListener;
	String mTitle;
	String mMessage;
	int mIconId;
	
	public interface ReadyListener {
		public void ready(boolean confirmed);
	}

	/**
	 * Initialize a {@link MarkupDialog} with the specified parameters.<br />
	 * A markup dialog is a simple dialog showing HTML formatted text.
	 * @param context Reference context.
	 * @param titleId Title resource id.
	 * @param messageId Message resource id.
	 * @param readyListener A listener to be called on dialog dismissal, or {@code null}.
	 */
	public MarkupDialog(Context context, int titleId, int messageId, ReadyListener readyListener) {
		this(context, titleId, messageId, 0, readyListener);
	}

	/**
	 * Initialize a {@link MarkupDialog} with the specified parameters.<br />
	 * A markup dialog is a simple dialog showing HTML formatted text.
	 * @param context Reference context.
	 * @param titleId Title resource id.
	 * @param messageId Message resource id.
	 * @param iconId An optional drawable resource id to be displayed on the top of the text, or {@code 0}.
	 * @param readyListener A listener to be called on dialog dismissal, or {@code null}.
	 */
	public MarkupDialog(Context context, int titleId, int messageId, int iconId, ReadyListener readyListener) {
		this(
				context,
				context.getResources().getString(titleId),
				context.getResources().getString(messageId),
				iconId,
				readyListener);
	}

	/**
	 * Initialize a {@link MarkupDialog} with the specified parameters.<br />
	 * A markup dialog is a simple dialog showing HTML formatted text.
	 * @param context Reference context.
	 * @param title Text to be displayed as the dialog title.
	 * @param message Text to be displayed as the dialog body.
	 * @param iconId An optional drawable resource id to be displayed on the top of the text, or {@code 0}.
	 * @param readyListener A listener to be called on dialog dismissal, or {@code null}.
	 */
	public MarkupDialog(Context context, String title, String message, int iconId, ReadyListener readyListener) {
		super(context);
		mRes = context.getResources();
		mMetrics = new DisplayMetrics();
		this.getWindow().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);

		mTitle = title;
		mMessage = message;
		mIconId = iconId;
		mReadyListener = readyListener;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		View mView = getLayoutInflater().inflate(R.layout.markup_dialog, null);
		
		setView(mView);
		
		setCancelable(false);
		setTitle(mTitle);
		setButton(BUTTON_NEGATIVE, this.getContext().getString(R.string.lblOk), this);
		
		super.onCreate(savedInstanceState);

		if (mIconId > 0) {
			((ImageView)findViewById(R.id.mkIcon)).setImageResource(mIconId);
		} else {
			((ImageView)findViewById(R.id.mkIcon)).setVisibility(View.GONE);
		}
		((TextView)findViewById(R.id.mkBody)).setText(Html.fromHtml(mMessage, this, null));
		((TextView)findViewById(R.id.mkBody)).setMovementMethod(LinkMovementMethod.getInstance());
	}
	
	@Override
	public Drawable getDrawable(String source) {
		Drawable retVal = null;
		int resID = mRes.getIdentifier(source, "drawable", this.getContext().getPackageName());
		retVal = mRes.getDrawable(resID);
		//retVal.setBounds(0, 0, retVal.getIntrinsicWidth(), retVal.getIntrinsicHeight());
		//retVal.setBounds(0, 0, 24, 24);
		retVal.setBounds(
				0, 
				0, 
				(int)(retVal.getIntrinsicWidth() * mMetrics.density), 
				(int)(retVal.getIntrinsicHeight() * mMetrics.density));
		return retVal;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (mReadyListener != null) {
			mReadyListener.ready(which == BUTTON_NEGATIVE);
		}
		dismiss();
	}

}
