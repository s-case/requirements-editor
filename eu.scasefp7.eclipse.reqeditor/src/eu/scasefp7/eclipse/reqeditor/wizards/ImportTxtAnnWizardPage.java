package eu.scasefp7.eclipse.reqeditor.wizards;

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
import java.util.HashMap;
import java.util.HashSet;
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
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.FileSystemElement;
import org.eclipse.ui.internal.wizards.datatransfer.WizardFileSystemResourceImportPage1;

import eu.scasefp7.eclipse.reqeditor.Activator;
import eu.scasefp7.eclipse.reqeditor.helpers.RQStoANNHelpers;

/**
 * The "Import" wizard page that allows importing rqs files from txt and ann format.
 * 
 * @author themis
 */
@SuppressWarnings("restriction")
public class ImportTxtAnnWizardPage extends WizardFileSystemResourceImportPage1 {

	/**
	 * Constructor for this page.
	 * 
	 * @param workbench the current workbench.
	 * @param selection the current selection.
	 */
	public ImportTxtAnnWizardPage(IWorkbench workbench, IStructuredSelection selection) {
		super(workbench, selection);
		setTitle("Requirements Editor Import Wizard");
		setDescription("Select your txt/ann files to import");
	}

	/**
	 * Reads the contents of a {@link File} to a {@link String}.
	 * 
	 * @param file the instance of {@link File} to be read.
	 * @return a {@link String} containing the contents of the file.
	 */
	private String readFiletoString(File file) {
		ArrayList<String> datalines = new ArrayList<String>();
		BufferedReader brlocal;
		try {
			brlocal = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			String line;
			while ((line = brlocal.readLine()) != null) {
				datalines.add(line);
			}
			brlocal.close();
		} catch (FileNotFoundException e) {
			Activator.log("Error when reading a txt/ann from the file system to import it", e);
		} catch (IOException e) {
			Activator.log("Error when reading a txt/ann from the file system to import it", e);
		}
		String filedata = "";
		for (String dataline : datalines) {
			filedata += dataline + "\n";
		}
		return filedata;
	}

	/**
	 * The Finish button was pressed. Create rqs files according to the imported txt and ann files.
	 * 
	 * @return {@code true} if rqs files are created correctly, {@code false} otherwise.
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
			String extension = filename.substring(i + 1);
			if (i <= 0 || !(extension.equals("txt") || extension.equals("ann"))) {
				setErrorMessage("All files imported must have the txt or ann extension!");
				return false;
			}
			fileSystemObjects.add(fileSystemObject);
		}
		HashSet<String> hasTxt = new HashSet<String>();
		HashSet<String> hasAnn = new HashSet<String>();
		for (File fileSystemObject : fileSystemObjects) {
			String filename = fileSystemObject.getName();
			int i = filename.lastIndexOf('.');
			String name = filename.substring(0, i);
			String extension = filename.substring(i + 1);
			if (extension.equals("txt"))
				hasTxt.add(name);
			else if (extension.equals("ann"))
				hasAnn.add(name);
		}
		for (File fileSystemObject : fileSystemObjects) {
			String filename = fileSystemObject.getName();
			String name = filename.substring(0, filename.lastIndexOf('.'));
			if (hasAnn.contains(name) && !hasTxt.contains(name)) {
				setErrorMessage("You cannot import an ann file without the corresponding txt!");
				return false;
			}
		}

		HashMap<String, TxtAndAnnFile> newFileSystemObjects = new HashMap<String, TxtAndAnnFile>();
		for (File fileSystemObject : fileSystemObjects) {
			String filename = fileSystemObject.getName();
			String name = filename.substring(0, filename.lastIndexOf('.'));
			if (!newFileSystemObjects.containsKey(name))
				newFileSystemObjects.put(name, new TxtAndAnnFile());
			newFileSystemObjects.get(name).addFile(fileSystemObject);
		}

		for (final TxtAndAnnFile txtAndAnnFile : newFileSystemObjects.values()) {

			IRunnableWithProgress op = new WorkspaceModifyOperation(null) {

				protected void execute(IProgressMonitor monitor) throws CoreException, InterruptedException {
					String fileName = txtAndAnnFile.getName() + ".rqs";

					String filedata;
					if (txtAndAnnFile.hasAnnFile()) {
						filedata = RQStoANNHelpers.transformTXTandANNtoRQS(readFiletoString(txtAndAnnFile.txtFile),
								readFiletoString(txtAndAnnFile.annFile));
					} else {
						filedata = "REQUIREMENTS\n------------\n";
						filedata += readFiletoString(txtAndAnnFile.txtFile);
						filedata += "------------\n\nANNOTATIONS\n------------\n";
						filedata += "------------\n";
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
				Activator.log("Error importing a txt and/or an ann file", e);
				return false;
			} catch (InvocationTargetException e) {
				Activator.log("Error importing a txt and/or an ann file", e);
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

	/**
	 * Contains a txt and an ann file.
	 */
	class TxtAndAnnFile {

		/**
		 * The txt file.
		 */
		public File txtFile;

		/**
		 * The ann file.
		 */
		public File annFile;

		/**
		 * Initializes both file to null.
		 */
		public TxtAndAnnFile() {
			txtFile = null;
			annFile = null;
		}

		/**
		 * Adds a new file as a txt or as an ann file according to its extension.
		 * 
		 * @param file the new file to be added
		 */
		public void addFile(File file) {
			String filename = file.getName();
			String extension = filename.substring(filename.lastIndexOf('.') + 1);
			if (extension.equals("txt"))
				txtFile = file;
			else if (extension.equals("ann"))
				annFile = file;
		}

		/**
		 * Checks if this object has an ann file.
		 * 
		 * @return {@code true} if there is an ann file, {@code false} otherwise.
		 */
		public boolean hasAnnFile() {
			return annFile != null;
		}

		/**
		 * Returns the name of the txt file without extension.
		 * 
		 * @return the name of the file without extension.
		 */
		public String getName() {
			if (txtFile != null) {
				String filename = txtFile.getName();
				return filename.substring(0, filename.lastIndexOf('.'));
			} else {
				return null;
			}
		}
	}
}
