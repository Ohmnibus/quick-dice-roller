package ohm.dexp;

//import java.util.Random;

/**
 * Simple class to handle and roll dice.
 * @author Ohmnibus
 *
 */
public class Dice {
	
	private int iTimes;
	private int iFaces;
	private int iModifier;
	//private static Random alea = new Random();
	private static long seed = System.nanoTime();

	/**
	 * Initialize a simple dice (1d6)
	 */
	public Dice() {
		this(1, 6, 0);
	};
	/**
	 * Initialize a dice.
	 * @param faces Dice faces.
	 */
	public Dice(int faces) {
		this(1, faces, 0);
	};
	/**
	 * Initialize a given amount of dices.
	 * @param times Dice number.
	 * @param faces Dice faces.
	 */
	public Dice(int times, int faces) {
		this(times, faces, 0);
	};
	/**
	 * Initialize a given amount of dices with a final modifier.
	 * @param times Dice number.
	 * @param faces Dice faces.
	 * @param modifier Modifier to apply.
	 */
	public Dice(int times, int faces, int modifier) {
		setTimes(times);
		setFaces(faces);
		setModifier(modifier);
	};

	public int getFaces() {
		return iFaces;
	}

	public void setFaces(int iFac) {
		if (iFac<1)
			iFaces = 1;
		else
			iFaces = iFac;
	}

	public int getTimes() {
		return iTimes;
	}

	public void setTimes(int iTim) {
		if (iTim<0)
			iTimes = 0;
		else
			iTimes = iTim;
	}

	public int getModifier() {
		return iModifier;
	}

	public void setModifier(int iModifier) {
		this.iModifier = iModifier;
	}

	/**
	 * Roll the dice defined by this instance.
	 * @return Result of the roll.
	 */
	public int roll(){
		return roll(iTimes, iFaces) + iModifier;
	}

	/**
	 * Perform an open d20 roll. A 20 add another roll, an 1 subtract the next roll.
	 * @return Result of the open d20 roll.
	 */
	public static int openRoll() {
		int retVal;
		int thisRoll;
		int lastRoll;
		retVal = 0;
		lastRoll = 20; //The first roll is always added
		do {
			thisRoll = roll(20);
			if (lastRoll == 1) {
				retVal -= thisRoll;
			} else {
				retVal += thisRoll;
			}
			lastRoll = thisRoll;
		} while(lastRoll == 1 || lastRoll == 20);
		
		return retVal;
	}

	/**
	 * Roll a specific dice.
	 * @param faces Dice faces.
	 * @return Result of the roll.
	 */
	public static int roll(int faces) {
		return roll(1, faces);
	}

	/**
	 * Roll a specific dice for the specific amount of times.
	 * @param times Dices to roll.
	 * @param faces Dice faces.
	 * @return Sum of all dices.
	 */
	public static int roll(int times, int faces) {
		int retVal;
		
		if (faces <= 0)
			return 0;
		
		if (times <= 0)
			return 0;
		
		if (faces==1)
			return times;

		retVal = 0;
		for (int i = 1; i <= times; i++) {
			retVal = retVal + (faces - (Math.abs(getRandomInt()) % faces));
		}
		return retVal;
	}

	/**
	 * Return a random value between the two given.
	 * @param min Min value.
	 * @param max Max value.
	 * @return Random value between the two given.
	 */
	public static int random(int min, int max) {
		int span;
		span = Math.abs(min - max) + 1;
		return Math.min(min, max) + (Math.abs(getRandomInt()) % span);
	}
	
	@Override
	public String toString() {
		StringBuilder retVal = new StringBuilder();
		if (iTimes == 0 && iModifier == 0) {
			return "0";
		}
		if (iTimes > 0) {
			retVal.append(iTimes).append("d").append(iFaces);
		}
		if (iModifier > 0) {
			retVal.append("+").append(iModifier);
		} else if (iModifier < 0) {
			retVal.append(iModifier);
		}
		return retVal.toString();
	}
	
//	private static int getRandomInt() {
//		return alea.nextInt();
//	}

	/**
	 * Generate a random number using XORShift algorithm.
	 * @see http://www.javamex.com/tutorials/random_numbers/xorshift.shtml
	 * @see http://www.javamex.com/tutorials/random_numbers/java_util_random_subclassing.shtml
	 */
	private static int getRandomInt() {
		seed ^= (seed << 21);
		seed ^= (seed >>> 35);
		seed ^= (seed << 4);
		return (int)seed;
	}
}
