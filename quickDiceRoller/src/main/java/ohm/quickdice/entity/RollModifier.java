package ohm.quickdice.entity;

import ohm.quickdice.R;

import android.content.Context;

public class RollModifier implements Modifier {

	public static final int TYPE_ID = 0;

	protected String title;
	protected String description;
	protected int value;
	protected int iconId;
	protected DiceBag parent;
	
	public RollModifier(Context context, int value) {
		if (value < 0) {
			//Penalty
//			this.title = String.format(context.getString(R.string.msgMalusTitle), value);
//			this.description = String.format(context.getString(R.string.msgMalusMessage), value);
			this.title = context.getString(R.string.msgMalusTitle, value);
			this.description = context.getString(R.string.msgMalusMessage, value);
			this.value = value;
			//this.resourceIndex = GraphicManager.INDEX_DICE_ICON_MALUS;
			this.iconId = IconCollection.ID_ICON_MALUS;
		} else {
			//Bonus
			this.title = context.getString(R.string.msgBonusTitle, value);
			this.description = context.getString(R.string.msgBonusMessage, value);
			this.value = value;
			//this.resourceIndex = GraphicManager.INDEX_DICE_ICON_BONUS;
			this.iconId = IconCollection.ID_ICON_BONUS;
		}
	}
	
	public RollModifier(String name, String description, int value, int iconId) {
		this.title = name;
		this.description = description;
		this.value = value;
		this.iconId = iconId;
	}

	@Override
	public int getTypeID() {
		return TYPE_ID;
	}

	@Override
	public String getName() {
		return title;
	}
	
	@Override
	public String getDescription() {
		return description;
	}
	
	@Override
	public int getValue() {
		return value;
	}

	@Override
	public String getValueString() {
		if (value < 0)
			return Integer.toString(value);
		else
			return "+" + Integer.toString(value);
	}
	
	@Override
	public int getResourceIndex() {
		//TODO: Rename to getIconID
		return iconId;
	}
	
	@Override
	public DiceBag getParent() {
		return parent;
	}

	@Override
	public void setParent(DiceBag parent) {
		this.parent = parent;
	}

	@Override
	public boolean isChanged() {
		return parent == null ? false : parent.isChanged();
	}

}
