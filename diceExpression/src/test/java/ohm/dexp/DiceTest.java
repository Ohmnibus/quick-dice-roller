package ohm.dexp;

import org.junit.Test;
import static org.junit.Assert.*;

public class DiceTest {

    @Test
    public void testBalance() {
        int faces = 20;
        int rolls = 10000000;
        Dice dice = new Dice(1, 20);
        int[] results = new int[faces];

        for (int i = 0; i < rolls; i++) {
            results[dice.roll()-1]++;
        }

        int max = results[0];
        int min = max;
        int total = 0;
        int diff;
        int maxDiff = 0;
        int maxDiffIndex = 0;

        int expected = rolls / faces;
        double tolerance = Math.sqrt((double) rolls / 2);

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
            System.out.print(i + 1);
            System.out.print("\t:");
            System.out.print(results[i]);
            System.out.print(" ");
            System.out.print(diff);
            System.out.println();
            assertEquals("Unbalanced result for " + (i + 1), expected, results[i]);
        }

        System.out.println();
        System.out.println("max:\t" + max);
        System.out.println("min:\t" + min);
        System.out.println("avg:\t" + ((double) total / rolls));
    }
}
