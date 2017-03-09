package ohm.dexp.function;

import java.util.ArrayList;

import ohm.dexp.DContext;
import ohm.dexp.TokenBase;
import ohm.dexp.exception.DException;

/**
 * Created by Ohmnibus on 09/03/2017.
 */

public class TokenFunctionEffe extends TokenFunction {

	private static final int INDEX_ROLL = 1;
	private static final int INDEX_POOL = 2;
	private static final int INDEX_TARGET = 3;
	private static final int INDEX_EXTRA = 4;

	@Override
	protected int initChildNumber() {
		return 3;
	}

	@Override
	protected int initOptionalChildNumber() {
		return 1;
	}

	@Override
	public int getType() {
		return 9999;
	}

	@Override
	protected void evaluateSelf(DContext instance) throws DException {
		//effe(1d10, 3, 4, 5)
		//effe(dice, rolls, target [, extraRolls])
		//Lanci: rolls + (int)(rolls/2) + extraRolls
		//Somma tutti i doppi tra loro, considerali un solo risultato.
		//Restituisci il valore più alto, se > di target.
		TokenBase tokenRoll = getChild(INDEX_ROLL);
		TokenBase tokenPool = getChild(INDEX_POOL);
		TokenBase tokenTarget = getChild(INDEX_TARGET);

		tokenPool.evaluate(instance);
		int poolBase = (int)tokenPool.getResult();
		int poolHalf = (int)(poolBase / 2);
		int poolExtra;
		if (getChild(INDEX_EXTRA) != null) {
			getChild(INDEX_EXTRA).evaluate(instance);
			poolExtra = (int)getChild(INDEX_EXTRA).getResult();
		} else {
			poolExtra = 0;
		}
		ArrayList<MyResult> results = new ArrayList<MyResult>();
		int[] counts = new int[] {poolBase, poolHalf, poolExtra};
		int[] types = new int[] {MyResult.TYPE_BASE, MyResult.TYPE_HALF, MyResult.TYPE_EXTRA};
		for (int j = 0; j < counts.length; j++) {
			for (int i = 0; i < counts[j]; i++) {
				tokenRoll.evaluate(instance);
				MyResult r = new MyResult();
				r.value = (int) tokenRoll.getResult();
				r.type = types[j];
				results.add(r);
			}
		}
		int grand = 0;
		int max = 0;
		int maxIndex = -1;
		for (int i = 0; i < (results.size() - 1); i++) {
			MyResult first = results.get(i);
			if (first.dup) {
				continue;
			}
			if (first.value > max) {
				max = first.value;
				maxIndex = i;
			}
			for (int j = i+1; j < results.size(); j++) {
				MyResult second = results.get(j);
				if (second.dup) {
					continue;
				}
				if (second.value > max) {
					max = second.value;
					maxIndex = j;
				}
				if (first.value == second.value) {
					first.dup = true;
					second.dup = true;
					grand += second.value;
				}
			}
			if (first.dup) {
				grand += first.value;
			}
		}
		if (grand > 0) {
			MyResult grandResult = new MyResult();
			grandResult.value = grand;
			grandResult.type = MyResult.TYPE_SUMMED;
			results.add(grandResult);
			if (grand > max) {
				max = grand;
				maxIndex = results.size() - 1;
			}
		}

		tokenTarget.evaluate(instance);
		int target = (int)tokenTarget.getResult();

		resultValue = (max >= target ? max : 0) * VALUES_PRECISION_FACTOR;
		resultString = SYM_BEGIN; //"[";
		for (int i = 0; i<results.size(); i++) {
			MyResult result = results.get(i);
			if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
				if (i > 0) {
					resultString += SYM_SEP; //",";
				}
				String prefix = "";
				String suffix = "";
				if (result.dup) {
					prefix = "+";
				}
				switch (result.type) {
					case MyResult.TYPE_BASE:
						suffix = "";
						break;
					case MyResult.TYPE_HALF:
						suffix = "*";
						break;
					case MyResult.TYPE_EXTRA:
						suffix = "x";
						break;
					case MyResult.TYPE_SUMMED:
						prefix = "{";
						suffix = "}";
						break;
				}
				if (maxIndex == i && max >= target) {
					suffix = SYM_SELECTED + suffix;
				}
				resultString += prefix + Integer.toString(result.value) + suffix;
			}
		}
		resultMaxValue = tokenRoll.getMaxResult();
		resultMinValue = tokenRoll.getMinResult();
		if (resultString.length() < MAX_TOKEN_STRING_LENGTH) {
			resultString += SYM_END; //"]";
		} else {
			//resultString = "[...=" + Long.toString(resultValue / VALUES_PRECISION_FACTOR) + "]";
			resultString = SYM_TRUNK_BEGIN + Long.toString(resultValue / VALUES_PRECISION_FACTOR) + SYM_TRUNK_END;
		}
	}

	private class MyResult {
		public static final int TYPE_BASE = 0;
		public static final int TYPE_HALF = 1;
		public static final int TYPE_EXTRA = 2;
		public static final int TYPE_SUMMED = 3;
		int value;
		int type;
		boolean dup;
	}
}
