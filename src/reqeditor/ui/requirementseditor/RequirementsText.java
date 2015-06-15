package reqeditor.ui.requirementseditor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import reqeditor.ui.StyledTextWithListeners;

/**
 * Class that shows the text of a requirement.
 * 
 * @author themis
 */
public class RequirementsText extends StyledTextWithListeners {

	/**
	 * The current text of the requirement. Used to allow canceling modifications.
	 */
	String currentText;

	/**
	 * Initializes this widget.
	 * 
	 * @param parent the parent widget of the new instance.
	 * @param style the style of this widget.
	 */
	public RequirementsText(Composite parent, int style, String text) {
		super(parent, style);
		setFont(new Font(getDisplay(), "Courier New", 10, SWT.NORMAL));
		setText(text);
		setEditable(false);
		setEnabled(false);
		currentText = text;
		addListener(SWT.Modify, new Listener() {
			@Override
			public void handleEvent(Event e) {
				if (getSize().x < 650) {
					setSize(computeSize(650, SWT.DEFAULT));
					layout();
				}
			}
		});
	}

	/**
	 * Initializes the editing of the requirement text.
	 */
	public void initializeEdit() {
		currentText = getText();
		setEnabled(true);
		setEditable(true);
		setFocus();
		setText(getText());
	}

	/**
	 * Finalizes the editing of the requirement text.
	 */
	public void finalizeEdit() {
		setEnabled(false);
		setEditable(false);
		currentText = getText();
	}

	/**
	 * Cancels the editing of the requirement text.
	 */
	public void cancelEdit() {
		setText(currentText);
		setEnabled(false);
		setEditable(false);
	}

	/**
	 * Swaps the text of this object with the text of another {@link RequirementsText} object, given as a parameter.
	 * 
	 * @param requirementsTextObject the {@link RequirementsText} object to swap its text with the text of this object.
	 */
	public void swapText(RequirementsText requirementsTextObject) {
		currentText = requirementsTextObject.getText();
		requirementsTextObject.setText(getText());
		setText(currentText);
	}
}
