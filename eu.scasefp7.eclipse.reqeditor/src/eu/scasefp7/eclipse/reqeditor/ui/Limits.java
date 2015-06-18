package eu.scasefp7.eclipse.reqeditor.ui;

/**
 * Class that keeps the limits of a string in the text.
 * 
 * @author themis
 */
public class Limits {

	/**
	 * The left limit of the string.
	 */
	public int left;

	/**
	 * The right limit of the string.
	 */
	public int right;

	/**
	 * Initializes the limits of the string.
	 * 
	 * @param left the left limit of the string.
	 * @param right the right limit of the string.
	 */
	public Limits(int left, int right) {
		this.left = left;
		this.right = right;
	}

	/**
	 * Initializes the limits of the string given string input.
	 * 
	 * @param left the left limit of the string.
	 * @param right the right limit of the string.
	 */
	public Limits(String left, String right) {
		this.left = Integer.parseInt(left);
		this.right = Integer.parseInt(right);
	}

	/**
	 * Checks if the limits of another string are in the limits of this object. This function can be used to check if
	 * two strings overlap.
	 * 
	 * @param obj the other string.
	 * @return {@code true} if the two strings overlap, and {@code false} otherwise.
	 */
	public boolean isInLimits(Limits obj) {
		return !(obj.left >= right || obj.right <= left);
	}

	@Override
	public String toString() {
		return "Limits {" + left + ", " + right + "}";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		return prime * (prime + left) + right;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Limits other = (Limits) obj;
		if (left != other.left || right != other.right)
			return false;
		return true;
	}
}
