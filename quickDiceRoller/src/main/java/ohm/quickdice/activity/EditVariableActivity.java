package ohm.quickdice.activity;

import java.util.Locale;

import ohm.quickdice.QuickDiceApp;
import ohm.quickdice.R;
import ohm.quickdice.control.SerializationManager;
import ohm.quickdice.dialog.NumberPickerDialog;
import ohm.quickdice.entity.IconCollection;
import ohm.quickdice.entity.Variable;
import ohm.quickdice.entity.VariableCollection;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class EditVariableActivity extends BaseActivity
	implements
		View.OnClickListener,
		SeekBar.OnSeekBarChangeListener {

	/**
	 * Open the activity to edit an existing variable.
	 */
	public static final int ACTIVITY_EDIT = 0x00050001;
	/**
	 * Open the activity to add a new variable.
	 */
	public static final int ACTIVITY_ADD = 0x00050002;
	/**
	 * Define the bundle content as {@link Variable}.
	 */
	public static final String BUNDLE_VARIABLE = "Var";
	/**
	 * Define the bundle content as a type of request ({@code ACTIVITY_EDIT} or {@code ACTIVITY_ADD}).
	 */
	public static final String BUNDLE_REQUEST_TYPE = "RequestType";
	/**
	 * Define the bundle content for the position on which the expression should be added.<br />
	 * This information is used only by the caller.
	 */
	public static final String BUNDLE_POSITION = "Pos";
	
	public static final int POSITION_UNDEFINED = -1;
	
	protected Variable variable;
	protected int position;
	protected int req;
	protected ImageButton ibtIconPicker;
	protected int currentResIndex;
	protected EditText txtName;
	protected EditText txtDescription;
	protected EditText txtLabel;
	protected Button cmdMinVal;
	protected SeekBar sbCurVal;
	protected TextView lblCurVal;
	protected Button cmdMaxVal;
	protected int minVal;
	protected int curVal;
	protected int maxVal;
	protected boolean textChanged;
	protected boolean valChanged;
	protected int initialResIndex;
	protected String initialLabel;

	//protected static final String KEY_VARIABLE = "KEY_VARIABLE";
	//protected static final String KEY_POSITION = "KEY_POSITION";
	protected static final String KEY_NAME = "KEY_NAME";
	protected static final String KEY_DESCRIPTION = "KEY_DESCRIPTION";
	protected static final String KEY_RES_INDEX = "KEY_RES_INDEX";
	protected static final String KEY_LABEL = "KEY_LABEL";
	protected static final String KEY_MIN_VAL = "KEY_MIN_VAL";
	protected static final String KEY_CUR_VAL = "KEY_CUR_VAL";
	protected static final String KEY_MAX_VAL = "KEY_MAX_VAL";
	protected static final String KEY_TEXT_CHANGED = "KEY_TEXT_CHANGED";
	protected static final String KEY_VAL_CHANGED = "KEY_VAL_CHANGED";
	protected static final String KEY_INITIAL_RES_INDEX = "KEY_INITIAL_RES_INDEX";
	protected static final String KEY_INITIAL_LABEL = "KEY_INITIAL_LABEL";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		setTheme(QuickDiceApp.getInstance().getPreferences().getDialogThemeResId());
		
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState != null) {
			//variable = SerializationManager.VariableSafe(savedInstanceState.getString(KEY_VARIABLE));
			variable = getVariableData(savedInstanceState);
			position = getVariablePosition(savedInstanceState);
			initViews(savedInstanceState, variable == null);
			initialResIndex = savedInstanceState.getInt(KEY_INITIAL_RES_INDEX);
			initialLabel = savedInstanceState.getString(KEY_INITIAL_LABEL);
			textChanged = savedInstanceState.getBoolean(KEY_TEXT_CHANGED);
			valChanged = savedInstanceState.getBoolean(KEY_VAL_CHANGED);
		} else {
			Bundle extras = getIntent().getExtras();
			if (extras != null) {
				req = extras.getInt(BUNDLE_REQUEST_TYPE);
				if (req == ACTIVITY_EDIT) {
					//variable = SerializationManager.VariableSafe(extras.getString(KEY_VARIABLE));
					variable = getVariableData(extras);
				} else {
					variable = null;
				}
//				if (extras.containsKey(BUNDLE_POSITION)) {
//					position = extras.getInt(BUNDLE_POSITION);
//				} else {
//					position = POSITION_UNDEFINED;
//				}
				position = getVariablePosition(extras);
			}
			initViews(variable);
			if (variable != null) {
				initialResIndex = variable.getResourceIndex();
				initialLabel = variable.getLabel();
				if (initialLabel != null) {
					initialLabel = initialLabel.toLowerCase(Locale.getDefault());
				} else {
					initialLabel = "";
				}
			} else {
				initialResIndex = 0;
				initialLabel = "";
			}
			textChanged = false;
			valChanged = false;
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString(BUNDLE_VARIABLE, SerializationManager.VariableSafe(variable));
		outState.putInt(BUNDLE_POSITION, position);
		
		outState.putString(KEY_NAME, txtName.getText().toString());
		outState.putString(KEY_DESCRIPTION, txtDescription.getText().toString());
		outState.putInt(KEY_RES_INDEX, currentResIndex);
		outState.putString(KEY_LABEL, txtLabel.getText().toString());
		outState.putInt(KEY_MIN_VAL, minVal);
		outState.putInt(KEY_CUR_VAL, curVal);
		outState.putInt(KEY_MAX_VAL, maxVal);
		outState.putBoolean(KEY_TEXT_CHANGED, textChanged);
		outState.putBoolean(KEY_VAL_CHANGED, valChanged);
		outState.putInt(KEY_INITIAL_RES_INDEX, initialResIndex);
		outState.putString(KEY_INITIAL_LABEL, initialLabel);
		super.onSaveInstanceState(outState);
	}
	
	private void initViews(Variable var) {
		Bundle bnd = new Bundle();
		variable = var;
		if (var != null) {
			bnd.putString(KEY_NAME, var.getName());
			bnd.putString(KEY_DESCRIPTION, var.getDescription());
			bnd.putInt(KEY_RES_INDEX, var.getResourceIndex());
			bnd.putString(KEY_LABEL, var.getLabel());
			bnd.putInt(KEY_MIN_VAL, var.getMinVal());
			bnd.putInt(KEY_CUR_VAL, var.getCurVal());
			bnd.putInt(KEY_MAX_VAL, var.getMaxVal());
		}
		initViews(bnd, var == null);
	}
	
	private void initViews(Bundle bundle, boolean isNew) {

		setContentView(R.layout.edit_variable_activity);

		if (isNew) {
			setTitle(R.string.mnuAddVariable);
		} else {
			setTitle(R.string.mnuEditVariable);
		}
		
		ibtIconPicker = (ImageButton)findViewById(R.id.cmdIconPicker);
		ibtIconPicker.setOnClickListener(this);
		
		txtName = (EditText) findViewById(R.id.txtName);
		txtName.setText(getString(bundle, KEY_NAME, ""));
		txtName.addTextChangedListener(genericTextWatcher);

		txtDescription = (EditText) findViewById(R.id.txtDesc);
		txtDescription.setText(getString(bundle, KEY_DESCRIPTION, ""));
		txtDescription.addTextChangedListener(genericTextWatcher);

		txtLabel = (EditText) findViewById(R.id.txtLabel);
		txtLabel.setText(getString(bundle, KEY_LABEL, ""));
		txtLabel.addTextChangedListener(genericTextWatcher);

		currentResIndex = getInt(bundle, KEY_RES_INDEX, IconCollection.ID_ICON_DEFAULT);
		minVal = getInt(bundle, KEY_MIN_VAL, 0);
		curVal = getInt(bundle, KEY_CUR_VAL, 5);
		maxVal = getInt(bundle, KEY_MAX_VAL, 20);

		lblCurVal = (TextView)findViewById(R.id.lblCurrent);
		cmdMinVal = (Button)findViewById(R.id.cmdMin);
		cmdMinVal.setOnClickListener(this);
		sbCurVal = (SeekBar)findViewById(R.id.sbCurrent);
		sbCurVal.setOnSeekBarChangeListener(this);
		cmdMaxVal = (Button)findViewById(R.id.cmdMax);
		cmdMaxVal.setOnClickListener(this);

		setValues();
		setCurrentIcon();

		findViewById(R.id.btuBarConfirm).setOnClickListener(this);
		findViewById(R.id.btuBarCancel).setOnClickListener(this);
	}
	
	private String getString(Bundle bundle, String key, String defValue) {
		if (bundle.containsKey(key)) {
			return bundle.getString(key);
		} else {
			return defValue;
		}
	}
	
	private int getInt(Bundle bundle, String key, int defValue) {
		if (bundle.containsKey(key)) {
			return bundle.getInt(key);
		} else {
			return defValue;
		}
	}

	@Override
	public void onClick(View v) {
		Variable retVal;
		switch (v.getId()) {
			case R.id.cmdIconPicker:
				IconPickerActivity.start(
						this,
						currentResIndex,
						R.string.lblVariableIconPicker);
				break;
			case R.id.cmdMin:
				//new ModifierBuilderDialog(this, position, minValueReadyListener).show();
				new NumberPickerDialog(
						this,
						R.string.lblMinimum,
						R.string.lblSelectMinValue,
						minVal,
						3,
						minValuePickedListener).show();
				break;
			case R.id.cmdMax:
				//new ModifierBuilderDialog(this, position, maxValueReadyListener).show();
				new NumberPickerDialog(
						this,
						R.string.lblMaximum,
						R.string.lblSelectMaxValue,
						maxVal,
						3,
						maxValuePickedListener).show();
				break;
			case R.id.btuBarConfirm:
				retVal = readVariable();
				if (retVal == null) {
					//The variable is not valid
					return;
				}
				returnToCaller(retVal, position, RESULT_OK);
				break;
			case R.id.btuBarCancel:
				if (dataChanged()) {
					askDropChanges();
					return;
				}
				returnToCaller(null, position, RESULT_CANCELED);
				break;
		}
	}
	
	private NumberPickerDialog.OnNumberPickedListener minValuePickedListener = new NumberPickerDialog.OnNumberPickedListener() {
		@Override
		public void onNumberPicked(boolean confirmed, int value) {
			if (confirmed && minVal != value) {
				valChanged = true;
				minVal = value;
				setValues();
			}
		}
	};
	
	private NumberPickerDialog.OnNumberPickedListener maxValuePickedListener = new NumberPickerDialog.OnNumberPickedListener() {
		@Override
		public void onNumberPicked(boolean confirmed, int value) {
			if (confirmed && maxVal != value) {
				valChanged = true;
				maxVal = value;
				setValues();
			}
		}
	};
	
	protected boolean dataChanged() {
		return textChanged || valChanged || currentResIndex != initialResIndex;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case IconPickerActivity.ACTIVITY_SELECT_ICON:
				if (resultCode == RESULT_OK) {
					currentResIndex = IconPickerActivity.getIconIdFromBundle(data);
					setCurrentIcon();
				}
				break;
		}
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

	private void setValues() {
		if (maxVal < minVal) {
			int tmp = maxVal;
			maxVal = minVal;
			minVal = tmp;
		}
		if (curVal < minVal) {
			curVal = minVal;
		} else if (curVal > maxVal) {
			curVal = maxVal;
		}
		cmdMinVal.setText(Integer.toString(minVal));
		lblCurVal.setText(Integer.toString(curVal));
		cmdMaxVal.setText(Integer.toString(maxVal));

		sbCurVal.setMax(maxVal-minVal);
		sbCurVal.setProgress(curVal - minVal);
	};

	private void setCurrentIcon() {
//		ibtIconPicker.setImageDrawable(
//				QuickDiceApp.getInstance().getGraphic().getDiceIcon(currentResIndex));
//		ibtIconPicker.setImageDrawable(
//				QuickDiceApp.getInstance().getBagManager().getIconDrawable(currentResIndex));
		QuickDiceApp.getInstance().getBagManager().setIconDrawable(ibtIconPicker, currentResIndex);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (fromUser) {
			curVal = progress + minVal;
			valChanged = true;
			lblCurVal.setText(Integer.toString(curVal));
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}

	protected Variable readVariable() {
		Variable retVal;
		
		retVal = new Variable();
		
		if (variable != null) {
			retVal.setID(variable.getID());
		} else {
			retVal.setID(-1);
		}
		
		retVal.setName(txtName.getText().toString().trim());
		
		if (retVal.getName().length() == 0) {
			txtName.requestFocus();
			Toast.makeText(this, R.string.err_var_name_required, Toast.LENGTH_LONG).show();
			return null;
		}
		
		retVal.setLabel(txtLabel.getText().toString());

		if (retVal.getLabel().length() < 2 || retVal.getLabel().length() > 5) {
			txtLabel.requestFocus();
			Toast.makeText(this, R.string.err_var_label_required, Toast.LENGTH_LONG).show();
			return null;
		}
		if (! checkLabelChar(retVal.getLabel())) {
			txtLabel.requestFocus();
			Toast.makeText(this, R.string.err_var_label_invalid_char, Toast.LENGTH_LONG).show();
			return null;
		}
		if (! retVal.matchLabel(initialLabel)) {
			//Either a new variable or label is changed.
			VariableCollection collection = QuickDiceApp.getInstance().getBagManager().getCurrent().getVariables();
			Variable duplicate = collection.getByLabel(retVal.getLabel());
			if (duplicate != null) {
				Toast.makeText(this, getString(R.string.err_var_label_exists,
						retVal.getLabel(),
						duplicate.getName()), Toast.LENGTH_LONG).show();
				return null;
			}
		}

		retVal.setDescription(txtDescription.getText().toString());
		
		retVal.setResourceIndex(currentResIndex);
		
		retVal.setMinVal(minVal);
		retVal.setCurVal(curVal);
		retVal.setMaxVal(maxVal);
		
		return retVal;
	}
	
	private boolean checkLabelChar(String label) {
		//TODO: Don't allow to enter invalid characters
		for (int i = 0; i < label.length(); i++) {
			char c = label.charAt(i);
			if (!((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z'))) {
				return false;
			}
		}
		return true;
	}
	
	private void returnToCaller(Variable retVal, int position, int result) {
		Bundle bundle = new Bundle();
		bundle.putInt(BUNDLE_POSITION, position);
		bundle.putString(BUNDLE_VARIABLE, SerializationManager.VariableSafe(retVal));
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
						returnToCaller(null, POSITION_UNDEFINED, RESULT_CANCELED);
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
	
	/**
	 * Launch the {@link EditVariableActivity} to insert a new variable, and then deliver the
	 * result to specified {@link Activity}.
	 * @param activity Activity where to deliver the result.
	 */
	public static void callInsert(Activity activity) {
		callInsert(activity, POSITION_UNDEFINED);
	}
	
	/**
	 * Launch the {@link EditVariableActivity} to insert a new variable, and then deliver the
	 * result to specified {@link Activity}.<br />
	 * The new variable will be added at the specified position.
	 * @param activity Activity where to deliver the result.
	 * @param position Where to put the new variable.
	 */
	public static void callInsert(Activity activity, int position) {
		Intent i = getIntentForInsert(activity, position);
		if (i != null) {
			activity.startActivityForResult(i, ACTIVITY_ADD);
		}
	}
	
	protected static Intent getIntentForInsert(Context context) {
		return getIntentForInsert(context, POSITION_UNDEFINED);
	}
	
	protected static Intent getIntentForInsert(Context context, int position) {
		Intent retVal;
		if (! QuickDiceApp.getInstance().canAddVariable()) {
			//Maximum number of allowed variables reached
			Toast.makeText(context, R.string.msgMaxVariablesReach, Toast.LENGTH_LONG).show();
			return null;
		}
		
		Bundle bundle = new Bundle();
		bundle.putInt(BUNDLE_REQUEST_TYPE, ACTIVITY_ADD);
		bundle.putString(BUNDLE_VARIABLE, null);
		bundle.putInt(BUNDLE_POSITION, position);
		
		retVal = new Intent(context, EditVariableActivity.class);
		retVal.putExtras(bundle);
		
		return retVal;
	}
	
	/**
	 * Launch the {@link EditVariableActivity} to edit an existing variable, and then deliver the
	 * result to specified {@link Activity}.
	 * @param activity Activity where to deliver the result.
	 * @param position Position of the variable to edit.
	 * @param data Variable to edit.
	 */
	public static void callEdit(Activity activity, int position, Variable data) {
		Intent i = getIntentForEdit(activity, position, data);
		if (i !=  null) {
			activity.startActivityForResult(i, ACTIVITY_EDIT);
		}
	}
	
	protected static Intent getIntentForEdit(Context context, int position, Variable data) {
		Intent retVal;
		
		Bundle bundle = new Bundle();
		bundle.putInt(BUNDLE_REQUEST_TYPE, ACTIVITY_EDIT);
		bundle.putString(BUNDLE_VARIABLE, SerializationManager.VariableSafe(data));
		bundle.putInt(BUNDLE_POSITION, position);
		
		retVal = new Intent(context, EditVariableActivity.class);
		retVal.putExtras(bundle);
		
		return retVal;
	}
	
	public static Variable getVariableData(Intent data) {
		return getVariableData(data.getExtras());
	}
	
	public static Variable getVariableData(Bundle data) {
		Variable retVal;
		if (data != null) {
			retVal = SerializationManager.VariableSafe(data.getString(BUNDLE_VARIABLE));
		} else {
			retVal = null;
		}
		return retVal;
	}
	
	public static int getVariablePosition(Intent data) {
		return getVariablePosition(data.getExtras());
	}
	
	public static int getVariablePosition(Bundle data) {
		int retVal;
		if (data != null && data.containsKey(BUNDLE_POSITION)) {
			retVal = data.getInt(BUNDLE_POSITION);
		} else {
			retVal = POSITION_UNDEFINED;
		}
		return retVal;
	}
}
