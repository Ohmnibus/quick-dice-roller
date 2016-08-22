package ohm.quickdice.entity;

/**
 * Created by Ohmnibus on 08/07/2016.
 */
public interface Modifier {

	/**
	 * Return a value unique for each implementation, for serializing purpose.
	 * @return Unique for each implementation
	 */
	int getTypeID();

	String getName();

	String getDescription();

	int getValue();

	String getValueString();

	int getResourceIndex();

	DiceBag getParent();

	void setParent(DiceBag parent);

	boolean isChanged();
}
