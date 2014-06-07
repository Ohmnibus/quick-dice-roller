package ohm.quickdice;

import ohm.dexp.function.*;
import ohm.quickdice.control.DiceBagManager;
import ohm.quickdice.control.GraphicManager;
import ohm.quickdice.control.PersistenceManager;
import ohm.quickdice.control.PreferenceManager;
import ohm.quickdice.entity.FunctionDescriptor;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;

public class QuickDiceApp extends Application {

	protected static final String KEY_LAST_VERSION = "KEY_LAST_VERSION";

	private static QuickDiceApp mySelf = null;
	private PreferenceManager prefs = null;
	private GraphicManager graphic = null;
	private PersistenceManager persistence = null;
	private DiceBagManager bagManager = null;
	
	@Override
	public void onCreate() {
		super.onCreate();

		initFunctions.run();

		mySelf = this;
	}
	
	/**
	 * Thread to handle functions initializations.<br />
	 * It's quite safe to initialize functions on a thread, since fist roll
	 * request won't be made until main UI initialization.<br />
	 * A check, though, would be great.
	 */
	private Thread initFunctions = new Thread(new Runnable(){
		@Override
		public void run(){
			//Initialize function list
			addFunction("max", TokenFunctionMax.class, R.drawable.ic_fnc, R.string.fncMaxName, R.string.fncMaxDesc, R.string.fncMaxURL, R.array.fncMaxParamNames, R.array.fncMaxParamHints);
			addFunction("min", TokenFunctionMin.class, R.drawable.ic_fnc, R.string.fncMinName, R.string.fncMinDesc, R.string.fncMinURL, R.array.fncMinParamNames, R.array.fncMinParamHints);
			addFunction("rand", TokenFunctionRandom.class, R.drawable.ic_fnc, R.string.fncRandName, R.string.fncRandDesc, R.string.fncRandURL, R.array.fncRandParamNames, R.array.fncRandParamHints);
			addFunction("exp", TokenFunctionExp.class, R.drawable.ic_fnc, R.string.fncExpName, R.string.fncExpDesc, R.string.fncExpURL, R.array.fncExpParamNames, R.array.fncExpParamHints);
			addFunction("expup", TokenFunctionExpUp.class, R.drawable.ic_fnc, R.string.fncExpUpName, R.string.fncExpUpDesc, R.string.fncExpUpURL, R.array.fncExpUpParamNames, R.array.fncExpUpParamHints);
			addFunction("explode", TokenFunctionExplode.class, R.drawable.ic_fnc, R.string.fncExplodeName, R.string.fncExplodeDesc, R.string.fncExplodeURL, R.array.fncExplodeParamNames, R.array.fncExplodeParamHints);
			addFunction("explodeup", TokenFunctionExplodeUp.class, R.drawable.ic_fnc, R.string.fncExplodeUpName, R.string.fncExplodeUpDesc, R.string.fncExplodeUpURL, R.array.fncExplodeUpParamNames, R.array.fncExplodeUpParamHints);

			addFunction("rak", TokenFunctionRollAndKeep.class, R.drawable.ic_fnc, R.string.fncRakName, R.string.fncRakDesc, R.string.fncRakURL, R.array.fncRakParamNames, R.array.fncRakParamHints);
			addFunction("pool", TokenFunctionPool.class, R.drawable.ic_fnc, R.string.fncPoolName, R.string.fncPoolDesc, R.string.fncPoolURL, R.array.fncPoolParamNames, R.array.fncPoolParamHints);
			addFunction("owod", TokenFunctionOWoD.class, R.drawable.ic_fnc, R.string.fncOwodName, R.string.fncOwodDesc, R.string.fncOwodURL, R.array.fncOwodParamNames, R.array.fncOwodParamHints);
			addFunction("nwod", TokenFunctionNWoD.class, R.drawable.ic_fnc, R.string.fncNwodName, R.string.fncNwodDesc, R.string.fncNwodURL, R.array.fncNwodParamNames, R.array.fncNwodParamHints);
			addFunction("exal", TokenFunctionExalted.class, R.drawable.ic_fnc, R.string.fncExalName, R.string.fncExalDesc, R.string.fncExalURL, R.array.fncExalParamNames, R.array.fncExalParamHints);
			addFunction("bwheel", TokenFunctionBWheel.class, R.drawable.ic_fnc, R.string.fncBWheelName, R.string.fncBWheelDesc, R.string.fncBWheelURL, R.array.fncBWheelParamNames, R.array.fncBWheelParamHints);
			addFunction("dwars", TokenFunctionDWars.class, R.drawable.ic_fnc, R.string.fncDWarsName, R.string.fncDWarsDesc, R.string.fncDWarsURL, R.array.fncDWarsParamNames, R.array.fncDWarsParamHints);
			addFunction("hero", TokenFunctionHERO.class, R.drawable.ic_fnc, R.string.fncHEROName, R.string.fncHERODesc, R.string.fncHEROURL, R.array.fncHEROParamNames, R.array.fncHEROParamHints);
			addFunction("bash", TokenFunctionBASH.class, R.drawable.ic_fnc, R.string.fncBASHName, R.string.fncBASHDesc, R.string.fncBASHURL, R.array.fncBASHParamNames, R.array.fncBASHParamHints);

			addFunction("rup", TokenFunctionRoundUp.class, R.drawable.ic_fnc, R.string.fncRupName, R.string.fncRupDesc, R.string.fncRupURL, R.array.fncRupParamNames, R.array.fncRupParamHints);
			addFunction("rdn", TokenFunctionRoundDown.class, R.drawable.ic_fnc, R.string.fncRdnName, R.string.fncRdnDesc, R.string.fncRdnURL, R.array.fncRdnParamNames, R.array.fncRdnParamHints);
			addFunction("abs", TokenFunctionAbs.class, R.drawable.ic_fnc, R.string.fncAbsName, R.string.fncAbsDesc, R.string.fncAbsURL, R.array.fncAbsParamNames, R.array.fncAbsParamHints);
		}
	});

