package ohm.quickdice.entity;

public class FolderItem implements Comparable<FolderItem>{
	
	public static final int TYPE_FOLDER = 0;
	public static final int TYPE_FILE = 1;
	public static final int TYPE_PARENT_FOLDER = 2; //The item "/.."
	
	String name;
	String description;
	String path;
	int type;
	
	public FolderItem(String name, String description, String path, int type) {
		this.name = name;
		this.description = description;
		this.path = path;
		this.type = type;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	@Override
	public int compareTo(FolderItem another) {
		int retVal;

		if (another == null) throw new IllegalArgumentException();
		
		if (this.type == another.type) {
			retVal = this.name.compareToIgnoreCase(another.name);
		} else {
			retVal = -(getTypePriority(type) - getTypePriority(another.type));
		}

		return retVal;
	}
	
	private int getTypePriority(int type) {
		switch (type) {
		case TYPE_PARENT_FOLDER:
			return 3;
		case TYPE_FOLDER:
			return 2;
		case TYPE_FILE:
			return 1;
		default:
			throw new IllegalArgumentException();
		}
	}
	
}
