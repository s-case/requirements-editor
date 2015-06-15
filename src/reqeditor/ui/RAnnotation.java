package reqeditor.ui;

import java.util.Comparator;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Class that represents a R-type annotation.
 * Example: 1:R3 ActsOn 1:T2 1:T3
 * 
 * @author themis
 */
public class RAnnotation extends Annotation {

	/**
	 * The Id of the first T-type annotation.
	 */
	public String Arg1;

	/**
	 * The Id of the second T-type annotation.
	 */
	public String Arg2;

	/**
	 * The position of the label of this annotation in the UI.
	 */
	public Point labelPosition;

	/**
	 * The rectangle within which the label of this annotation is placed.
	 */
	public Rectangle labelRectangleLimits;

	/**
	 * Initializes this object.
	 * 
	 * @param reqnum the requirement that this annotation belongs to.
	 * @param Id the id of this annotation.
	 * @param type the type of this annotation (e.g. ActsOn).
	 * @param Arg1 the Id of the first T-type annotation.
	 * @param Arg2 the Id of the second T-type annotation.
	 */
	public RAnnotation(int reqnum, String Id, String type, String Arg1, String Arg2) {
		super(reqnum, Id, type);
		this.Arg1 = Arg1;
		this.Arg2 = Arg2;
	}

	/**
	 * Sets the id of the requirement that this annotation belongs to. This function also updates the id of the
	 * annotation and the ids of the related T-type annotations.
	 * 
	 * @param reqnum the id of the requirement that this annotation belongs to.
	 */
	public void setRequirementId(int reqnum) {
		super.setRequirementId(reqnum);
		Arg1 = reqnum + ":" + Arg1.split(":")[1];
		Arg2 = reqnum + ":" + Arg2.split(":")[1];
	}

	/**
	 * Sets the position of the label of this annotation in the UI.
	 * 
	 * @param xpos the x coordinate of the position.
	 * @param ypos the y coordinate of the position.
	 */
	public void setLabelPosition(int xpos, int ypos) {
		labelPosition = new Point(xpos, ypos);
	}

	/**
	 * Sets the rectangle within which the label of this annotation is placed.
	 * 
	 * @param x the x coordinate of the origin of the rectangle
	 * @param y the y coordinate of the origin of the rectangle
	 * @param width the width of the rectangle
	 * @param height the height of the rectangle
	 */
	public void setLabelRectangleLimits(int x, int y, int width, int height) {
		labelRectangleLimits = new Rectangle(x, y, width, height);
	}

	/**
	 * A {@link Comparator} used to sort the annotations according to their label position.
	 */
	public static Comparator<RAnnotation> LabelPositionComparator = new Comparator<RAnnotation>() {
		public int compare(RAnnotation annotation1, RAnnotation annotation2) {
			Integer xpos1 = annotation1.labelPosition.x;
			Integer xpos2 = annotation2.labelPosition.x;
			return xpos1.compareTo(xpos2);
		}
	};

	/**
	 * Checks if this annotation is connected to the given T-type annotation id.
	 * 
	 * @param annotationId the T-type annotation id to check if it is connected to this annotation.
	 * @return {@code true} is this annotation is connected to the T-type annotation with {@code annotationId}, and
	 *         {@code false} otherwise.
	 */
	public boolean isConnectedToAnnotationId(String annotationId) {
		return Arg1.equals(annotationId) || Arg2.equals(annotationId);
	}

	@Override
	public String toString() {
		return Id + "\t" + type + " " + Arg1 + " " + Arg2;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((Arg1 == null) ? 0 : Arg1.hashCode());
		result = prime * result + ((Arg2 == null) ? 0 : Arg2.hashCode());
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
		RAnnotation other = (RAnnotation) obj;
		if (Arg1 == null) {
			if (other.Arg1 != null)
				return false;
		} else if (!Arg1.equals(other.Arg1))
			return false;
		if (Arg2 == null) {
			if (other.Arg2 != null)
				return false;
		} else if (!Arg2.equals(other.Arg2))
			return false;
		return true;
	};
}
