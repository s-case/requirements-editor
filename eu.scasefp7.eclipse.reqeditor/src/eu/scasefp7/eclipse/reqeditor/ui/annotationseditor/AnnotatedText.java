package eu.scasefp7.eclipse.reqeditor.ui.annotationseditor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import eu.scasefp7.eclipse.reqeditor.Activator;
import eu.scasefp7.eclipse.reqeditor.ui.RAnnotation;
import eu.scasefp7.eclipse.reqeditor.ui.Requirement;
import eu.scasefp7.eclipse.reqeditor.ui.RequirementsReader;
import eu.scasefp7.eclipse.reqeditor.ui.StyledTextWithListeners;
import eu.scasefp7.eclipse.reqeditor.ui.TAnnotation;

/**
 * Class that shows the text and the annotations.
 * 
 * @author themis
 */
public class AnnotatedText extends StyledTextWithListeners {

	/**
	 * The color black.
	 */
	private Color BLACK;

	/**
	 * The color cyan.
	 */
	private Color CYAN;

	/**
	 * The color blue.
	 */
	private Color BLUE;

	/**
	 * The font of the text.
	 */
	private Font textfont;

	/**
	 * The font of the annotations.
	 */
	private Font annotationfont;

	/**
	 * The {@link RequirementsReader} object that holds all the requirements and annotations.
	 */
	protected RequirementsReader reader;

