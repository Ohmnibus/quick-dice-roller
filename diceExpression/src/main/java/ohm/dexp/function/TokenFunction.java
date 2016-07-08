package ohm.dexp.function;

import java.util.Hashtable;
import java.util.Locale;

import ohm.dexp.DContext;
import ohm.dexp.TokenBase;
import ohm.dexp.exception.DException;

public abstract class TokenFunction extends TokenBase {

	protected TokenFunction() {
		super(0);
	}

	private static Hashtable<String, Class<? extends TokenFunction>> _allowedFunctions = new Hashtable<String, Class<? extends TokenFunction>>();

	protected static final long UNDEFINED = Long.MIN_VALUE + 1; //+1 so that I can still use Long.MIN_VALUE.
	/** Right arrow */
	protected static final String CH_RARR = "\u2192";
	/** Left arrow */
	protected static final String CH_LARR = "\u2190";
	/** Round up open bracket */
	protected static final String CH_RUP_OP = "\u2308";
	/** Round up closed bracket */
	protected static final String CH_RUP_CL = "\u2309";
	/** Round down open bracket */
	protected static final String CH_RDN_OP = "\u230a";
	/** Round down closed bracket */
	protected static final String CH_RDN_CL = "\u230b";
	/** Absolute open bracket */
	protected static final String CH_ABS_OP = "|";
	/** Absolute closed bracket */
	protected static final String CH_ABS_CL = "|";
	/** Greater than */
	protected static final String CH_GT = ">";
	/** Lower than */
	protected static final String CH_LT = "<";
	/** Equal */
	protected static final String CH_EQUAL = "=";
	
	/** Begin of a complex result */
	protected static final String SYM_BEGIN = "[";
	/** End of a complex result */
	protected static final String SYM_END = "]";
	/** Begin of a complex result, alternative */
	protected static final String SYM_BEGIN_ALT = "{";
	/** End of a complex result, alternative */
	protected static final String SYM_END_ALT = "}";
	/** Separator for different roll result */
	protected static final String SYM_SEP = ",";
	/** Separator for different value of same roll */
	protected static final String SYM_SEP_SAME = ":";
	/** Separator for final overall (fumble, critical, botch, glitch) */
	protected static final String SYM_SEP_FINAL = "\u2261"; //"=" (with 3 lines)
	/** Denotes a success */
	protected static final String SYM_SUCCESS = "!";
	/** Denotes a failure */
	protected static final String SYM_FAILURE = "*";
	/** Denotes an extra result */
	protected static final String SYM_EXTRA = "!";
	/** Denotes a selected result */
	protected static final String SYM_SELECTED = "!";
	/** Separator for exploding rolls */
	protected static final String SYM_EXPLODE = "\u00bb"; //">>"
	
	/** Truncated output: ellipsis */
	protected static final String SYM_TRUNK_PART_ELLIPSIS = "\u2026"; //"..."
	/** Truncated output: equal */
	protected static final String SYM_TRUNK_PART_EQUAL = CH_EQUAL; //"="
	/** Truncated output: begin */
	protected static final String SYM_TRUNK_BEGIN = SYM_BEGIN + SYM_TRUNK_PART_ELLIPSIS + SYM_TRUNK_PART_EQUAL; //"[...="
	/** Truncated output: end */
	protected static final String SYM_TRUNK_END = SYM_END; //"]"
	
	/**
	 * Initialize the right function token by it's name.
	 * @param token Token of the function.
	 * @param position Token position.
	 * @return An instance representing the function, or {@code null} if not found.
	 */
	public static TokenFunction InitToken(String token, int position) {
		TokenFunction retVal;
		Class<? extends TokenFunction> fncClass;
		
		retVal = null;
		fncClass = _allowedFunctions.get(token.toLowerCase(Locale.getDefault()));
		
		if (fncClass != null) {
			try {
				retVal = fncClass.newInstance();
				retVal.position = position;
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
	
	/**
	 * Get the raw result of the child at specified index, if exists, or the specified default result.<br />
	 * This method is used to get the value of optional parameters.
	 * @param instance Instance used to evaluate the child.
	 * @param index Position (1-based) of the child to evaluate.
	 * @param defaultResult Value returned if child at {@code index} does not exists.
	 * @return Raw result of the child at specified index, if exists, or {@code defaultResult}.
	 * @throws DException Thrown if an error occurred during expression tree evaluation.
	 */
	protected long getOptionalChildRawResult(DContext instance, int index, long defaultResult) throws DException {
		long retVal;
		TokenBase tmpRoll = getChild(index);
		if (tmpRoll != null) {
			tmpRoll.evaluate(instance);
			retVal = tmpRoll.getRawResult();
		} else {
			retVal = defaultResult;
		}
		return retVal;
	}

	@Override
	public int getPriority() {
		return PRIO_FUNCTION;
	}
}