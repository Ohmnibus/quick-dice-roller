package ohm.dexp;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Ohmnibus on 03/06/2016.
 */
public class DiceTest {

	@Test
	public void FirstTest() {
		Dice dice = new Dice(1, 20);
		int[] results = new int[20];
		for (int i=0; i<10000000; i++) {
			results[dice.roll()-1]++;
		}
		int max=results[0];
		int min=max;
		for (int i = 0; i < 20; i++) {
			if (max < results[i]) {
				max = results[i];
			}
			if (min > results[i]) {
				min = results[i];
			}
			System.out.print(i+1);
			System.out.print("\t:");
			System.out.println(results[i]);
		}
		System.out.println();
		System.out.println("max:\t" + max);
		System.out.println("min:\t" + min);
		System.out.println("diff:\t" + (1D - ((double)min/(double)max))*100D + "%");
	}
}
