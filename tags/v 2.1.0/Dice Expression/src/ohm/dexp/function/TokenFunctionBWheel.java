package ohm.dexp.function;

/**
 * This class implement a function designed to implement Burning Wheel system.<br />
 * @author Ohmnibus
 * 
 */
public class TokenFunctionBWheel extends TokenFunctionShRun5 {

	@Override
	protected int initChildNumber() {
		return 4;
	}

	@Override
	public int getType() {
		return 75;
	}

	@Override
	public int getPriority() {
		return 0;
	}
	
	@Override
	protected boolean emitGlitch() {
		return false;
	}

}
