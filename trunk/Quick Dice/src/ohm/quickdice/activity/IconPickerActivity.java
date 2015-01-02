package ohm.quickdice.activity;

import ohm.quickdice.QuickDiceApp;
import ohm.quickdice.R;
import ohm.quickdice.adapter.IconAdapter;
import ohm.quickdice.entity.Icon;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class IconPickerActivity extends Activity implements OnClickListener, OnItemClickListener, OnItemLongClickListener {

	/** Open the activity to select an icon. */
	public static final int ACTIVITY_SELECT_ICON = 0x001C0400;

	/** Used to define the resource for the title */
	public static final String BUNDLE_TITLE_ID = "TitleId";
	/** Used to define the title */
	public static final String BUNDLE_TITLE = "Title";
	/** Used to get and set the Icon Id */
	public static final String BUNDLE_ICON_ID = "IconId";
	
	/** Icon Id to use to set selection to none */
	public static final int ICON_UNDEFINED = IconAdapter.ID_ICON_NONE;
	
	private static final int PICK_IMAGE = ACTIVITY_SELECT_ICON | 0xFF;
	
	private String title = null;
	private int defaultIconId = ICON_UNDEFINED;
	
	private GridView gridView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		setTheme(QuickDiceApp.getInstance().getPreferences().getDialogThemeResId());
		
		super.onCreate(savedInstanceState);
		
		//Read data from bundle
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			defaultIconId = getIconIdFromBundle(extras);
			if (extras.containsKey(BUNDLE_TITLE_ID)) {
				title = getString(extras.getInt(BUNDLE_TITLE_ID));
			} else {
				title = extras.getString(BUNDLE_TITLE);
			}
		}


		initViews();
	}
	
	private void initViews() {
		setContentView(R.layout.icon_picker_activity);

		if (title == null) {
			title = getString(R.string.lblIconPicker);
		}
		
		this.setTitle(title);
		
		gridView = (GridView)findViewById(R.id.ipdIcons);
		gridView.setAdapter(new IconAdapter(
				this,
				R.layout.item_icon,
				QuickDiceApp.getInstance().getBagManager().getIconCollection(),
				defaultIconId));
		gridView.setOnItemClickListener(this);
		gridView.setOnItemLongClickListener(this);

		Button btu;
		btu = (Button) findViewById(R.id.btuBarConfirm);
		btu.setOnClickListener(this);
		btu.setText(R.string.lblOk);

		btu = (Button) findViewById(R.id.btuBarCancel);
		btu.setOnClickListener(this);
		btu.setText(R.string.lblCancel);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (id == IconAdapter.ID_ICON_ADDNEW) {
			//Add new icon
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
		} else {
			//Selected an icon
			IconAdapter myAdapter = (IconAdapter)parent.getAdapter();
			myAdapter.setSelectedId((int)id);
			myAdapter.notifyDataSetChanged();
		}
	}
	
	@Override
	public void onClick(View v) {
		int iconId;
		if (v.getId() == R.id.btuBarConfirm) {
			//The dialog has been confirmed
			iconId = ((IconAdapter)gridView.getAdapter()).getSelectedId();
		} else {
			iconId = ICON_UNDEFINED;
		}
		returnToCaller(v.getId() == R.id.btuBarConfirm, iconId);
	}
	
	/** Index of the icon to delete */
	private int iconToDelete = 0;
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		boolean consumed = false;
		Icon icon = ((IconAdapter)gridView.getAdapter()).getItem(position);
		if (icon != null) {
			AlertDialog.Builder bld = new AlertDialog.Builder(this);
			bld.setTitle(R.string.msgRemoveIconTitle);
			//bld.setIcon(icon.getDrawable(this));
			bld.setIcon(QuickDiceApp.getInstance().getBagManager().getIconDrawable((int)id, 32, 32));
			if (icon.isCustom()) {
				//Count instances.
				iconToDelete = position;
				int[] instances = QuickDiceApp.getInstance().getBagManager().getIconInstances((int)id);
				if (instances[0] == 0) {
					bld.setMessage(R.string.msgRemoveIconUnused);
				} else {
					bld.setMessage(getString(R.string.msgRemoveIconUsed, instances[0]));
				}
				bld.setPositiveButton(R.string.lblYes, deleteIconClickListener);
				bld.setNegativeButton(R.string.lblNo, dismissClickListener);
			} else {
				//Message telling that this can't be deleted
				bld.setMessage(R.string.msgRemoveIconSystem);
				bld.setPositiveButton(R.string.lblOk, dismissClickListener);
			}
			bld.show();
			consumed = true;
		} else {
			//This is the add button. Do nothing.
		}
		return consumed;
	}
	
	DialogInterface.OnClickListener deleteIconClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			Icon removed = QuickDiceApp.getInstance().getBagManager().getIconCollection().remove(iconToDelete);
			if (removed != null) {
				removed.recycle(IconPickerActivity.this);
				((IconAdapter)gridView.getAdapter()).notifyDataSetChanged();
			}
			
			dialog.dismiss();
		}
	};
	
	DialogInterface.OnClickListener dismissClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
		}
	};
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
			
			//Get reference to the image
			Cursor cursor = getContentResolver().query(
					data.getData(),
					new String[] { android.provider.MediaStore.Images.ImageColumns.DATA },
					null, null, null);
			cursor.moveToFirst();

			//Link to the image
			String imageFilePath = cursor.getString(0);
			cursor.close();
			
			//Create icon instance
			int iconIdx = -1;
			Icon icon = Icon.newIcon(this, imageFilePath);
			if (icon != null) {
				iconIdx = QuickDiceApp.getInstance().getBagManager().getIconCollection().add(icon);
			}
			
			if (iconIdx >= 0) {
				//Select the icon
				IconAdapter myAdapter = (IconAdapter)gridView.getAdapter();
				myAdapter.setSelectedId(icon.getId());
				myAdapter.notifyDataSetChanged();
			}
		}
	}
	
	private void returnToCaller(boolean confirmed, int iconId) {
		Intent mIntent = new Intent();
		mIntent.putExtra(BUNDLE_ICON_ID, iconId);
		mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		setResult(confirmed ? RESULT_OK : RESULT_CANCELED, mIntent);
		finish();
	}

	/**
	 * Start a {@link IconPickerActivity} requesting a result.<br />
	 * The {@code requestCode} is {@code ACTIVITY_SELECT_ICON}.
	 * @param activity Calling activity.
	 */
	public static void start(Activity activity) {
		start(activity, ICON_UNDEFINED);
	}

	/**
	 * Start a {@link IconPickerActivity} requesting a result.<br />
	 * The {@code requestCode} is {@code ACTIVITY_SELECT_ICON}.
	 * @param activity Calling activity.
	 * @param defaultIconId Identifier of the currently selected icon.
	 */
	public static void start(Activity activity, int defaultIconId) {
		start(activity, defaultIconId, -1);
	}

	/**
	 * Start a {@link IconPickerActivity} requesting a result.<br />
	 * The {@code requestCode} is {@code ACTIVITY_SELECT_ICON}.
	 * @param activity Calling activity.
	 * @param defaultIconId Identifier of the currently selected icon.
	 * @param titleResId String resource ID to use as title.
	 */
	public static void start(Activity activity, int defaultIconId, int titleResId) {
		Intent intent = IconPickerActivity.getIntentForPick(
				activity,
				defaultIconId,
				titleResId);
		activity.startActivityForResult(intent, ACTIVITY_SELECT_ICON);
	}
	
	public static Intent getIntentForPick(Context context) {
		return getIntentForPick(context, ICON_UNDEFINED);
	}
	
	public static Intent getIntentForPick(Context context, int defaultIconId) {
		return getIntentForPick(context, defaultIconId, -1);
	}
	
	public static Intent getIntentForPick(Context context, int defaultIconId, int titleResId) {
		Intent retVal;
		
		retVal = new Intent(context, IconPickerActivity.class);
		if (titleResId > 0) retVal.putExtra(BUNDLE_TITLE_ID, titleResId);
		retVal.putExtra(BUNDLE_ICON_ID, defaultIconId);
		
		return retVal;
	}
	
	public static int getIconIdFromBundle(Intent intent) {
		return getIconIdFromBundle(intent.getExtras());
	}
	
	public static int getIconIdFromBundle(Bundle bundle) {
		if (bundle == null) return ICON_UNDEFINED;
		return bundle.getInt(BUNDLE_ICON_ID, ICON_UNDEFINED);
	}

}
