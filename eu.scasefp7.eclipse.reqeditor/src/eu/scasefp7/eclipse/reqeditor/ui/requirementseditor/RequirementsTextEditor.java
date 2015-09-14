package eu.scasefp7.eclipse.reqeditor.ui.requirementseditor;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import eu.scasefp7.eclipse.reqeditor.ui.Requirement;
import eu.scasefp7.eclipse.reqeditor.ui.RequirementsReader;

/**
 * Class that shows the requirements and allows adding, modifying and deleting them. It is implemented as a
 * {@link ScrolledComposite} and contains a {@link Composite} object that acts as a grid.
 * 
 * @author themis
 */
public class RequirementsTextEditor extends ScrolledComposite {

	/**
	 * The grid object that contains all the objects (buttons and text areas).
	 */
	Composite grid;

	/**
	 * The button areas.
	 */
	final HashMap<Integer, ButtonArea> buttonAreas;

	/**
	 * The text areas of the requirements.
	 */
	final HashMap<Integer, RequirementsText> requirementsTexts;

	/**
	 * The {@link RequirementsReader} object that holds all the requirements and annotations.
	 */
	private RequirementsReader reader;

	/**
	 * Boolean indicating whether the widget is currently in add mode ({@code true}) or modify mode ({@code false}).
	 */
	private boolean addMode;

	/**
	 * Initializes this widget.
	 * 
	 * @param parent the parent widget of the new instance.
	 * @param style the style of this widget.
	 */
	public RequirementsTextEditor(Composite parent, int style) {
		super(parent, style);
		grid = null;
		buttonAreas = new HashMap<Integer, ButtonArea>();
		requirementsTexts = new HashMap<Integer, RequirementsText>();
		addMode = true;
	}

	/**
	 * Sets the size and calls the {@link #layout()} function of this widget. This function has to be called whenever
	 * buttons are added or deleted to the widget.
	 */
	private void setSizeAndLayout() {
		grid.setSize(grid.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		grid.layout();
		if (grid.getSize().x < 750) {
			grid.setSize(grid.computeSize(750, SWT.DEFAULT));
			grid.layout();
		}
	}

	/**
	 * Disables all the buttons of this widget.
	 */
	private void disableAllButtons() {
		for (ButtonArea buttonArea : buttonAreas.values()) {
			buttonArea.disableAllButtons();
		}
	}

	/**
	 * Enables all the buttons of this widget. Also checks and disables the move up and move down any buttons that
	 * should not be pressed.
	 */
	private void enableAllButtons() {
		for (ButtonArea buttonArea : buttonAreas.values()) {
			buttonArea.enableAllButtons();
		}
		if (buttonAreas.size() > 1) {
			buttonAreas.get(1).getButton("Move up").setEnabled(false);
			buttonAreas.get(buttonAreas.size() - 1).getButton("Move down").setEnabled(false);
		}
	}

	/**
	 * Sets the text requirements of this widget. The buttons and the event are also intialized.
	 * 
	 * @param reader the {@link RequirementsReader} object that holds all the requirements and annotations.
	 */
	public void setText(final RequirementsReader reader) {
		// Remove any previously drawn data
		this.reader = reader;
		if (grid != null)
			grid.dispose();
		grid = new Composite(this, SWT.NONE);
		setLayoutData(new GridData(GridData.FILL_BOTH));
		setContent(grid);
		grid.setLayoutData(new GridData());
		grid.setLayout(new GridLayout(2, false));
		buttonAreas.clear();
		requirementsTexts.clear();

		// Add the buttons and the text for each requirement
		final ArrayList<Requirement> requirements = reader.getRequirements();
		for (final Requirement requirement : requirements) {
			ButtonArea buttonArea = new ButtonArea(grid, SWT.NONE);
			buttonArea.newButtons("Delete", "Modify", "Move down", "Move up");
			buttonAreas.put(requirement.id, buttonArea);
			RequirementsText requirementsText = new RequirementsText(grid, SWT.NONE, requirement.text);
			requirementsTexts.put(requirement.id, requirementsText);
		}
		// Add an "add" button to allow adding new requirements
		final ButtonArea addButtonArea = new ButtonArea(grid, SWT.NONE);
		addButtonArea.newButton("Add");
		buttonAreas.put(-1, addButtonArea);
		setSizeAndLayout();

		// Enable all the buttons and add the listeners for each requirement
		enableAllButtons();
		for (final Requirement requirement : requirements) {
			addListeners(requirement.id);
		}
		addAddListener();
	}

	/**
	 * Adds the selection listeners for the buttons of the requirement of which the id is given.
	 * 
	 * @param requirementId the id of the requirement to add the listeners.
	 */
	private void addListeners(final int requirementId) {
		// Get the button area and the text area of the requirement
		final ButtonArea buttonArea = buttonAreas.get(requirementId);
		final RequirementsText requirementsText = requirementsTexts.get(requirementId);

		// Add a listener for the delete button
		buttonArea.getButton("Delete").addListenerByUser(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				// Delete the requirement and remove the button area and text area
				reader.deleteRequirement(requirementId);
				reader.refresh();
				buttonAreas.remove(requirementId).dispose();
				requirementsTexts.remove(requirementId).dispose();
				// Move up the button areas and the text areas that are below the one removed and update the listeners
				for (int i = requirementId; i < requirementsTexts.size() + 1; i++) {
					buttonAreas.get(i + 1).removeAllListeners();
					requirementsTexts.get(i + 1).removeAllListeners();
				}
				for (int i = requirementId; i < requirementsTexts.size() + 1; i++) {
					requirementsTexts.put(i, requirementsTexts.remove(i + 1));
					buttonAreas.put(i, buttonAreas.remove(i + 1));
				}
				for (int i = requirementId; i < requirementsTexts.size() + 1; i++) {
					addListeners(i);
				}
				setSizeAndLayout();
				enableAllButtons();
			}
		});

