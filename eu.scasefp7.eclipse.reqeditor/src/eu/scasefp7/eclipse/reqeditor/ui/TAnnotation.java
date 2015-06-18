package eu.scasefp7.eclipse.reqeditor.ui;

import java.util.Comparator;

import org.eclipse.swt.graphics.Rectangle;

/**
 * Class that represents a T-type annotation.
 * Example: 1:T1 Actor 2 6 user
 * 
 * @author themis
 */
public class TAnnotation extends Annotation {

	/**
	 * The word (or phrase) that this annotation refers to.
	 */
	public String word;

	/**
	 * The word limits of this annotation, as written in the ann file.
	 */
	public Limits annLimits;

	/**
	 * The rectangle within which the label of this annotation is placed.
	 */
	public Rectangle rectangleLimits;

	/**
	 * Initializes this object.
	 * 
	 * @param reqnum the requirement that this annotation belongs to.
	 * @param Id the id of this annotation.
	 * @param type the type of this annotation (e.g. Actor).
	 * @param word the word (or phrase) that this annotation refers to.
	 * @param annLimits the word limits of this annotation, as written in the ann file.
	 */
	public TAnnotation(int reqnum, String Id, String type, String word, Limits annLimits) {
		super(reqnum, Id, type);
		this.word = word;
		this.annLimits = annLimits;
	}

	/**
	 * Sets the rectangle within which the label of this annotation is placed.
	 * 
	 * @param x the x coordinate of the origin of the rectangle
	 * @param y the y coordinate of the origin of the rectangle
	 * @param width the width of the rectangle
	 * @param height the height of the rectangle
	 */
	public void setRectangleLimits(int x, int y, int width, int height) {
		rectangleLimits = new Rectangle(x, y, width, height);
	}

	/**
	 * A {@link Comparator} used to sort the annotations according to their label rectangle.
	 */
	public static Comparator<TAnnotation> LabelRectangleComparator = new Comparator<TAnnotation>() {
		public int compare(TAnnotation annotation1, TAnnotation annotation2) {
			Integer xpos1 = annotation1.rectangleLimits.x;
			Integer xpos2 = annotation2.rectangleLimits.x;
			return xpos1.compareTo(xpos2);
		}
	};

	@Override
	public String toString() {
		return Id + "\t" + type + " " + annLimits.left + " " + annLimits.right + "\t" + word;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((annLimits == null) ? 0 : annLimits.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TAnnotation other = (TAnnotation) obj;
		if (annLimits == null) {
			if (other.annLimits != null)
				return false;
		} else if (!annLimits.equals(other.annLimits))
			return false;
		return true;
	}
}
