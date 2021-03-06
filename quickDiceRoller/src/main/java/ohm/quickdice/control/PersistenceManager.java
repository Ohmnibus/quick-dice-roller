package ohm.quickdice.control;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import ohm.quickdice.R;
import ohm.quickdice.control.SerializationManager.InvalidVersionException;
import ohm.quickdice.entity.DiceCollection;
import ohm.quickdice.entity.ModifierCollection;
import ohm.quickdice.entity.RollResult;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class PersistenceManager {

	private static final String TAG = "PersistenceManager";
	
	// Object for intrinsic lock
	public static final Object dataAccessLock = new Object();

	public static final String FILE_NAME_DICEBAGS = "DiceBags";
	public static final String FILE_NAME_RESULTLIST = "ResultList";
	protected static final String OBSOLETE_FILE_NAME_DICEBAG = "DiceBag";	//No longer supported since version 8
	protected static final String OBSOLETE_FILE_NAME_BONUSBAG = "BonusBag";	//No longer supported since version 8
	
	protected static final int ACTION_SAVE = 0;
	protected static final int ACTION_EXPORT = 1;
	protected static final int ACTION_LOAD = 2;
	protected static final int ACTION_IMPORT = 3;
	
	protected static final int RES_MESSAGE_NONE = 0x00000000;

	private Context context;
	
	public PersistenceManager(Context context) {
		this.context = context;
	}
	
	public Context getContext() {
		return context;
	}

	private Uri systemArchiveUri = null;
	public Uri getSystemArchiveUri() {
		if (systemArchiveUri == null) {
			File path = new File(context.getFilesDir(), FILE_NAME_DICEBAGS);
			systemArchiveUri = Uri.fromFile(path);
		}
		return systemArchiveUri;
	}
	
	/** No error has occurred */
	public static final int ERR_NONE = 0x00000000;
	/** A generic unspecified error has occurred */
	public static final int ERR_GENERIC = 0x00000001;
	/** File cannot be found at given location */
	public static final int ERR_FILE_NOT_FOUND = 0x00000002;
	/** File cannot be read because is empty */
	public static final int ERR_EMPTY = 0x00000004;
	/** File cannot be read because it's version isn't supported */
	public static final int ERR_INVALID_VERSION = 0x00000008;
	/** File cannot be read because it's format is invalid */
	public static final int ERR_INVALID_FORMAT = 0x00000016;
	
//	/**
//	 * Load the names of all the collections.
//	 * @param collections List to populate.
//	 * @param uri The location of the file to read.
//	 * @return Error code. One of the {@code ERR_*} constant.
//	 */
//	public int readCollections(List<String> collections, Uri uri) {
//		return readCollections(collections, uri, RES_MESSAGE_NONE);
//	}
//
//	/**
//	 * Load the names of all the collections.
//	 * @param collections Dice Bag name list to populate.
//	 * @param uri The location of the file to read.
//	 * @param errorMessageResId The resource ID of the error message to show on error, or {@code RES_MESSAGE_NONE} to disable error output.
//	 * @return Error code. One of the {@code ERR_*} constant.
//	 */
//	public int readCollections(List<String> collections, Uri uri, int errorMessageResId) {
//		int retVal = ERR_NONE;
//		InputStream fis;
//
//		try {
//			synchronized (dataAccessLock) {
//
//				fis = context.getContentResolver().openInputStream(uri);
//
//				//TODO: SerializationManager.DiceBagManager(fis, diceBagManager);
//
//				fis.close();
//			}
//
//			if (collections.size() == 0) {
//				retVal = ERR_EMPTY;
//			}
//			Log.i(TAG, "readCollections: " + retVal);
//		} catch (FileNotFoundException e) {
//			Log.w(TAG, "readCollections", e);
//			retVal = ERR_FILE_NOT_FOUND;
//			showErrorMessage(context, errorMessageResId);
//		} catch (InvalidVersionException e) {
//			Log.w(TAG, "readCollections", e);
//			retVal = ERR_INVALID_VERSION;
//			showErrorMessage(context, errorMessageResId);
//		} catch (IOException e) {
//			Log.e(TAG, "readCollections", e);
//			retVal = ERR_INVALID_FORMAT;
//			showErrorMessage(context, errorMessageResId);
//		} catch (Exception e) {
//			Log.e(TAG, "readCollections", e);
//			retVal = ERR_GENERIC;
//			showErrorMessage(context, errorMessageResId);
//		}
//
//		return retVal;
//	}
	
	/**
	 * Load the Dice Bag Manager from the device internal memory, if found.
	 * @param diceBagManager Dice Bag Manager to populate.
	 * @param uri The location of the file to read.
	 * @return Error code. One of the {@code ERR_*} constant.
	 */
	public int readDiceBagManager(DiceBagManager diceBagManager, Uri uri) {
		return readDiceBagManager(diceBagManager, uri, RES_MESSAGE_NONE);
	}
	
	/**
	 * Load the Dice Bag Manager from the specified resource.
	 * @param diceBagManager Dice Bag Manager to populate.
	 * @param uri The location of the file to read.
	 * @param errorMessageResId The resource ID of the error message to show on error, or {@code RES_MESSAGE_NONE} to disable error output.
	 * @return Error code. One of the {@code ERR_*} constant.
	 */
	public int readDiceBagManager(DiceBagManager diceBagManager, Uri uri, int errorMessageResId) {
		int retVal = ERR_NONE;
		InputStream fis;
		
		try {
			synchronized (dataAccessLock) {
				
				fis = context.getContentResolver().openInputStream(uri);

				SerializationManager.DiceBagManager(fis, diceBagManager);

				fis.close();
			}
			
			if (diceBagManager.getDiceBagCollection().size() == 0) {
				retVal = ERR_EMPTY;
			}
			Log.i(TAG, "readDiceBagManager: " + retVal);
		} catch (FileNotFoundException e) {
			Log.w(TAG, "readDiceBagManager", e);
			retVal = ERR_FILE_NOT_FOUND;
			showErrorMessage(context, errorMessageResId);
		} catch (InvalidVersionException e) {
			Log.w(TAG, "readDiceBagManager", e);
			retVal = ERR_INVALID_VERSION;
			showErrorMessage(context, errorMessageResId);
		} catch (IOException e) {
			Log.e(TAG, "readDiceBagManager", e);
			retVal = ERR_INVALID_FORMAT;
			showErrorMessage(context, errorMessageResId);
		} catch (Exception e) {
			Log.e(TAG, "readDiceBagManager", e);
			retVal = ERR_GENERIC;
			showErrorMessage(context, errorMessageResId);
		}
		
		return retVal;
	}
	
	/**
	 * Store the Dice Bag Manager at the specified Uri.
	 * @param diceBagManager The Dice Bag Manager to export.
	 * @param uri The location where to write.
	 * @param export {@code true} if the serialization is for export purpose, {@code false}
	 * if the serialization is to store data internally.
	 * @return Error code. One of the {@code ERR_*} constant.
	 */
	protected int writeDiceBagManager(DiceBagManager diceBagManager, Uri uri, boolean export) {
		return writeDiceBagManager(diceBagManager, uri, export, RES_MESSAGE_NONE);
	}
	
	/**
	 * Store the Dice Bag Manager at the specified Uri.
	 * @param diceBagManager The Dice Bag Manager to export.
	 * @param uri The location where to write.
	 * @param export {@code true} if the serialization is for export purpose, {@code false}
	 * if the serialization is to store data internally.
	 * @param errorMessageResId The resource ID of the error message to show on error, or {@code RES_MESSAGE_NONE} to disable error output.
	 * @return Error code. One of the {@code ERR_*} constant.
	 */
	protected int writeDiceBagManager(DiceBagManager diceBagManager, Uri uri, boolean export, int errorMessageResId) {
		int retVal = ERR_NONE;
		OutputStream fos;

		try {
			synchronized (dataAccessLock) {
				
				fos = context.getContentResolver().openOutputStream(uri);
				
				SerializationManager.DiceBagManager(fos, diceBagManager, export);
				
				fos.close();
			}
			retVal = ERR_NONE; //Redundant
			Log.i(TAG, "writeDiceBagManager: " + retVal);
		} catch (FileNotFoundException e) {
			Log.e(TAG, "writeDiceBagManager", e);
			retVal = ERR_FILE_NOT_FOUND;
			showErrorMessage(context, errorMessageResId);
		} catch (IOException e) {
			Log.e(TAG, "writeDiceBagManager", e);
			retVal = ERR_GENERIC; //More specific?
			showErrorMessage(context, errorMessageResId);
		} catch (Exception e) {
			Log.e(TAG, "writeDiceBagManager", e);
			retVal = ERR_GENERIC;
			showErrorMessage(context, errorMessageResId);
		}
		return retVal;
	}
	
	
	/**
	 * Legacy method to load from old modifier file.
	 * @param collection Collection to populate
	 */
	protected boolean loadModifierCollection(ModifierCollection collection) {
		boolean retVal = false;
		FileInputStream fis;
		
		try {
			fis = context.openFileInput(OBSOLETE_FILE_NAME_BONUSBAG);

			SerializationManager.ModifierCollection(fis, collection);
			
			fis.close();
			
			retVal = collection.size() > 0;
		} catch (FileNotFoundException e) {
			//Should show toast only if this is not the fist attempt
			Log.w(TAG, "loadBonusBag", e);
			//showErrorMessage(context, R.string.err_cannot_read_mod);
		} catch (IOException e) {
			Log.e(TAG, "loadBonusBag", e);
			showErrorMessage(context, R.string.err_cannot_read_mod);
		} catch (Exception e) {
			Log.e(TAG, "loadBonusBag", e);
			showErrorMessage(context, R.string.err_cannot_read_mod);
		}
		
		return retVal;
	}
	
	/**
	 * Load a dice bag from the old dice storage file.
	 * @param collection Collection to populate.
	 */
	protected boolean loadDiceCollection(DiceCollection collection) {
		boolean retVal = false;
		FileInputStream fis;
		
		try {
			fis = context.openFileInput(OBSOLETE_FILE_NAME_DICEBAG);

			SerializationManager.DiceCollection(fis, collection);
			
			fis.close();

			retVal = collection.size() > 0;
		} catch (FileNotFoundException e) {
			//Should show toast only if this is not the fist attempt
			Log.w(TAG, "loadDiceBag", e);
			//showErrorMessage(context, R.string.err_cannot_read);
		} catch (IOException e) {
			Log.e(TAG, "loadDiceBag", e);
			showErrorMessage(context, R.string.err_cannot_read);
		} catch (Exception e) {
			Log.e(TAG, "loadDiceBag", e);
			showErrorMessage(context, R.string.err_cannot_read);
		}
		
		return retVal;
	}
	
	public void saveResultList(RollResult[] lastResult, ArrayList<RollResult[]> resultList) {
		FileOutputStream fos;

		resultList.add(0, lastResult);

		try {
			synchronized (dataAccessLock) {
				fos = context.openFileOutput(FILE_NAME_RESULTLIST, Context.MODE_PRIVATE);
				
				SerializationManager.ResultList(fos, resultList);
				
				fos.close();
			}
		} catch (FileNotFoundException e) {
			Log.e(TAG, "saveResultList", e);
			showErrorMessage(context, R.string.err_cannot_update);
		} catch (IOException e) {
			Log.e(TAG, "saveResultList", e);
			showErrorMessage(context, R.string.err_cannot_update);
		} catch (Exception e) {
			Log.e(TAG, "saveResultList", e);
			showErrorMessage(context, R.string.err_cannot_update);
		}
		
		resultList.remove(0);
		
		resultListCache = null;
	}

	public ArrayList<RollResult[]> loadResultList() {
		if (resultListCache == null) {
			preloadResultList();
		}
		//Since the first element is the "Last Roll", it is
		//usually removed (lastResult = loadResultList().remove(0)).
		//Thus, this function has to return a copy of the cache
		//ArrayList in order to preserve all it's elements.
		return resultListCache == null ? null : new ArrayList<RollResult[]>(resultListCache);
	}

	ArrayList<RollResult[]> resultListCache = null;
	
	public void preloadResultList() {
		ArrayList<RollResult[]> retVal;
		FileInputStream fis;
		
		retVal = null;
		
		try {
			fis = context.openFileInput(FILE_NAME_RESULTLIST);
			
			retVal = SerializationManager.ResultList(fis);

			fis.close();
		} catch (FileNotFoundException e) {
			//Should show toast only if this is not the fist attempt
			Log.w(TAG, "preloadResultList", e);
			//showErrorMessage(context, R.string.err_cannot_read);
		} catch (IOException e) {
			Log.e(TAG, "preloadResultList", e);
			showErrorMessage(context, R.string.err_cannot_read);
		} catch (Exception e) {
			Log.e(TAG, "preloadResultList", e);
			showErrorMessage(context, R.string.err_cannot_read);
		}
		
		if (retVal != null && retVal.size() == 0) {
			Log.i(TAG, "preloadResultList: empty");
			retVal = null;
		}

		resultListCache = retVal;
	}
	
	private void showErrorMessage(final Context ctx, final int messageResId) {
		if (messageResId != RES_MESSAGE_NONE) {
			new android.os.Handler(ctx.getMainLooper()).post(new java.lang.Runnable() {
				@Override
				public void run() {
					Toast.makeText(ctx, messageResId, Toast.LENGTH_LONG).show();
				}
			});
		}
	}
}
