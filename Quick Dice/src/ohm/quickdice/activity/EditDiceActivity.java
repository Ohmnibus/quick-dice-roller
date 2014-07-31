package ohm.quickdice.activity;

import ohm.dexp.exception.DException;
import ohm.dexp.exception.DParseException;
import ohm.dexp.exception.UnknownVariable;
import ohm.quickdice.QuickDiceApp;
import ohm.quickdice.R;
import ohm.quickdice.control.SerializationManager;
import ohm.quickdice.dialog.BuilderDialogBase;
import ohm.quickdice.dialog.BuilderDialogBase.ReadyListener;
import ohm.quickdice.dialog.IconPickerDialog;
import ohm.quickdice.entity.Dice;
import ohm.quickdice.entity.DiceBag;
import ohm.quickdice.util.Helper;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class EditDiceActivity extends BaseActivity implements OnClickListener {

	/**
	 * Open the activity to edit an existing dice expression.
	 */
	public static final int ACTIVITY_EDIT = 0x00010001;
	/**
	 * Open the activity to add a new dice expression.
	 */
	public static final int ACTIVITY_ADD = 0x00010002;
	/**
	 * The activity was closed pressing "Ok"
	 */
	public static final int RESULT_OK = 0x00010001;
	/**
	 * The activity was closed pressing "Cancel" or the back button
	 */
	public static final int RESULT_CANCEL = 0x00010002;
	/**
	 * Define the bundle content as {@link Dice}.
	 */
	public static final String BUNDLE_DICE = "Dice";
	/**
	 * Define the bundle content as a type of request ({@code ACTIVITY_EDIT} or {@code ACTIVITY_ADD}).
	 */
	public static final String BUNDLE_REQUEST_TYPE = "RequestType";
	/**
	 * Define the bundle content for the position on which the expression should be added.<br />
	 * This information is used only by the caller.
	 */
	public static final String BUNDLE_POSITION = "Position";
	
	public static final int POSITION_UNDEFINED = -1;
	
	protected DiceBag currentDiceBag;
	protected Dice expression;
	protected int position;
	protected int req;
	protected ImageButton ibtIconPicker;
	protected int currentResIndex;
	protected EditText txtName;
	protected EditText txtDescription;
	//protected Gallery glrResourceIndex;
	protected EditText txtExpression;
	protected Button confirm;
	protected Button cancel;
	protected boolean textChanged;
	protected int initialResIndex;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		setTheme(QuickDiceApp.getInstance().getPreferences().getDialogThemeResId());
		
		super.onCreate(savedInstanceState);

		currentDiceBag = QuickDiceApp.getInstance().getBagManager().getCurrent();

		if (savedInstanceState != null) {
			//expression = SerializationManager.DiceSafe(savedInstanceState.getString(KEY_DICE));
			//position = savedInstanceState.getInt(KEY_POSITION);
			expression = getDice(savedInstanceState);
			position = getDicePosition(savedInstanceState);
			initViews(
					expression == null,
					savedInstanceState.getString(KEY_NAME),
					savedInstanceState.getString(KEY_DESCRIPTION),
					savedInstanceState.getInt(KEY_RES_INDEX),
					savedInstanceState.getString(KEY_EXPRESSION));
			initialResIndex = savedInstanceState.getInt(KEY_INITIAL_RES_INDEX);
			textChanged = savedInstanceState.getBoolean(KEY_TEXT_CHANGED);
		} else {
			Bundle extras = getIntent().getExtras();
			if (extras != null) {
				req = extras.getInt(BUNDLE_REQUEST_TYPE);
				if (req == ACTIVITY_EDIT) {
					//expression = SerializationManager.DiceSafe(extras.getString(BUNDLE_DICE));
					expression = getDice(extras);
				} else {
					expression = null;
				}
//				if (extras.containsKey(BUNDLE_POSITION)) {
//					position = extras.getInt(BUNDLE_POSITION);
//				} else {
//					position = POSITION_UNDEFINED;
//				}
				position = getDicePosition(extras);
			}
			initViews(expression);
		}
	}
	
	//protected static final String KEY_DICE = "KEY_DICE";
	//protected static final String KEY_POSITION = "KEY_POSITION";
	protected static final String KEY_NAME = "KEY_NAME";
	protected static final String KEY_DESCRIPTION = "KEY_DESCRIPTION";
	protected static final String KEY_RES_INDEX = "KEY_RES_INDEX";
	protected static final String KEY_EXPRESSION = "KEY_EXPRESSION";
	protected static final String KEY_TEXT_CHANGED = "KEY_TEXT_CHANGED";
	protected static final String KEY_INITIAL_RES_INDEX = "KEY_INITIAL_RES_INDEX";
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString(BUNDLE_DICE, SerializationManager.DiceSafe(expression));
		outState.putInt(BUNDLE_POSITION, position);
		
		outState.putString(KEY_NAME, txtName.getText().toString());
		outState.putString(KEY_DESCRIPTION, txtDescription.getText().toString());
		outState.putInt(KEY_RES_INDEX, currentResIndex);
		outState.putString(KEY_EXPRESSION, txtExpression.getText().toString());
		outState.putBoolean(KEY_TEXT_CHANGED, textChanged);
		outState.putInt(KEY_INITIAL_RES_INDEX, initialResIndex);
		super.onSaveInstanceState(outState);
	}
	
	private void initViews(Dice exp) {
		expression = exp;

		if (exp == null) {
			initViews(
					true, 
					"", 
					"", 
					0, 
					"");
			initialResIndex = 0;
			textChanged = false;
		} else {
			initViews(
					false,
					exp.getName(),
					exp.getDescription(),
					exp.getResourceIndex(),
					exp.getExpression());
			initialResIndex = expression.getResourceIndex();
			textChanged = false;
		}
	}

	private void initViews(boolean isNew, String name, String description, int resIndex, String exp) {

		setContentView(R.layout.edit_dice_activity);

		if (isNew) {
			setTitle(R.string.mnuAddDice);
		} else {
			setTitle(R.string.mnuEdit);
		}

		ibtIconPicker = (ImageButton)findViewById(R.id.edIconPicker);
		ibtIconPicker.setOnClickListener(this);

		txtName = (EditText) findViewById(R.id.edNameText);
		txtName.setText(name);
		txtName.addTextChangedListener(genericTextWatcher);

		txtDescription = (EditText) findViewById(R.id.edDescText);
		txtDescription.setText(description);
		txtDescription.addTextChangedListener(genericTextWatcher);
		
		txtExpression = (EditText) findViewById(R.id.edExpText);
		txtExpression.setText(exp);
		txtExpression.addTextChangedListener(genericTextWatcher);

		currentResIndex = resIndex;
		initialResIndex = resIndex;
		textChanged = false;

		setCurrentIcon();

		confirm = (Button) findViewById(R.id.btuBarConfirm);
		confirm.setOnClickListener(this);
		cancel = (Button) findViewById(R.id.btuBarCancel);
		cancel.setOnClickListener(this);

		((ImageButton)findViewById(R.id.btuWizard)).setOnClickListener(Helper.getExpressionActionsClickListener(builderReadyListener));
	}

	private TextWatcher genericTextWatcher = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			textChanged = true;
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}
		
		@Override
		public void afterTextChanged(Editable s) {
		}
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btuBarConfirm:
				//Confirm button
				//retExp = readDice();
				//if (retExp == null) {
				//	//The expression is not valid
				//	return;
				//}
				//returnToCaller(retExp, position, RESULT_OK);
				handleConfirmButton();
				break;
			case R.id.btuBarCancel:
				//Cancel button
