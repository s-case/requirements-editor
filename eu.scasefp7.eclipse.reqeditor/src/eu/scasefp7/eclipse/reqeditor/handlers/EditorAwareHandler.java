package eu.scasefp7.eclipse.reqeditor.handlers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import eu.scasefp7.eclipse.reqeditor.editors.MyReqEditor;
import eu.scasefp7.eclipse.reqeditor.helpers.MyProgressMonitor;

/**
 * A command handler that also checks for any open editors.
 * 
 * @author themis
 */
public abstract class EditorAwareHandler extends AbstractHandler {

	/**
	 * Writes a string to a file system resource.
	 * 
	 * @param string the string to be written.
	 * @param file the files system resource.
	 */
	protected void writeStringToFile(String string, IFile file) {
		InputStream stream = new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
		try {
			file.setContents(stream, IFile.FORCE, new MyProgressMonitor());
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Updates the editor of the given file if it is open in the annotations editor tab.
	 * 
	 * @param file the file of which the open editor is updated.
	 */
	protected void updateEditor(IFile file) {
		IEditorPart geditor = findEditorFor(file);
		if (geditor instanceof MyReqEditor) {
			MyReqEditor editor = (MyReqEditor) geditor;
			int activePage = editor.getActivePage();
			if (activePage == 1)
				editor.setActivePage(1);
		}
	}

	/**
	 * Checks whether a file system resource is open in an editor and returns it.
	 * 
	 * @param file the file system resource.
	 * @return the editor in which the resource is open, or {@code null} if the resource is not open in any editor.
	 */
	private IEditorPart findEditorFor(IFile file) {
		IWorkbench workbench = PlatformUI.getWorkbench();

		IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();

		for (int i = 0; i < windows.length; i++) {
			IWorkbenchPage[] pages = windows[i].getPages();
			for (int x = 0; x < pages.length; x++) {

				IEditorReference[] editors = pages[x].getEditorReferences();

				for (int z = 0; z < editors.length; z++) {

					IEditorReference ref = editors[z];
					IEditorPart editor = ref.getEditor(true);

					if (editor == null) {
						continue;
					}

					IEditorInput input = editor.getEditorInput();
					IFile editorFile = (IFile) input.getAdapter(IFile.class);

					if (editorFile != null && editorFile.equals(file))
						return editor;

				}
			}
		}
		return null;
	}
}
