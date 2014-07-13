package ohm.quickdice.activity;

import ohm.quickdice.QuickDiceApp;
import ohm.quickdice.R;
import ohm.quickdice.dialog.IconPickerDialog;
import ohm.quickdice.entity.DiceBag;
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

public class EditBagActivity extends BaseActivity {

	/**
	 * Open the activity to edit an existing dice bag.
	 */
	public static final int ACTIVITY_EDIT = 0x00030001;
	/**
	 * Open the activity to add a new dice bag.
	 */
	public static final int ACTIVITY_ADD = 0x00030002;
	/**
	 * The activity was closed pressing "Ok"
	 */
	public static final int RESULT_OK = 0x00030001;
	/**
	 * The activity was closed pressing "Cancel" or the back button
	 */
	public static final int RESULT_CANCEL = 0x00030002;
	/**
	 * Define the bundle content as {@link DiceBag}.
	 */
	public static final String BUNDLE_DICE_BAG = "DiceBag";
	/**
	 * Define the bundle content as a type of request ({@code ACTIVITY_EDIT} or {@code ACTIVITY_ADD}).
	 */
	public static final String BUNDLE_REQUEST_TYPE = "RequestType";
	/**
	 * Define the bundle content for the position on which the bag should be added.<br />
	 * This information is used only by the caller.
	 */
	public static final String BUNDLE_POSITION = "Position";
	
	public static final int POSITION_UNDEFINED = -1;
	
	protected DiceBag bag;
	protected int position;
	protected int req;
	protected ImageButton ibtIconPicker;
	protected EditText txtName;
	protected EditText txtDescription;
	protected Button confirm;
	protected Button cancel;
	protected boolean textChanged;
	protected int currentResIndex;
	protected int initialResIndex;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		setTheme(QuickDiceApp.getInstance().getPreferences().getDialogThemeResId());
		
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			bag = (DiceBag) savedInstanceState.getSerializable(KEY_DICEBAG);
			position = savedInstanceState.getInt(KEY_POSITION);
			initViews(
					bag == null,
					savedInstanceState.getString(KEY_NAME),
					savedInstanceState.getString(KEY_DESCRIPTION),
					savedInstanceState.getInt(KEY_RES_INDEX));
			initialResIndex = savedInstanceState.getInt(KEY_INITIAL_RES_INDEX);
			textChanged = savedInstanceState.getBoolean(KEY_TEXT_CHANGED);
		} else {
			Bundle extras = getIntent().getExtras();
			if (extras != null) {
				req = extras.getInt(BUNDLE_REQUEST_TYPE);
				if (req == ACTIVITY_EDIT) {
					bag = (DiceBag)extras.getSerializable(BUNDLE_DICE_BAG);
				} else {
					bag = null;
				}
				if (extras.containsKey(BUNDLE_POSITION)) {
					position = extras.getInt(BUNDLE_POSITION);
				} else {
					position = POSITION_UNDEFINED;
				}
			}
			initViews(bag);
		}
	}

	protected static final String KEY_DICEBAG = "KEY_DICEBAG";
	protected static final String KEY_POSITION = "KEY_POSITION";
	protected static final String KEY_NAME = "KEY_NAME";
	protected static final String KEY_DESCRIPTION = "KEY_DESCRIPTION";
	protected static final String KEY_RES_INDEX = "KEY_RES_INDEX";
	protected static final String KEY_TEXT_CHANGED = "KEY_TEXT_CHANGED";
	protected static final String KEY_INITIAL_RES_INDEX = "KEY_INITIAL_RES_INDEX";
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(KEY_DICEBAG, bag);
		outState.putInt(KEY_POSITION, position);
		outState.putString(KEY_NAME, txtName.getText().toString());
		outState.putString(KEY_DESCRIPTION, txtDescription.getText().toString());
		outState.putInt(KEY_RES_INDEX, currentResIndex);
		outState.putBoolean(KEY_TEXT_CHANGED, textChanged);
		outState.putInt(KEY_INITIAL_RES_INDEX, initialResIndex);
		super.onSaveInstanceState(outState);
	}

	private void initViews(DiceBag db) {
		bag = db;

		if (db == null) {
			initViews(
					true, 
					"", 
					"", 
					0);
			initialResIndex = 0;
			textChanged = false;
		} else {
			initViews(
					false,
					db.getName(),
					db.getDescription(),
					db.getResourceIndex());
			initialResIndex = bag.getResourceIndex();
			textChanged = false;
		}
	}

	private void initViews(boolean isNew, String name, String description, int resIndex) {
		setContentView(R.layout.edit_bag_activity);

		if (isNew) {
			setTitle(R.string.mnuAddDiceBag);
		} else {
			setTitle(R.string.mnuEditDiceBag);
		}

		ibtIconPicker = (ImageButton)findViewById(R.id.ebIconPicker);
		ibtIconPicker.setOnClickListener(iconPickerClickListener);

		txtName = (EditText) findViewById(R.id.ebNameText);
		txtName.setText(name);
		txtName.addTextChangedListener(genericTextWatcher);

		txtDescription = (EditText) findViewById(R.id.ebDescText);
		txtDescription.setText(description);
		txtDescription.addTextChangedListener(genericTextWatcher);
		
		initialResIndex = resIndex;
		currentResIndex = resIndex;
		textChanged = false;

		setCurrentIcon();

		confirm = (Button) findViewById(R.id.btuBarConfirm);
		confirm.setOnClickListener(confirmCancelClickListener);
		cancel = (Button) findViewById(R.id.btuBarCancel);
		cancel.setOnClickListener(confirmCancelClickListener);
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
			DiceBag retVal;
			int result;
			if (v == confirm) {
				retVal = readDiceBag();
				if (retVal == null) {
					//The dice bag is not valid
					return;
				}
				result = RESULT_OK;
			} else {
				if (dataChanged()) {
					askDropChanges();
					return;
				}
				retVal = null;
				result = RESULT_CANCEL;
			}
			returnToCaller(retVal, position, result);
		}
	};
	
	private OnClickListener iconPickerClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			new IconPickerDialog(
					EditBagActivity.this,
					R.string.lblBagIconPicker,
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

	protected DiceBag readDiceBag() {
		DiceBag retVal;
		
		if (txtName.getText().toString().trim().length() == 0) {
			txtName.requestFocus();
			retVal = null;
			Toast.makeText(this, R.string.err_bag_name_required, Toast.LENGTH_LONG).show();
		} else {
			if (bag != null) {
				retVal = bag;
			} else {
				//This should be a call for adding a dice bag.
				retVal = ((QuickDiceApp)getApplication()).getBagManager().getNewDiceBag();
			}
			
			retVal.setName(txtName.getText().toString().trim());

			retVal.setDescription(txtDescription.getText().toString());
			
			retVal.setResourceIndex(currentResIndex);
		}
		
		return retVal;
	}
	
	protected boolean dataChanged() {
		return textChanged || currentResIndex != initialResIndex;
	}
	
	private void returnToCaller(DiceBag retVal, int position, int result) {
		Bundle bundle = new Bundle();
		bundle.putInt(BUNDLE_POSITION, position);
		bundle.putSerializable(BUNDLE_DICE_BAG, retVal);
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
