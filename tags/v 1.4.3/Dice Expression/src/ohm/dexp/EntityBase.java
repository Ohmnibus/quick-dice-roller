package ohm.dexp;

import java.io.Serializable;

/**
 * This is the base class for all the main entity in ohm.dexp namespace, and aim to 
 * contain all the distinctive value like name, description and so on. 
 * @author Ohmnibus
 *
 */
public class EntityBase implements Serializable {
	
	/**
	 * Serial version UID used for serialization. 
	 */
	private static final long serialVersionUID = 4887901085870527243L;
	
	int _id;
	String _name;
	String _description;
	int _resource;

	/**
	 * Set the identifier of the current instance.
	 * @param id Identifier of the current instance.
	 */
	public void setID(int id) {
		_id = id;
	}

	/**
	 * Get the identifier of the current instance.
	 * @return Identifier of the current instance.
	 */
	public int getID() {
		return _id;
	}

	/**
	 * Set the name for the current instance.
	 * @param name Name of the current instance.
	 */
	public void setName(String name) {
		_name = name;
	}

	/**
	 * Get the name of the current instance.
	 * @return Name of the current instance.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Set the description for the current instance.
	 * @param description Description for the current instance
	 */
	public void setDescription(String description) {
		_description = description;
	}

	/**
	 * Get the description of the current instance.
	 * @return Description of the current instance.
	 */
	public String getDescription() {
		return _description;
	}
	
	/**
	 * Set the graphic resource index for the current instance.
	 * @param resource Graphic resource index for the current instance.
	 */
	public void setResourceIndex(int resourceIndex) {
		_resource = resourceIndex;
	}

	/**
	 * Get the graphic resource index of the current instance.
	 * @return Graphic resource index of the current instance.
	 */
	public int getResourceIndex() {
		return _resource;
	}

}
