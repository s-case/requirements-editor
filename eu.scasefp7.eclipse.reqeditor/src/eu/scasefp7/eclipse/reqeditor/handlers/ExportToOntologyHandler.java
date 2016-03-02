package eu.scasefp7.eclipse.reqeditor.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import eu.scasefp7.eclipse.core.ontology.StaticOntologyAPI;
import eu.scasefp7.eclipse.reqeditor.helpers.RQSHelpers;

/**
 * A command handler for exporting all the annotated instances to the static ontology.
 * 
 * @author themis
 */
public class ExportToOntologyHandler extends ProjectAwareHandler {

	/**
	 * This function is called when the user selects the menu item. It reads the selected resource(s) and populates the
	 * static ontology.
	 * 
	 * @param event the event containing the information about which file was selected.
	 * @return the result of the execution which must be {@code null}.
	 */
	@SuppressWarnings("unchecked")
	public Object execute(ExecutionEvent event) throws ExecutionException {
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
					// Instantiate the static ontology
					StaticOntologyAPI ontology = new StaticOntologyAPI(file.getProject(), true);
					instantiateOntology(file, ontology);
					ontology.close();
				}
			}
		}
		return null;
	}

	protected void instantiateOntology(IFile file, StaticOntologyAPI ontology) {
		// Read the annotations and add them to the ontology
		ArrayList<String> requirements = RQSHelpers.getRequirements(file);
		ArrayList<String> annotations = RQSHelpers.getAnnotations(file);

		for (int i = 1; i < requirements.size() + 1; i++) {
			// Add a new requirement
			String reqId = "FR" + i;
			ontology.addRequirement(reqId, requirements.get(i - 1));

			HashMap<String, String> idmap = new HashMap<String, String>();
			for (String annotation : annotations) {
				if (annotation.startsWith(i + ":")) {
					String Id = annotation.split("\t")[0];
					String type = Id.split(":")[1].substring(0, 1);
					if (type.equals("T")) {
						// Add a class instance in the ontology
						String anntype = annotation.split("\t")[1].split(" ")[0];
						String annword = annotation.split("\t")[2];
						if (anntype.equals("Actor")) {
							ontology.addActor(annword);
							ontology.connectRequirementToConcept(reqId, annword);
						} else if (anntype.equals("Action")) {
							ontology.addAction(annword);
							ontology.connectRequirementToOperation(reqId, annword);
						} else if (anntype.equals("Object")) {
							ontology.addObject(annword);
							ontology.connectRequirementToConcept(reqId, annword);
						} else if (anntype.equals("Property")) {
							ontology.addProperty(annword);
							ontology.connectRequirementToConcept(reqId, annword);
						}
						idmap.put(Id, annword);
					}
				}
			}
			for (String annotation : annotations) {
				if (annotation.startsWith(i + ":")) {
					String Id = annotation.split("\t")[0];
					String type = Id.split(":")[1].substring(0, 1);
					if (type.equals("R")) {
						// Add a property in the ontology
						String anninfo = annotation.split("\t")[1];
						String anntype = anninfo.split(" ")[0];
						String tann1 = anninfo.split(" ")[1];
						String tann2 = anninfo.split(" ")[2];
						String entity1 = idmap.get(tann1);
						String entity2 = idmap.get(tann2);
						if (entity1 != null && entity2 != null) {
							if (anntype.equals("IsActorOf")) {
								ontology.connectActorToAction(entity1, entity2);
							} else if (anntype.equals("ActsOn")) {
								ontology.connectActionToObject(entity1, entity2);
							} else if (anntype.equals("HasProperty")) {
								ontology.connectElementToProperty(entity1, entity2);
							} else if (anntype.equals("RelatesTo")) {
								ontology.connectObjectToObject(entity1, entity2);
							}
						}
					}
				}
			}
		}
	}

}
