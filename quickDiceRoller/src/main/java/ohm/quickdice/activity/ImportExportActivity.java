package ohm.quickdice.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import ohm.library.compat.CompatFileProvider;
import ohm.library.compat.CompatIntent;
import ohm.library.compat.CompatIntent.OnEvalResolveInfoListener;
import ohm.quickdice.QuickDiceApp;
import ohm.quickdice.R;
import ohm.quickdice.adapter.MostRecentFilesAdapter;
import ohm.quickdice.control.DiceBagManager;
import ohm.quickdice.control.SerializationManager;
import ohm.quickdice.dialog.DiceBagPickerDialog;
import ohm.quickdice.dialog.DiceBagPickerDialog.OnItemPickedListener;
import ohm.quickdice.entity.Dice;
import ohm.quickdice.entity.DiceBag;
import ohm.quickdice.entity.DiceBagCollection;
import ohm.quickdice.entity.Icon;
import ohm.quickdice.entity.IconCollection;
import ohm.quickdice.entity.MostRecentFile;
import ohm.quickdice.entity.Variable;
import ohm.quickdice.util.Helper;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.BadParcelableException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class ImportExportActivity extends BaseActivity implements OnClickListener, OnItemClickListener {

	/**
	 * Open the activity to import or export definitions.
	 */
	public static final int ACTIVITY_IMPORT_EXPORT = 0x00040000;
	/**
	 * The activity was closed after importing dice definitions
	 */
	public static final int RESULT_IMPORT = 0x00040001;
	/**
	 * The activity was closed after and importing dice definitions failed
	 */
	public static final int RESULT_IMPORT_FAILED = 0x00040004;
	/**
	 * The activity was closed after exporting dice definitions
	 */
	public static final int RESULT_EXPORT = 0x00040002;
	/**
	 * Define the bundle content as a type of request ({@code ACTIVITY_IMPORT_EXPORT}).
	 */
	public static final String BUNDLE_REQUEST_TYPE = "RequestType";
	
	private static final String CACHE_EXPORT_PATH = "temp/";
	private static final String FILE_EXTENSION = ".qdr.json";
	private static final String DEFAULT_FILE_NAME = "DiceDef" + FILE_EXTENSION;
	private static final String KEY_RECENT_FILES = "KEY_RECENT_FILES";
	private static final String KEY_TARGET_URI = "KEY_TARGET_URI";
	private static final int MAX_RECENT_FILES = 16;
	private static final int REQUEST_GET_CONTENT = 0x00000000;
	private static final int REQUEST_SEND = 0x00000001;
	private static final int REQUEST_MERGE_CONTENT = 0x00000002;

	ListView lstRecentFiles;
	Button btuMerge;
	Button btuImport;
	Button btuExport;
	Button btuCancel;
	SharedPreferences config = null;
	ArrayList<MostRecentFile> recentFiles;
	Uri targetUri = null; //Reference to the sent resource, used to remove permissions
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		setTheme(QuickDiceApp.getInstance().getPreferences().getDialogThemeResId());
		
		super.onCreate(savedInstanceState);
		
		config = PreferenceManager.getDefaultSharedPreferences(this);

		recentFiles = loadRecentFiles();
		
		initViews();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(KEY_TARGET_URI, targetUri);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		targetUri = savedInstanceState.getParcelable(KEY_TARGET_URI);
	}

	private void initViews() {
		setContentView(R.layout.import_export_activity);
		
		lstRecentFiles = (ListView)findViewById(R.id.eiRecentList);
		lstRecentFiles.setAdapter(new MostRecentFilesAdapter(
				this,
				R.layout.import_export_item,
				recentFiles));
		lstRecentFiles.setOnItemClickListener(this);
		
		Drawable drawable;
		
		btuMerge = (Button)findViewById(R.id.eiMerge);
		//drawable = Helper.resizeDrawable(this, R.drawable.ic_import, 64, 32);
		//drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		drawable = Helper.boundedDrawable(this, R.drawable.ic_import, R.dimen.import_export_icon_width, R.dimen.import_export_icon_height);
		btuMerge.setCompoundDrawables(drawable, null, null, null);
		btuMerge.setOnClickListener(this);
		
		btuImport = (Button)findViewById(R.id.eiImport);
		//drawable = Helper.resizeDrawable(this, R.drawable.ic_import, 64, 32);
		//drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		drawable = Helper.boundedDrawable(this, R.drawable.ic_import, R.dimen.import_export_icon_width, R.dimen.import_export_icon_height);
		btuImport.setCompoundDrawables(drawable, null, null, null);
		btuImport.setOnClickListener(this);
		
		btuExport = (Button)findViewById(R.id.eiExport);
		//drawable = Helper.resizeDrawable(this, R.drawable.ic_export, 64, 32);
		//drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		drawable = Helper.boundedDrawable(this, R.drawable.ic_export, R.dimen.import_export_icon_width, R.dimen.import_export_icon_height);
		btuExport.setCompoundDrawables(drawable, null, null, null);
		btuExport.setOnClickListener(this);
		
		btuCancel = (Button)findViewById(R.id.btuBarCancel);
		btuCancel.setOnClickListener(this);
		
		findViewById(R.id.btuBarConfirm).setVisibility(View.GONE);
		//findViewById(R.id.vwDivider).setVisibility(View.GONE);
	}
	
	public void onItemClick(AdapterView<?> adapter,
			View view,
			int position,
			long id) {
		
		MostRecentFile recentFile = recentFiles.get(position);
		//doImport(recentFile.getPath());
		Intent intent = new Intent();
		intent.setData(Uri.parse(recentFile.getUri()));
		intent.putExtra(FilePickerActivity.EXTRA_USE_FOR_MRU, true);
		confirmImport(intent);
	};
	
	public void onClick(View v) {
		if (v.getId() == btuMerge.getId()) {
			onMergeClick(v);
		} else if (v.getId() == btuImport.getId()) {
			onImportClick(v);
		} else if (v.getId() == btuExport.getId()) {
			onExportClick(v);
		} else if (v.getId() == btuCancel.getId()) {
			returnToCaller(RESULT_CANCELED);
		}
	};
	
	private void onMergeClick(View v) {
		
		Intent chooser = prepareChooser(R.string.lblMergeDiceDef);
		
		startActivityForResult(chooser, REQUEST_MERGE_CONTENT);
	}
	
	private void onImportClick(View v) {
		
		Intent chooser = prepareChooser(R.string.lblImportDiceDef);
		
		startActivityForResult(chooser, REQUEST_GET_CONTENT);
	}
	
	private Intent prepareChooser(int titleResId) {
//		Bundle bundle = new Bundle();
//		bundle.putInt(FilePickerActivity.BUNDLE_REQUEST_TYPE, FilePickerActivity.ACTIVITY_SELECT_FILE);
//		bundle.putStringArray(FilePickerActivity.BUNDLE_FILTER_EXTENSION_LIST, new String[] {FILE_EXTENSION});
//		Intent i = new Intent(ImportExportActivity.this, FilePickerActivity.class);
//		i.putExtras(bundle);
//		startActivityForResult(i, FilePickerActivity.ACTIVITY_SELECT_FILE);
		
		//1-Create the intent to pick the file
		Intent baseIntent = new Intent();
		baseIntent.setAction(Intent.ACTION_GET_CONTENT);
		baseIntent.setType("application/json");
		
		//2-Create the extra intent to be added to the intent chooser
		Intent extraIntent = new Intent(ImportExportActivity.this, FilePickerActivity.class);
		extraIntent.setAction(baseIntent.getAction());
		extraIntent.setType(baseIntent.getType());

		//4-Invoke the intent chooser
		//startChooserForResult(baseIntent, REQUEST_GET_CONTENT, R.string.lblImportDiceDef, extraIntent);
		targetUri = null;
		Intent chooser = CompatIntent.createChooser(
				this,
				baseIntent,
				getString(titleResId),
				extraIntent,
				null);
		
		return chooser;
	}
	
	private void onExportClick(View v) {
//			Bundle bundle = new Bundle();
//			bundle.putInt(FilePickerActivity.BUNDLE_REQUEST_TYPE, FilePickerActivity.ACTIVITY_NEW_FILE);
//			bundle.putStringArray(FilePickerActivity.BUNDLE_FILTER_EXTENSION_LIST, new String[] {FILE_EXTENSION});
//			bundle.putString(FilePickerActivity.BUNDLE_DEFAULT_FILE_NAME, DEFAULT_FILE_NAME);
//			Intent i = new Intent(ImportExportActivity.this, FilePickerActivity.class);
//			i.putExtras(bundle);
//			startActivityForResult(i, FilePickerActivity.ACTIVITY_NEW_FILE);
			
		//1-Create file with data to be exported
		Uri contentUri = prepareTempFile();

		if (contentUri != null) {
			//2-Create the intent to send the file
			Intent baseIntent = new Intent();
			baseIntent.setAction(Intent.ACTION_SEND);
			baseIntent.setType("application/json");
			//baseIntent.setDataAndType(contentUri, "*/*");
			//baseIntent.setDataAndType(contentUri, "application/json");
			baseIntent.putExtra(Intent.EXTRA_SUBJECT, contentUri.getLastPathSegment());
			baseIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
			baseIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //This is useless
			
			//3-Create the extra intent to be added to the intent chooser
			Intent extraIntent = new Intent(ImportExportActivity.this, FilePickerActivity.class);
			extraIntent.setAction(baseIntent.getAction());
			extraIntent.setType(baseIntent.getType());
			//extraIntent.setDataAndType(baseIntent.getData(), baseIntent.getType());
			extraIntent.putExtra(Intent.EXTRA_STREAM, baseIntent.getParcelableExtra(Intent.EXTRA_STREAM));
			extraIntent.setFlags(baseIntent.getFlags());
	
			//4-Invoke the intent chooser
			//startChooserForResult(baseIntent, REQUEST_SEND, R.string.lblExportDiceDef, extraIntent);
			targetUri = contentUri;
			Intent chooser = CompatIntent.createChooser(
					this,
					baseIntent,
					getString(R.string.lblExportDiceDef),
					extraIntent,
					onEvalResolveInfoListener);
			startActivityForResult(chooser, REQUEST_SEND);
		} else {
			//Cannot save temporary file.
			Toast.makeText(this, R.string.err_cannot_export, Toast.LENGTH_LONG).show();
		}
	}
	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_SEND:
				if (targetUri != null) {
					this.revokeUriPermission(targetUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
					//TODO: Optionally delete file
				}
				if (resultCode == RESULT_OK) {
					boolean useForMru = false;
					try {
						useForMru = data.getBooleanExtra(FilePickerActivity.EXTRA_USE_FOR_MRU, false);
					} catch (BadParcelableException ex) {
						//This is caused by DropBox and I don't know how to prevent it
						Log.w("ImportExportActivity", "Cannot read " + FilePickerActivity.EXTRA_USE_FOR_MRU, ex);
						useForMru = false;
					}
					if (useForMru) {
						MostRecentFile mru = createRecentFileInstance(data.getData());
						updateRecentFileList(mru);
						saveRecentFiles(recentFiles);
					}
					returnToCaller(RESULT_EXPORT);
				}
				break;
			case REQUEST_GET_CONTENT:
				if (resultCode == RESULT_OK) {
					//File selected. Need to import.
					confirmImport(data);
				}
				break;
			case REQUEST_MERGE_CONTENT:
				if (resultCode == RESULT_OK) {
					//File selected. Need to merge.
					doMerge(data);
				}
				break;
		}
	};
	
	protected void returnToCaller(int result) {
		Intent mIntent = new Intent();
		mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		setResult(result, mIntent);
		finish();
	}
	
