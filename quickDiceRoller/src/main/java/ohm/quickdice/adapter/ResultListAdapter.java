package ohm.quickdice.adapter;

import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import ohm.library.adapter.CachedArrayAdapter;
import ohm.quickdice.QuickDiceApp;
import ohm.quickdice.R;
import ohm.quickdice.entity.RollResult;

public class ResultListAdapter extends CachedArrayAdapter<RollResult[]> {

	static int[] resultFontSizes;
	static boolean swapResult;

	/**
	 * This class represents the views needed to display a Roll Result.
	 * @author Ohmnibus
	 *
	 */
	public static class ItemViews {
		
		public ImageView diceIcon;
		public TextView resultValue;
		public TextView name;
		public TextView resultText;
		public ImageView resultIcon;
		
		public ItemViews() {
		}
		
		public ItemViews(ImageView diceIcon, TextView resultValue, TextView name, TextView resultText, ImageView resultIcon) {
			this.diceIcon = diceIcon;
			this.resultValue = resultValue;
			this.name = name;
			this.resultText = resultText;
			this.resultIcon = resultIcon;
		}
	}

	/**
	 * This class contain all the variable view of a single item in a result list item.
	 * @author Ohmnibus
	 *
	 */
	private class ExpViewCache extends ViewCache  {

		ItemViews itemViews;
		
		public ExpViewCache(View baseView) {
			super(baseView);
		}

		@Override
		protected void findAllViews(View baseView) {
			//baseView.setBackgroundResource(android.R.drawable.list_selector_background);
			itemViews = new ItemViews();
			itemViews.name = (TextView) baseView.findViewById(R.id.riName);
			itemViews.resultText = (TextView) baseView.findViewById(R.id.riResultText);
			itemViews.resultValue = (TextView) baseView.findViewById(R.id.riResult);
			itemViews.diceIcon = (ImageView) baseView.findViewById(R.id.riImage);
			itemViews.resultIcon = (ImageView) baseView.findViewById(R.id.riResultIcon);
		}
	}

	public ResultListAdapter(Context context, int resourceId, List<RollResult[]> objects) {
		super(context, resourceId, objects);
	}

	@Override
	protected ViewCache createCache(int position, View convertView) {
		ExpViewCache cache = new ExpViewCache(convertView);
		return cache;
	}

	@Override
	protected void bindData(ViewCache viewCache) {
		ExpViewCache cache = (ExpViewCache)viewCache;
		RollResult[] resList = null;
		//HACK: Curiously the cast "(RollResult[])cache.data" sometimes throw an exception
		//java.lang.ClassCastException: java.lang.Object[] cannot be cast to ohm.quickdice.entity.RollResult[]
		if (cache.data != null && cache.data instanceof RollResult[]) {
			resList = (RollResult[])cache.data;
		}
		bindData(
				getContext(),
				resList,
				cache.itemViews);
	}

	@Override
	protected void bindDropDownData(ViewCache viewCache) {
		bindData(viewCache);
	}
	
	/**
	 * Tell if the roll result (extended) and description has to be switched.
	 * @param doSwap New setting.
	 */
	public static void setSwapNameResult(boolean doSwap) {
		swapResult = doSwap;		
	}
	
	public static void bindData(
			Context context,
			RollResult[] resList,
			ItemViews itemViews) {
		
		RollResult res = RollResult.mergeResultList(resList);

		if (res != null) {
			//QuickDiceApp app = (QuickDiceApp)context.getApplicationContext();
			QuickDiceApp app = QuickDiceApp.getInstance();
			//itemViews.diceIcon.setImageDrawable(app.getBagManager().getIconDrawable(res.getResourceIndex()));
			app.getBagManager().setIconDrawable(itemViews.diceIcon, res.getResourceIndex());
			if (swapResult) {
				itemViews.name.setText(res.getResultText());
				itemViews.resultText.setText(res.getName());
			} else {
				itemViews.name.setText(res.getName());
				itemViews.resultText.setText(res.getResultText());
			}
			//String result = Long.toString(res.getResultValue());
			String result = res.getResultString();
			itemViews.resultValue.setText(result);
			itemViews.resultValue.setTextSize(TypedValue.COMPLEX_UNIT_DIP, getFontSize(context, result));
			itemViews.resultIcon.setImageResource(res.getResultIconID());
		} else { 
			itemViews.diceIcon.setImageDrawable(null);
			itemViews.name.setText("");
			itemViews.resultText.setText("");
			itemViews.resultValue.setText("");
			itemViews.resultIcon.setImageDrawable(null);
		}
	}
	
	public static float getFontSize(Context context, long result) {
		return getFontSize(context, Long.toString(result));
	}
	
	public static float getFontSize(Context context, String result) {
		float retVal;
		
		if (resultFontSizes == null) {
			TypedArray resultFontSizeArray;
			resultFontSizeArray = context.getResources().obtainTypedArray(R.array.roll_result_font_size);
			resultFontSizes = new int[resultFontSizeArray.length()];
			for (int i = 0; i < resultFontSizes.length; i++) {
				resultFontSizes[i] = resultFontSizeArray.getInt(i, 12);
			}
			resultFontSizeArray.recycle();
		}
		
		if (result.length() < resultFontSizes.length) {
			//retVal = resultFontSizes.getInt(result.length(), 12);
			retVal = resultFontSizes[result.length()];
		} else {
			//retVal = resultFontSizes.getInt(resultFontSizes.length() - 1, 12);
			retVal = resultFontSizes[resultFontSizes.length - 1];
		}

		return retVal;
	}

}
