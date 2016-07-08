package ohm.dexp;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Ohmnibus on 03/06/2016.
 */
public class DiceTest {

	@Test
	public void testBalance() {
		int faces = 20;
		int rolls = 10000000;
		//Dice dice = new Dice(1, 20);
		int[] results = new int[faces];
		for (int i=0; i<rolls; i++) {
			results[Dice.roll(faces)-1]++;
		}
		int max=results[0];
		int min=max;
		int total = 0;
		int diff;
		int maxDiff = 0;
		int maxDiffIndex = 0;

		int expected = rolls / faces;
		//double tolerance = expected * 0.005;
		double tolerance = Math.sqrt((double)rolls / 2);
		for (int i = 0; i < faces; i++) {
			total += (results[i] * (i + 1));
			if (max < results[i]) {
				max = results[i];
			}
			if (min > results[i]) {
				min = results[i];
			}
			diff = Math.abs(results[i] - expected);
			if (diff > maxDiff) {
				maxDiff = diff;
				maxDiffIndex = i;
			}
			System.out.print(i+1);
			System.out.print("\t:");
			System.out.print(results[i]);
			System.out.print(" ");
			System.out.print(diff);
			//System.out.print(" ");
			//System.out.print((double)(diff) / results[i]);
			System.out.println();
			assertEquals("Unbalanced result for " + (i + 1), expected, results[i], tolerance);
		}

		//System.out.println("Mdiff:\t" + maxDiff + " (" + maxDiffIndex + 1 + ")");
		System.out.println();
		System.out.println("max:\t" + max);
		System.out.println("min:\t" + min);
		System.out.println("avg:\t" + ((double)total / rolls));
		//System.out.println("diff:\t" + (1D - ((double)min/(double)max))*100D + "%");
	}
}
