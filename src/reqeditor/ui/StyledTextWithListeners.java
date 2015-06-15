package reqeditor.ui;

import java.util.HashMap;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;

/**
 * A {@link StyledText} that allows adding and removing listeners. This class is
 * created because the original {@link StyledText} cannot remove listeners.
 * 
 * @author themis
 */
public class StyledTextWithListeners extends StyledText {

	/**
	 * The listeners that are currently added to this widget.
	 */
	private HashMap<Integer, Listener> listeners;

	/**
	 * Initializes this object.
	 * 
	 * @param parent the parent widget of the new instance.
	 * @param style the style of this widget.
	 */
	public StyledTextWithListeners(Composite parent, int style) {
		super(parent, style);
		listeners = new HashMap<Integer, Listener>();
	}

	/**
	 * Adds a user defined {@link Listener}.
	 * 
	 * @param eventType the type of event to listen for.
	 * @param listener the listener which will be notified when the event occurs.
	 */
	public void addListenerByUser(int eventType, Listener listener) {
		addListener(eventType, listener);
		listeners.put(eventType, listener);
	}

	/**
	 * Removes all previously added listeners.
	 */
	public void removeAllListeners() {
		for (Integer type : listeners.keySet())
			removeListener(type.intValue(), listeners.get(type));
		listeners = new HashMap<Integer, Listener>();
	}

}
