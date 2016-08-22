package ohm.quickdice.entity;

import android.content.Context;

import ohm.quickdice.R;

/**
 * Created by Ohmnibus on 20/08/2016.
 */
public class PercentModifier implements Modifier {

	public static final int TYPE_ID = 2;

	protected String title;
	protected String description;
	protected int value;
	protected int iconId;
	protected DiceBag parent;

	public PercentModifier(Context context, int value) {
		if (value < 0) {
			//Subtract percentage
			initValues(
				context.getString(R.string.msgSubPercentTitle, value, Math.abs(value)),
				context.getString(R.string.msgSubPercentMessage, value, Math.abs(value)),
				value,
				IconCollection.ID_ICON_SUB_PERCENT
			);
		} else {
			//Add percentage
			initValues(
				context.getString(R.string.msgAddPercentTitle, value, Math.abs(value)),
				context.getString(R.string.msgAddPercentMessage, value, Math.abs(value)),
				value,
				IconCollection.ID_ICON_ADD_PERCENT
			);
		}
	}

	public PercentModifier(String name, String description, int value, int iconId) {
		initValues(name, description, value, iconId);
	}

	private void initValues(String name, String description, int value, int iconId) {
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
			return Integer.toString(value) + "%";
		else
			return "+" + Integer.toString(value) + "%";
	}

	@Override
	public int getResourceIndex() {
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
