package ohm.quickdice.activity;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import ohm.quickdice.R;
import ohm.quickdice.adapter.FolderContentAdapter;
import ohm.quickdice.entity.FolderItem;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FilePickerActivity extends Activity {

	//final static String ROOT_FOLDER_NAME = "sdcard";
	//final static String ROOT_FOLDER = "/" + ROOT_FOLDER_NAME + "/";
	
	/**
	 * Open the activity to select an existing folder.
	 */
	public static final int ACTIVITY_SELECT_FOLDER = 0x00F17E01;
	/**
	 * Open the activity to select an existing file.
	 */
	public static final int ACTIVITY_SELECT_FILE = 0x00F17E02;
	/**
	 * Open the activity to select a name for a new file.
	 */
	public static final int ACTIVITY_NEW_FILE = 0x00F17E03;
	/**
	 * The activity was closed pressing "OK"
	 */
	public static final int RESULT_OK = 0x00F17E01;
	/**
	 * The activity was closed pressing "Cancel" or the back button
	 */
	public static final int RESULT_CANCEL = 0x00F17E02;
	/**
	 * Define the bundle content as a type of request ({@code ACTIVITY_SELECT_FOLDER}, {@code ACTIVITY_SELECT_FILE} or {@code ACTIVITY_NEW_FILE}).
	 */
	public static final String BUNDLE_REQUEST_TYPE = "RequestType";
	public static final String BUNDLE_FILTER_EXTENSION_LIST = "FilterExtensionList";
	public static final String BUNDLE_DEFAULT_FILE_NAME = "DefaultFileName";
	/**
	 * Define the title to use for the activity. Used only if {@link BUNDLE_TITLE_ID} is not set.
	 */
	public static final String BUNDLE_TITLE = "Title";
	/**
	 * Define the resource to use for the title to use for the activity.
	 */
	public static final String BUNDLE_TITLE_ID = "TitleId";

	public static final String BUNDLE_RESULT_PATH = "ResultPath";

	private static final int SIZE_KB = 1024;
	private static final int SIZE_MB = SIZE_KB * 1024;
	private static final int SIZE_GB = SIZE_MB * 1024;

	java.text.DateFormat dateFormat;
	java.text.DateFormat timeFormat;
	
	int requestType;
	String defaultFileName = null;
	String title = null;
	FileFilter fileFilter = null;
	
	File root;
	File curFolder;
	
	Button confirm;
	Button cancel;
	TextView lblCurrent;
	ListView listView;
	EditText txtFileName;
	FolderContentAdapter folderContentAdapter;
	
	private class ExtensionFileFilter implements FileFilter {

		String[] extensions;
		
		public ExtensionFileFilter(String[] extensions) {
			this.extensions = extensions;
		}

		@Override
		public boolean accept(File pathname) {
			boolean retVal = false;
			if (pathname.isDirectory()) {
				retVal = true;
			} else {
				for (int i = 0; i < extensions.length; i++) {
					if (pathname.getName().endsWith(extensions[i])) {
						retVal = true;
						break;
					}
				}
			}
			return retVal;
		}
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.file_picker_activity);
		
		requestType = ACTIVITY_SELECT_FILE;
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			requestType = extras.getInt(BUNDLE_REQUEST_TYPE, ACTIVITY_SELECT_FILE);
			defaultFileName = extras.getString(BUNDLE_DEFAULT_FILE_NAME);
			String[] ext = extras.getStringArray(BUNDLE_FILTER_EXTENSION_LIST);
			if (ext != null && ext.length > 0) {
				fileFilter = new ExtensionFileFilter(ext);
			}
			if (extras.containsKey(BUNDLE_TITLE_ID)) {
				int titleId = extras.getInt(BUNDLE_TITLE_ID);
				title = getString(titleId);
			} else {
				title = extras.getString(BUNDLE_TITLE);
			}
		}
		
		dateFormat = DateFormat.getMediumDateFormat(this);
		timeFormat = DateFormat.getTimeFormat(this);

		root = null;
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			root = Environment.getExternalStorageDirectory();
		}
		curFolder = root;

		initViews();
	}
	
	private void initViews() {

		lblCurrent = (TextView)findViewById(R.id.fpCurrentLocation);
		
		listView = (ListView)findViewById(R.id.fpFileList);
		
		txtFileName = (EditText)findViewById(R.id.fpFileName);
		
    	confirm = (Button) findViewById(R.id.btuBarConfirm);
    	confirm.setOnClickListener(confirmCancelClickListener);
    	cancel = (Button) findViewById(R.id.btuBarCancel);
    	cancel.setOnClickListener(confirmCancelClickListener);

    	switch (requestType) {
    		case ACTIVITY_SELECT_FILE:
    			if (title == null) title = getString(R.string.lblSelectFile);
    			//Confirm button not needed.
    			confirm.setVisibility(View.GONE);
    			//Enable file name input not needed.
    			txtFileName.setVisibility(View.GONE);
    			break;
    		case ACTIVITY_SELECT_FOLDER:
    			if (title == null) title = getString(R.string.lblSelectFolder);
    			//Enable file name input not needed.
    			txtFileName.setVisibility(View.GONE);
    			break;
    		case ACTIVITY_NEW_FILE:
    			if (title == null) title = getString(R.string.lblNewFile);
    			//Enable file name input
    			txtFileName.setText(defaultFileName);
    			txtFileName.setVisibility(View.VISIBLE);
    			break;
    	}
    	
    	this.setTitle(title);

    	if (curFolder != null) { 
    		showFolder(curFolder);
    	} else {
    		//Storage not available
    		//R.string.err_storage_not_found
    		lblCurrent.setText(R.string.err_storage_not_found);
    		confirm.setVisibility(View.GONE);
    		txtFileName.setVisibility(View.GONE);
    	}
	}

	private void showFolder(File folder) {
		File[] files = null;
        List<FolderItem> items = new ArrayList<FolderItem>();

    	files = folder.listFiles(fileFilter);

		lblCurrent.setText(String.format(getString(R.string.lblCurrentFolder), folder.getName()));
        
        FolderItem item;
        	
        try {
        	for (File file : files) {
        		item = getFolderItem(file);
        		if (item != null) {
        			items.add(item);
        		}
        	}
        } catch (Exception e) {
        	Toast.makeText(this, R.string.msgCannotRead, Toast.LENGTH_LONG).show();
        }

        if (!folder.getPath().equalsIgnoreCase(root.getPath())) {
        	//If this is not the root, allow to go back
            if (folder.getParentFile() != null) {
            	item = new FolderItem(
            			"..",
            			getString(R.string.lblParentFolder),
            			folder.getParent(),
            			FolderItem.TYPE_PARENT_FOLDER);
            
            	items.add(item);
            }
        }
        
        Collections.sort(items);
        
        folderContentAdapter = new FolderContentAdapter(
        		listView.getContext(),
        		R.layout.file_picker_item,
                items);
        
        listView.setAdapter(folderContentAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

				@Override
				public void onItemClick(
						AdapterView<?> adapter,
						View view,
						int position,
						long id) {
					
					FolderItem item = folderContentAdapter.getItem(position);
					handleSelection(item);
				}
                
        });
		
	}
	
	private FolderItem getFolderItem(File file) {
		FolderItem retVal = null;
		if (! file.isHidden()) {
			retVal = new FolderItem(
					file.getName(),
					getFolderItemDescription(file),
					file.getAbsolutePath(),
					file.isFile() ? FolderItem.TYPE_FILE : FolderItem.TYPE_FOLDER);
		}
		return retVal;
	}
	
	private String getFolderItemDescription(File file) {
		String retVal = null;
		if (! file.isHidden()) {
			if (file.isFile()) {
				Date date = new Date(file.lastModified());
				retVal = String.format(
						getString(R.string.lblItemFile),
						getReadableFileSize(file.length()),
						dateFormat.format(date),
						timeFormat.format(date));
			} else {
				retVal = getString(R.string.lblItemFolder);
			}
		} else {
			retVal = getString(R.string.lblItemHiddenFolder);
		}
		return retVal;
	}

	private String getReadableFileSize(long size) {
		String retVal = null;
		double mySize = size;
		
		DecimalFormat format = null;
		format = new DecimalFormat();
		format.setMinimumFractionDigits(0);
		format.setMaximumFractionDigits(2);
		format.setMinimumIntegerDigits(1);
		format.setMaximumIntegerDigits(Integer.MAX_VALUE);
		
		Resources res = getResources();
		
		if (mySize > SIZE_GB) { //GB
			retVal = format.format(mySize / SIZE_GB) + " " +
					res.getString(R.string.lblGb);
		} else if (mySize > SIZE_MB) { //MB
			retVal = format.format(mySize / SIZE_MB) + " " +
					res.getString(R.string.lblMb);
		} else if (mySize > SIZE_KB) { //KB
			retVal = format.format(mySize / SIZE_KB) + " " +
					res.getString(R.string.lblKb);
		} else {
			retVal = Long.toString(size) + " " +
					res.getString(R.string.lblB);
		}
		
		return retVal;
	}

	protected void handleSelection(FolderItem item) {
		switch (item.getType()) {
			case FolderItem.TYPE_PARENT_FOLDER:
			case FolderItem.TYPE_FOLDER:
				//Go to selected folder
				curFolder = new File(item.getPath());
				showFolder(curFolder);
				break;
			default:
				if (requestType == ACTIVITY_SELECT_FILE) {
					returnToCaller(item.getPath(), RESULT_OK);
				} else if (requestType == ACTIVITY_NEW_FILE) {
					txtFileName.setText(item.getName());
				}
				break;
		}
	}
	
	private OnClickListener confirmCancelClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			FolderItem item = null;
			if (v == confirm) {
				switch (requestType) {
					case ACTIVITY_SELECT_FILE:
						//This should not happen
						break;
					case ACTIVITY_SELECT_FOLDER:
						item = getFolderItem(curFolder);
						returnToCaller(
								item.getPath(),
								RESULT_OK);
						break;
					case ACTIVITY_NEW_FILE:
						//Check if overwrite
						askOverwrite();
						break;
				}
			} else {
				returnToCaller(null, RESULT_CANCEL);
			}
		}
	};

	protected void returnToCaller(String path, int result) {
		Bundle bundle = new Bundle();
		bundle.putString(BUNDLE_RESULT_PATH, path);
		Intent mIntent = new Intent();
		mIntent.putExtras(bundle);
		//mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		setResult(result, mIntent);
		finish();
	}

	private void askOverwrite() {
		String fileName = txtFileName.getText().toString();
		if (isValidFileName(curFolder, fileName)) {
			final File newFile = new File(curFolder, fileName);
			if (newFile.exists()) {
				AlertDialog.Builder builder;
		    	builder = new AlertDialog.Builder(this);
		    	builder.setTitle(this.getTitle());
		    	builder.setMessage(String.format(getString(R.string.msgFileNameExists), fileName));
		    	builder.setPositiveButton(
		    			R.string.lblYes,
		    			new DialogInterface.OnClickListener() {
		    				public void onClick(DialogInterface dialog, int id) {
		    					returnToCaller(newFile.getAbsolutePath(), RESULT_OK);
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
				returnToCaller(newFile.getAbsolutePath(), RESULT_OK);
			}
		} else {
			Toast.makeText(
					this,
					String.format(getString(R.string.msgFileNameInvalid), fileName),
					Toast.LENGTH_LONG).show();
		}
	}

	private boolean isValidFileName(File path, String fileName) {
		boolean retVal;
		
		//return new File(path, fileName).isFile();

		try {
			new File(path, fileName).getCanonicalPath();
			retVal = true;
		} catch (IOException ex) {
			retVal = false;
		}
		return retVal;
	}
	
}
