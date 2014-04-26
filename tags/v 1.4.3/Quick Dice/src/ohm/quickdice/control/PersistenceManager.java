package ohm.quickdice.control;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import ohm.dexp.DExpression;
import ohm.quickdice.R;
import ohm.quickdice.entity.DiceBag;
import ohm.quickdice.entity.RollModifier;
import ohm.quickdice.entity.RollResult;

import android.content.Context;
import android.widget.Toast;

public class PersistenceManager {

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

	private Context context;
	
	public PersistenceManager(Context context) {
		this.context = context;
	}
	
	public Context getContext() {
		return context;
	}

	/**
	 * Load all the Dice Bags from the device internal memory, if found.
	 * @return An {@code ArrayList<DiceBag>} representing all the dice bags, or {@code null} if an error occurred.
	 */
	public ArrayList<DiceBag> loadDiceBags() {
		return loadOrImportDiceBags(null, ACTION_LOAD);
	}

	/**
	 * Load all the Dice Bags from the device external memory, if found.
	 * @param path The path where to import from
	 * @return An {@code ArrayList<DiceBag>} representing all the dice bags, or {@code null} if an error occurred.
	 */
	public ArrayList<DiceBag> importDiceBags(String path) {
		return loadOrImportDiceBags(path, ACTION_IMPORT);
	}

