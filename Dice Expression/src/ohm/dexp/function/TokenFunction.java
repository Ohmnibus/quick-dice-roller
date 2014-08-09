package ohm.dexp.function;

import java.util.Hashtable;
import java.util.Locale;

import ohm.dexp.TokenBase;

public abstract class TokenFunction extends TokenBase {

	private static Hashtable<String, Class<? extends TokenFunction>> _allowedFunctions = new Hashtable<String, Class<? extends TokenFunction>>();
	
	/**
	 * Initialize the right function token by it's name.
	 * @param token Token of the function.
	 * @return An instance representing the function, or {@code null} if not found.
	 */
	public static TokenFunction InitToken(String token) {
		TokenFunction retVal;
		Class<? extends TokenFunction> fncClass;
		
		retVal = null;
		fncClass = _allowedFunctions.get(token.toLowerCase(Locale.getDefault()));
		
		if (fncClass != null) {
			try {
				retVal = (TokenFunction) fncClass.newInstance();
			} catch (IllegalAccessException e) {
				retVal = null;
			} catch (InstantiationException e) {
				retVal = null;
			}
		}
		
		return retVal;
	}
	
	/**
	 * Add a function handler to the list of allowed functions.<br />
	 * Each function is identified by a unique token.
	 * @param token Function token.
	 * @param functionClass Class descriptor for the function.
	 */
	public static void addFunction(String token, Class<? extends TokenFunction> functionClass) {
		_allowedFunctions.put(token, functionClass);
	}

	/**
	 * Get a function name from it's class type.
	 * @param functionClass Class of the function.
	 * @return Function name or {@code null} if not found.
	 */
	protected static String getFunctionName(Class<? extends TokenFunction> functionClass) {
		String retVal = null;
		for (String s: _allowedFunctions.keySet()) {
			if (_allowedFunctions.get(s) == functionClass) {
				retVal = s;
				break;
			}
		}
		return retVal;
	}
}