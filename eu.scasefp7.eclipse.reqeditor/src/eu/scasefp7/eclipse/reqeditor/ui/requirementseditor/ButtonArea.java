package eu.scasefp7.eclipse.reqeditor.ui.requirementseditor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import eu.scasefp7.eclipse.reqeditor.Activator;
import eu.scasefp7.eclipse.reqeditor.ui.ButtonWithListeners;

/**
 * Class that holds the buttons of a requirement.
 * 
 * @author themis
 */
@SuppressWarnings("deprecation")
public class ButtonArea extends Composite {

	/**
	 * The buttons of the actions that are applicable on the requirement text.
	 */
	HashMap<String, ButtonWithListeners> buttons;

	/**
	 * The images of the buttons.
	 */
	protected static final HashMap<String, Image> images;
	static {
		// Load the images of the buttons from the plugin
		images = new HashMap<String, Image>();
		try {
			List<String> imageNames = Arrays.asList("Add", "Cancel", "Confirm", "Delete", "Move down", "Modify",
					"Move up");
			for (String imageName : imageNames) {
				URL url = null;
				if (Activator.getDefault() != null) {
					url = new URL(Activator.getDefault().getDescriptor().getInstallURL(), "icons/" + imageName + ".gif");
					ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(url);
					images.put(imageName, imageDescriptor.createImage());
				}
			}
		} catch (MalformedURLException e) {
		}
	}

	/**
	 * Initializes this widget, creating a grid layout and setting the margins to zero.
	 * 
	 * @param parent the parent widget of the new instance.
	 * @param style the style of this widget.
	 */
	public ButtonArea(Composite parent, int style) {
		super(parent, style);
		// Set an horizontal grid layout for the buttons
		setLayoutData(new GridData());
		GridLayout buttonAreaLayout = new GridLayout(4, false);
		buttonAreaLayout.marginHeight = 0;
		buttonAreaLayout.marginWidth = 0;
		setLayout(buttonAreaLayout);
		buttons = new HashMap<String, ButtonWithListeners>();
		// Load the images from disk if in testing (non-plugin) mode
		if (Activator.getDefault() == null) {
			List<String> imageNames = Arrays.asList("Add", "Cancel", "Confirm", "Delete", "Move down", "Modify",
					"Move up");
			for (String imageName : imageNames) {
				images.put(imageName, new Image(getDisplay(), "icons\\" + imageName + ".gif"));
			}
		}
	}

	/**
	 * Adds a new button in this button area. The button name is also the filename of its icon and its tooltip text.
	 * 
	 * @param name the name of the newly added button.
	 */
	void newButton(String name) {
		ButtonWithListeners button = new ButtonWithListeners(this, SWT.NONE);
		button.setImage(images.get(name));
		button.setToolTipText(name);
		buttons.put(name, button);
		layout();
	}

	/**
	 * Adds new buttons given their names. The names are also the filenames of their icons and their tooltip texts.
	 * 
	 * @param names the names of the newly added buttons.
	 */
	void newButtons(String... names) {
		for (String name : names) {
			newButton(name);
		}
	}

	/**
	 * Returns a button of this button area given its name.
	 * 
	 * @param name the name of the button to be returned.
	 * @return a button given its name.
	 */
	ButtonWithListeners getButton(String name) {
		return buttons.get(name);
	}

	/**
	 * Removes a button of this button area given its name.
	 * 
	 * @param name the name of the button to be removed.
	 */
	public void removeButton(String name) {
		buttons.remove(name).dispose();
	}

	/**
	 * Removes all the buttons of this button area.
	 */
	public void removeAllButtons() {
		Iterator<String> iter = buttons.keySet().iterator();
		while (iter.hasNext()) {
			String name = iter.next();
			buttons.get(name).dispose();
			iter.remove();
		}
	}

	/**
	 * Enables all the buttons of this button area.
	 */
	public void enableAllButtons() {
		for (ButtonWithListeners button : buttons.values()) {
			button.setEnabled(true);
		}
	}

	/**
	 * Disables all the buttons of this button area.
	 */
	public void disableAllButtons() {
		for (ButtonWithListeners button : buttons.values()) {
			button.setEnabled(false);
		}
	}

	/**
	 * Removes all listeners added to the buttons of this area. Note that in the case that {@link Button} subclassing
	 * has to be avoided, this function could be implemented by removing the buttons and re-adding them.
	 */
	public void removeAllListeners() {
		for (ButtonWithListeners button : buttons.values()) {
			button.removeAllListeners();
		}
	}
}
