package ohm.quickdice.entity;

import java.util.Date;

public class MostRecentFile implements Comparable<MostRecentFile>{

	String name;
	int bagsNum;
	int diceNum;
	int modsNum;
	String path;
	Date lastUsed;
	
	public MostRecentFile(String name, String path, int bagsNum, int diceNum, int modsNum, Date lastUsed) {
		this.name = name;
		this.bagsNum = bagsNum;
		this.diceNum = diceNum;
		this.modsNum = modsNum;
		this.path = path;
		this.lastUsed = lastUsed;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the bagsNum
	 */
	public int getBagsNum() {
		return bagsNum;
	}

	/**
	 * @return the diceNum
	 */
	public int getDiceNum() {
		return diceNum;
	}

	/**
	 * @return the modsNum
	 */
	public int getModsNum() {
		return modsNum;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @return the lastUsed
	 */
	public Date getLastUsed() {
		return lastUsed;
	}

	@Override
	public int compareTo(MostRecentFile another) {
		int retVal;

		if (another == null) throw new IllegalArgumentException();
		
		retVal = lastUsed.compareTo(another.getLastUsed());
		if (retVal == 0) {
			retVal = name.compareToIgnoreCase(another.getName());
		}

		return -retVal;
	}
	
	
}
