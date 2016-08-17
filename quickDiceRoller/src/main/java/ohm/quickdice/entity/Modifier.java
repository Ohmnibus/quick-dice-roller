package ohm.quickdice.entity;

/**
 * Created by Ohmnibus on 08/07/2016.
 */
public interface Modifier {

	String getName();

	String getDescription();

	int getValue();

	String getValueString();

	int getResourceIndex();

	DiceBag getParent();

	void setParent(DiceBag parent);

	boolean isChanged();
}
