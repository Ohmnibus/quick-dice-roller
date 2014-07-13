package ohm.quickdice.activity;

import ohm.dexp.DExpression;
import ohm.dexp.exception.DException;
import ohm.dexp.exception.DParseException;
import ohm.quickdice.QuickDiceApp;
import ohm.quickdice.R;
import ohm.quickdice.dialog.BuilderDialogBase;
import ohm.quickdice.dialog.BuilderDialogBase.ReadyListener;
import ohm.quickdice.dialog.IconPickerDialog;
import ohm.quickdice.util.Helper;
import android.app.AlertDialog;
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

public class EditDiceActivity extends BaseActivity {

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
	 * Define the bundle content as {@link DExpression}.
	 */
	public static final String BUNDLE_DICE_EXPRESSION = "DExpression";
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
	
	protected DExpression expression;
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

		if (savedInstanceState != null) {
			expression = (DExpression) savedInstanceState.getSerializable(KEY_DEXPRESSION);
			position = savedInstanceState.getInt(KEY_POSITION);
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
					expression = (DExpression)extras.getSerializable(BUNDLE_DICE_EXPRESSION);
				} else {
					expression = null;
				}
				if (extras.containsKey(BUNDLE_POSITION)) {
					position = extras.getInt(BUNDLE_POSITION);
				} else {
					position = POSITION_UNDEFINED;
				}
			}
			initViews(expression);
		}
	}
	
	protected static final String KEY_DEXPRESSION = "KEY_DEXPRESSION";
	protected static final String KEY_POSITION = "KEY_POSITION";
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
		outState.putSerializable(KEY_DEXPRESSION, expression);
		outState.putInt(KEY_POSITION, position);
		outState.putString(KEY_NAME, txtName.getText().toString());
		outState.putString(KEY_DESCRIPTION, txtDescription.getText().toString());
		//outState.putInt(KEY_RES_INDEX, glrResourceIndex.getSelectedItemPosition());
		outState.putInt(KEY_RES_INDEX, currentResIndex);
		outState.putString(KEY_EXPRESSION, txtExpression.getText().toString());
		outState.putBoolean(KEY_TEXT_CHANGED, textChanged);
		outState.putInt(KEY_INITIAL_RES_INDEX, initialResIndex);
		super.onSaveInstanceState(outState);
	}
	
	private void initViews(DExpression exp) {
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
		ibtIconPicker.setOnClickListener(iconPickerClickListener);

		txtName = (EditText) findViewById(R.id.edNameText);
		txtName.setText(name);
		txtName.addTextChangedListener(genericTextWatcher);

		txtDescription = (EditText) findViewById(R.id.edDescText);
		txtDescription.setText(description);
		txtDescription.addTextChangedListener(genericTextWatcher);
		
//    	glrResourceIndex = (Gallery) findViewById(R.id.edIconGallery);
//    	glrResourceIndex.setAdapter(new IconAdapter(this));
//    	glrResourceIndex.setSelection(resIndex);
		
		txtExpression = (EditText) findViewById(R.id.edExpText);
		txtExpression.setText(exp);
		txtExpression.addTextChangedListener(genericTextWatcher);

		currentResIndex = resIndex;
		initialResIndex = resIndex;
		textChanged = false;

		setCurrentIcon();

		confirm = (Button) findViewById(R.id.btuBarConfirm);
		confirm.setOnClickListener(confirmCancelClickListener);
		cancel = (Button) findViewById(R.id.btuBarCancel);
		cancel.setOnClickListener(confirmCancelClickListener);

		//((ImageButton)findViewById(R.id.btuDiceBuilder)).setOnClickListener(diceBuilderClickListener);
		//((ImageButton)findViewById(R.id.btuHelp)).setOnClickListener(helpClickListener);
		//((ImageButton)findViewById(R.id.btuCheckExpression)).setOnClickListener(checkExpressionClickListener);

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

	//Listener to the confirm and cancel button click
	private OnClickListener confirmCancelClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			DExpression retExp;
			int result;
			if (v == confirm) {
				retExp = readExpression();
				if (retExp == null) {
					//The expression is not valid
					return;
				}
				result = RESULT_OK;
			} else {
				if (dataChanged()) {
					askDropChanges();
					return;
				}
				retExp = null;
				result = RESULT_CANCEL;
			}
			returnToCaller(retExp, position, result);
		}
	};
	

