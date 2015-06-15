package reqeditor.ui;

/**
 * Class denoting an annotation.
 * 
 * @author themis
 */
public abstract class Annotation {

	/**
	 * The requirement that this annotation belongs to.
	 */
	public int reqnum;

	/**
	 * The id of this annotation.
	 */
	public String Id;

	/**
	 * The type of this annotation.
	 */
	public String type;

	/**
	 * Initializes this object.
	 * 
	 * @param reqnum the id of the requirement that this annotation belongs to.
	 * @param Id the id of this annotation.
	 * @param type the type of this annotation.
	 */
	public Annotation(int reqnum, String Id, String type) {
		this.reqnum = reqnum;
		this.Id = Id;
		this.type = type;
	}

	/**
	 * Sets the id of the requirement that this annotation belongs to. This function also updates the id of the
	 * annotation.
	 * 
	 * @param reqnum the id of the requirement that this annotation belongs to.
	 */
	public void setRequirementId(int reqnum) {
		this.reqnum = reqnum;
		Id = reqnum + ":" + Id.split(":")[1];
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Annotation other = (Annotation) obj;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
}
