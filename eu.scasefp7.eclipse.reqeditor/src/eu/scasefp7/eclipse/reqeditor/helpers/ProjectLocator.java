package eu.scasefp7.eclipse.reqeditor.helpers;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * A helper class containing functions for finding the current project given a selection.
 * 
 * @author themis
 */
public class ProjectLocator {

	/**
	 * Returns the project that the selected resource(s) belong to.
	 * 
	 * @param selection the selected resource(s).
	 * @return the project that the selected resource(s) belong to.
	 */
	@SuppressWarnings("unchecked")
	public static IProject getProjectOfSelectionList(IStructuredSelection selection) {
		return getProjectOfSelectionList(selection.toList());
	}

	/**
	 * Returns the project that the selected resource(s) belong to.
	 * 
	 * @param selectionList the selected resource(s).
	 * @return the project that the selected resource(s) belong to.
	 */
	public static IProject getProjectOfSelectionList(List<Object> selectionList) {
		IProject project = null;
		for (Object object : selectionList) {
			IResource resource = (IResource) Platform.getAdapterManager().getAdapter(object, IResource.class);
			IProject theproject = null;
			if (resource != null) {
				theproject = resource.getProject();
			} else {
				theproject = (IProject) Platform.getAdapterManager().getAdapter(object, IProject.class);
			}
			if (theproject != null) {
				if (project == null) {
					project = theproject;
				} else {
					if (!project.equals(theproject)) {
						return null;
					}
				}
			}
		}
		return project;
	}
}
