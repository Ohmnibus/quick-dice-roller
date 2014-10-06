package ohm.quickdice.util;

import ohm.library.compat.CompatMisc;
import ohm.library.widget.KeyboardView;
import ohm.quickdice.QuickDiceApp;
import ohm.quickdice.R;
import ohm.quickdice.entity.FunctionDescriptor;
import ohm.quickdice.entity.VariableCollection;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.os.Build;
import android.text.Editable;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * When an activity hosts a keyboardView, this class allows several {@link EditText}'s to register for it.
 */
public class CustomKeyboard {

	/** A link to the KeyboardView that is used to render this CustomKeyboard. */
	private KeyboardView mKeyboardView;
	
	/** Activity or dialog hosting the keyboard */
	private Host mHost;

	private boolean mHapticFeedback = false;
	
	private boolean mHideAtStartup = false;
	
	private interface Host {
		Context getContext();
		Window getWindow();
	}
	
	private class HostActivity implements Host {
		private Activity activity;
		
		public HostActivity(Activity activity) {
			this.activity = activity;
		}

		@Override
		public Context getContext() {
			return activity;
		}

		@Override
		public Window getWindow() {
			return activity.getWindow();
		}
	}
	
	private class HostDialog implements Host {
		private AlertDialog dialog;
		
		public HostDialog(AlertDialog dialog) {
			this.dialog = dialog;
		}

		@Override
		public Context getContext() {
			return dialog.getContext();
		}

		@Override
		public Window getWindow() {
			return dialog.getWindow();
		}
		
	}

	/** Main keyboard handler. */
	private OnKeyboardActionListener mOnKeyboardActionListener = new OnKeyboardActionListener() {

		public final static int CodeDelete   = -5; // Keyboard.KEYCODE_DELETE
		public final static int CodeFnc      = 55000;
		public final static int CodeVar      = 55001;
		public final static int CodeEnter    = 55002;

		private EditText getTarget() {
			View focusCurrent = mHost.getWindow().getCurrentFocus();
			
			if (focusCurrent instanceof EditText) {
				return (EditText)focusCurrent;
			}
			return null;
		}
		
		@Override
		public void onKey(int primaryCode, int[] keyCodes) {

			EditText editText = getTarget();
			
			if (editText == null) {
				//Nothing to write to.
				return;
			}
			
			Editable editable = editText.getText();
			
			if (editable == null) {
				//It can even happen?
				return;
			}
			
			int selStart = editText.getSelectionStart();
			int selEnd = editText.getSelectionEnd();
			
			if (selStart > selEnd) {
				int tmp = selStart;
				selStart = selEnd;
				selEnd = tmp;
			}
			
			// Apply the key to the EditText
			switch (primaryCode) {
				case CodeDelete:
					if (selStart != selEnd) {
						editable.delete(selStart, selEnd);
					} else if (selStart > 0) {
						editable.delete(selStart - 1, selEnd);
					}
					break;
				case CodeFnc:
					pickFunction();
					break;
				case CodeVar:
					pickVariable();
					break;
				case CodeEnter:
					View next = editText.focusSearch(View.FOCUS_RIGHT);
					//if (next == null) {
					//	next = editText.focusSearch(View.FOCUS_DOWN);
					//}
					
					if (next != null) {
						next.requestFocus();
						Log.d("onKey", "Sent focus to next view.");
					} else {
						hide();
						Log.d("onKey", "Next view not found. Hide kbd.");
					}
					break;
				default:
					//Insert character
					if(selStart != selEnd){
						editable.delete(selStart, selEnd);
					}
					editable.insert(selStart, Character.toString((char) primaryCode));
					break;
			}
		}
		
		private String[] fncNames = null;
		private String[] fncLabels = null;
		
		private void pickFunction() {
			if (fncNames == null) {
				FunctionDescriptor[] fnc = QuickDiceApp.getInstance().getFunctionDescriptors();
				fncNames = new String[fnc.length];
				fncLabels = new String[fnc.length];
				for (int i = 0; i < fnc.length; i++) {
					fncNames[i] = fnc[i].getName();
					fncLabels[i] = fnc[i].getToken();
				}
			}
			
			pickFromList(R.string.lblSelectFunction, fncNames, fncLabels);
		}

		private void pickVariable() {
			//new VariablePickerDialog(mHostActivity, onVariableSelectedListener).show();
			VariableCollection var = QuickDiceApp.getInstance().getBagManager().getCurrent().getVariables();
			if (var.size() == 0) {
				new AlertDialog.Builder(mHost.getContext())
				.setMessage(R.string.lblSelectVariableNone)
				.setPositiveButton(R.string.lblOk, onCancelClickListener)
				.show();
				return;
			}
			
			String[] varNames = null;
			String[] varLabels = null;

			varNames = new String[var.size()];
			varLabels = new String[var.size()];
			for (int i = 0; i < varNames.length; i++) {
				varNames[i] = var.get(i).getName();
				varLabels[i] = var.get(i).getLabel();
			}
			
			pickFromList(R.string.lblSelectVariable, varNames, varLabels);
		}
		
		private String[] curLabels = null;
		
		private void pickFromList(int titleId, String[] names, String[] labels) {
			curLabels = labels;
			AlertDialog.Builder builder = new AlertDialog.Builder(mHost.getContext());
			builder
			.setTitle(titleId)
			.setNegativeButton(R.string.lblCancel, onCancelClickListener)
			.setItems(names, onConfirmClickListener)
			.show();
		}
		
		private DialogInterface.OnClickListener onCancelClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		};
		
