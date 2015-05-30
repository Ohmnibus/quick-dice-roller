package ohm.quickdice.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import ohm.library.compat.CompatIntent;
import ohm.library.compat.CompatIntent.OnEvalResolveInfoListener;
import ohm.quickdice.QuickDiceApp;
import ohm.quickdice.R;
import ohm.quickdice.adapter.MostRecentFilesAdapter;
import ohm.quickdice.control.SerializationManager;
import ohm.quickdice.entity.DiceBag;
import ohm.quickdice.entity.DiceBagCollection;
import ohm.quickdice.entity.MostRecentFile;
import ohm.quickdice.util.Helper;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.FileProvider;
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

	ListView lstRecentFiles;
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
		
		btuImport = (Button)findViewById(R.id.eiImport);
		drawable = Helper.resizeDrawable(this, R.drawable.ic_import, 64, 32);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		btuImport.setCompoundDrawables(drawable, null, null, null);
		btuImport.setOnClickListener(this);
		
		btuExport = (Button)findViewById(R.id.eiExport);
		drawable = Helper.resizeDrawable(this, R.drawable.ic_export, 64, 32);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
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
		if (v.getId() == btuImport.getId()) {
			onImportClick(v);
		} else if (v.getId() == btuExport.getId()) {
			onExportClick(v);
		} else if (v.getId() == btuCancel.getId()) {
			returnToCaller(RESULT_CANCELED);
		}
	};
	
	private void onImportClick(View v) {
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
				getString(R.string.lblImportDiceDef),
				extraIntent,
				null);
		startActivityForResult(chooser, REQUEST_GET_CONTENT);
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
					if (data.getBooleanExtra(FilePickerActivity.EXTRA_USE_FOR_MRU, false)) {
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
	 * and return its Uri computed by FileProvider.
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
			retVal = FileProvider.getUriForFile(this, "ohm.quickdice", tempFile);
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
	
//	void startChooserForResult(Intent baseIntent, int requestCode, int titleResId, Intent extraIntent) {
//		targetUri = baseIntent.getParcelableExtra(Intent.EXTRA_STREAM); //Maybe is better to use "getData()"?
//		List<ResolveInfo> resInfo = this.getPackageManager().queryIntentActivities(baseIntent, 0);
//		List<Intent> chooserOptionList = new ArrayList<Intent>();
//		for (ResolveInfo resolveInfo : resInfo) {
//			String packageName = resolveInfo.activityInfo.packageName;
//
//			//if (!packageName.contains(".google")) { //Filter
//			Intent chooserOption = new Intent(baseIntent);
//			chooserOption.setPackage(packageName);
//			chooserOption.setComponent(new ComponentName(packageName, resolveInfo.activityInfo.name));
//			
//			chooserOptionList.add(chooserOption);
//			
//			//Grant permission to access the file
//			if (targetUri != null) {
//				this.grantUriPermission(packageName, targetUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
//			}
//			//}
//		}
//		
//		chooserOptionList.add(extraIntent);
//		
//		Intent chooser = Intent.createChooser(chooserOptionList.remove(0), getString(titleResId));
//		chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, chooserOptionList.toArray(new Parcelable[]{}));
//		
//		startActivityForResult(chooser, requestCode);
//	}
	
	//	private void doExport(String path) {
//		//Check extension
//		java.io.File test = new java.io.File(path);
//		if (! test.getName().endsWith(FILE_EXTENSION)) {
////			if (! path.endsWith(".")) {
////				path = path + ".";
////			}
//			path = path + FILE_EXTENSION;
//		}
//		QuickDiceApp app = QuickDiceApp.getInstance();
//		if (app.getBagManager().exportAll(path)) {
//			//Save this entry to list (or bring it to top)
//			MostRecentFile mru = createRecentFileInstance(path, app);
//			updateRecentFileList(mru);
//			saveRecentFiles(recentFiles);
//		} else {
//			//This path is no good, remove from list.
//			removeFromRecentFileList(path);
//			saveRecentFiles(recentFiles);
//		}
//
//		returnToCaller(RESULT_EXPORT);
//	}
	
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
	
	private void doImport(Intent importIntent) {
		QuickDiceApp app = QuickDiceApp.getInstance();
		if (importIntent == null || importIntent.getData() == null) {
			//Missing data
			returnToCaller(RESULT_IMPORT_FAILED);
		} else if (app.getBagManager().importAll(importIntent.getData())) {
			//Save this entry to list (or bring it to top)
			if (importIntent.getBooleanExtra(FilePickerActivity.EXTRA_USE_FOR_MRU, false)) {
				MostRecentFile mru = createRecentFileInstance(importIntent.getData());
				updateRecentFileList(mru);
				saveRecentFiles(recentFiles);
			}
			returnToCaller(RESULT_IMPORT);
		} else {
			//This path is no good, remove from list.
			removeFromRecentFileList(importIntent.getData());
			saveRecentFiles(recentFiles);

			returnToCaller(RESULT_IMPORT_FAILED);
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
