package ohm.quickdice.dialog;

import ohm.library.widget.KeyboardView;
import ohm.quickdice.QuickDiceApp;
import ohm.quickdice.R;
import ohm.quickdice.util.CustomKeyboard;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public abstract class BuilderDialogBase implements OnClickListener, OnKeyListener {
	
	public static final int ACTION_EDIT = 1; 
	public static final int ACTION_CHECK = 2; 
	
	//private Context context;
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
		//this.context = context;
		this.callingView = view;
		this.readyListener = readyListener;
		
		dialog = new AlertDialog.Builder(context).create();
	}
	
	private AlertDialog getDialog() {
		if (! initialized) {
			setupDialog(dialog);
			
			dialog.setButton(AlertDialog.BUTTON_POSITIVE, dialog.getContext().getString(R.string.lblOk), this);
			dialog.setButton(AlertDialog.BUTTON_NEGATIVE, dialog.getContext().getString(R.string.lblCancel), this);

//			//Hack to avoid automatic dismiss on button click.
//			dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//				@Override
//				public void onShow(DialogInterface dlg) {
//					//Hack to avoid automatic dismiss on button click.
//					//Note that "getButton" will return "null" if called before the Dialog is shown
//					dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
//						@Override
//						public void onClick(View v) {
//							BuilderDialogBase.this.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
//						}
//					});
//				}
//			});
			initialized = true;
		}
		return dialog;
	}
	
	public void show() {
		getDialog().show();

		//Avoid automatic dismiss on button click.
		Button buttonPositive = getDialog().getButton(AlertDialog.BUTTON_POSITIVE);
		buttonPositive.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				BuilderDialogBase.this.onClick(getDialog(), DialogInterface.BUTTON_POSITIVE);
			}
		});
		
		//Add keyboard to layout, if needed
		if (keyboard != null) {
			//Look for a suitable layout where to add the keyboard
			ViewGroup root = null;
			ViewParent view = buttonPositive.getParent();
			while (view != null) {
				if (view instanceof LinearLayout) {
					if (((LinearLayout)view).getOrientation() == LinearLayout.VERTICAL) {
						//	if (root == null) {
						root = (ViewGroup) view.getParent();
						break;
						//		root.setBackgroundResource(R.drawable.bg_selector_state_selected); //This is to test where I'm putting the kbd
						//	}
						//	Log.i("show", view.getClass().toString() + ": Vertical");
						//} else {
						//	Log.i("show", view.getClass().toString() + ": Horizontal");
					}
					//} else {
					//	Log.i("show", view.getClass().toString());
				}
				view = view.getParent();
			}
			if (root != null) { //Found a place for the keyboard!
				//Add the keyboard to the layout
				root.addView(getKeyboardLayout());
				
				//Handle "Back" button to hide keyboard
				dialog.setOnKeyListener(this);
				
				keyboard.hideAtStartup(true);
			}
		}
	}

	@Override
	public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (keyboard != null && keyboard.isVisible()) {
				keyboard.hide();
				return true;
			}
		}
		return false;
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
	
	private View keyboardLayout = null;
	private CustomKeyboard keyboard = null;
	
	/**
	 * Register an EditText to be handled with the custom keyboard,
	 * if enabled by configuration.
	 * @param editText {@code EditText} to register.
	 */
	protected void registerEditText(EditText editText) {
		if (QuickDiceApp.getInstance().getPreferences().getCustomKeyboard()) {
			if (keyboard == null) {
				keyboard = new CustomKeyboard(
					dialog,
					getKeyboard(),
					R.xml.kbd_dice);
			}
			keyboard.registerEditText(editText);
		}
	}
	
	private View getKeyboardLayout() {
		if (keyboardLayout == null) {
			LayoutInflater inflater = dialog.getLayoutInflater();
			
			keyboardLayout = inflater.inflate(R.layout.inc_keyboard, null);
		}
		return keyboardLayout;
	}
	
	
	private KeyboardView getKeyboard() {
		return (KeyboardView) getKeyboardLayout().findViewById(R.id.kvwKeyboard);
	}
	
	protected abstract void setupDialog(AlertDialog dialog);
	
	protected abstract int getActionType();

	protected abstract boolean checkExpression();

	protected abstract String getExpression();

}
