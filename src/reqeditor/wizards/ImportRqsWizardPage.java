package reqeditor.wizards;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.FileSystemElement;
import org.eclipse.ui.internal.wizards.datatransfer.WizardFileSystemResourceImportPage1;

/**
 * The "Import" wizard page that allows importing rqs files.
 * 
 * @author themis
 */
@SuppressWarnings("restriction")
public class ImportRqsWizardPage extends WizardFileSystemResourceImportPage1 {

	/**
	 * Constructor for this page.
	 * 
	 * @param workbench the current workbench.
	 * @param selection the current selection.
	 */
	public ImportRqsWizardPage(IWorkbench workbench, IStructuredSelection selection) {
		super(workbench, selection);
		setTitle("Requirements Editor Import Wizard");
		setDescription("Select your requirements file to import");
	}

	/**
	 * The Finish button was pressed. Import the selected rqs files.
	 * 
	 * @return {@code true} if rqs files are imported correctly, {@code false} otherwise.
	 */
	public boolean finish() {
		if (!ensureSourceIsValid()) {
			return false;
		}

		saveWidgetValues();

		@SuppressWarnings("unchecked")
		Iterator<FileSystemElement> resourcesEnum = getSelectedResources().iterator();
		ArrayList<File> fileSystemObjects = new ArrayList<File>();
		while (resourcesEnum.hasNext()) {
			File fileSystemObject = (File) resourcesEnum.next().getFileSystemObject();
			String filename = fileSystemObject.getName();
			int i = filename.lastIndexOf('.');
			if (i <= 0 || !filename.substring(i + 1).equals("rqs")) {
				setErrorMessage("All files imported must have the rqs extension!");
				return false;
			}
			fileSystemObjects.add(fileSystemObject);
		}
		for (final File file : fileSystemObjects) {

			IRunnableWithProgress op = new WorkspaceModifyOperation(null) {

				protected void execute(IProgressMonitor monitor) throws CoreException, InterruptedException {

					String fileName = file.getName();

					ArrayList<String> datalines = new ArrayList<String>();
					BufferedReader brlocal;
					try {
						brlocal = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
						String line;
						while ((line = brlocal.readLine()) != null) {
							datalines.add(line);
						}
						brlocal.close();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					String filedata = "";
					for (String dataline : datalines) {
						filedata += dataline + "\n";
					}
					InputStream stream = new ByteArrayInputStream(filedata.getBytes(StandardCharsets.UTF_8));
					IPath resourcePath = getResourcePath();
					IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
					IProject project = root.getProject(resourcePath.toString());
					project.getFile(fileName).create(stream, true, monitor);
				}
			};
			try {
				getContainer().run(false, true, op);
			} catch (InterruptedException e) {
				return false;
			} catch (InvocationTargetException e) {
				if (e.getTargetException() instanceof CoreException) {
					ErrorDialog.openError(getContainer().getShell(), "Error in file creation", null,
							((CoreException) e.getTargetException()).getStatus());
					System.out.println("Error in file creation");
				} else {
					System.out.println("Error creating file");
				}
				return false;
			}
		}
		return true;
	}

	/**
	 * Empty fuction used to disable the options for simplicity.
	 */
	@Override
	protected void createOptionsGroup(Composite parent) {

	}

	/**
	 * Creates the import source specification controls.
	 *
	 * @param parent the parent control.
	 */
	@Override
	protected void createSourceGroup(Composite parent) {
		createRootDirectoryGroup(parent);
		createFileSelectionGroup(parent);
		Composite buttonComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.makeColumnsEqualWidth = true;
		buttonComposite.setLayout(layout);
		buttonComposite.setFont(parent.getFont());
		GridData buttonData = new GridData(SWT.FILL, SWT.FILL, true, false);
		buttonComposite.setLayoutData(buttonData);
	}

	/**
	 * Empty fuction used to avoid enabling the non-implemented button group.
	 */
	@Override
	protected void enableButtonGroup(boolean enable) {

	}

}
