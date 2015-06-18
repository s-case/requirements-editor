package eu.scasefp7.eclipse.reqeditor.ui;

/**
 * Class representing a functional requirement.
 * 
 * @author themis
 */
public class Requirement {

	/**
	 * The id of this requirement.
	 */
	public int id;

	/**
	 * The text of this requirement.
	 */
	public String text;

	/**
	 * The length of this requirement.
	 */
	public int size;

	/**
	 * Initializes this requirement providing its id and text.
	 * 
	 * @param id the id of this requirement.
	 * @param text the text of this requirement.
	 */
	public Requirement(int id, String text) {
		this.id = id;
		this.text = text;
		this.size = text.length();
	}

	@Override
	public String toString() {
		return text;
	}

}