		private DialogInterface.OnClickListener onConfirmClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				EditText editText = getTarget();
				
				if (editText != null) {
					Helper.setTextInsideSelection(editText, curLabels[which], false);
				}
			}
		};
		
//		OnItemSelectedListener<Variable> onVariableSelectedListener = new OnItemSelectedListener<Variable>() {
//		@Override
//		public void onItemSelected(boolean confirmed, int itemId, Variable item) {
//			if (confirmed && item != null) {
//
//				View focusCurrent = mHostActivity.getWindow().getCurrentFocus();
//				
//				if (focusCurrent instanceof EditText) {
//					Helper.setTextInsideSelection((EditText)focusCurrent, item.getLabel(), false);
//				}
//			}
//		}
//	};
	
		@SuppressLint("InlinedApi")
		@Override
		public void onPress(int primaryCode) {
			if(mHapticFeedback && primaryCode != 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
				mKeyboardView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
			}
		}

		@Override
		public void onRelease(int primaryCode) {
		}

		@Override
		public void onText(CharSequence text) {
		}

		@Override
		public void swipeDown() {
		}

		@Override
		public void swipeLeft() {
		}

		@Override
		public void swipeRight() {
		}

		@Override
		public void swipeUp() {
		}
	};

	/**
	 * Hide and show the keyboard on focus change.
	 */
	private OnFocusChangeListener onFocusChangeListener = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus) {
				if (! mHideAtStartup) {
					showForView(v);
				} else {
					mHideAtStartup = false;
				}
			} else {
				hide();
			}
		}
	};
	
	/**
	 * Show the keyboard again by tapping on an EditText that already had focus
	 */
	private OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v.isFocused()) {
				showForView(v);
			}
		}
	};
	

	/**
	 * Create a custom keyboard.<br />
	 * It will use the {@link KeyboardView} with resource id {@code viewId}
	 * of the {@code host} activity, loading the keyboard layout from xml file
	 * {@code layoutId}.<br />
	 * Note that the {@code host} activity must have a {@link KeyboardView} in
	 * its layout (typically aligned with the bottom of the activity).<br />
	 * Note that the keyboard layout xml file may include key codes for navigation; 
	 * see the constants in this class for their values.<br />
	 * Note that to enable {@link EditText}'s to use this custom keyboard is 
	 * required to call the {@link #registerEditText(EditText)}.
	 *
	 * @param host The hosting activity.
	 * @param viewId The id of the KeyboardView.
	 * @param layoutId The id of the xml file containing the keyboard layout.
	 */
	public CustomKeyboard(Activity activity, KeyboardView keyboardView, int layoutId) {
		initCustomKeyboard(
				new HostActivity(activity),
				keyboardView,
				layoutId);
	}
	
	public CustomKeyboard(AlertDialog dialog, KeyboardView keyboardView, int layoutId) {
		initCustomKeyboard(
				new HostDialog(dialog),
				keyboardView,
				layoutId);
	}
	
	protected void initCustomKeyboard(Host parent, KeyboardView keyboardView, int layoutId) {
		this.mHost = parent;
		mKeyboardView = keyboardView;
		mKeyboardView.setKeyboard(new Keyboard(this.mHost.getContext(), layoutId));
		mKeyboardView.setPreviewEnabled(false); // NOTE Do not show the preview balloons
		mKeyboardView.setOnKeyboardActionListener(mOnKeyboardActionListener);
		
		// Hide the standard keyboard initially
		this.mHost.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
				| WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
	}
	
	/** Returns whether the CustomKeyboard is visible. */
	public boolean isVisible() {
		return mKeyboardView.getVisibility() == View.VISIBLE;
	}

	/** Make the CustomKeyboard visible, and hide the system keyboard for view v. */
	public void showForView( View v ) {
		mKeyboardView.setVisibility(View.VISIBLE);
		mKeyboardView.setEnabled(true);

		hideSystemKeyboard(v);
	}

	/** Make the CustomKeyboard invisible. */
	public void hide() {
		mKeyboardView.setVisibility(View.GONE);
		mKeyboardView.setEnabled(false);
	}

	/**
	 * Register {@link EditText} for using this custom keyboard.
	 *
	 * @param editText The EditText to register to the custom keyboard.
	 */
	public void registerEditText(EditText editText) {

		editText.setOnFocusChangeListener(onFocusChangeListener);
		
		editText.setOnClickListener(onClickListener);
		
		//editText.setInputType(editText.getInputType() | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		CompatMisc.getInstance().setInputTypeNoKeyboard(editText);
	}
	
	private void hideSystemKeyboard(View v) {
		if (v != null) {
			InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE); 
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		}
	}
	
	/**
	 * Hide the keyboard when the activity is first shown.
	 * @param hide {@code true} to hide the kbd when the activity is first shown, {@code false} otherwise.
	 */
	public void hideAtStartup(boolean hide) {
		mHideAtStartup = true;
	}
}
