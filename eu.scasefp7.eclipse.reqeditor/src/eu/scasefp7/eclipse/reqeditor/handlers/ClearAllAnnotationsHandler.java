package eu.scasefp7.eclipse.reqeditor.handlers;

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import eu.scasefp7.eclipse.reqeditor.helpers.RQSHelpers;

/**
 * A command handler for deleting all the annotations of a file.
 * 
 * @author themis
 */
public class ClearAllAnnotationsHandler extends EditorAwareHandler {

	/**
	 * This function is called when the user selects the menu item. It reads the selected resource(s) and deletes all
	 * the annotations.
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
					// Clear the new annotations and update any open editors
					String[] txtAndAnn = RQSHelpers.getRequirementsAndAnnotationsStrings(file);
					String ntext = "REQUIREMENTS\n------------\n";
					ntext += txtAndAnn[0];
					ntext += "------------\n\nANNOTATIONS\n------------\n";
					ntext += "------------\n";
					writeStringToFile(ntext, file);
					updateEditor(file);
				}
			}
		}
		return null;
	}
}
