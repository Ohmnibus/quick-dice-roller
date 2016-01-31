package ohm.quickdice.util;

import java.io.IOException;

import ohm.quickdice.control.PersistenceManager;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.FileBackupHelper;
import android.app.backup.SharedPreferencesBackupHelper;
import android.os.ParcelFileDescriptor;

public class BackupAgent extends BackupAgentHelper {
	
	// A key to uniquely identify the set of backup data
    static final String FILES_DICE_BAGS_KEY = "QuickDiceBags";

    // A key to uniquely identify the set of backup preference
    static final String MAIN_PREFERENCES_KEY = "QuickDicePref";
    
	/* (non-Javadoc)
	 * @see android.app.backup.BackupAgent#onCreate()
	 */
	@Override
	public void onCreate() {
	    addHelper(
	    		MAIN_PREFERENCES_KEY,
	    		new SharedPreferencesBackupHelper(this, this.getPackageName () + "_preferences"));

        addHelper(
        		FILES_DICE_BAGS_KEY,
        		new FileBackupHelper(this, PersistenceManager.FILE_NAME_DICEBAGS));
	}

	/* (non-Javadoc)
	 * @see android.app.backup.BackupAgentHelper#onBackup(android.os.ParcelFileDescriptor, android.app.backup.BackupDataOutput, android.os.ParcelFileDescriptor)
	 */
	@Override
	public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) throws IOException {
		// Hold the lock while the FileBackupHelper performs backup
	    synchronized (PersistenceManager.dataAccessLock) {
	    	super.onBackup(oldState, data, newState);
	    }
	}

	/* (non-Javadoc)
	 * @see android.app.backup.BackupAgentHelper#onRestore(android.app.backup.BackupDataInput, int, android.os.ParcelFileDescriptor)
	 */
	@Override
	public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException {
		// Hold the lock while the FileBackupHelper restores the file
	    synchronized (PersistenceManager.dataAccessLock) {
			super.onRestore(data, appVersionCode, newState);
	    }
	}

}
