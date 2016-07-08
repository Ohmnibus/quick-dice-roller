package ohm.dexp.function;

public class TokenFunctionDWars extends TokenFunctionShRun5 {

	@Override
	protected int initChildNumber() {
		return 4;
	}

	@Override
	public int getType() {
		return 76;
	}

	@Override
	protected boolean extraSuccessOnRollAgain() {
		return true;
	}
	
	@Override
	protected boolean emitGlitch() {
		return false;
	}
}
