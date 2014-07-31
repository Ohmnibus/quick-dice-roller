package ohm.dexp.function;

import ohm.dexp.DContext;
import ohm.dexp.TokenBase;
import ohm.dexp.exception.DException;

public class TokenFunctionMax extends TokenFunction {

	@Override
	protected int initChildNumber() {
		return 2;
	}

	@Override
	public int getType() {
		return 13;
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	protected void evaluateSelf(DContext instance) throws DException {

		TokenBase lChild = getChild(1);
		TokenBase rChild = getChild(2);
		
		lChild.evaluate(instance);
		rChild.evaluate(instance);

		resultValue = Math.max(lChild.getRawResult(), rChild.getRawResult());

		resultMaxValue = Math.max(lChild.getMaxResult(), rChild.getMaxResult());
		resultMinValue = Math.max(lChild.getMinResult(), rChild.getMinResult());

		//Check result length
		String lRes = lChild.getResultString();
		String rRes = rChild.getResultString();
		if (lRes.length() + rRes.length() + 10 > MAX_TOKEN_STRING_LENGTH) {
			//Output will be too long, use short format
			resultString = 
				"[" + 
				Long.toString(lChild.getRawResult() / VALUES_PRECISION_FACTOR) + 
				"?" + 
				Long.toString(rChild.getRawResult() / VALUES_PRECISION_FACTOR) + 
				"=" + 
				Long.toString(resultValue / VALUES_PRECISION_FACTOR) + 
				"]";
		} else {
			//Long format
			resultString = 
				"[" + 
				lRes + 
				"?" + 
				rRes + 
				"=" + 
				Long.toString(resultValue / VALUES_PRECISION_FACTOR) + 
				"]";
		}
	}

}
