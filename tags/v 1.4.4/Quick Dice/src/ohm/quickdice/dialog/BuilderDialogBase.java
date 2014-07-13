package ohm.quickdice.dialog;

import ohm.quickdice.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.View;

public abstract class BuilderDialogBase implements OnClickListener {
	
	public static final int ACTION_EDIT = 1; 
	public static final int ACTION_CHECK = 2; 
	
	private Context context;
	private ReadyListener readyListener;
	private View callingView;
	
	private boolean initialized = false;
	private final AlertDialog dialog;

	/**
	 * Dialog dismiss listener.
	 * @author Ohmnibus
	 *
	 */
	public interface ReadyListener {
		/**
		 * Called when the Builder Dialog is dismissed.
		 * @param view {@link View} that requested the dialog.
		 * @param confirmed Tell if the dialog was confirmed.
		 * @param action Action performed by the dialog. Can be {@code ACTION_EDIT} or {@code ACTION_CHECK}. 
		 * @param diceExpression A text used for {@link action}s of type {@code ACTION_EDIT}.
		 */
        public void ready(View view, boolean confirmed, int action, String diceExpression);
    }

	protected BuilderDialogBase(Context context, View view, ReadyListener readyListener) {
		this.context = context;
		this.callingView = view;
		this.readyListener = readyListener;
		
		dialog = new AlertDialog.Builder(context).create();
		
	}
	
	public AlertDialog getDialog() {
		if (! initialized) {
			setupDialog(dialog);
			
			dialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.lblOk), this);
			dialog.setButton(AlertDialog.BUTTON_NEGATIVE, context.getString(R.string.lblCancel), this);

			//Hack to avoid automatic dismiss on button click.
			dialog.setOnShowListener(new DialogInterface.OnShowListener() {
				@Override
				public void onShow(DialogInterface dlg) {
					//Hack to avoid automatic dismiss on button click.
					//Note that "getButton" will return "null" if called before the Dialog is shown
					dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							BuilderDialogBase.this.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
						}
					});
				}
			});
		}
		return dialog;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		boolean pass = true;
		String returnExpression = null;
		if (which == DialogInterface.BUTTON_POSITIVE) {
			//The dialog has been confirmed
			pass = checkExpression();
			if (pass) {
				returnExpression = getExpression();
			}
		}
		if (pass) {
			if (readyListener != null) {
				readyListener.ready(callingView, which == DialogInterface.BUTTON_POSITIVE, getActionType(), returnExpression);
			}
			dialog.dismiss();
		}
	}
	
	protected abstract void setupDialog(AlertDialog dialog);
	
	protected abstract int getActionType();

	protected abstract boolean checkExpression();

	protected abstract String getExpression();

}
