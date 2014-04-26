package ohm.quickdice.entity;

import java.io.Serializable;

import ohm.dexp.DResult;
import ohm.quickdice.R;
import ohm.quickdice.control.GraphicManager;

/**
 * This class define and handle all the data relate to the result of a dice roll.
 * @author Ohmnibus
 *
 */
public class RollResult implements Serializable {
	
	/**
	 * Serial version UID used for serialization.
	 */
	private static final long serialVersionUID = -8278370559503945512L;

	public static final int DEFAULT_RESULT_ICON = R.drawable.ic_dxx_gray;
	
	protected String title;
	protected String description;
	protected String resultText;
	protected long resultValue;
	protected long maxResultValue;
	protected long minResultValue;
	protected int resourceIndex;
	
	/**
	 * Initialize a {@link RollResult} object from a {@link DResult}.
	 * @param dResult
	 */
	public RollResult(DResult dResult) {
		this(
				new String(dResult.getExpression().getName()),
				new String(dResult.getExpression().getDescription()),
				new String(dResult.getResultText()),
				dResult.getResult(),
				dResult.getMaxResult(),
				dResult.getMinResult(),
				dResult.getExpression().getResourceIndex());
	}

	/**
	 * Initialize a {@link RollResult} object from given parameters.
	 * @param title
	 * @param description
	 * @param resultText
	 * @param resultValue
	 * @param maxResultValue
	 * @param minResultValue
	 * @param resourceIndex
	 */
	public RollResult(String title, String description, String resultText,
			long resultValue, long maxResultValue, long minResultValue,
			int resourceIndex) {
		super();
		this.title = title;
		this.description = description;
		this.resultText = resultText;
		this.resultValue = resultValue;
		this.maxResultValue = maxResultValue;
		this.minResultValue = minResultValue;
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
	 * @return the resultValue
	 */
	public long getResultValue() {
		return resultValue;
	}
	/**
	 * @return the maxResultValue
	 */
	public long getMaxResultValue() {
		return maxResultValue;
	}
	/**
	 * @return the minResultValue
	 */
	public long getMinResultValue() {
		return minResultValue;
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

		range = maxResultValue - minResultValue;
		baseResult = resultValue - minResultValue;
		
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
		return (resultValue >= maxResultValue) && (maxResultValue - minResultValue >= 3);
	}
	
	/**
	 * Tell if this roll is a fumble, that is it's value
	 * is equal or less the minimum allowed.<br />
	 * Only dice whit a range of 4 or more value can generate fumbles.
	 * @return True if the roll is a fumble.
	 */
	public boolean isFumble() {
		return (resultValue <= minResultValue) && (maxResultValue - minResultValue >= 3);
	}
	
	public static RollResult mergeResultList(RollResult[] resList) {
		RollResult retVal;
		String tmp;
		
		if (resList != null && resList.length > 0) {
			RollResult res;
			res = resList[0];
			retVal = new RollResult(
					res.getName(), 
					res.getDescription(), 
					res.getResultText(), 
					res.getResultValue(), 
					res.getMaxResultValue(), 
					res.getMinResultValue(), 
					res.getResourceIndex());
			
			for (int i = 1; i < resList.length; i++) {
				res = resList[i];
				//Default icon never changes.
				if (retVal.resourceIndex != GraphicManager.INDEX_DICE_ICON_DEFAULT 
						&& retVal.resourceIndex != res.getResourceIndex()) {
					if (retVal.resourceIndex == GraphicManager.INDEX_DICE_ICON_BONUS || retVal.resourceIndex == GraphicManager.INDEX_DICE_ICON_MALUS) {
						//Bonus/Malus icon is not considered
						retVal.resourceIndex = res.resourceIndex;
					} else if (res.resourceIndex != GraphicManager.INDEX_DICE_ICON_BONUS && res.resourceIndex != GraphicManager.INDEX_DICE_ICON_MALUS) {
						retVal.resourceIndex = GraphicManager.INDEX_DICE_ICON_DEFAULT;
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
				retVal.resultValue += res.getResultValue();
				retVal.maxResultValue += res.getMaxResultValue();
				retVal.minResultValue += res.getMinResultValue();
			}
		} else { 
			retVal = null;
		}
		
		return retVal;
	}
}
