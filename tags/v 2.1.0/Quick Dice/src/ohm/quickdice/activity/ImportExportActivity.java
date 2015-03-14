package ohm.quickdice.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

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
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

public class ImportExportActivity extends BaseActivity {

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
	 * The activity was closed pressing "Cancel" or the back button
	 */
	public static final int RESULT_CANCEL = 0x00040003;
	/**
	 * Define the bundle content as a type of request ({@code ACTIVITY_IMPORT_EXPORT}).
	 */
	public static final String BUNDLE_REQUEST_TYPE = "RequestType";
	
	private static final String FILE_EXTENSION = ".qdr.json";
	private static final String DEFAULT_FILE_NAME = "DiceDef" + FILE_EXTENSION;
	private static final String KEY_RECENT_FILES = "KEY_RECENT_FILES";
	private static final int MAX_RECENT_FILES = 16;

	ListView lstRecentFiles;
	Button btuImport;
	Button btuExport;
	Button btuCancel;
	SharedPreferences config = null;
	ArrayList<MostRecentFile> recentFiles;
	
	//GraphicManager graphicManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		setTheme(QuickDiceApp.getInstance().getPreferences().getDialogThemeResId());
		
		super.onCreate(savedInstanceState);
		
		config = PreferenceManager.getDefaultSharedPreferences(this);

		recentFiles = loadRecentFiles();
		
		//graphicManager = new Graphic(getResources());
		//graphicManager = QuickDiceApp.getInstance().getGraphic();
		
