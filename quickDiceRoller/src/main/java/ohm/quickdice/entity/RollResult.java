package ohm.quickdice.entity;

import ohm.dexp.TokenBase;
import ohm.quickdice.R;

/**
 * This class define and handle all the data relate to the result of a dice roll.
 * @author Ohmnibus
 *
 */
public class RollResult {
	
	public static final int DEFAULT_RESULT_ICON = R.drawable.ic_dxx_gray;
	public static final int VALUES_PRECISION_FACTOR = TokenBase.VALUES_PRECISION_FACTOR;
	
	protected String title;
	protected String description;
	protected String resultText;
	protected long rawResultValue;
	protected long maxRawResultValue;
	protected long minRawResultValue;
	protected int resourceIndex;
	
	/**
	 * Copy constructor.
	 * @param rollResult
	 */
	public RollResult(RollResult rollResult) {
		this(
				rollResult.title,
				rollResult.description,
				rollResult.resultText,
				rollResult.rawResultValue,
				rollResult.maxRawResultValue,
				rollResult.minRawResultValue,
				rollResult.resourceIndex
				);
	}
	
	/**
	 * Initialize a {@link RollResult} object from given parameters.
	 * @param title
	 * @param description
	 * @param resultText
	 * @param rawResultValue
	 * @param maxRawResultValue
	 * @param minRawResultValue
	 * @param resourceIndex
	 */
	public RollResult(String title, String description, String resultText,
			long rawResultValue, long maxRawResultValue, long minRawResultValue,
			int resourceIndex) {
		super();
		this.title = title;
		this.description = description;
		this.resultText = resultText;
		this.rawResultValue = rawResultValue;
		this.maxRawResultValue = maxRawResultValue;
		this.minRawResultValue = minRawResultValue;
		this.resourceIndex = resourceIndex;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return title;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @return the resultText
	 */
	public String getResultText() {
		return resultText;
	}
	/**
	 * @return the rawResultValue
	 */
	public long getRawResultValue() {
		return rawResultValue;
	}
	/**
	 * @return the resultValue in string format
	 */
	public String getResultString() {
		if (rawResultValue < 0 && rawResultValue > -VALUES_PRECISION_FACTOR) {
			//Special "-0" case
			return "-0";
		}
		return Long.toString(getResultValue());
	}
	/**
	 * @return the resultValue
	 */
	public long getResultValue() {
		return rawResultValue / VALUES_PRECISION_FACTOR;
	}
	/**
	 * @return the maxRawResultValue
	 */
	public long getMaxRawResultValue() {
		return maxRawResultValue;
	}
	/**
	 * @return the maxResultValue
	 */
	public long getMaxResultValue() {
		return maxRawResultValue / VALUES_PRECISION_FACTOR;
	}
	/**
	 * @return the minRawResultValue
	 */
	public long getMinRawResultValue() {
		return minRawResultValue;
	}
	/**
	 * @return the minResultValue
	 */
	public long getMinResultValue() {
		return minRawResultValue / VALUES_PRECISION_FACTOR;
	}
	/**
	 * @return the resourceIndex
	 */
	public int getResourceIndex() {
		return resourceIndex;
	}
	
	public int getResultIconID() {
		int retVal;
		long range;
		long baseResult;

		range = getMaxResultValue() - getMinResultValue();
		baseResult = getResultValue() - getMinResultValue();
		
		if (range < 0) {
			//Negative range. Something went wrong.
			retVal = R.drawable.ic_dxx_gray;
		} else if (range == 0) {
			//No range: this is not a roll, this is a computation.
			retVal = R.drawable.ic_res_100;
		} else {
			long scale = (baseResult * 100) / range;
			if (scale <= 0) {
				//Fumble
				retVal = R.drawable.ic_res_fumble;
			} else if (scale <= 13) {
				retVal = R.drawable.ic_res_000;
			} else if (scale <= 38) {
				retVal = R.drawable.ic_res_025;
			} else if (scale <= 62) {
				retVal = R.drawable.ic_res_050;
			} else if (scale <= 87) {
				retVal = R.drawable.ic_res_075;
			} else if (scale < 100) {
				retVal = R.drawable.ic_res_100;
			} else {
				//Critical
				retVal = R.drawable.ic_res_critical;
			}
		}

		return retVal;
	}
	
	/**
	 * Tell if this roll is a critical, that is it's value
	 * is equal or greater the maximum allowed.<br />
	 * Only dice whit a range of 4 or more value can generate criticals.
	 * @return True if the roll is a critical.
	 */
	public boolean isCritical() {
		//return (resultValue >= maxResultValue) && (maxResultValue - minResultValue >= 3 * VALUES_PRECISION_FACTOR);
		return (getResultValue() >= getMaxResultValue()) && (getMaxResultValue() - getMinResultValue() >= 3);
	}
	
	/**
	 * Tell if this roll is a fumble, that is it's value
	 * is equal or less the minimum allowed.<br />
	 * Only dice whit a range of 4 or more value can generate fumbles.
	 * @return True if the roll is a fumble.
	 */
	public boolean isFumble() {
		//return (resultValue <= minResultValue) && (maxResultValue - minResultValue >= 3 * VALUES_PRECISION_FACTOR);
		return (getResultValue() <= getMinResultValue()) && (getMaxResultValue() - getMinResultValue() >= 3);
	}
	
	public static RollResult mergeResultList(RollResult[] resList) {
		RollResult retVal;
		String tmp;
		
		if (resList != null && resList.length > 0) {
			RollResult res;
			res = resList[0];
//			retVal = new RollResult(
//					res.getName(), 
//					res.getDescription(), 
//					res.getResultText(), 
//					res.getResultValue(), 
//					res.getMaxResultValue(), 
//					res.getMinResultValue(), 
//					res.getResourceIndex());
			retVal = new RollResult(res);
			
			for (int i = 1; i < resList.length; i++) {
				res = resList[i];
				//Default icon never changes.
				if (retVal.resourceIndex != IconCollection.ID_ICON_DEFAULT 
						&& retVal.resourceIndex != res.getResourceIndex()) {
					if (retVal.resourceIndex == IconCollection.ID_ICON_BONUS 
							|| retVal.resourceIndex == IconCollection.ID_ICON_MALUS) {
						//Bonus/Malus icon is not considered
						retVal.resourceIndex = res.resourceIndex;
					} else if (res.resourceIndex != IconCollection.ID_ICON_BONUS 
							&& res.resourceIndex != IconCollection.ID_ICON_MALUS) {
						retVal.resourceIndex = IconCollection.ID_ICON_DEFAULT;
					}
				}
				retVal.title += " + " + res.getName(); 
				retVal.description += "; " + res.getDescription();
				tmp = res.getResultText();
				if (tmp.length() > 0 && tmp.startsWith("+") || tmp.startsWith("-")) {
					//The exp already start with a sign - don't add another
					retVal.resultText += " " + tmp;
				} else {
					retVal.resultText += " + " + tmp;
				}
//				retVal.resultValue += res.getResultValue();
//				retVal.maxResultValue += res.getMaxResultValue();
//				retVal.minResultValue += res.getMinResultValue();
				retVal.rawResultValue += res.rawResultValue;
				retVal.maxRawResultValue += res.maxRawResultValue;
				retVal.minRawResultValue += res.minRawResultValue;
			}
		} else { 
			retVal = null;
		}
		
		return retVal;
	}
}
