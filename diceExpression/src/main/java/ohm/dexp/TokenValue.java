package ohm.dexp;

import ohm.dexp.exception.DException;
import ohm.dexp.exception.UnknownVariable;

public abstract class TokenValue extends TokenBase {

	protected TokenValue(int position) {
		super(position);
	}

//	public static TokenValue InitToken(int value) {
//		return new TokenValueConstant(value * VALUES_PRECISION_FACTOR);
//	}

	/**
	 * Initialize a token of type {@link TokenValueConstant}.
	 * @param value Token value, expressed as fixed point decimal value.
	 * @return New token instance.
	 */
	public static TokenValue InitToken(long value, int position) {
		return new TokenValueConstant(value, position);
	}

	/**
	 * Initialize a token of type {@link TokenValueVariable}.
	 * @param name Variable name.
	 * @param position Starting position inside the expression, used in {@link UnknownVariable} exception.
	 * @return New token instance.
	 */
	public static TokenValue InitToken(String name, int position) {
		return new TokenValueVariable(name, position);
	}

	/**
	 * Initialize a token of type {@link TokenValueVariable}.
	 * @param name Variable name.
	 * @param context Expression context.
	 * @return New token instance, or {@code null} if the variable name was not found.
	 */
	public static TokenValue InitToken(String name, int position, DContext context) {
		return context == null || !context.checkName(name) ? null : new TokenValueVariable(name, position);
	}

	/**
	 * Parse a value and return a long expressed as fixed point value.
	 * @param str Number to parse
	 * @return Parsed number as fixed point long.
	 */
	public static long ParseRawValue(String str) {
		long retVal;
		int iDotPlace = str.indexOf(".");
		if (iDotPlace>=0) {
			//TODO: Following code should be optimized/cleaned
			
			/* Get decimal value */
			if ((str.length()-1)>iDotPlace) {
				retVal = Long.parseLong((str+"000").substring(iDotPlace+1, iDotPlace+1+VALUES_PRECISION_DIGITS));
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
	
	public TokenValueConstant(long value, int position) {
		super(position);
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
		return PRIO_VALUE;
	}

	@Override
	protected void evaluateSelf(DContext instance) throws DException {
		resultString = rawValueToString(resultValue);
	}

}

class TokenValueVariable extends TokenValue {
	
	protected String name;
	//protected int position;
	
	public TokenValueVariable(String name, int position) {
		super(position);
		this.name = name;
		//this.position = position;
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
		return PRIO_VALUE;
	}

	@Override
	protected void evaluateSelf(DContext instance) throws DException {
		try {
			if (instance == null) {
				//Name not defined for sure
				throw new IllegalArgumentException();
			}
			resultValue = instance.getValue(name);
			//resultMinValue = instance.getMinValue(name);
			//resultMaxValue = instance.getMaxValue(name);
			//Using instance's max and min values lead to ranges too broad.
			resultMinValue = resultValue;
			resultMaxValue = resultValue;
			resultString = "[" + name + ":" + rawValueToString(resultValue) + "]";
		} catch (IllegalArgumentException ex) {
			resultValue = 0;
			resultMinValue = resultValue;
			resultMaxValue = resultValue;
			resultString = "";
			throw new UnknownVariable(name, getPosition());
		}
	}
}