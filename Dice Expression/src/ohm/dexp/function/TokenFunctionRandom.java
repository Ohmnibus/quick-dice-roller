package ohm.dexp.function;

import ohm.dexp.DInstance;
import ohm.dexp.Dice;
import ohm.dexp.TokenBase;
import ohm.dexp.exception.DException;

public class TokenFunctionRandom extends TokenFunction {

	@Override
	protected int initChildNumber() {
		return 2;
	}

	@Override
	public int getType() {
		return 15;
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	protected void evaluateSelf(DInstance instance) throws DException {

		TokenBase lChild = getChild(1);
		TokenBase rChild = getChild(2);
		
		lChild.evaluate(instance);
		rChild.evaluate(instance);
		
		resultValue = Dice.random((int)lChild.getResult(), (int)rChild.getResult()) * VALUES_PRECISION_FACTOR;

		resultMaxValue = Math.max(lChild.getMaxResult(), rChild.getMaxResult());
		resultMinValue = Math.min(lChild.getMinResult(), rChild.getMinResult());

		//Check result length
//		String lRes = lChild.getResultString();
//		String rRes = rChild.getResultString();
//		if (lRes.length() + rRes.length() + 10 > MAX_TOKEN_STRING_LENGTH) {
//			//Output will be too long, use short format
//			resultString = 
//				"[" + 
//				Long.toString(lChild.getRawResult() / VALUES_PRECISION_FACTOR) + 
//				":" + 
//				Long.toString(rChild.getRawResult() / VALUES_PRECISION_FACTOR) + 
//				"=" + 
//				Long.toString(resultValue / VALUES_PRECISION_FACTOR) + 
//				"]";
//		} else {
//			//Long format
//			resultString = 
//				"[" + 
//				lRes + 
//				":" + 
//				rRes + 
//				"=" + 
//				Long.toString(resultValue / VALUES_PRECISION_FACTOR) + 
//				"]";
//		}
		resultString = "[" + Long.toString(resultValue / VALUES_PRECISION_FACTOR) + "]";
	}

}