	/**
	 * Initializes this widget, its colors and its fonts.
	 * 
	 * @param parent the parent widget of the new instance.
	 * @param style the style of this widget.
	 */
	public AnnotatedText(Composite parent, int style) {
		super(parent, style);
		final Display display = getDisplay();
		BLACK = display.getSystemColor(SWT.COLOR_BLACK);
		CYAN = display.getSystemColor(SWT.COLOR_CYAN);
		BLUE = display.getSystemColor(SWT.COLOR_BLUE);
		textfont = new Font(display, "Courier New", 10, SWT.NORMAL);
		annotationfont = new Font(display, "Tahoma", 9, SWT.NORMAL);
		setCursor(new Cursor(display, SWT.CURSOR_ARROW));
		setCaret(null);
		if (Activator.getDefault() != null)
			Activator.getDefault().getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
				@Override
				public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent event) {
					if (reader != null && !isDisposed())
						setTextAndAnnotations(reader);
				}
			});
	}

	/**
	 * Sets the text and the annotations of this widget.
	 * 
	 * @param reader the {@link RequirementsReader} object that holds all the requirements and annotations.
	 */
	public void setTextAndAnnotations(final RequirementsReader reader) {
		// Remove any previously drawn data
		this.reader = reader;
		removeAllListeners();
		setText("");

		// Set the text of the requirements
		final ArrayList<Requirement> requirements = reader.getRequirements();
		setFont(textfont);
		for (Requirement requirement : requirements) {
			for (int j = 0; j < reader.indent; j++)
				append("\n");
			append(requirement.text + "\n");
		}

		// Add a paint listener to draw the annotations
		addListenerByUser(SWT.Paint, new Listener() {
			public void handleEvent(Event event) {
				// Paint all T-type annotations
				for (int i = 1; i < requirements.size() + 1; i++) {
					// Get the annotations for each requirement
					ArrayList<TAnnotation> annotations = reader.getTAnnotationsByRequirementId(i);

					for (TAnnotation annotation : annotations) {
						// Find text limits
						int left = getOffsetAtLine(i * reader.indent + i - 1) + annotation.annLimits.left;
						// int right = getOffsetAtLine(i * reader.indent + i - 1) + annotation.annLimits.right;

						// Find the annotation position by averaging over the position of the word to be annotated
						int wordWidth = event.gc.stringExtent(annotation.word).x;
						int lineHeight = getLineHeight();
						Point topLeft = getLocationAtOffset(left);
						int xleft = topLeft.x - 1;
						int ytop = topLeft.y - lineHeight;
						int xwidth = wordWidth;
						int ywidth = lineHeight;

						// Highlight annotations
						Color backgroundColor = event.gc.getBackground();
						setBackgroundColor(event, "EntityText");
						event.gc.fillRectangle(xleft, ytop + lineHeight, xwidth, ywidth);
						event.gc.setBackground(backgroundColor);
						Color foregroundColor = event.gc.getForeground();
						event.gc.setForeground(BLACK);
						event.gc.drawString(annotation.word, xleft + 1, ytop + lineHeight + 1, true);
						event.gc.setForeground(foregroundColor);
						event.gc.setFont(annotationfont);
						int annotationWidth = event.gc.stringExtent(annotation.type).x;
						boolean expandLeft = true;
						while (xwidth < annotationWidth) {
							xwidth++;
							if (expandLeft) {
								xleft--;
								expandLeft = false;
							} else
								expandLeft = true;
						}

						// Set the rectangle of the annotation
						int leftX = xleft + xwidth / 2 - annotationWidth / 2;
						int rightX = leftX + annotationWidth;
						int upperY = ytop;
						int lowerY = ytop - ywidth;
						if (annotation.annLimits.left == 0 && wordWidth < annotationWidth) {
							leftX += 3 + Math.floor((double) (annotationWidth - wordWidth) / 2.0);
							rightX = leftX + annotationWidth;
						}
						annotation.setRectangleLimits(leftX - 1, upperY, rightX - leftX + 1, upperY - lowerY);
						event.gc.setFont(textfont);
					}
					Collections.sort(annotations, TAnnotation.LabelRectangleComparator);
					event.gc.setFont(annotationfont);
					for (int j = 0; j < annotations.size(); j++) {
						// Find the label rectangle position of the annotation
						TAnnotation annotation = annotations.get(j);
						Rectangle rLimits = annotation.rectangleLimits;
						int xpos = rLimits.x;
						int ypos = rLimits.y;
						int currentLeft = xpos;

						// Use greedy strategy based on previous annotation label to determine the final position for
						// the current annotation label, so that they do not overlap.
						if (j > 0) {
							TAnnotation previousAnnotation = annotations.get(j - 1);
							int previousAnnotationWidth = event.gc.stringExtent(previousAnnotation.type).x;
							int previousRight = previousAnnotation.rectangleLimits.x + previousAnnotationWidth;
							if (previousRight >= currentLeft - 2) {
								currentLeft += (2 + previousRight - currentLeft);
								annotation.setRectangleLimits(currentLeft, ypos, rLimits.width, rLimits.height);
							}
						}

						// Draw the rectangle and the text label
						rLimits = annotation.rectangleLimits;
						setForegroundColor(event, "Entity");
						event.gc.drawRectangle(rLimits);
						event.gc.drawText(annotation.type, rLimits.x + 1, rLimits.y, true);
					}
					event.gc.setFont(textfont);
				}

				// Paint all R-type annotations
				for (int i = 1; i < requirements.size() + 1; i++) {
					// Get the annotations for each requirement
					ArrayList<RAnnotation> annotations = reader.getRAnnotationsByRequirementId(i);

					// Remove any R-type annotations that are not connected to T-type annotations
					for (Iterator<RAnnotation> it = annotations.iterator(); it.hasNext();) {
						RAnnotation annotation = it.next();
						if (reader.getTAnnotation(annotation.Arg1) == null
								|| reader.getTAnnotation(annotation.Arg2) == null) {
							reader.removeAnnotation(annotation);
							it.remove();
						}
					}

					// Draw the arcs
					for (RAnnotation annotation : annotations) {
						// Get the position of the relevant T-type annotations
						TAnnotation arg1 = reader.getTAnnotation(annotation.Arg1);
						TAnnotation arg2 = reader.getTAnnotation(annotation.Arg2);
						int xone = arg1.rectangleLimits.x + arg1.rectangleLimits.width / 2;
						int yone = arg1.rectangleLimits.y;
						int xtwo = arg2.rectangleLimits.x + arg2.rectangleLimits.width / 2;
						// int ytwo = arg2.rectangleLimits.y;

						// Calculate the arc starting and ending point and draw it
						int lineHeight = getLineHeight();
						int leftX = xone;
						int rightX = xtwo;
						int upperY = (int) (yone - 1.5 * lineHeight);
						int lowerY = (int) (yone + 1.5 * lineHeight);
						setForegroundColor(event, annotation.type);
						event.gc.drawArc(leftX, upperY, rightX - leftX, lowerY - upperY, 0, 180);
						event.gc.drawLine(rightX, upperY + lineHeight + 10, rightX + 3, upperY + lineHeight + 5);
						event.gc.drawLine(rightX, upperY + lineHeight + 10, rightX - 3, upperY + lineHeight + 5);
						int xpos = leftX + (rightX - leftX) / 2;
						int ypos = upperY - lineHeight;
						annotation.setLabelPosition(xpos, ypos);
					}

					// Draw label text on top of arcs
					event.gc.setFont(annotationfont);
					Collections.sort(annotations, RAnnotation.LabelPositionComparator);
					for (int j = 0; j < annotations.size(); j++) {
						// Find the label position of the annotation
						RAnnotation annotation = annotations.get(j);
						int annotationWidth = event.gc.stringExtent(annotation.type).x;
						int xpos = annotation.labelPosition.x;
						int ypos = annotation.labelPosition.y;
						int currentLeft = xpos - annotationWidth / 2;

						// Use greedy strategy based on previous annotation label to determine the final position for
						// the current annotation label, so that they do not overlap.
						if (j > 0) {
							RAnnotation previousAnnotation = annotations.get(j - 1);
							int previousAnnotationWidth = event.gc.stringExtent(previousAnnotation.type).x;
							int previousRight = previousAnnotation.labelPosition.x + previousAnnotationWidth / 2;
							if (previousRight >= currentLeft - 10) {
								currentLeft += (10 + previousRight - currentLeft);
								annotation.setLabelPosition(currentLeft + annotationWidth / 2, ypos);
							}
						}

						// Draw the label.
						setForegroundColor(event, annotation.type);
						event.gc.drawText(annotation.type, currentLeft, ypos, true);
					}

					// Find rectangle limits for the label of each annotation
					for (RAnnotation annotation : annotations) {
						int xpos = annotation.labelPosition.x;
						int ypos = annotation.labelPosition.y;
						int annotationWidth = event.gc.stringExtent(annotation.type).x;
						int lineHeight = getLineHeight();
						int leftX = xpos - annotationWidth / 2;
						int rightX = xpos + annotationWidth / 2;
						int upperY = ypos;
						int lowerY = ypos - lineHeight;
						annotation.setLabelRectangleLimits(leftX, upperY, rightX - leftX, upperY - lowerY);
					}
					event.gc.setFont(textfont);
				}
			}

			/**
			 * Sets the foreground color using the given preferences.
			 * 
			 * @param event the event required to get the graphics context.
			 * @param name the name of the preference to use.
			 */
			private void setForegroundColor(Event event, String name) {
				if (Activator.getDefault() != null) {
					String colorString = Activator.getDefault().getPreferenceStore().getString(name);
					int r = Integer.parseInt(colorString.split(",")[0]);
					int g = Integer.parseInt(colorString.split(",")[1]);
					int b = Integer.parseInt(colorString.split(",")[2]);
					event.gc.setForeground(new Color(getDisplay(), r, g, b));
				} else
					event.gc.setForeground(BLUE);
			}

			/**
			 * Sets the background color using the given preferences.
			 * 
			 * @param event the event required to get the graphics context.
			 * @param name the name of the preference to use.
			 */
			private void setBackgroundColor(Event event, String name) {
				if (Activator.getDefault() != null) {
					String colorString = Activator.getDefault().getPreferenceStore().getString(name);
					int r = Integer.parseInt(colorString.split(",")[0]);
					int g = Integer.parseInt(colorString.split(",")[1]);
					int b = Integer.parseInt(colorString.split(",")[2]);
					event.gc.setBackground(new Color(getDisplay(), r, g, b));
				} else
					event.gc.setBackground(CYAN);
			}
		});
		redraw();
	}
}