//				if (dataChanged()) {
//					askDropChanges();
//				} else {
//					returnToCaller(null, position, RESULT_CANCEL);
//				}
				handleCancelButton();
				break;
			case R.id.edIconPicker:
				//Icon picker dialog
				new IconPickerDialog(
						EditDiceActivity.this,
						R.string.lblDiceIconPicker,
						currentResIndex,
						iconPickerReadyListener).show();
				break;
		}
	}

	private BuilderDialogBase.ReadyListener builderReadyListener = new ReadyListener() {
		@Override
		public void ready(View view, boolean confirmed, int action, String diceExpression) {
			if (confirmed) {
				if (action == BuilderDialogBase.ACTION_EDIT) {
					EditText txt;
					int selStart;
					int selEnd;
					String oldDiceExp;
					String newDiceExp;
	
					txt = (EditText) findViewById(R.id.edExpText);
					selStart = txt.getSelectionStart();
					selEnd = txt.getSelectionEnd();
					oldDiceExp = txt.getText().toString();
					
					newDiceExp = oldDiceExp.substring(0, selStart) +
						diceExpression +
						oldDiceExp.substring(selEnd);
					
					txt.setText(newDiceExp);
					txt.setSelection(selStart, selStart + diceExpression.length());
					txt.requestFocus();
				} else {
					Dice retExp = readDice();
					if (retExp != null) {
						//The expression is valid
						Toast.makeText(EditDiceActivity.this, R.string.lblCheckPassed, Toast.LENGTH_SHORT).show();
					}			
				}
			}
		}
	};
	
	private IconPickerDialog.OnIconPickedListener iconPickerReadyListener = new IconPickerDialog.OnIconPickedListener() {
		
		@Override
		public void onIconPicked(boolean confirmed, int iconId) {
			if (confirmed) {
				currentResIndex = iconId;
				setCurrentIcon();
			}
		}
	};
	
	private void setCurrentIcon() {
		ibtIconPicker.setImageDrawable(
				QuickDiceApp.getInstance().getGraphic().getDiceIcon(currentResIndex));
	}
	
	protected Dice readDice() {
		Dice retVal;
		
		retVal = new Dice();
		
		if (expression != null) {
			retVal.setID(expression.getID());
		} else {
			retVal.setID(-1);
		}
		
		retVal.setName(txtName.getText().toString().trim());
		
		if (retVal.getName().length() == 0) {
			txtName.requestFocus();
			retVal = null;
			Toast.makeText(this, R.string.err_name_required, Toast.LENGTH_LONG).show();
		} else {
			retVal.setDescription(txtDescription.getText().toString());
			
			retVal.setResourceIndex(currentResIndex);
			
			retVal.setExpression(txtExpression.getText().toString());
			
			try {
				//Make a dummy roll to check for error.
				retVal.setContext(currentDiceBag);
				retVal.getNewResult();
			} catch (UnknownVariable e) {
				//This issue will be checked later
			} catch (DException e) {
				retVal = null;
				showExpressionError(e);
			}
		}
		
		return retVal;
	}
	
	protected void showExpressionError(DException e) {
		EditText txt;

		txt = (EditText) findViewById(R.id.edExpText);
		
		if (e instanceof DParseException) {
			DParseException ex = (DParseException) e;
			if ((ex.getFromChar() - 1) >= 0 && (ex.getToChar() - 1) < txt.getText().length()) {
				txt.setSelection(ex.getFromChar() - 1, ex.getToChar() - 1);
			}
		} else if (e instanceof UnknownVariable) {
			UnknownVariable ex = (UnknownVariable) e;
			int fromChar = ex.getPosition() - 1;
			int toChar = ex.getPosition() + ex.getName().length() - 1;
			if (fromChar >= 0 && toChar < txt.getText().length()) {
				txt.setSelection(fromChar, toChar);
			}
		}
		txt.requestFocus();
		
		Helper.showErrorToast(this, e);
	}
	
	protected boolean dataChanged() {
		return textChanged || currentResIndex != initialResIndex;
	}
	
	private void returnToCaller(Dice retExp, int position, int result) {
		Bundle bundle = new Bundle();
		bundle.putInt(BUNDLE_POSITION, position);
		//bundle.putSerializable(BUNDLE_DICE_EXPRESSION, retExp);
		bundle.putString(BUNDLE_DICE, SerializationManager.DiceSafe(retExp));
		Intent mIntent = new Intent();
		mIntent.putExtras(bundle);
		mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		setResult(result, mIntent);
		finish();
	}
	
	private void handleCancelButton() {
		if (dataChanged()) {
			new AlertDialog.Builder(this)
				.setTitle(this.getTitle())
				.setMessage(R.string.msgLostChange)
				.setPositiveButton(R.string.lblYes, handleCancelButtonClickListener)
				.setNegativeButton(R.string.lblNo, handleCancelButtonClickListener)
				.show();
		} else {
			returnToCaller(null, position, RESULT_CANCEL);
		}
	}
	
	private DialogInterface.OnClickListener handleCancelButtonClickListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int id) {
			if (id == AlertDialog.BUTTON_POSITIVE) {
				returnToCaller(null, position, RESULT_CANCEL);
			} else if (id == AlertDialog.BUTTON_NEGATIVE) {
				dialog.cancel();
			}
		}
	};

	private void handleConfirmButton() {
		
		Dice dice = readDice();
		if (dice == null) {
			//The expression is not valid
			//Do nothing.
			return;
		}

		String[] badLabels = dice.getUnavailableVariables(currentDiceBag);
		
		if (badLabels.length > 0) {
			//Some variables required by the dice are not available.
			//Show alert.
			String labelChain = "";
			for (String label : badLabels) {
				if (labelChain.length() > 0) {
					labelChain += ", ";
				}
				labelChain += label;
			}
			
			diceToSendBack = dice;
			
			new AlertDialog.Builder(this)
				.setTitle(this.getTitle())
				.setMessage(getString(R.string.msgMissingVar, labelChain))
				.setPositiveButton(R.string.lblYes, handleConfirmButtonClickListener)
				.setNegativeButton(R.string.lblNo, handleConfirmButtonClickListener)
				.show();
		} else {
			returnToCaller(dice, position, RESULT_OK);
		}
	}
	
	private Dice diceToSendBack;
	
	private DialogInterface.OnClickListener handleConfirmButtonClickListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int id) {
			if (id == AlertDialog.BUTTON_POSITIVE) {
				returnToCaller(diceToSendBack, position, RESULT_OK);
			} else if (id == AlertDialog.BUTTON_NEGATIVE) {
				dialog.cancel();
			}
		}
	};

	/**
	 * Launch the {@link EditDiceActivity} to insert a new dice, and then deliver the
	 * result to specified {@link Activity}.
	 * @param activity Activity where to deliver the result.
	 */
	public static void callInsert(Activity activity) {
		callInsert(activity, POSITION_UNDEFINED);
	}
	
	/**
	 * Launch the {@link EditDiceActivity} to insert a new dice, and then deliver the
	 * result to specified {@link Activity}.<br />
	 * The new dice will be added at the specified position.
	 * @param activity Activity where to deliver the result.
	 * @param position Where to put the new dice.
	 */
	public static void callInsert(Activity activity, int position) {
		Intent i = getIntentForInsert(activity, position);
		if (i !=  null) {
			activity.startActivityForResult(i, ACTIVITY_ADD);
		}
	}
	
	protected static Intent getIntentForInsert(Context context) {
		return getIntentForInsert(context, POSITION_UNDEFINED);
	}
	
	protected static Intent getIntentForInsert(Context context, int position) {
		Intent retVal;
		if (! QuickDiceApp.getInstance().canAddDiceBag()) {
			//Maximum number of allowed bags reached
			Toast.makeText(context, R.string.msgMaxBagsReach, Toast.LENGTH_LONG).show();
			return null;
		}
		
		Bundle bundle = new Bundle();
		bundle.putInt(BUNDLE_REQUEST_TYPE, ACTIVITY_ADD);
		bundle.putString(BUNDLE_DICE, null);
		bundle.putInt(BUNDLE_POSITION, position);
		
		retVal = new Intent(context, EditDiceActivity.class);
		retVal.putExtras(bundle);
		
		return retVal;
	}
	
	/**
	 * Launch the {@link EditDiceActivity} to edit an existing dice, and then deliver the
	 * result to specified {@link Activity}.
	 * @param activity Activity where to deliver the result.
	 * @param position Position of the dice to edit.
	 * @param data Dice to edit.
	 */
	public static void callEdit(Activity activity, int position, Dice data) {
		Intent i = getIntentForEdit(activity, position, data);
		if (i !=  null) {
			activity.startActivityForResult(i, ACTIVITY_EDIT);
		}
	}
	
	protected static Intent getIntentForEdit(Context context, int position, Dice data) {
		Intent retVal;
		
		Bundle bundle = new Bundle();
		bundle.putInt(BUNDLE_REQUEST_TYPE, ACTIVITY_EDIT);
		bundle.putString(BUNDLE_DICE, SerializationManager.DiceSafe(data));
		bundle.putInt(BUNDLE_POSITION, position);
		
		retVal = new Intent(context, EditDiceActivity.class);
		retVal.putExtras(bundle);
		
		return retVal;
	}
	
	public static Dice getDice(Intent data) {
		return getDice(data.getExtras());
	}
	
	public static Dice getDice(Bundle data) {
		Dice retVal;
		if (data != null) {
			retVal = SerializationManager.DiceSafe(data.getString(BUNDLE_DICE));
		} else {
			retVal = null;
		}
		return retVal;
	}
	
	public static int getDicePosition(Intent data) {
		return getDicePosition(data.getExtras());
	}
	
	public static int getDicePosition(Bundle data) {
		int retVal;
		if (data != null && data.containsKey(BUNDLE_POSITION)) {
			retVal = data.getInt(BUNDLE_POSITION);
		} else {
			retVal = POSITION_UNDEFINED;
		}
		return retVal;
	}
}
