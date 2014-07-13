package ohm.dexp.function;

import java.util.Hashtable;
import java.util.Locale;

import ohm.dexp.TokenBase;

public abstract class TokenFunction extends TokenBase {

	private static Hashtable<String, Class<? extends TokenFunction>> _allowedFunctions = new Hashtable<String, Class<? extends TokenFunction>>();
	
	//protected String mToken = null;
	
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
		
//		if (token.equals("ge")) {
//			return new TokenFunctionGE();
//		} else if (token.equals("le")) {
//			return new TokenFunctionLE();
//		}
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
	
//	/**
//	 * Set a token with which the function is recognized.<br />
//	 * Without a call to this method the function cannot be recognized by the parser
//	 * and thus cannot be used in expressions.<br />
//	 * The token bust be at least three characters long, and is case insensitive.<br />
//	 * A function can be recognized only by one token, so if the function 
//	 * already got a token, a call to this method will override the previous one.<br />
//	 * If another function is associated with the specified token, an exception
//	 * of type {@link IllegalArgumentException} will be raised.<br />
//	 * Specifying a {@code null} value for the token will remove current token
//	 * so that the function can no longer be used.<br />
//	 * @param token Token to assign to this function, or {@link null} to reset the assignment.
//	 * @throws IllegalArgumentException Thrown if the token is assigned to another function.
//	 * @throws InvalidParameterException Thrown if the token length is less than three characters.
//	 */
//	public void setToken(String token) throws IllegalArgumentException, InvalidParameterException {
//		Class<? extends TokenFunction> prevVal = null;
//		
//		if (token != null && token.length() < 3) {
//			//Token too short
//			throw new InvalidParameterException("The token \"" + token + "\" too short.");
//		}
//		
//		if (mToken != null) {
//			prevVal = _allowedFunctions.get(mToken);
//		}
//		
//		if (prevVal != null && ! prevVal.equals(this.getClass())) {
//			//Attempting to override another function
//			throw new IllegalArgumentException("The token \"" + token + "\" is already in use for the function \"" + prevVal.getName() + "\".");
//		}
//		
//		//internalSetToken(token);
//		//setToken(token, this.getClass());
//		if (token != null) {
//			//Add or change token
//			_allowedFunctions.put(token.toLowerCase(), this.getClass());
//		} else if (prevVal != null) {
//			//Remove token
//			_allowedFunctions.remove(mToken);
//		}
//		mToken = token.toLowerCase();
//	}
//
//	/**
//	 * Get the token identifying the function.
//	 * @return Token identifying the function, or {@code null} if no token was assigned.
//	 */
//	public String getToken() {
//		return mToken;
//	}

	//abstract protected void internalSetToken(String token) throws IllegalArgumentException;
	
//	/**
//	 * Add a function handler to the list of allowed functions.
//	 * @param token Function token.
//	 * @param functionClass Class descriptor for the function.
//	 */
//	protected static void setToken(String token, Class<? extends TokenFunction> functionClass) {
//		_allowedFunctions.put(token, functionClass);
//	}
//	
//	/**
//	 * Return the function class for the given token.
//	 * @param token Function token.
//	 * @param functionClass Class descriptor for the function.
//	 */
//	protected static Class<? extends TokenFunction> getToken(String token) {
//		return _allowedFunctions.get(token);
//	}
//	
//	/**
//	 * Remove a function handler from the list of allowed functions.
//	 * @param token Token identifying the function to remove.
//	 */
//	protected static void removeToken(String token) {
//		_allowedFunctions.remove(token);
//	}
}