		// Add a listener for the modify button
		buttonArea.getButton("Modify").addListenerByUser(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				// Remove any pre-existing buttons from the button area
				buttonArea.removeAllButtons();
				// Disable the buttons of all other button areas and the add button
				disableAllButtons();
				// Add the confirm and cancel buttons and initialize the edit
				buttonArea.newButtons("Cancel", "Confirm");
				requirementsText.initializeEdit();
				// Add a listener for confirming the changes
				buttonArea.getButton("Confirm").addListenerByUser(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						// Set the changes and refresh
						requirementsText.finalizeEdit();
						reader.modifyRequirement(requirementId, requirementsText.getText());
						reader.refresh();
						buttonArea.removeAllButtons();
						buttonArea.newButtons("Delete", "Modify", "Move down", "Move up");
						setSizeAndLayout();
						enableAllButtons();
						addListeners(requirementId);
					}
				});
				// Add a listener for canceling the changes
				buttonArea.getButton("Cancel").addListenerByUser(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						// Cancel the changes and set back the old buttons and enable them
						buttonArea.removeAllButtons();
						requirementsText.cancelEdit();
						buttonArea.newButtons("Delete", "Modify", "Move down", "Move up");
						setSizeAndLayout();
						enableAllButtons();
						addListeners(requirementId);
					}
				});
				addMode = false;

				// Add a listener for confirming using the enter key and canceling using the escape key
				requirementsText.addVerifyKeyListener(new VerifyKeyListener() {
					@Override
					public void verifyKey(VerifyEvent e) {
						if (!addMode){
							if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
								// Set the changes and refresh
								e.doit = false;
								requirementsText.finalizeEdit();
								reader.modifyRequirement(requirementId, requirementsText.getText());
								reader.refresh();
								buttonArea.removeAllButtons();
								buttonArea.newButtons("Delete", "Modify", "Move down", "Move up");
								setSizeAndLayout();
								enableAllButtons();
								addListeners(requirementId);
							} else if (e.keyCode == SWT.ESC) {
								// Cancel the changes and set back the old buttons and enable them
								e.doit = false;
								buttonArea.removeAllButtons();
								requirementsText.cancelEdit();
								buttonArea.newButtons("Delete", "Modify", "Move down", "Move up");
								setSizeAndLayout();
								enableAllButtons();
								addListeners(requirementId);
							}
						}
					}
				});
			}
		});

		// Add a listener for the move down button
		buttonArea.getButton("Move down").addListenerByUser(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				reader.swapRequirements(requirementId, requirementId + 1);
				reader.refresh();
				requirementsText.swapText(requirementsTexts.get(requirementId + 1));
				setSizeAndLayout();
			}
		});

		// Add a listener for the move up button
		buttonArea.getButton("Move up").addListenerByUser(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				reader.swapRequirements(requirementId, requirementId - 1);
				reader.refresh();
				requirementsText.swapText(requirementsTexts.get(requirementId - 1));
				setSizeAndLayout();
			}
		});
	}

	/**
	 * Adds the selection listener for the add button.
	 */
	private void addAddListener() {
		// Get the add button area
		final ButtonArea addButtonArea = buttonAreas.get(-1);

		// Add a listener for the add button
		addButtonArea.getButton("Add").addListenerByUser(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				// Add a new requirement with its id, button area, and text area
				final int requirementId = requirementsTexts.size() + 1;
				final ButtonArea buttonArea = new ButtonArea(grid, SWT.NONE);
				final RequirementsText requirementsText = new RequirementsText(grid, SWT.NONE, "");

				// Disable the buttons of all other button areas and the add button
				disableAllButtons();
				// Add the confirm and cancel buttons and initialize the edit
				buttonArea.newButtons("Cancel", "Confirm");
				requirementsText.initializeEdit();
				addButtonArea.moveBelow(requirementsText);

				// Add a listener for confirming the changes
				buttonArea.getButton("Confirm").addListenerByUser(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						// Set the changes and refresh
						requirementsText.finalizeEdit();
						reader.addRequirement(requirementId, requirementsText.getText());
						reader.refresh();
						buttonArea.removeAllButtons();
						buttonArea.newButtons("Delete", "Modify", "Move down", "Move up");
						setSizeAndLayout();
						buttonAreas.put(requirementId, buttonArea);
						requirementsTexts.put(requirementId, requirementsText);
						enableAllButtons();
						addListeners(requirementId);
					}
				});

				// Add a listener for canceling the changes
				buttonArea.getButton("Cancel").addListenerByUser(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						// Cancel the changes, dispose the new buttons and set back the old buttons and enable them
						requirementsText.cancelEdit();
						requirementsText.dispose();
						buttonArea.dispose();
						setSizeAndLayout();
						enableAllButtons();
					}
				});
				addMode = true;

				// Add a listener for confirming using the enter key and canceling using the escape key
				requirementsText.addVerifyKeyListener(new VerifyKeyListener() {
					@Override
					public void verifyKey(VerifyEvent e) {
						if (addMode){
							if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
								// Set the changes and refresh
								e.doit = false;
								requirementsText.finalizeEdit();
								reader.addRequirement(requirementId, requirementsText.getText());
								reader.refresh();
								buttonArea.removeAllButtons();
								buttonArea.newButtons("Delete", "Modify", "Move down", "Move up");
								setSizeAndLayout();
								buttonAreas.put(requirementId, buttonArea);
								requirementsTexts.put(requirementId, requirementsText);
								enableAllButtons();
								addListeners(requirementId);
							} else if (e.keyCode == SWT.ESC) {
								// Cancel the changes, dispose the new buttons and set back the old buttons and enable them
								e.doit = false;
								requirementsText.cancelEdit();
								requirementsText.dispose();
								buttonArea.dispose();
								setSizeAndLayout();
								enableAllButtons();
							}
						}
					}
				});

				// Set the size and the layout of the widget
				setSizeAndLayout();
				requirementsText.setText("");
			}
		});
	}

	/**
	 * This is a test function that receives an rqs file and creates an SWT {@link RequirementsTextEditor} widget for
	 * this rqs file. The path to the rqs file must be given as an argument (e.g. "path/to/new_file.rqs")
	 * 
	 * @param args the arguments given to this function, the first and only argument is the path to the rqs file.
	 */
	public static void main(String[] args) {
		Shell shell = new Shell();
		shell.setLayout(new FillLayout());
		shell.setSize(850, 500);
		final Display display = shell.getDisplay();
		final RequirementsTextEditor widget = new RequirementsTextEditor(shell, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.BORDER);
		RequirementsReader reader = new RequirementsReader();
		reader.parseFile(args[0]);
		widget.setText(reader);
		shell.open();
		while (!shell.isDisposed())
			if (!display.readAndDispatch())
				display.sleep();
	}

}