		initViews();
	}

	private void initViews() {
		setContentView(R.layout.import_export_activity);
		
		lstRecentFiles = (ListView)findViewById(R.id.eiRecentList);
		lstRecentFiles.setAdapter(new MostRecentFilesAdapter(
				this,
				R.layout.import_export_item,
				recentFiles));
		lstRecentFiles.setOnItemClickListener(recentFileClickListener);
		
		Drawable drawable;
		
		btuImport = (Button)findViewById(R.id.eiImport);
		//drawable = graphicManager.resizeDrawable(R.drawable.ic_import, 64, 32);
		drawable = Helper.resizeDrawable(this, R.drawable.ic_import, 64, 32);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		btuImport.setCompoundDrawables(drawable, null, null, null);
		btuImport.setOnClickListener(importClickListener);
		
		btuExport = (Button)findViewById(R.id.eiExport);
		//drawable = graphicManager.resizeDrawable(R.drawable.ic_export, 64, 32);
		drawable = Helper.resizeDrawable(this, R.drawable.ic_export, 64, 32);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		btuExport.setCompoundDrawables(drawable, null, null, null);
		btuExport.setOnClickListener(exportClickListener);
		
		btuCancel = (Button)findViewById(R.id.btuBarCancel);
		btuCancel.setOnClickListener(cancelClickListener);
		
		findViewById(R.id.btuBarConfirm).setVisibility(View.GONE);
		//findViewById(R.id.vwDivider).setVisibility(View.GONE);
	}
	
	OnItemClickListener recentFileClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(
				AdapterView<?> adapter,
				View view,
				int position,
				long id) {
			
			MostRecentFile recentFile = recentFiles.get(position);
			doImport(recentFile.getPath());
		}
	};
	
	OnClickListener importClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Bundle bundle = new Bundle();
			bundle.putInt(FilePickerActivity.BUNDLE_REQUEST_TYPE, FilePickerActivity.ACTIVITY_SELECT_FILE);
			bundle.putStringArray(FilePickerActivity.BUNDLE_FILTER_EXTENSION_LIST, new String[] {FILE_EXTENSION});
			Intent i = new Intent(ImportExportActivity.this, FilePickerActivity.class);
			i.putExtras(bundle);
			startActivityForResult(i, FilePickerActivity.ACTIVITY_SELECT_FILE);
		}
	};
	
	OnClickListener exportClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Bundle bundle = new Bundle();
			bundle.putInt(FilePickerActivity.BUNDLE_REQUEST_TYPE, FilePickerActivity.ACTIVITY_NEW_FILE);
			bundle.putStringArray(FilePickerActivity.BUNDLE_FILTER_EXTENSION_LIST, new String[] {FILE_EXTENSION});
			bundle.putString(FilePickerActivity.BUNDLE_DEFAULT_FILE_NAME, DEFAULT_FILE_NAME);
			Intent i = new Intent(ImportExportActivity.this, FilePickerActivity.class);
			i.putExtras(bundle);
			startActivityForResult(i, FilePickerActivity.ACTIVITY_NEW_FILE);
		}
	};
	
	OnClickListener cancelClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			returnToCaller(RESULT_CANCEL);
		}
	};
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case FilePickerActivity.ACTIVITY_SELECT_FILE:
			if (resultCode == FilePickerActivity.RESULT_OK) {
				//File selected. Need to import.
				doImport(getPath(data));
			}
			break;
		case FilePickerActivity.ACTIVITY_NEW_FILE:
			if (resultCode == FilePickerActivity.RESULT_OK) {
				//File name choose. Need to export.
				doExport(getPath(data));
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
	
	protected String getPath(Intent intent) {
		Bundle extras = intent.getExtras();
		return extras.getString(FilePickerActivity.BUNDLE_RESULT_PATH);
	}
	
	private void doExport(String path) {
		//Check extension
		java.io.File test = new java.io.File(path);
		if (! test.getName().endsWith(FILE_EXTENSION)) {
//			if (! path.endsWith(".")) {
//				path = path + ".";
//			}
			path = path + FILE_EXTENSION;
		}
		QuickDiceApp app = QuickDiceApp.getInstance();
		if (app.getBagManager().exportAll(path)) {
			//Save this entry to list (or bring it to top)
			MostRecentFile mru = createRecentFileInstance(path, app);
			updateRecentFileList(mru);
			saveRecentFiles(recentFiles);
		} else {
			//This path is no good, remove from list.
			removeFromRecentFileList(path);
			saveRecentFiles(recentFiles);
		}

		returnToCaller(RESULT_EXPORT);
	}
	
	private void doImport(String path) {
		//String path = getPath(data);
		final String importPath = path;

		//Confirm operation
		AlertDialog.Builder builder;
		builder = new AlertDialog.Builder(this);
		builder.setTitle(this.getTitle());
		builder.setMessage(R.string.msgConfirmImport);
		builder.setPositiveButton(
				R.string.lblYes,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						QuickDiceApp app = QuickDiceApp.getInstance();
						if (app.getBagManager().importAll(importPath)) {
							//Save this entry to list (or bring it to top)
							MostRecentFile mru = createRecentFileInstance(importPath, app);
							updateRecentFileList(mru);
							saveRecentFiles(recentFiles);

							returnToCaller(RESULT_IMPORT);
						} else {
							//This path is no good, remove from list.
							removeFromRecentFileList(importPath);
							saveRecentFiles(recentFiles);

							returnToCaller(RESULT_IMPORT_FAILED);
						}
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
	
	private void removeFromRecentFileList(String path) {
		int index = -1;
		for (int i = 0; i < recentFiles.size(); i++) {
			if (recentFiles.get(i).getPath().compareToIgnoreCase(path) == 0) {
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
			if (recentFiles.get(i).getPath().compareToIgnoreCase(mru.getPath()) == 0) {
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
	
	private MostRecentFile createRecentFileInstance(String path, QuickDiceApp app) {
		java.io.File file;
		int bags = 0;
		int dice = 0;
		int mods = 0;
		int vars = 0;
		
		DiceBagCollection diceBags = app.getBagManager().getDiceBagCollection();
		for (DiceBag diceBag : diceBags) {
			bags += 1;
			dice += diceBag.getDice().size();
			mods += diceBag.getModifiers().size();
			vars += diceBag.getVariables().size();
		}
		
		file = new java.io.File(path);
		
		return new MostRecentFile(
				file.getName(),
				path,
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
