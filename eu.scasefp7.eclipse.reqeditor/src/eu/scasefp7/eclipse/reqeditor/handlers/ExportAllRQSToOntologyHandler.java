package eu.scasefp7.eclipse.reqeditor.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import eu.scasefp7.eclipse.core.ontology.StaticOntologyAPI;

/**
 * A command handler for exporting all the annotated instances of all RQS files to the static ontology.
 * 
 * @author themis
 */
public class ExportAllRQSToOntologyHandler extends ExportToOntologyHandler {

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
			IProject project = getProjectOfSelectionList(selectionList);
			ArrayList<IFile> files = getFilesOfProject(project, "rqs");
			StaticOntologyAPI ontology = new StaticOntologyAPI(project, true);
			// Iterate over the selected files
			for (IFile file : files) {
				if (file != null) {
					instantiateOntology(file, ontology);
				}
			}
			ontology.close();
		}
		return null;
	}

}
