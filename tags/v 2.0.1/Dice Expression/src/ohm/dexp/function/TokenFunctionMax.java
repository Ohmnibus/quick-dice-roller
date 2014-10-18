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

		String sign;
		String lFlag = "";
		String rFlag = "";
		
		//resultValue = Math.max(lChild.getRawResult(), rChild.getRawResult());
		if (lChild.getRawResult() > rChild.getRawResult()) {
			resultValue = lChild.getRawResult();
			lFlag = SYM_SELECTED; //"!";
			sign = CH_GT; //">";
		} else if (lChild.getRawResult() < rChild.getRawResult()) {
			resultValue = rChild.getRawResult();
			rFlag = SYM_SELECTED; //"!";
			sign = CH_LT; //"<";
		} else {
			resultValue = lChild.getRawResult();
			lFlag = SYM_SELECTED; //"!";
			sign = CH_EQUAL; //"=";
		}

		resultMaxValue = Math.max(lChild.getMaxResult(), rChild.getMaxResult());
		resultMinValue = Math.max(lChild.getMinResult(), rChild.getMinResult());

		//Check result length
		String lRes = lChild.getResultString();
		String rRes = rChild.getResultString();
		if (lRes.length() + rRes.length() + 10 > MAX_TOKEN_STRING_LENGTH) {
			//Output will be too long, use short format
			lRes = Long.toString(lChild.getRawResult() / VALUES_PRECISION_FACTOR);
			rRes = Long.toString(rChild.getRawResult() / VALUES_PRECISION_FACTOR);
		}
//		resultString = 
//				"[" + 
//				lRes + 
//				"?" + 
//				rRes + 
//				"=" + 
//				Long.toString(resultValue / VALUES_PRECISION_FACTOR) + 
//				"]";
		resultString = 
				SYM_BEGIN + 
				lFlag + lRes +
				sign + 
				rRes + rFlag +
				SYM_END;
	}

}