	private FunctionDescriptor[] functions = null;
	
	private void addFunction(String token, Class<? extends TokenFunction> functionClass, int resId, int nameId, int descriptionId, int onlineReferenceId, int paramNamesId, int paramHintsId) {
		if (functions == null) {
			functions = new FunctionDescriptor[1];
		} else {
			FunctionDescriptor[] newFnc = new FunctionDescriptor[functions.length + 1];
			for (int i = 0; i<functions.length; i++){
				newFnc[i] = functions[i];
			}
			functions = newFnc;
		}
		
		TokenFunction.addFunction(token, functionClass);
		
		functions[functions.length - 1] = FunctionDescriptor.initDescriptor(
				getBaseContext(), 
				token,
				resId,
				nameId, 
				descriptionId, 
				onlineReferenceId,
				paramNamesId, 
				paramHintsId);
	}
	
	public FunctionDescriptor[] getFunctionDescriptors() {
		//TODO: Put a lock until "initFunctions" has completed.
		return functions;
	}
	
	/**
	 * Return the instance of the singleton.
	 * @return An instance of this singleton.
	 */
	public static QuickDiceApp getInstance() {
		return mySelf;
	}
	
	/**
	 * Return the application version number.
	 * @return Current application version number
	 */
	public int getCurrentVersion() {
		int currentVersion;
		try {
			currentVersion = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			currentVersion = 1;
		}
		return currentVersion;
	}

	int lastVersionExecuted = -1; //This is not handled by the common cache
	/**
	 * Return the application version number executed last time.<br />
	 * This is used to properly show the "What's new" pop-up.
	 * @return Last version number or -1 if this is the first time the app is executed.
	 */
	public int getLastVersionExecuted() {
		if (lastVersionExecuted < 0) {
			lastVersionExecuted = android.preference.PreferenceManager.getDefaultSharedPreferences(this).getInt(KEY_LAST_VERSION, -1);
		}
		return lastVersionExecuted;
	}

	/**
	 * Set the application version number to the preference.
	 * @param version Version number to set
	 */
	public void setLastVersionExecuted(int version) {
		SharedPreferences.Editor edit;
		edit = android.preference.PreferenceManager.getDefaultSharedPreferences(this).edit();
		edit.putInt(KEY_LAST_VERSION, version);
		edit.commit();
		lastVersionExecuted = version;
	}
	
	/**
	 * Get a {@link PersistenceManager} to store and retrieve data to and from
	 * the device internal memory. 
	 * @return The default {@link PersistenceManager}.
	 */
	public PreferenceManager getPreferences() {
		if (prefs == null)
			prefs = new PreferenceManager(this);
		return prefs;
	}

	/**
	 * Get a {@link PersistenceManager} to store and retrieve data to and from
	 * the device internal memory. 
	 * @return The default {@link PersistenceManager}.
	 */
	public GraphicManager getGraphic() {
		if (graphic == null)
			graphic = new GraphicManager(this); //Graphic(getBaseContext())
		return graphic;
	}

	/**
	 * Get a {@link PersistenceManager} to store and retrieve data to and from
	 * the device internal memory. 
	 * @return The default {@link PersistenceManager}.
	 */
	public PersistenceManager getPersistence() {
		if (persistence == null)
			persistence = new PersistenceManager(getBaseContext());
		return persistence;
	}

	/**
	 * Get a {@link DiceBagManager} to store and retrieve dice bags to and from
	 * the device internal memory. 
	 * @return The default {@link DiceBagManager}.
	 */
	public DiceBagManager getBagManager() {
		if (bagManager == null) {
			bagManager = new DiceBagManager(getPersistence());
		}
		return bagManager;
	}

}
