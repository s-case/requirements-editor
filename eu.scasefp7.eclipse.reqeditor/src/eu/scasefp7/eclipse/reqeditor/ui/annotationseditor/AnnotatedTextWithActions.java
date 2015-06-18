package eu.scasefp7.eclipse.reqeditor.ui.annotationseditor;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import eu.scasefp7.eclipse.reqeditor.ui.Annotation;
import eu.scasefp7.eclipse.reqeditor.ui.Limits;
import eu.scasefp7.eclipse.reqeditor.ui.RAnnotation;
import eu.scasefp7.eclipse.reqeditor.ui.RequirementsReader;
import eu.scasefp7.eclipse.reqeditor.ui.TAnnotation;

/**
 * Class that shows the text and annotations (extended by {@link AnnotatedText}) and also includes action listeners for
 * modifying the annotations.
 * 
 * @author themis
 */
public class AnnotatedTextWithActions extends AnnotatedText {

	/**
	 * The position of mouse click.
	 */
	private Point clickposition;

	/**
	 * The limits of the selected text.
	 */
	private Limits textselection;

	/**
	 * Initializes this widget.
	 * 
	 * @param parent the parent widget of the new instance.
	 * @param style the style of this widget.
	 */
	public AnnotatedTextWithActions(Composite parent, int style) {
		super(parent, style);
	}

	/**
	 * Sets the text and the annotations of this widget. The mouse event and selection event listeners are also
	 * initialized.
	 * 
	 * @param reader the {@link RequirementsReader} object that holds all the requirements and annotations.
	 */
	public void setTextAndAnnotations(final RequirementsReader reader) {
		super.setTextAndAnnotations(reader);
		setMouseEventListener();
		setSelectionEventListener();
	}

