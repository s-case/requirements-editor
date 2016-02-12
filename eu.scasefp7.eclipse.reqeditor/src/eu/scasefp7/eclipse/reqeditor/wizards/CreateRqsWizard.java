package eu.scasefp7.eclipse.reqeditor.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.*;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;

import java.io.*;

import org.eclipse.ui.*;
import org.eclipse.ui.ide.IDE;

import eu.scasefp7.eclipse.reqeditor.Activator;

/**
 * A wizard class for creating a new rqs file.
 * 
 * @author themis
 */
public class CreateRqsWizard extends Wizard implements INewWizard {

	/**
	 * The page of this wizard.
	 */
	private CreateRqsWizardPage page;

	/**
	 * The current selection.
	 */
	private ISelection selection;

	/**
	 * The constructor of this wizard.
	 */
	public CreateRqsWizard() {
		super();
	}

	/**
	 * Adding the page to the wizard.
	 */
	public void addPages() {
		page = new CreateRqsWizardPage(selection);
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in the wizard. We will create an operation and run it using
	 * wizard as execution context.
	 *
	 * @return {@code true} if everything is executed correctly, {@code false} otherwise.
	 */
	public boolean performFinish() {
		final String containerName = page.getContainerName();
		final String fileName = page.getFileName();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(containerName, fileName, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			Activator.log("Error creating or opening of a new rqs file", e);
			return false;
		} catch (InvocationTargetException e) {
			Activator.log("Error creating or opening of a new rqs file", e);
			return false;
		}
		return true;
	}

	/**
	 * The worker method. It will find the container, create the file if missing
	 * or just replace its contents, and open the editor on the newly created
	 * file.
	 * 
	 * @param containerName the container (project).
	 * @param fileName the name of the file.
	 * @param monitor the progress monitor.
	 * @throws CoreException
	 */
	private void doFinish(String containerName, String fileName, IProgressMonitor monitor) throws CoreException {
		// create a sample file
		monitor.beginTask("Creating " + fileName, 2);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throw new CoreException(new Status(IStatus.ERROR, "ReqEditor", IStatus.OK, "Container \"" + containerName
					+ "\" does not exist.", null));
		}
		IContainer container = (IContainer) resource;
		final IFile file = container.getFile(new Path(fileName));
		try {
			InputStream stream = new ByteArrayInputStream("".getBytes("UTF-8"));
			if (file.exists()) {
				file.setContents(stream, true, true, monitor);
			} else {
				file.create(stream, true, monitor);
			}
			stream.close();
		} catch (IOException e) {
			Activator.log("Error creating a new rqs file", e);
		}
		monitor.worked(1);
		monitor.setTaskName("Opening file for editing...");
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(page, file, true);
				} catch (PartInitException e) {
					Activator.log("Error opening the newly created rqs file", e);
				}
			}
		});
		monitor.worked(1);
	}

	/**
	 * Initializes this creation wizard using the passed workbench and object selection.
	 *
	 * @param workbench the current workbench.
	 * @param selection the current object selection.
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
		setWindowTitle("New Requirements Editor file");
		setDefaultPageImageDescriptor(Activator.getImageDescriptor("icons/sample.gif")); //$NON-NLS-1$
		setNeedsProgressMonitor(true);
	}
}