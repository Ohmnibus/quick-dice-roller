package ohm.quickdice.entity;

import java.util.Date;

public class MostRecentFile implements Comparable<MostRecentFile>{

	String name;
	int bagsNum;
	int diceNum;
	int modsNum;
	int varsNum;
	String uri;
	Date lastUsed;
	
	public MostRecentFile(String name, String uri, int bagsNum, int diceNum, int modsNum, int varsNum, Date lastUsed) {
		this.name = name;
		this.bagsNum = bagsNum;
		this.diceNum = diceNum;
		this.modsNum = modsNum;
		this.varsNum = varsNum;
		this.uri = uri;
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
	 * @return the varsNum
	 */
	public int getVarsNum() {
		return varsNum;
	}

	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
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