//	protected String getPath(Intent intent) {
//		Bundle extras = intent.getExtras();
//		return extras.getString(FilePickerActivity.BUNDLE_RESULT_PATH);
//	}
	
	/**
	 * Store definitions to a temporary file
	 * and return its Uri computed by {@link CompatFileProvider}.
	 * @return Uri for the temporary file, or {@code null} if an error occurred.
	 */
	private Uri prepareTempFile() {
		Uri retVal = null;
		
		File tempFile = getCacheDir();
		tempFile = new File(tempFile, CACHE_EXPORT_PATH);
		if (! tempFile.exists()) {
			tempFile.mkdirs();
		}
		
		tempFile = new File(tempFile, DEFAULT_FILE_NAME);
		QuickDiceApp app = QuickDiceApp.getInstance();
		if (app.getBagManager().exportAll(Uri.fromFile(tempFile))) {
			retVal = CompatFileProvider.getUriForFile(this, "ohm.quickdice", tempFile);
		}
		
		return retVal;
	}
	
	private CompatIntent.OnEvalResolveInfoListener onEvalResolveInfoListener = new OnEvalResolveInfoListener() {
		@Override
		public ResolveInfo onEvalResolveInfo(ResolveInfo resolveInfo, Intent target) {
			//Grant permission to access the file
			//Uri uri = target.getParcelableExtra(Intent.EXTRA_STREAM); //Maybe is better to use "getData()"?
			Uri uri = targetUri;
			ImportExportActivity.this.grantUriPermission(
					resolveInfo.activityInfo.packageName,
					uri,
					Intent.FLAG_GRANT_READ_URI_PERMISSION);
			
			return resolveInfo;
		}
	};
	
	
	private void confirmImport(Intent data) {
		if (data != null && data.getData() != null) {
			//final Uri importUri = data.getData();
			final Intent importIntent = data;
			//Confirm operation
			AlertDialog.Builder builder;
			builder = new AlertDialog.Builder(this);
			builder.setTitle(this.getTitle());
			builder.setMessage(R.string.msgConfirmImport);
			builder.setPositiveButton(
					R.string.lblYes,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							doImport(importIntent);
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
		} else {
			//Data not found
			
			Toast.makeText(this, R.string.msgFileNotFound, Toast.LENGTH_LONG).show();
			
			returnToCaller(RESULT_IMPORT_FAILED);
		}
	}
	
	private void doImport(Intent data) {
		QuickDiceApp app = QuickDiceApp.getInstance();
		if (data == null || data.getData() == null) {
			//Missing data
			returnToCaller(RESULT_IMPORT_FAILED);
		} else if (app.getBagManager().importAll(data.getData())) {
			//Save this entry to list (or bring it to top)
			boolean useForMru = false;
			try {
				useForMru = data.getBooleanExtra(FilePickerActivity.EXTRA_USE_FOR_MRU, false);
			} catch (BadParcelableException ex) {
				//This is caused by DropBox and I don't know how to prevent it
				Log.w("ImportExportActivity", "Cannot read " + FilePickerActivity.EXTRA_USE_FOR_MRU, ex);
				useForMru = false;
			}
			if (useForMru) {
				MostRecentFile mru = createRecentFileInstance(data.getData());
				updateRecentFileList(mru);
				saveRecentFiles(recentFiles);
			}
			returnToCaller(RESULT_IMPORT);
		} else {
			//This path is no good, remove from list.
			removeFromRecentFileList(data.getData());
			saveRecentFiles(recentFiles);

			returnToCaller(RESULT_IMPORT_FAILED);
		}
	}
	
	private void doMerge(Intent data) {
		QuickDiceApp app = QuickDiceApp.getInstance();
		DiceBagManager newData = new DiceBagManager(app.getPersistence());
		newData.setCacheIconFolder();
		if (data == null || data.getData() == null) {
			//Missing data
			returnToCaller(RESULT_IMPORT_FAILED);
		} else if (newData.importAll(data.getData())) {
			DiceBagPickerDialog dlg = new DiceBagPickerDialog(this, newData, new OnItemPickedListener() {
				@Override
				public void onItemPicked(boolean confirmed, DiceBagManager data, SparseBooleanArray selected, boolean override) {
					if (confirmed) {
						doMerge(data, selected, override);
						returnToCaller(RESULT_IMPORT);
					} else {
						returnToCaller(RESULT_CANCELED);
					}
				}
			});
			dlg.show();
		} else {
			//This path is no good, remove from list.
			removeFromRecentFileList(data.getData());
			saveRecentFiles(recentFiles);

			returnToCaller(RESULT_IMPORT_FAILED);
		}
	}
	
	private void doMerge(DiceBagManager data, SparseBooleanArray selected, boolean override) {
		QuickDiceApp app = QuickDiceApp.getInstance();
		DiceBagManager main = app.getBagManager();
		/** Maps source icon Id (key) to dest icon Id (value) icon id */
		SparseIntArray iconMapping = new SparseIntArray();
		for (int i = 0; i < selected.size(); i++) {
			if (selected.valueAt(i) == false)
				continue;
			
			int iconId;
			int bagIdx = selected.keyAt(i); //Position of the bag to merge
			DiceBag diceBag = data.getDiceBagCollection().get(bagIdx);
			ArrayList<Integer> iconIds = new ArrayList<Integer>();
			
			//Get all the custom icons used by this DiceBag
			iconId = diceBag.getResourceIndex();
			collectIcon(data, iconIds, iconId);
//			if (data.getIconCollection().getByID(iconId).isCustom() && iconIds.contains(iconIds) == false) {
//				iconIds.add(iconId);
//			}
			for (Dice dice : diceBag.getDice()) {
				iconId = dice.getResourceIndex();
				collectIcon(data, iconIds, iconId);
//				if (data.getIconCollection().getByID(iconId).isCustom() && iconIds.contains(iconIds) == false) {
//					iconIds.add(iconId);
//				}
			}
			for (Variable variable : diceBag.getVariables()) {
				iconId = variable.getResourceIndex();
				collectIcon(data, iconIds, iconId);
//				if (data.getIconCollection().getByID(iconId).isCustom() && iconIds.contains(iconIds) == false) {
//					iconIds.add(iconId);
//				}
			}
			
			//Move custom icons and create a map from old ID to new ID.
			for (Integer iid : iconIds) {
				int id = iid;
				IconCollection sourceIconList = data.getIconCollection();
				IconCollection destIconList = main.getIconCollection();
				int pos = sourceIconList.getPositionByID(id);
				if (pos == -1)
					//Already moved (by previous DiceBag)
					continue;
				
				Icon ico = sourceIconList.remove(pos, false);
				if (ico == null)
					//Redundant control
					continue;
				
				//int oldId = ico.getId();
				ico.setId(-1); //Need to reset Id
				
				int newId;
				pos = destIconList.indexOf(ico);
				if (pos >= 0) {
					//Main already contains the same icon
					newId = destIconList.get(pos).getId();
				} else {
					//newId = destIconList.add(ico);
					//newId = ico.getId();
					pos = destIconList.add(ico);
					newId = destIconList.get(pos).getId();
				}
				//int newId = destIconList.add(ico);
				//iconMapping.append(oldId, newId);
				if (id != newId) {
					iconMapping.append(id, newId);
				}
			}
			
			//Change icon references
			if (iconMapping.size() > 0) {
				iconId = iconMapping.get(diceBag.getResourceIndex(), -1);
				if (iconId != -1) {
					diceBag.setResourceIndex(iconId);
				}
				for (Dice item : diceBag.getDice()) {
					iconId = iconMapping.get(item.getResourceIndex(), -1);
					if (iconId != -1) {
						item.setResourceIndex(iconId);
					}
				}
				for (Variable item : diceBag.getVariables()) {
					iconId = iconMapping.get(item.getResourceIndex(), -1);
					if (iconId != -1) {
						item.setResourceIndex(iconId);
					}
				}
			}
			
			//Copy DiceBag
			if (override) {
				//Remove DiceBag with same name
				//String bagName = diceBag.getName();
				for (int j=0; j < main.getDiceBagCollection().size(); j++) {
					if (main.getDiceBagCollection().get(j).getName().equals(diceBag.getName())) {
						main.getDiceBagCollection().remove(j);
						break;
					}
				}
				//main.getDiceBagCollection().get(position)
			}
			//data.getDiceBagCollection().get(bagIdx);
			main.getDiceBagCollection().add(diceBag);
		}
	}
	
	private void collectIcon(DiceBagManager data, ArrayList<Integer> iconIds, int iconId) {
		Icon icon = data.getIconCollection().getByID(iconId);
		if (icon != null && icon.isCustom() && iconIds.contains(iconId) == false) {
			iconIds.add(iconId);
		}
	}
	
	private void removeFromRecentFileList(Uri uri) {
		int index = -1;
		String uriString = uri.toString();
		for (int i = 0; i < recentFiles.size(); i++) {
			if (recentFiles.get(i).getUri().compareToIgnoreCase(uriString) == 0) {
				index = i;
				break;
			}
		}
		if (index >= 0) {
			recentFiles.remove(index);
		}
	}
	
	private void updateRecentFileList(MostRecentFile mru) {
		int index = -1;
		for (int i = 0; i < recentFiles.size(); i++) {
			if (recentFiles.get(i).getUri().compareToIgnoreCase(mru.getUri()) == 0) {
				index = i;
				break;
			}
		}
		if (index >= 0) {
			recentFiles.remove(index);
			recentFiles.add(index, mru);
		} else {
			recentFiles.add(mru);
		}
		
		Collections.sort(recentFiles);
		
		if (recentFiles.size() >= MAX_RECENT_FILES) {
			recentFiles.remove(recentFiles.size()-1);
		}
	}
	
	private MostRecentFile createRecentFileInstance(Uri uri) {
		//java.io.File file;
		int bags = 0;
		int dice = 0;
		int mods = 0;
		int vars = 0;
		
		DiceBagCollection diceBags = QuickDiceApp.getInstance().getBagManager().getDiceBagCollection();
		for (DiceBag diceBag : diceBags) {
			bags += 1;
			dice += diceBag.getDice().size();
			mods += diceBag.getModifiers().size();
			vars += diceBag.getVariables().size();
		}
		
		//file = new java.io.File(path);
		
		return new MostRecentFile(
				uri.getLastPathSegment(), //file.getName(),
				uri.toString(), //path,
				bags,
				dice,
				mods,
				vars,
				new Date());
	}
	
	private ArrayList<MostRecentFile> loadRecentFiles() {
		ArrayList<MostRecentFile> retVal = null;
		
		try {
			String mrf = config.getString(KEY_RECENT_FILES, null);
			if (mrf != null) {
				retVal = SerializationManager.MostRecentFileList(mrf);
			}
		} catch (Exception e) {
			retVal = null;
		}
		
		if (retVal == null) {
			retVal = new ArrayList<MostRecentFile>();
		}
		
		return retVal;
	}
	
	private void saveRecentFiles(ArrayList<MostRecentFile> recentFiles) {
		String serialized = null;

		try {
			serialized = SerializationManager.MostRecentFileList(recentFiles);
		} catch (Exception e) {
			serialized = null;
		}

		SharedPreferences.Editor edit;
		edit = config.edit();
		if (serialized != null && serialized.length() > 0) {
			edit.putString(KEY_RECENT_FILES, serialized);
		} else {
			edit.remove(KEY_RECENT_FILES);
		}
		edit.commit();
	}
}
