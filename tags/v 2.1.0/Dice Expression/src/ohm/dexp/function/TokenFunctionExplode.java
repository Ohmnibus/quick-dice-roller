package ohm.dexp.function;

import ohm.dexp.DContext;
import ohm.dexp.TokenBase;
import ohm.dexp.exception.DException;

public class TokenFunctionExplode extends TokenFunctionExplodeBase {

	private static final int INDEX_ROLL = 1;
	private static final int INDEX_TOLERANCE = 2;

	@Override
	protected int initChildNumber() {
		return 2;
	}

	@Override
	public int getType() {
		return 22;
	}

	@Override
	protected void evaluateSelf(DContext instance) throws DException {
		TokenBase token;
		long tolerance;
		long upperTarget;
		long lowerTarget;
		
		token = getChild(INDEX_TOLERANCE);
		token.evaluate(instance);
		tolerance = token.getRawResult() - VALUES_PRECISION_FACTOR; //tolerance - 1
		
		token = getChild(INDEX_ROLL);
		token.evaluate(instance);
		//upperTarget = token.getMaxResult() - (tolerance - VALUES_PRECISION_FACTOR); //max - (tolerance - 1)
		//lowerTarget = token.getMinResult() + (tolerance - VALUES_PRECISION_FACTOR); //min + (tolerance - 1)
		upperTarget = token.getMaxResult() - tolerance; //max - (tolerance - 1)
		lowerTarget = token.getMinResult() + tolerance; //min + (tolerance - 1)

		evaluateExplode(instance, token, upperTarget, lowerTarget, 0, 0, true);
	}

}
