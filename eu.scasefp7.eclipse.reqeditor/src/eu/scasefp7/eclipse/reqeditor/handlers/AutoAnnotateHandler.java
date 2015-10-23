package eu.scasefp7.eclipse.reqeditor.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
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
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import eu.scasefp7.eclipse.reqeditor.helpers.RQSHelpers;

/**
 * A command handler for automatically annotating an rqs file.
 * 
 * @author themis
 */
public class AutoAnnotateHandler extends EditorAwareHandler {

	/**
	 * The address of the NLP Server.
	 */
	private static final String NLPServerAddress = "http://nlp.scasefp7.eu:8010/nlpserver/project";

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
							// Read the requirements and the annotations of the file
							String[] txtAndAnn = RQSHelpers.getRequirementsAndAnnotationsStrings(file);
							// Get the new annotations for the requirements
							String annotationsText = getAnnotationsForText(file);
							if (annotationsText != null) {
								// Set the new annotations and update any open editors
								String ntext = "REQUIREMENTS\n------------\n";
								ntext += txtAndAnn[0];
								ntext += "------------\n\nANNOTATIONS\n------------\n";
								ntext += annotationsText;
								ntext += "------------\n";
								writeStringToFile(ntext, file);
								final IFile ffile = file;
								Display.getDefault().asyncExec(new Runnable() {
									public void run() {
										updateEditor(ffile);
									}
								});
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
	 * Automatically annotates the given file resource.
	 * 
	 * @param file the resource to be annotated.
	 * @return the annotations of the file.
	 */
	private String getAnnotationsForText(IFile file) {
		ArrayList<String> requirements = RQSHelpers.getRequirements(file);
		return getAnnotationsForRequirements(requirements);
	}

	/**
	 * Automatically annotates the given requirements.
	 * 
	 * @param requirements the requirements to be annotated.
	 * @return the annotations of the requirements.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static String getAnnotationsForRequirements(ArrayList<String> requirements) {
		int j = 1;
		String projectRequirements = "";
		for (String requirement : requirements) {
			j++;
			projectRequirements += "{\"id\":\"" + "FR" + (j - 1) + "\",\"text\":\"" + requirement + "\"},";
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
			String finalAnnotationsString = "";
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

			for (int i = 1; i < totalRequirements + 1; i++) {
				String id = "FR" + i;
				for (String annotation : annotationsOfRequirements.get(id)) {
					if (annotation.startsWith("T")) {
						String[] splitAnnotation = annotation.split(" ");
						String newid = i + ":T" + splitAnnotation[0].substring(1);
						String newtype = splitAnnotation[1].equals("Theme") ? "Object" : splitAnnotation[1];
						annotation = newid + "\t" + newtype + " " + splitAnnotation[2] + " " + splitAnnotation[3]
								+ "\t" + splitAnnotation[4];
					} else if (annotation.startsWith("R")) {
						String[] splitAnnotation = annotation.split(" ");
						String newleftlimit = i + ":T" + splitAnnotation[2].split(":")[1].substring(1);
						String newrightlimit = i + ":T" + splitAnnotation[3].split(":")[1].substring(1);
						String newid = i + ":R" + splitAnnotation[0].substring(1);
						annotation = newid + "\t" + splitAnnotation[1] + " " + newleftlimit + " " + newrightlimit;
					}
					finalAnnotationsString += annotation + "\n";
				}
			}
			return finalAnnotationsString;
		} else
			return null;
	}

	/**
	 * Makes a REST request with JSON body to the NLP server and returns the response in JSON format.
	 * 
	 * @param query the JSON query to be sent.
	 * @return the JSON response of the request.
	 */
	private static String makeRestRequest(String query) {
		String response = null;
		try {
			URL url = new URL(NLPServerAddress);
			// Open POST connection
			URLConnection urlc = url.openConnection();
			urlc.setRequestProperty("Content-Type", "application/json");
			urlc.setDoOutput(true);
			urlc.setAllowUserInteraction(false);

			// Send query
			PrintStream ps = new PrintStream(urlc.getOutputStream(), false, "UTF-8");
			ps.print(query);
			ps.close();

			// Get result
			BufferedReader br = new BufferedReader(new InputStreamReader(urlc.getInputStream(), "UTF-8"));
			String l = null;
			while ((l = br.readLine()) != null) {
				response = l;
			}
			br.close();
		} catch (ConnectException e) {

		} catch (IOException e) {

		}
		return response;
	}

	/**
	 * Parses a JSON string and returns a java object.
	 * 
	 * @param json the JSON string.
	 * @return a java object including the JSON information.
	 */
	private static Object parseJSON(String json) {
		JSONParser parser = new JSONParser();
		try {
			return parser.parse(json);
		} catch (ParseException pe) {
			throw new RuntimeException("Invalid json", pe);
		}
	}

	/**
	 * This is a test function that receives an rqs file and automatically annotates it. The path to the rqs file must
	 * be given as an argument (e.g. "path/to/new_file.rqs")
	 * 
	 * @param args the arguments given to this function, the first and only argument is the path to the rqs file.
	 */
	public static void main(String[] args) {
		String rqsfilename = args[0];
		File file = new File(rqsfilename);
		ArrayList<String> requirements = RQSHelpers.getRequirements(file);
		String finalAnnotationsString = AutoAnnotateHandler.getAnnotationsForRequirements(requirements);
		System.out.println(finalAnnotationsString);
	}

}
