package ohm.quickdice.entity;

import android.content.Context;

import ohm.quickdice.R;

/**
 * Created by Ohmnibus on 08/07/2016.
 */
public class VarModifier implements Modifier {

	public static final int TYPE_ID = 1;

	protected String label;
	protected Variable ref;
	protected DiceBag parent;

	//region Cache
	protected String title;
	protected String description;
	protected int value;
	protected int iconId;
	//endregion

	public VarModifier(String label) {
		this.label = label;
		this.ref = null;
		this.parent = null;

		initCache();
	}

	@Override
	public int getTypeID() {
		return TYPE_ID;
	}

	public String getLabel() {
		return label;
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
		return ref == null ? 0 : ref.getCurVal();
	}

	@Override
	public String getValueString() {
		return Integer.toString(getValue());
	}

	@Override
	public int getResourceIndex() {
		return this.iconId;
	}

	@Override
	public DiceBag getParent() {
		return parent;
	}

	@Override
	public void setParent(DiceBag parent) {
		this.parent = parent;
		initCache();
	}

	@Override
	public boolean isChanged() {
		return parent != null && parent.isChanged();
	}

	protected void initCache() {
		if (parent != null) {
			ref = parent.getVariables().getByLabel(label);
		}
		if (ref != null) {
			this.title = ref.getName();
			this.description = ref.getDescription();
			this.value = ref.getCurVal();
			this.iconId = ref.getResourceIndex();
		} else {
			this.title = label;
			this.description = label;
			this.value = 0;
			this.iconId = IconCollection.ID_ICON_DEFAULT;
		}
	}
}
