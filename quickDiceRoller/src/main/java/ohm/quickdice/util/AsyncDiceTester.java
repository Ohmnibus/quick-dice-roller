package ohm.quickdice.util;

import java.security.InvalidParameterException;

import ohm.quickdice.entity.Dice;
import ohm.quickdice.entity.DiceBag;
import android.os.AsyncTask;

public class AsyncDiceTester extends AsyncTask<Void, Void, Exception> {

	DiceBag diceBag;
	Dice dice;
	OnReadDiceListener listener;
	
	public interface OnReadDiceListener {
		void onRead(Dice dice);
		void onError(Exception ex);
	}
	
	@Override
	protected Exception doInBackground(Void... params) {
		Exception retVal = null;
		try {
			if (dice.getName().length() == 0) {
				throw new InvalidParameterException();
			}
			dice.setContext(diceBag);
			dice.getNewResult();
		} catch (Exception ex) {
			retVal = ex;
		}
		return retVal;
	}
	
	@Override
	protected void onPostExecute(Exception result) {
		if (result == null) {
			listener.onRead(dice);
		} else {
			listener.onError(result);
		}
	}
	
//	public void execute(DiceBag diceBag, Dice dice, OnReadDiceListener listener) {
//		this.diceBag = diceBag;
//		this.dice = dice;
//		this.listener = listener;
//		execute();
//	}
	
	public static void execute(DiceBag diceBag, Dice dice, OnReadDiceListener listener) {
		AsyncDiceTester dt = new AsyncDiceTester();
		dt.diceBag = diceBag;
		dt.dice = dice;
		dt.listener = listener;
		dt.execute();
	}

}
