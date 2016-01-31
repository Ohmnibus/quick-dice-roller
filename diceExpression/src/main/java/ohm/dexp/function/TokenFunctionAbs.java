package ohm.dexp.function;

import ohm.dexp.DContext;
import ohm.dexp.TokenBase;
import ohm.dexp.exception.DException;

public class TokenFunctionAbs extends TokenFunction {

	@Override
	protected int initChildNumber() {
		return 1;
	}

	@Override
	public int getType() {
		return 16;
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	protected void evaluateSelf(DContext instance) throws DException {

		TokenBase child = getChild(1);
		
		child.evaluate(instance);
		
		resultValue = Math.abs(child.getRawResult());

		resultMaxValue = child.getMaxResult();
		resultMinValue = child.getMinResult();
		
		if (resultMaxValue < 0 && resultMinValue < 0) {
			//Both negatives. Range change sign
			resultMaxValue = -resultMaxValue;
			resultMinValue = -resultMinValue;
		} else if (resultMaxValue < 0 || resultMinValue < 0) {
			//Only one negative. Range vary from 0 to higher absolute
			resultMaxValue = Math.abs(resultMaxValue);
			resultMinValue = Math.abs(resultMinValue);
			if (resultMaxValue < resultMinValue) {
				resultMaxValue = 0;
			} else {
				resultMinValue = 0;
			}
		}
		
		reorderMaxMinValues();

		//Check result length
		String res = child.getResultString();
		if (res.length() + 2 > MAX_TOKEN_STRING_LENGTH) {
			//Output will be too long, use short format
			//resultString = "|" + Long.toString(resultValue / VALUES_PRECISION_FACTOR) + "|";
			resultString = CH_ABS_OP + SYM_TRUNK_PART_ELLIPSIS + SYM_TRUNK_PART_EQUAL + Long.toString(resultValue / VALUES_PRECISION_FACTOR) + CH_ABS_CL;
		} else {
			//Long format
			//resultString = "|" + res + "|";
			resultString = CH_ABS_OP + res + CH_ABS_CL;
		}
	}

}
