package ohm.dexp.function;

import ohm.dexp.DInstance;
import ohm.dexp.exception.DException;

public class TokenFunctionExplode extends TokenFunctionExplodeBase {

	@Override
	protected int initChildNumber() {
		return 2;
	}

	@Override
	public int getType() {
		return 22;
	}

	@Override
	protected void evaluateSelf(DInstance instance) throws DException {
		getChild(2).evaluate(instance);
		
		evaluateExplode(instance, getChild(1), getChild(2).getResult(), true);
	}

}