	/**
	 * Load all the Dice Bags from the device internal memory, if found.
	 * @param path The path where to import from (used only if {@code action} == {@link #ACTION_IMPORT})
	 * @param action Either {@link #ACTION_LOAD} or {@link #ACTION_IMPORT}
	 * @return An {@code ArrayList<DiceBag>} representing all the dice bags, or {@code null} if an error occurred.
	 */
	protected ArrayList<DiceBag> loadOrImportDiceBags(String path, int action) {
		ArrayList<DiceBag> retVal;
		int errorMessageResId;
		FileInputStream fis;
		
		retVal = null;
		if (action == ACTION_LOAD) {
			errorMessageResId = R.string.err_cannot_read;
		} else if (action == ACTION_IMPORT) {
			errorMessageResId = R.string.err_cannot_import;
		} else {
			throw new IllegalArgumentException();
		}

		try {
			synchronized (dataAccessLock) {
				if (action == ACTION_LOAD) {
					//Internal storage
					fis = context.openFileInput(FILE_NAME_DICEBAGS);
				} else {
					//External storage
					fis = new FileInputStream(new File(path));
				}

				retVal = SerializationManager.DiceBags(fis);

				fis.close();
			}
		} catch (FileNotFoundException e) {
			//Should show toast only if this is not the fist attempt
			if (action == ACTION_IMPORT) {
				Toast.makeText(context, errorMessageResId, Toast.LENGTH_LONG).show();
			}
		} catch (IOException e) {
			Toast.makeText(context, errorMessageResId, Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			Toast.makeText(context, errorMessageResId, Toast.LENGTH_LONG).show();
		}
		
		if (retVal != null && retVal.size() == 0) {
			retVal = null;
		}

		return retVal;
	}

	/**
	 * Store all the Dice Bags in the device internal memory.
	 * @param diceBags An {@code ArrayList<DiceBag>} representing all the Dice Bags.
	 * @return {@code true} if data where saved, {@code false} otherwise
	 */
	public boolean saveDiceBags(ArrayList<DiceBag> diceBags) {
		return saveOrExportDiceBags(diceBags, null, ACTION_SAVE);
	}
	
	/**
	 * Store all the Dice Bags in the device external memory.
	 * @param diceBags An {@code ArrayList<DiceBag>} representing all the Dice Bags.
	 * @param path Path where to save the dice bags data.
	 * @return {@code true} if data where saved, {@code false} otherwise
	 */
	public boolean exportDiceBags(ArrayList<DiceBag> diceBags, String path) {
		return saveOrExportDiceBags(diceBags, path, ACTION_EXPORT);
	}

	
	/**
	 * Store all the Dice Bags in the device internal or external memory.
	 * @param diceBags An ArrayList<DiceBag> representing all the Dice Bags
	 * @param path The path where to save (used only if {@code action} == {@link #ACTION_EXPORT})
	 * @param action Either {@link #ACTION_SAVE} or {@link #ACTION_EXPORT}
	 * @return {@code true} if data where saved, {@code false} otherwise
	 */
	protected boolean saveOrExportDiceBags(ArrayList<DiceBag> diceBags, String path, int action) {
		boolean retVal = false;
		int errorMessageResId;
		FileOutputStream fos;

		if (action == ACTION_SAVE) {
			errorMessageResId = R.string.err_cannot_update;
		} else if (action == ACTION_EXPORT) {
			errorMessageResId = R.string.err_cannot_export;
		} else {
			throw new IllegalArgumentException();
		}

		try {
			synchronized (dataAccessLock) {
				if (action == ACTION_SAVE) {
					//Internal storage
					fos = context.openFileOutput(FILE_NAME_DICEBAGS, Context.MODE_PRIVATE);
				} else {
					//External storage
					fos = new FileOutputStream(new File(path));
				}
				SerializationManager.DiceBags(fos, diceBags);
				fos.close();
			}
			retVal = true;
		} catch (FileNotFoundException e) {
			Toast.makeText(context, errorMessageResId, Toast.LENGTH_LONG).show();
		} catch (IOException e) {
			Toast.makeText(context, errorMessageResId, Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			Toast.makeText(context, errorMessageResId, Toast.LENGTH_LONG).show();
		}
		return retVal;
	}


	/**
	 * Load a bonus bag from the device internal memory.
	 * @param context Context.
	 * @return An ArrayList<RollModifier> representing a bonus bag, or null if an error occurred.
	 * @deprecated This function is for backward compatibility only. Use {@link loadDiceBags} instead.
	 */
	public ArrayList<RollModifier> loadBonusBag() {
		/*
		ArrayList<RollModifier> retVal;
		FileInputStream fis;
		InputStreamReader isr;
		BufferedReader br;
		StringBuilder text;
		String line;
		
		retVal = null;
		
		try {
			fis = context.openFileInput(OBSOLETE_FILE_NAME_BONUSBAG);
			isr = new InputStreamReader(fis);
			br = new BufferedReader(isr);
			text = new StringBuilder();
			
			while ((line = br.readLine()) != null) {
				if (text.length() > 0) {
					text.append("\n");
				}
				text.append(line);
			}
			//retVal = convertBonusBagFromJSONString(text.toString());
			retVal = SerializationManager.BonusList(text.toString());
			fis.close();
		} catch (FileNotFoundException e) {
			//Should show toast only if this is not the fist attempt
			//Toast.makeText(getBaseContext(), R.string.err_cannot_read, Toast.LENGTH_LONG);
			//if (DEBUG) {Log.i(TAG, "loadDiceBag:FileNotFoundException", e);}
		} catch (JSONException e) {
			Toast.makeText(context, R.string.err_cannot_read_mod, Toast.LENGTH_LONG).show();
		} catch (IOException e) {
			Toast.makeText(context, R.string.err_cannot_read_mod, Toast.LENGTH_LONG).show();
		}
		
		if (retVal != null && retVal.size() == 0) {
			retVal = null;
		}

		return retVal;
		*/
		ArrayList<RollModifier> retVal;
		FileInputStream fis;
		
		retVal = null;
		
		try {
			fis = context.openFileInput(OBSOLETE_FILE_NAME_BONUSBAG);

			retVal = SerializationManager.BonusList(fis);
			
			fis.close();
		} catch (FileNotFoundException e) {
			//Should show toast only if this is not the fist attempt
			//Toast.makeText(getBaseContext(), R.string.err_cannot_read, Toast.LENGTH_LONG);
			//if (DEBUG) {Log.i(TAG, "loadDiceBag:FileNotFoundException", e);}
		} catch (IOException e) {
			Toast.makeText(context, R.string.err_cannot_read_mod, Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			Toast.makeText(context, R.string.err_cannot_read_mod, Toast.LENGTH_LONG).show();
		}
		
		if (retVal != null && retVal.size() == 0) {
			retVal = null;
		}

		return retVal;
	}
	
	/**
	 * Load a dice bag from the device internal memory.
	 * @param context Context.
	 * @return An ArrayList<DExpression> representing a dice bag, or null if an error occurred.
	 * @deprecated This function is for backward compatibility only. Use {@link loadDiceBags} instead.
	 */
	public ArrayList<DExpression> loadDiceBag() {
		/*
		ArrayList<DExpression> retVal;
		FileInputStream fis;
		InputStreamReader isr;
		BufferedReader br;
		StringBuilder text;
		String line;
		
		retVal = null;
		
		try {
			fis = context.openFileInput(OBSOLETE_FILE_NAME_DICEBAG);
			isr = new InputStreamReader(fis);
			br = new BufferedReader(isr);
			text = new StringBuilder();
			
			while ((line = br.readLine()) != null) {
				if (text.length() > 0) {
					text.append("\n");
				}
				text.append(line);
			}
			//retVal = convertDiceBagFromJSONString(text.toString());
			retVal = SerializationManager.DiceList(text.toString());
			fis.close();
		} catch (FileNotFoundException e) {
			//Should show toast only if this is not the fist attempt
			//Toast.makeText(getBaseContext(), R.string.err_cannot_read, Toast.LENGTH_LONG);
			//if (DEBUG) {Log.i(TAG, "loadDiceBag:FileNotFoundException", e);}
		} catch (JSONException e) {
			//if (DEBUG) {Log.e(TAG, "loadDiceBag:JSONException", e);}
			Toast.makeText(context, R.string.err_cannot_read, Toast.LENGTH_LONG).show();
		} catch (IOException e) {
			//if (DEBUG) {Log.e(TAG, "loadDiceBag:IOException", e);}
			Toast.makeText(context, R.string.err_cannot_read, Toast.LENGTH_LONG).show();
		}
		
		if (retVal != null && retVal.size() == 0) {
			//if (DEBUG) {Log.i(TAG, "loadDiceBag:noData");}
			retVal = null;
		}

		return retVal;
		*/
		ArrayList<DExpression> retVal;
		FileInputStream fis;
		
		retVal = null;
		
		try {
			fis = context.openFileInput(OBSOLETE_FILE_NAME_DICEBAG);

			retVal = SerializationManager.DiceList(fis);
			
			fis.close();
		} catch (FileNotFoundException e) {
			//Should show toast only if this is not the fist attempt
			//Toast.makeText(getBaseContext(), R.string.err_cannot_read, Toast.LENGTH_LONG);
			//if (DEBUG) {Log.i(TAG, "loadDiceBag:FileNotFoundException", e);}
		} catch (IOException e) {
			//if (DEBUG) {Log.e(TAG, "loadDiceBag:IOException", e);}
			Toast.makeText(context, R.string.err_cannot_read, Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			//if (DEBUG) {Log.e(TAG, "loadDiceBag:JSONException", e);}
			Toast.makeText(context, R.string.err_cannot_read, Toast.LENGTH_LONG).show();
		}
		
		if (retVal != null && retVal.size() == 0) {
			//if (DEBUG) {Log.i(TAG, "loadDiceBag:noData");}
			retVal = null;
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
			//if (DEBUG) {Log.e(TAG, "saveDiceBag:FileNotFoundException", e);}
			Toast.makeText(context, R.string.err_cannot_update, Toast.LENGTH_LONG).show();
		} catch (IOException e) {
			//if (DEBUG) {Log.e(TAG, "saveDiceBag:IOException", e);}
			Toast.makeText(context, R.string.err_cannot_update, Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			//if (DEBUG) {Log.e(TAG, "saveDiceBag:JSONException", e);}
			Toast.makeText(context, R.string.err_cannot_update, Toast.LENGTH_LONG).show();
		}
		
		resultList.remove(0);
	}

	public ArrayList<RollResult[]> loadResultList() {
		ArrayList<RollResult[]> retVal;
		FileInputStream fis;
		
		retVal = null;
		
		try {
			fis = context.openFileInput(FILE_NAME_RESULTLIST);
			
			retVal = SerializationManager.ResultList(fis);

			fis.close();
		} catch (FileNotFoundException e) {
			//Should show toast only if this is not the fist attempt
			//Toast.makeText(getBaseContext(), R.string.err_cannot_read, Toast.LENGTH_LONG);
			//if (DEBUG) {Log.i(TAG, "loadDiceBag:FileNotFoundException", e);}
		} catch (IOException e) {
			//if (DEBUG) {Log.e(TAG, "loadDiceBag:IOException", e);}
			Toast.makeText(context, R.string.err_cannot_read, Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			//if (DEBUG) {Log.e(TAG, "loadDiceBag:JSONException", e);}
			Toast.makeText(context, R.string.err_cannot_read, Toast.LENGTH_LONG).show();
		}
		
		if (retVal != null && retVal.size() == 0) {
			//if (DEBUG) {Log.i(TAG, "loadDiceBag:noData");}
			retVal = null;
		}

		return retVal;
	}
}
