package ohm.quickdice.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import ohm.quickdice.control.DiceBagManager;
import ohm.quickdice.control.PersistenceManager;

import android.annotation.TargetApi;
import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataInputStream;
import android.app.backup.BackupDataOutput;
import android.app.backup.FileBackupHelper;
import android.app.backup.SharedPreferencesBackupHelper;
import android.content.Context;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.FROYO)
public class BackupAgent extends BackupAgentHelper {

	private static final String TAG = "BackupAgent";
	
	// A key to uniquely identify the set of backup preference
	static final String MAIN_PREFERENCES_KEY = "QuickDicePref";

	// A key to uniquely identify the set of backup data
	static final String FILES_DICE_BAGS_KEY = "QuickDiceBags";

	// A key to uniquely identify the set of backup data
	static final String FILES_ICONS_KEY = "QuickDiceIcons";

	/* (non-Javadoc)
	 * @see android.app.backup.BackupAgent#onCreate()
	 */
	@Override
	public void onCreate() {

		Log.d(TAG, "onCreate");

		addHelper(
				MAIN_PREFERENCES_KEY,
				new SharedPreferencesBackupHelper(this, this.getPackageName() + "_preferences"));

		addHelper(
				FILES_DICE_BAGS_KEY,
				new FileBackupHelper(this, PersistenceManager.FILE_NAME_DICEBAGS));

//		String[] backupFiles = getFiles();
//
//		addHelper(
//				FILES_DICE_BAGS_KEY,
//				new FileBackupHelper(this, backupFiles));

		addHelper(
				FILES_ICONS_KEY,
				new FolderBackupHelper(this, DiceBagManager.ICON_FOLDER));
	}

	/* (non-Javadoc)
	 * @see android.app.backup.BackupAgentHelper#onBackup(android.os.ParcelFileDescriptor, android.app.backup.BackupDataOutput, android.os.ParcelFileDescriptor)
	 */
	@Override
	public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) throws IOException {
		Log.d(TAG, "onBackup");
		// Hold the lock while the FileBackupHelper performs backup
	    synchronized (PersistenceManager.dataAccessLock) {
			//RefreshFileHelper(getFiles());
	    	super.onBackup(oldState, data, newState);
	    }
	}

	/* (non-Javadoc)
	 * @see android.app.backup.BackupAgentHelper#onRestore(android.app.backup.BackupDataInput, int, android.os.ParcelFileDescriptor)
	 */
	@Override
	public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException {
		Log.d(TAG, "onRestore");
		// Hold the lock while the FileBackupHelper restores the file
	    synchronized (PersistenceManager.dataAccessLock) {
			//RefreshFileHelper(new String[] { "" });
			super.onRestore(data, appVersionCode, newState);
	    }
	}

//	private String[] getFiles() {
//		String[] retVal;
//		File iconFolder = getApplicationContext().getDir(DiceBagManager.ICON_FOLDER, Context.MODE_PRIVATE);
//		Log.d(TAG, "getFiles folder: " + iconFolder.getAbsolutePath());
//		File[] icons = iconFolder.listFiles();
//		if (icons == null) {
//			retVal = new String[] {PersistenceManager.FILE_NAME_DICEBAGS};
//			Log.d(TAG, "getFiles <no icons>");
//		} else {
//			retVal = new String[icons.length + 1];
//			retVal[0] = PersistenceManager.FILE_NAME_DICEBAGS;
//			File basePath = getApplicationContext().getFilesDir();
//			File temp;
//			for (int i = 0; i < icons.length; i++) {
//				Log.d(TAG, "getFiles " + icons[i].getAbsolutePath());
//				temp = new File(basePath.toURI().relativize(icons[i].toURI()));
//				retVal[i + 1] = temp.getPath();
//				Log.d(TAG, "getFiles " + retVal[i + 1]);
//				Log.d(TAG, "getFiles " + (new File(getApplicationContext().getFilesDir(), retVal[i + 1])).getAbsolutePath());
//			}
//		}
//		return retVal;
//	}

//	private void RefreshFileHelper(String files[]) {
//		FileBackupHelper aHelper = new DynamicFileBackupHelper(this, files);
//		addHelper(FILES_DICE_BAGS_KEY, aHelper);
//	}

	private class FolderBackupHelper extends FileBackupHelper {

		private static final String TAG = "FolderBackupHelper";
		private static final int BUFFER_SIZE = 2048;

		private String folder;

		public FolderBackupHelper(Context context, String folder) {
			super(context, folder);
			this.folder = folder;
		}

		@Override
		public void performBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) {
			Log.d(TAG, "performBackup " + folder);
			packFiles(folder);
			super.performBackup(oldState, data, newState);
		}

		@Override
		public void restoreEntity(BackupDataInputStream data) {
			Log.d(TAG, "restoreEntity " + data.getKey());
			super.restoreEntity(data);
			unpackFiles(folder);
		}

		private void packFiles(String folderName) {
			File pack = new File(getApplicationContext().getFilesDir(), folderName);

			Log.d(TAG, "packFiles - writing " + pack.getAbsolutePath());

			File folder = getApplicationContext().getDir(folderName, Context.MODE_PRIVATE);
			File[] files = folder.listFiles();

			//BufferedInputStream origin;
			try {
				ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(pack)));
				byte data[] = new byte[BUFFER_SIZE];
				try {
					for (int i = 0; i < files.length; i++) {
						if (files[i].isDirectory()) {
							continue;
						}
//						FileInputStream fi = new FileInputStream(files[i]);
//						origin = new BufferedInputStream(fi, BUFFER_SIZE);
						BufferedInputStream in = new BufferedInputStream(new FileInputStream(files[i]), BUFFER_SIZE);
						try {
							Log.d(TAG, "packFiles - packing " + files[i].getAbsolutePath());
							ZipEntry entry = new ZipEntry(files[i].getName());
							out.putNextEntry(entry);
							int count;
							while ((count = in.read(data)) > 0) {
								out.write(data, 0, count);
							}
						} finally {
							in.close();
						}
					}
				} finally {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void unpackFiles(String folderName) {
			File pack = new File(getApplicationContext().getFilesDir(), folderName);

			Log.d(TAG, "unpackFiles - reading " + pack.getAbsolutePath());

			File folder = getApplicationContext().getDir(folderName, Context.MODE_PRIVATE);

			try {
				if(!folder.isDirectory()) {
					folder.mkdirs();
				}
				ZipInputStream in = new ZipInputStream(new FileInputStream(pack));
				byte data[] = new byte[BUFFER_SIZE];
				try {
					ZipEntry entry = null;
					while ((entry = in.getNextEntry()) != null) {
						File file = new File(folder, entry.getName());

//						if (ze.isDirectory()) {
//							if(!file.isDirectory()) {
//								file.mkdirs();
//							}
//						} else {
						FileOutputStream out = new FileOutputStream(file, false);
						try {
							Log.d(TAG, "unpackFiles - unpacking " + file.getAbsolutePath());
							int count;
							while ((count = in.read(data)) > 01) {
								out.write(data, 0, count);
							}
							in.closeEntry();
						} finally {
							out.close();
						}
//						}
					}
				} finally {
					in.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
