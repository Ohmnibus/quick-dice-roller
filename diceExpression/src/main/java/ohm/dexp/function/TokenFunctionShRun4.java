package ohm.dexp.function;

/**
 * This class implement a function designed to implement Shadowrun 4 system.<br />
 * Is almost identical to Shadowrun 5 system, but the glitch is obtained if the
 * count of "ones" is greater *or equal* half the total rolls.
 * @author Ohmnibus
 * 
 */
public class TokenFunctionShRun4 extends TokenFunctionShRun5 {

	@Override
	public int getType() {
		return 78;
	}

	@Override
	protected boolean isGlitch(int oneCount, int rollCount) {
		return (oneCount * 2 >= rollCount);
	}
}
