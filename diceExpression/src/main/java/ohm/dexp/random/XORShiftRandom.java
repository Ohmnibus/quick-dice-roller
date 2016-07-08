package ohm.dexp.random;

import java.util.Random;

/**
 * Subclass of java.lang.Random to use the XORShift random generator.<br>
 * @see java.util.Random
 * @see <a href="http://www.javamex.com/tutorials/random_numbers/java_util_random_subclassing.shtml">Subclassing java.util.Random</a>
 */
public class XORShiftRandom extends Random {
	private long seed = System.nanoTime();

	public XORShiftRandom() {
	}

	protected int next(int nbits) {
		long x = this.seed;
		x ^= (x << 21);
		x ^= (x >>> 35);
		x ^= (x << 4);
		this.seed = x;
		x &= ((1L << nbits) -1);
		return (int) x;
	}
}
