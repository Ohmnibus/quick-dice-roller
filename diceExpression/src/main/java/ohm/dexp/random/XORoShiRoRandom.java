package ohm.dexp.random;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Subclass of java.lang.Random to use the xoroshiro128+ random generator.<br>
 * @see java.util.Random
 * @see <a href="http://http://xoroshiro.di.unimi.it/">xoroshiro128+</a>
 */
public class XORoShiRoRandom extends Random {
	private long[] seeds;

	/**
	 * Constructs a random generator with an initial state that is unlikely to be duplicated by a subsequent instantiation.
	 */
	public XORoShiRoRandom() {
		SecureRandom seeder = new SecureRandom();
		seeds = new long[2];
		int bytePerSeed = Long.SIZE / 8;
		for (int i = 0; i < 2; i++) {
			byte[] byteSeed = seeder.generateSeed(bytePerSeed);
			for (byte b : byteSeed) {
				seeds[i] = (seeds[i] << 8) + (b & 0xff);
			}
		}
	}

	public XORoShiRoRandom(long seed) {
		super(seed);
	}

	@Override
	public synchronized void setSeed(long seed) {
		super.setSeed(seed);
		seeds = new long[2];
		seeds[0] = splitMix(seed);
		seeds[1] = splitMix(seeds[0]);
	}

	private static final long c1 = 0x9E3779B97F4A7C15L;
	private static final long c2 = 0xBF58476D1CE4E5B9L;
	private static final long c3 = 0x94D049BB133111EBL;

	/** This is a fixed-increment version of Java 8's SplittableRandom generator<br>
	 * See http://dx.doi.org/10.1145/2714064.2660195 and
	 * http://docs.oracle.com/javase/8/docs/api/java/util/SplittableRandom.html<br>
	 * @param x base value
	 * @return Next random value
	 */
	private long splitMix(long x) {
		long z = (x + c1);
		z = (z ^ (z >> 30)) * c2;
		z = (z ^ (z >> 27)) * c3;
		return z ^ (z >> 31);
	}

	protected int next(int nbits) {
		long s0 = seeds[0];
		long s1 = seeds[1];
		long result = s0 + s1;

		s1 ^= s0;
		seeds[0] = rotl(s0, 55) ^ s1 ^ (s1 << 14); // a, b
		seeds[1] = rotl(s1, 36); // c

		result &= ((1L << nbits) -1);
		return (int)result;
	}

	private static long rotl(long x, int k) {
		return (x << k) | (x >> (64 - k));
	}
}
