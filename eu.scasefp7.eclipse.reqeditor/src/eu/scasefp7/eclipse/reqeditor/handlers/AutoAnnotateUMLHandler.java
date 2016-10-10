package eu.scasefp7.eclipse.reqeditor.handlers;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import eu.scasefp7.eclipse.reqeditor.Activator;
import eu.scasefp7.eclipse.reqeditor.helpers.UMLHelpers;

/**
 * A command handler for automatically annotating a uml file.
 * 
 * @author themis
 */
public class AutoAnnotateUMLHandler extends AutoAnnotateSBDHandler {

	/**
	 * This function is called when the user selects the menu item. It reads the selected resource(s) and automatically
	 * annotates them.
	 * 
	 * @param event the event containing the information about which file was selected.
	 * @return the result of the execution which must be {@code null}.
	 */
	@SuppressWarnings("unchecked")
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		// Create a job since annotation may take long
		Job job = new Job("Auto annotating requirements") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				ISelection selection = HandlerUtil.getCurrentSelection(event);
				if (selection instanceof IStructuredSelection) {
					IStructuredSelection structuredSelection = (IStructuredSelection) selection;
					List<Object> selectionList = structuredSelection.toList();
					// Iterate over the selected files
					for (Object object : selectionList) {
						IFile file = (IFile) Platform.getAdapterManager().getAdapter(object, IFile.class);
						if (file == null) {
							if (object instanceof IAdaptable) {
								file = (IFile) ((IAdaptable) object).getAdapter(IFile.class);
							}
						}
						if (file != null) {
							// Read the requirements of the file
							ArrayList<String> requirements = UMLHelpers.getRequirements(file);
							ArrayList<String> annotations = getAnnotationsForRequirements(requirements);

							// Get the new annotations for the storyboard
							if (annotations != null) {
								// Set the new annotations and update any open editors

								DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
								DocumentBuilder db;
								try {
									db = dbf.newDocumentBuilder();
									Document dom = db.parse(file.getContents());
									Element doc = dom.getDocumentElement();
									doc.normalize();

									Node root;
									Node secondChild = doc.getFirstChild().getNextSibling();
									if (secondChild.getNodeName().equals("packagedElement")
											&& secondChild.getAttributes().getNamedItem("xmi:type").getTextContent()
													.equals("uml:Activity")) {
										root = secondChild;
									} else
										root = doc;
									NodeList nodes = root.getChildNodes();
									int k = 0;
									if (annotations.size() > 0){
										for (int i = 0; i < nodes.getLength(); i++) {
											Node node = nodes.item(i);
											if ((node.getNodeName().equals("packagedElement") && node.getAttributes()
													.getNamedItem("xmi:type").getTextContent().equals("uml:UseCase"))
													|| (node.getNodeName().equals("node") && node.getAttributes()
															.getNamedItem("xmi:type").getTextContent()
															.equals("uml:OpaqueAction"))) {
												String annotationsText = annotations.get(k);
												k++;
												if (!annotationsText.equals("")) {
													annotationsText = annotationsText.substring(0,
															annotationsText.length() - 2);
													if (node.getAttributes().getNamedItem("annotations") == null)
														node.getAttributes().setNamedItem(
																node.getOwnerDocument().createAttribute("annotations"));
													node.getAttributes().getNamedItem("annotations")
															.setTextContent(annotationsText);
												}
											}
										}
									}
									TransformerFactory tf = TransformerFactory.newInstance();
									Transformer transformer = tf.newTransformer();
									transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
									StringWriter writer = new StringWriter();
									transformer.transform(new DOMSource(doc), new StreamResult(writer));
									String ntext = writer.getBuffer().toString();
									writeStringToFile(ntext, file);
									final IFile ffile = file;
									Display.getDefault().asyncExec(new Runnable() {
										public void run() {
											updateEditor(ffile);
										}
									});
								} catch (TransformerException | ParserConfigurationException | SAXException
										| IOException | CoreException e) {
									Activator.log("Error reading or writing annotations in UML diagram", e);
								}
							} else {
								return Status.CANCEL_STATUS;
							}
						}
					}
				}
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
		return null;
	}

	/**
	 * This is a test function that receives an sbd file and automatically annotates it. The path to the uml file must
	 * be given as an argument (e.g. "path/to/new_file.uml")
	 * 
	 * @param args the arguments given to this function, the first and only argument is the path to the sbd file.
	 */
	public static void main(String[] args) {
		String sbdfilename = args[0];
		File file = new File(sbdfilename);
		ArrayList<String> requirements = UMLHelpers.getRequirements(file);
		ArrayList<String> finalAnnotationsString = AutoAnnotateUMLHandler.getAnnotationsForRequirements(requirements);
		for (String annotations : finalAnnotationsString) {
			System.out.println(annotations);
		}
	}

}