//	private OnClickListener diceBuilderClickListener = new OnClickListener() {
//		@Override
//		public void onClick(View v) {
//			new DiceBuilderDialog(EditDiceActivity.this, v, builderReadyListener).show();
//		}
//	};
//	
//	private OnClickListener helpClickListener = new OnClickListener() {
//		@Override
//		public void onClick(View v) {
//	    	new MarkupDialog(
//	    			EditDiceActivity.this,
//	    			R.string.msgOnlineHelpTitle,
//	    			R.string.msgOnlineHelp,
//	    			0, //R.drawable.ic_launcher,
//	    			null).show();
//		}
//	};
	
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
					DExpression retExp = readExpression();
					if (retExp != null) {
						//The expression is valid
						Toast.makeText(EditDiceActivity.this, R.string.lblCheckPassed, Toast.LENGTH_SHORT).show();
					}			
				}
			}
		}
	};
	
	private OnClickListener iconPickerClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			new IconPickerDialog(
					EditDiceActivity.this,
					R.string.lblDiceIconPicker,
					currentResIndex,
					iconPickerReadyListener).show();
		}
	};
	
	private IconPickerDialog.ReadyListener iconPickerReadyListener = new IconPickerDialog.ReadyListener() {
		
		@Override
		public void ready(boolean confirmed, int iconId) {
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
	
//	private OnClickListener checkExpressionClickListener = new OnClickListener() {
//		@Override
//		public void onClick(View v) {
//			DExpression retExp = readExpression();
//			if (retExp != null) {
//				//The expression is valid
//				Toast.makeText(EditDiceActivity.this, R.string.lblCheckPassed, Toast.LENGTH_SHORT).show();
//			}			
//		}
//	};
	
	protected DExpression readExpression() {
		DExpression retVal;
		//EditText txt;
		//Gallery gallery;
		
		retVal = new DExpression();
		
		if (expression != null) {
			retVal.setID(expression.getID());
		} else {
			retVal.setID(-1);
		}
		
		//txt = (EditText) findViewById(R.id.edNameText);
		//retVal.setName(txt.getText().toString().trim());
		retVal.setName(txtName.getText().toString().trim());
		
		if (retVal.getName().length() == 0) {
			//txt.requestFocus();
			txtName.requestFocus();
			retVal = null;
			Toast.makeText(this, R.string.err_name_required, Toast.LENGTH_LONG).show();
		} else {
			//txt = (EditText) findViewById(R.id.edDescText);
			//retVal.setDescription(txt.getText().toString());
			retVal.setDescription(txtDescription.getText().toString());
			
			//gallery = (Gallery) findViewById(R.id.edIconGallery);
			retVal.setResourceIndex(currentResIndex);
			
			//txt = (EditText) findViewById(R.id.edExpText);
			//retVal.setExpression(txt.getText().toString());
			retVal.setExpression(txtExpression.getText().toString());
			
			try {
				retVal.getResult();
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
		}
		txt.requestFocus();
		
		Helper.showErrorToast(this, e);
	}
	
	protected boolean dataChanged() {
		return textChanged || currentResIndex != initialResIndex;
	}
	
	private void returnToCaller(DExpression retExp, int position, int result) {
		Bundle bundle = new Bundle();
		bundle.putInt(BUNDLE_POSITION, position);
		bundle.putSerializable(BUNDLE_DICE_EXPRESSION, retExp);
		Intent mIntent = new Intent();
		mIntent.putExtras(bundle);
		mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		setResult(result, mIntent);
		finish();
	}
	
	private void askDropChanges() {
		AlertDialog.Builder builder;
		builder = new AlertDialog.Builder(this);
		builder.setTitle(this.getTitle());
		builder.setMessage(R.string.msgLostChange);
		builder.setPositiveButton(
				R.string.lblYes,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						returnToCaller(null, POSITION_UNDEFINED, RESULT_CANCEL);
					}
				});
		builder.setNegativeButton(
				R.string.lblNo,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		builder.create().show();
	}
}
