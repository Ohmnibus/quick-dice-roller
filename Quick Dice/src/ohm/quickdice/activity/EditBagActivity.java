package ohm.quickdice.activity;

import ohm.quickdice.QuickDiceApp;
import ohm.quickdice.R;
import ohm.quickdice.control.SerializationManager;
import ohm.quickdice.entity.DiceBag;
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
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;
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
	protected CheckBox cbFullBag;
	protected Button confirm;
	protected Button cancel;
	protected boolean textChanged;
	protected int currentResIndex;
	protected int initialResIndex;
	protected boolean fullBag;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		setTheme(QuickDiceApp.getInstance().getPreferences().getDialogThemeResId());
		
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			bag = getDiceBag(savedInstanceState);
			position = getDiceBagPosition(savedInstanceState);
			initViews(
					bag == null,
					savedInstanceState.getString(KEY_NAME),
					savedInstanceState.getString(KEY_DESCRIPTION),
					savedInstanceState.getInt(KEY_RES_INDEX),
					savedInstanceState.getBoolean(KEY_FULL_BAG));
			initialResIndex = savedInstanceState.getInt(KEY_INITIAL_RES_INDEX);
			textChanged = savedInstanceState.getBoolean(KEY_TEXT_CHANGED);
		} else {
			Bundle extras = getIntent().getExtras();
			if (extras != null) {
				req = extras.getInt(BUNDLE_REQUEST_TYPE);
				if (req == ACTIVITY_EDIT) {
					bag = getDiceBag(extras);
				} else {
					bag = null;
				}
				position = getDiceBagPosition(extras);
			}
			initViews(bag);
		}
	}

	protected static final String KEY_NAME = "KEY_NAME";
	protected static final String KEY_DESCRIPTION = "KEY_DESCRIPTION";
	protected static final String KEY_RES_INDEX = "KEY_RES_INDEX";
	protected static final String KEY_TEXT_CHANGED = "KEY_TEXT_CHANGED";
	protected static final String KEY_INITIAL_RES_INDEX = "KEY_INITIAL_RES_INDEX";
	protected static final String KEY_FULL_BAG = "KEY_FULL_BAG";
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString(BUNDLE_DICE_BAG, SerializationManager.DiceBagSafe(bag));
		outState.putInt(BUNDLE_POSITION, position);
		
		outState.putString(KEY_NAME, txtName.getText().toString());
		outState.putString(KEY_DESCRIPTION, txtDescription.getText().toString());
		outState.putInt(KEY_RES_INDEX, currentResIndex);
		outState.putBoolean(KEY_TEXT_CHANGED, textChanged);
		outState.putInt(KEY_INITIAL_RES_INDEX, initialResIndex);
		outState.putBoolean(KEY_FULL_BAG, fullBag);
		
		super.onSaveInstanceState(outState);
	}

	private void initViews(DiceBag db) {
		bag = db;

		if (db == null) {
			initViews(
					true, 
					"", 
					"", 
					0,
					true);
			initialResIndex = 0;
			textChanged = false;
		} else {
			initViews(
					false,
					db.getName(),
					db.getDescription(),
					db.getResourceIndex(),
					false);
			initialResIndex = bag.getResourceIndex();
			textChanged = false;
		}
	}

	private void initViews(boolean isNew, String name, String description, int resIndex, boolean fullBag) {
		setContentView(R.layout.edit_bag_activity);

		cbFullBag = (CheckBox)findViewById(R.id.ebFullBag);
		
		if (isNew) {
			setTitle(R.string.mnuAddDiceBag);
			cbFullBag.setVisibility(View.VISIBLE);
			setFullBagChecked(fullBag);
			cbFullBag.setOnCheckedChangeListener(checkedChangeListener);
		} else {
			setTitle(R.string.mnuEditDiceBag);
			cbFullBag.setVisibility(View.GONE);
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

	protected void setFullBagChecked(boolean isChecked) {
		fullBag = isChecked;
		cbFullBag.setChecked(isChecked);
		if (isChecked) {
			cbFullBag.setText(getString(R.string.lblFullBag));
		} else {
			cbFullBag.setText(getString(R.string.lblSmallBag));
			
		}
	}
	
	private OnCheckedChangeListener checkedChangeListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			setFullBagChecked(isChecked);
		}
	};
	
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
				result = RESULT_CANCELED;
			}
			returnToCaller(retVal, position, result);
		}
	};
	
	private OnClickListener iconPickerClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			IconPickerActivity.start(
					EditBagActivity.this,
					currentResIndex,
					R.string.lblBagIconPicker);
		}
	};
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode) {
			case IconPickerActivity.ACTIVITY_SELECT_ICON:
				if (resultCode == RESULT_OK) {
					currentResIndex = IconPickerActivity.getIconIdFromBundle(data);
					setCurrentIcon();
				}
				break;
		}
	};
	
	private void setCurrentIcon() {
		QuickDiceApp.getInstance().getBagManager().setIconDrawable(ibtIconPicker, currentResIndex);
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
				retVal = QuickDiceApp.getInstance().getBagManager().getNewDiceBag(fullBag);
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
		//bundle.putSerializable(BUNDLE_DICE_BAG, retVal);
		bundle.putString(BUNDLE_DICE_BAG, SerializationManager.DiceBagSafe(retVal));
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
	 * Launch the {@link EditBagActivity} to insert a new dice bag, and then deliver the
	 * result to specified {@link Activity}.
	 * @param activity Activity where to deliver the result.
	 */
	public static void callInsert(Activity activity) {
		callInsert(activity, POSITION_UNDEFINED);
	}
	
	/**
	 * Launch the {@link EditBagActivity} to insert a new dice bag, and then deliver the
	 * result to specified {@link Activity}.<br />
	 * The new dice bag will be added at the specified position.
	 * @param activity Activity where to deliver the result.
	 * @param position Where to put the new dice bag.
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
		bundle.putString(BUNDLE_DICE_BAG, null);
		bundle.putInt(BUNDLE_POSITION, position);
		
		retVal = new Intent(context, EditBagActivity.class);
		retVal.putExtras(bundle);
		
		return retVal;
	}
	
	/**
	 * Launch the {@link EditBagActivity} to edit an existing dice bag, and then deliver the
	 * result to specified {@link Activity}.
	 * @param activity Activity where to deliver the result.
	 * @param position Position of the dice bag to edit.
	 * @param data Dice bag to edit.
	 */
	public static void callEdit(Activity activity, int position, DiceBag data) {
		Intent i = getIntentForEdit(activity, position, data);
		if (i !=  null) {
			activity.startActivityForResult(i, ACTIVITY_EDIT);
		}
	}
	
	protected static Intent getIntentForEdit(Context context, int position, DiceBag data) {
		Intent retVal;
		
		Bundle bundle = new Bundle();
		bundle.putInt(BUNDLE_REQUEST_TYPE, ACTIVITY_EDIT);
		bundle.putString(BUNDLE_DICE_BAG, SerializationManager.DiceBagSafe(data)); //TODO: Pass only bag data
		bundle.putInt(BUNDLE_POSITION, position);
		
		retVal = new Intent(context, EditBagActivity.class);
		retVal.putExtras(bundle);
		
		return retVal;
	}
	
	public static DiceBag getDiceBag(Intent data) {
		return getDiceBag(data.getExtras());
	}
	
	public static DiceBag getDiceBag(Bundle data) {
		DiceBag retVal;
		if (data != null) {
			//retVal = (DiceBag)extras.getSerializable(BUNDLE_DICE_BAG);
			retVal = SerializationManager.DiceBagSafe(data.getString(BUNDLE_DICE_BAG));
		} else {
			retVal = null;
		}
		return retVal;
	}
	
	public static int getDiceBagPosition(Intent data) {
		return getDiceBagPosition(data.getExtras());
	}
	
	public static int getDiceBagPosition(Bundle data) {
		int retVal;
		if (data != null && data.containsKey(BUNDLE_POSITION)) {
			retVal = data.getInt(BUNDLE_POSITION);
		} else {
			retVal = POSITION_UNDEFINED;
		}
		return retVal;
	}
}
