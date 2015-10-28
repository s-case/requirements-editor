package eu.scasefp7.eclipse.reqeditor.handlers;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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

import eu.scasefp7.eclipse.reqeditor.helpers.SBDHelpers;

/**
 * A command handler for automatically annotating an rqs file.
 * 
 * @author themis
 */
public class AutoAnnotateSBDHandler extends AutoAnnotateHandler {

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
							ArrayList<String> requirements = SBDHelpers.getRequirements(file);
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

									Node root = doc.getElementsByTagName("auth.storyboards:StoryboardDiagram").item(0);
									NodeList nodes = root.getChildNodes();
									int k = 0;
									for (int i = 0; i < nodes.getLength(); i++) {
										Node node = nodes.item(i);
										if (node.getNodeName().equals("storyboardactions")) {
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
									TransformerFactory tf = TransformerFactory.newInstance();
									Transformer transformer = tf.newTransformer();
									StringWriter writer = new StringWriter();
									transformer.transform(new DOMSource(doc), new StreamResult(writer));
									String ntext = writer.getBuffer().toString();
									ntext = new StringBuilder(ntext).insert(ntext.indexOf('>') + 1, "\n").toString();
									writeStringToFile(ntext, file);
									final IFile ffile = file;
									Display.getDefault().asyncExec(new Runnable() {
										public void run() {
											updateEditor(ffile);
										}
									});
								} catch (TransformerException | ParserConfigurationException | SAXException
										| IOException | CoreException e) {
									e.printStackTrace();
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
	 * Automatically annotates the given requirements.
	 * 
	 * @param requirements the requirements to be annotated.
	 * @return the annotations of the requirements.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static ArrayList<String> getAnnotationsForRequirements(ArrayList<String> requirements) {
		int j = 1;
		String projectRequirements = "";
		for (String requirement : requirements) {
			j++;
			projectRequirements += "{\"id\":\"" + "FR" + (j - 1) + "\",\"text\":\"" + requirement.toLowerCase()
					+ "\"},";
		}
		projectRequirements = projectRequirements.substring(0, projectRequirements.length() - 1);
		int totalRequirements = j - 1;
		String query = "{\"project_name\":\"any\",\"annotation_format\":\"ann\",\"project_requirements\":["
				+ projectRequirements + "]}";

		// Example query:
		// "{\"project_name\":\"any\",\"project_requirements\":
		// [{\"id\":\"FR1\",\"text\":\"The user must be able to create an account.\"},
		// {\"id\":\"FR2\",\"text\":\"The user must be able to login to his/her account.\"},
		// ...],\"annotation_format\":\"ann\"}";

		String response = makeRestRequest(query);
		// Example Response:
		// "{\"created_at\":\"2015-03-16T13:08Z\",\"project_name\":\"any\",\"project_requirements\":
		// [{\"id\":\"FR1\",\"text\":\"The user must be able to create an account.\"},
		// {\"id\":\"FR2\",\"text\":\"The user must be able to login to his/her account.\"},...],
		// \"annotations\":[{\"id\":\"FR1\",\"annotation\":[\"R1 ActsOn Arg1:T1 Arg2:T2\",\"R3 IsActorOf Arg1:T3
		// Arg2:T1\", "T1 Action 23 29 create\",\"T2 Theme 37 44 account\",\"T3 Actor 2 6 user\"]},...],
		// \"annotation_format\":\"ann\"}";
		if (response != null) {
			HashMap jsonResponse = (HashMap) parseJSON(response);
			ArrayList<Object> annotations = (ArrayList<Object>) jsonResponse.get("annotations");
			HashMap<String, ArrayList<String>> annotationsOfRequirements = new HashMap<String, ArrayList<String>>();
			for (Object annotation : annotations) {
				HashMap annotationobject = (HashMap) annotation;
				String id = (String) annotationobject.get("id");
				ArrayList<Object> annForId = (ArrayList<Object>) annotationobject.get("annotation");
				ArrayList<String> annotationsForId = new ArrayList<String>();
				for (Object annForIdk : annForId) {
					annotationsForId.add((String) annForIdk);
				}
				annotationsOfRequirements.put(id, annotationsForId);
			}

			ArrayList<String> annotationsString = new ArrayList<String>();
			for (int i = 1; i < totalRequirements + 1; i++) {
				String id = "FR" + i;
				String finalAnnotationsString = "";
				for (String annotation : annotationsOfRequirements.get(id)) {
					if (annotation.startsWith("T")) {
						String[] splitAnnotation = annotation.split(" ");
						String newid = i + ":T" + splitAnnotation[0].substring(1);
						String newtype = splitAnnotation[1].equals("Theme") ? "Object" : splitAnnotation[1];
						String word = requirements.get(i - 1).substring(Integer.parseInt(splitAnnotation[2]),
								Integer.parseInt(splitAnnotation[3]));
						annotation = newid + "\\t" + newtype + " " + splitAnnotation[2] + " " + splitAnnotation[3]
								+ "\\t" + word;
					} else if (annotation.startsWith("R")) {
						String[] splitAnnotation = annotation.split(" ");
						String newleftlimit = i + ":T" + splitAnnotation[2].split(":")[1].substring(1);
						String newrightlimit = i + ":T" + splitAnnotation[3].split(":")[1].substring(1);
						String newid = i + ":R" + splitAnnotation[0].substring(1);
						annotation = newid + "\\t" + splitAnnotation[1] + " " + newleftlimit + " " + newrightlimit;
					}
					finalAnnotationsString += annotation + "\\n";
				}
				annotationsString.add(finalAnnotationsString);
			}
			return annotationsString;
		} else
			return null;
	}

	/**
	 * This is a test function that receives an sbd file and automatically annotates it. The path to the sbd file must
	 * be given as an argument (e.g. "path/to/new_file.sbd")
	 * 
	 * @param args the arguments given to this function, the first and only argument is the path to the sbd file.
	 */
	public static void main(String[] args) {
		String sbdfilename = args[0];
		File file = new File(sbdfilename);
		ArrayList<String> requirements = SBDHelpers.getRequirements(file);
		ArrayList<String> finalAnnotationsString = AutoAnnotateSBDHandler.getAnnotationsForRequirements(requirements);
		for (String annotations : finalAnnotationsString) {
			System.out.println(annotations);
		}
	}

}
