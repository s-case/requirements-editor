package eu.scasefp7.eclipse.reqeditor.ui.annotationseditor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A {@link HashMap} that also keeps track of current key values and returns a
 * new non-already-existing key value if required.
 * 
 * @author themis
 *
 * @param <V> the type of the {@link HashMap} values.
 */
@SuppressWarnings({ "serial" })
public class IdHashMap<V> extends HashMap<String, V> implements Map<String, V> {

	/**
	 * The initial char of the key.
	 */
	private String idchar;

	/**
	 * Initializes this object and sets the initial char of the key.
	 * 
	 * @param idchar the initial char of the key.
	 */
	public IdHashMap(String idchar) {
		super();
		this.idchar = idchar;
	}

	/**
	 * Returns a new key value that does not already exist in the keyset of the map. For example, given that
	 * {@code idchar} has value {@code R} and the requirement id is {@code 1}, and the keyset has keys
	 * {@code 1:R1, 1:R3, 1:R4, 2:R!, 2:R2}, this function would return the string {@code 1:R2}.
	 * 
	 * @param the id of the requirement of the new annotation.
	 * 
	 * @return a new key value that does not already exist in the keyset.
	 */
	public String getNewId(int reqnum) {
		String reqid = Integer.toString(reqnum) + ":" + idchar;
		Set<String> keyset = keySet();
		int cid = 1;
		String sid = reqid + Integer.toString(cid);
		while (keyset.contains(sid)) {
			sid = reqid + Integer.toString(++cid);
		}
		return sid;
	}

}
