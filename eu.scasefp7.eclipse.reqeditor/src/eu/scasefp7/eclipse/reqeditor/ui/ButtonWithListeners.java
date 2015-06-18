package eu.scasefp7.eclipse.reqeditor.ui;

import java.util.HashMap;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;

/**
 * A {@link Button} that allows adding and removing listeners. This class is
 * created because the original {@link Button} cannot remove listeners.
 * 
 * @author themis
 */
public class ButtonWithListeners extends Button {

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
	public ButtonWithListeners(Composite parent, int style) {
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

	/**
	 * Empty method to allow subclassing the {@link Button} class. Note that subclassing {@link Button} is generally not
	 * recommended. However, this is all right since the layer of subclassing is on {@link Control}, and refers to
	 * adding/removing listeners.
	 */
	@Override
	protected void checkSubclass() {

	}
}