	/**
	 * Sets a mouse event listener for right click. When the right mouse button is clicked, the click position is stored
	 * in the variable {@code clickposition} and the {@code openMenu} function is called.
	 */
	private void setMouseEventListener() {
		addListenerByUser(SWT.MouseUp, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (event.button == 3) {
					clickposition = new Point(event.x, event.y);
					openMenu();
				}
			}
		});
	}

	/**
	 * Sets a text selection event listener. When text is selected, its limits are stored in the variable
	 * {@code textselection}.
	 */
	private void setSelectionEventListener() {
		addListenerByUser(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				textselection = new Limits(event.x, event.y);
				if (!textSelectionIsValid())
					setSelection(event.y);
			}
		});
	}

	/**
	 * Opens the right click context menu.
	 */
	private void openMenu() {
		// Check whether an annotation is selected
		Annotation selectedAnnotation = annotationIsSelected();
		if (selectedAnnotation != null) {
			// If a T-type annotation is selected
			if (selectedAnnotation.Id.contains(":T")) {
				// If a T-type annotation is selected
				final TAnnotation annotation = (TAnnotation) selectedAnnotation;
				// Find the other annotations of the relevant requirement
				ArrayList<TAnnotation> requirementTAnnotations = reader
						.getTAnnotationsByRequirementId(annotation.reqnum);
				ArrayList<RAnnotation> requirementRAnnotations = reader
						.getRAnnotationsByRequirementId(annotation.reqnum);
				Menu menu = new Menu(this);

				// Iterate over all R-type annotation types
				for (final String[] alltypes : new String[][] { new String[] { "IsActorOf", "Actor", "Action" },
						new String[] { "ActsOn", "Action", "Object" }, new String[] { "ActsOn", "Action", "Property" },
						new String[] { "HasProperty", "Actor", "Property" },
						new String[] { "HasProperty", "Object", "Property" },
						new String[] { "HasProperty", "Property", "Property" } }) {
					// Check whether the annotation may participate to any R-type annotation
					final String rType = alltypes[0];
					String selectedTType = alltypes[1];
					String otherTType = alltypes[2];
					if (annotation.type.equals(selectedTType)) {
						// Add a menu item for the R-type annotation
						MenuItem createItem = null;
						if (menu.getItemCount() > 0) {
							for (MenuItem item : menu.getItems()) {
								if (item.getText().equals(rType))
									createItem = item;
							}
						}
						if (createItem == null) {
							createItem = new MenuItem(menu, SWT.CASCADE);
							createItem.setText(rType);
							createItem.setMenu(new Menu(menu));
						}
						Menu createMenu = createItem.getMenu();

						// Add menu subitems for all possible T-type annotations
						for (final TAnnotation tAnnotation : requirementTAnnotations) {
							if (tAnnotation.type.equals(otherTType) && !tAnnotation.equals(annotation)) {
								MenuItem createRType = new MenuItem(createMenu, SWT.CASCADE);
								createRType.setText(tAnnotation.word);
								// Enable the annotation only if it does not already exist
								if (reader.hasRAnnotation(rType, annotation.Id, tAnnotation.Id))
									createRType.setEnabled(false);
								else {
									// Add a listener to create the annotation
									createRType.addListener(SWT.Selection, new Listener() {
										@Override
										public void handleEvent(Event e) {
											reader.addRAnnotation(rType, annotation.Id, tAnnotation.Id);
											reader.refresh();
											redraw();
										}
									});
								}
							}
						}
					}
				}
				// If there are not any possible annotations set a disabled (none) menu subitem
				for (int i = 0; i < menu.getItemCount(); i++) {
					Menu createMenu = menu.getItem(i).getMenu();
					if (createMenu.getItemCount() == 0) {
						MenuItem createRType = new MenuItem(createMenu, SWT.CASCADE);
						createRType.setText("(none)");
						createRType.setEnabled(false);
					}
				}
				// Add a delete menu item
				MenuItem deleteItem = new MenuItem(menu, SWT.NONE);
				deleteItem.setText("Delete");
				// Enable the delete item only if the annotation is not connected to anything
				boolean hasRAnnotations = false;
				for (RAnnotation rAnnotation : requirementRAnnotations) {
					if (rAnnotation.isConnectedToAnnotationId(annotation.Id)) {
						hasRAnnotations = true;
						break;
					}
				}
				if (hasRAnnotations)
					deleteItem.setEnabled(false);
				else {
					// Add a listener to remove the annotation
					deleteItem.addListener(SWT.Selection, new Listener() {
						@Override
						public void handleEvent(Event e) {
							reader.removeAnnotation(annotation);
							reader.refresh();
							redraw();
						}
					});
				}
				menu.setVisible(true);
			} else {
				// If an R-type annotation is selected
				final RAnnotation annotation = (RAnnotation) selectedAnnotation;
				// Add a delete menu item
				Menu deleteMenu = new Menu(this);
				MenuItem deleteItem = new MenuItem(deleteMenu, SWT.NONE);
				deleteItem.setText("Delete");
				// Add a listener to remove the annotation
				deleteItem.addListener(SWT.Selection, new Listener() {
					@Override
					public void handleEvent(Event e) {
						reader.removeAnnotation(annotation);
						reader.refresh();
						redraw();
					}
				});
				deleteMenu.setVisible(true);
			}
		} else {
			// If text is selected
			if (textselection != null) {
				// Check if selection is valid
				if (textSelectionIsValid()) {
					// Check if mouse click is on text selection
					if (clickInTextSelection()) {
						// Open new menu with the possible T-types as menu items
						Menu menu = new Menu(this);
						for (final String type : new String[] { "Actor", "Action", "Object", "Property" }) {
							MenuItem actorItem = new MenuItem(menu, SWT.NONE);
							actorItem.setText(type);
							// Add a listener to add the new T-type annotation
							actorItem.addListener(SWT.Selection, new Listener() {
								@Override
								public void handleEvent(Event e) {
									int leftLineOfSelection = getLineAtOffset(textselection.left);
									int rightLineOfSelection = getLineAtOffset(textselection.right);
									int reqnum = reader.findRequirementOfSelection(leftLineOfSelection, rightLineOfSelection);
									int lineOffset = getOffsetAtLine(leftLineOfSelection);
									Limits annLimits = new Limits(textselection.left - lineOffset, textselection.right - lineOffset);
									reader.addTAnnotation(type, annLimits, reqnum);
									reader.refresh();
									setSelection(textselection.right);
									redraw();
								}
							});
						}
						menu.setVisible(true);
					}
				}
			}
		}
	}

	/**
	 * Checks if the right click position of the mouse is on some annotation.
	 * 
	 * @return the {@link Annotation} that the mouse clicked on, or {@code null} if no annotation was clicked.
	 */
	private Annotation annotationIsSelected() {
		for (TAnnotation annotation : reader.getTAnnotations()) {
			if (annotation.rectangleLimits.contains(clickposition)) {
				return annotation;
			}
		}
		for (RAnnotation annotation : reader.getRAnnotations()) {
			if (annotation.labelRectangleLimits.contains(clickposition)) {
				return annotation;
			}
		}
		return null;
	}

	/**
	 * Checks if the selected text can form a valid annotation, i.e. does not span over multiple lines and does not
	 * include an already existing annotation.
	 * 
	 * @return {@code true} is the selected text can form a valid annotation, and {@code false} otherwise.
	 */
	private boolean textSelectionIsValid() {
		boolean valid = true;
		if (textselection.left >= textselection.right) {
			valid = false;
		}
		int leftLineOfSelection = getLineAtOffset(textselection.left);
		int rightLineOfSelection = getLineAtOffset(textselection.right);
		int reqnum = reader.findRequirementOfSelection(leftLineOfSelection, rightLineOfSelection);
		if (reqnum <= 0)
			valid = false;
		else {
			int lineOffset = getOffsetAtLine(leftLineOfSelection);
			Limits annLimits = new Limits(textselection.left - lineOffset, textselection.right - lineOffset);
			String word = reader.findWordOfAnnotation(annLimits, reqnum);
			if (word == null
					|| word.contains("\n")
					|| (word.length() == 1 && !Character.isLetter(word.charAt(0)) && !Character.isDigit(word.charAt(0))))
				valid = false;
			else {
				for (TAnnotation annotation : reader.getTAnnotationsByRequirementId(reqnum)) {
					if (annotation.annLimits.isInLimits(annLimits))
						valid = false;
				}
			}
		}
		return valid;
	}

	/**
	 * Checks if the position of the right click of the mouse is within the limits of the selected text.
	 * 
	 * @return {@code true} if mouse click is on the selected text, and {@code false} otherwise.
	 */
	private boolean clickInTextSelection() {
		Point topLeft = getLocationAtOffset(textselection.left);
		Point topRight = getLocationAtOffset(textselection.right);
		return clickposition.x > topLeft.x && clickposition.x < topRight.x && clickposition.y > topLeft.y
				&& clickposition.y < topLeft.y + getLineHeight();
	}

	/**
	 * This is a test function that receives an rqs file and creates an SWT {@link AnnotatedTextWithActions} widget for
	 * this rqs file. The path to the rqs file must be given as an argument (e.g. "path/to/new_file.rqs")
	 * 
	 * @param args the arguments given to this function, the first and only argument is the path to the rqs file.
	 */
	public static void main(String[] args) {
		Shell shell = new Shell();
		shell.setLayout(new FillLayout());
		shell.setSize(800, 800);
		final Display display = shell.getDisplay();
		final AnnotatedTextWithActions widget = new AnnotatedTextWithActions(shell, SWT.BORDER | SWT.V_SCROLL);
		RequirementsReader reader = new RequirementsReader();
		reader.parseFile(args[0]);
		widget.setTextAndAnnotations(reader);
		shell.open();
		while (!shell.isDisposed())
			if (!display.readAndDispatch())
				display.sleep();
	}

}
