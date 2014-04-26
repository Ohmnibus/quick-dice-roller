package ohm.dexp;

import ohm.dexp.exception.DException;
import ohm.dexp.exception.UnknownVariable;

public abstract class TokenValue extends TokenBase {

	public static TokenValue InitToken(int value) {
		return new TokenValueConstant(value * VALUES_PRECISION_FACTOR);
	}

	public static TokenValue InitToken(long value) {
		return new TokenValueConstant(value);
	}

	/**
	 * Initialize a token of type {@link TokenValueVariable}.
	 * @param name Variable name.
	 * @param context Expression context.
	 * @return New token instance, or {@literal null} if the variable name was not found.
	 */
	public static TokenValue InitToken(String name, DContext context) {
		return context == null || !context.checkVariable(name) ? null : new TokenValueVariable(name);
	}

	public static long ParseRawValue(String str) {
		long retVal;
	    int iDotPlace = str.indexOf(".");
	    if (iDotPlace>=0) {
	    	//TODO: Ottimizzare quanto segue, il codice mi sembra piuttosto sporco
	        /* Get decimal value */
	        if ((str.length()-1)>iDotPlace) {
	        	retVal = Long.parseLong((str+"000").substring(iDotPlace+1, iDotPlace+1+VALUES_PRECISION));
	        } else
	        	retVal = 0;

	        /* Normalize to three digits */
	        while (retVal>VALUES_PRECISION_FACTOR) retVal=retVal/10;
	        /* Add integer value */
	        retVal = retVal + (Long.parseLong(str.substring(0, iDotPlace)) * VALUES_PRECISION_FACTOR);
	    } else {
	    	retVal = Long.parseLong(str) * VALUES_PRECISION_FACTOR;
	    }
	    return retVal;
	}
}

class TokenValueConstant extends TokenValue {
	
	public TokenValueConstant(long value) {
		resultValue = value;
		resultMinValue = value;
		resultMaxValue = value;
	}

	@Override
	protected int initChildNumber() {
		return 0;
	}

	@Override
	public int getType() {
		return 1;
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	protected void evaluateSelf(DInstance instance) throws DException {
	    resultString = rawValueToString(resultValue);
	}
	
}

class TokenValueVariable extends TokenValue {
	
	protected String _name;
	
	public TokenValueVariable(String name) {
		this._name = name;
	}

	@Override
	protected int initChildNumber() {
		return 0;
	}

	@Override
	public int getType() {
		return 20;
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	protected void evaluateSelf(DInstance instance) throws DException {
		try {
			resultValue = instance.getValue(_name);
			resultMinValue = resultValue;
			resultMaxValue = resultValue;
		    resultString = "[" + rawValueToString(resultValue) + "]";
		} catch (IllegalArgumentException ex) {
			resultValue = 0;
			resultMinValue = resultValue;
			resultMaxValue = resultValue;
		    resultString = "";
		    throw new UnknownVariable(_name, 0, 0);
		}
	}
}