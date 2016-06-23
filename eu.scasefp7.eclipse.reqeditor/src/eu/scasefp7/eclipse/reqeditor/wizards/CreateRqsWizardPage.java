package eu.scasefp7.eclipse.reqeditor.wizards;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

import eu.scasefp7.eclipse.reqeditor.Activator;
import eu.scasefp7.eclipse.reqeditor.helpers.ProjectLocator;

/**
 * The "New" wizard page that allows setting the container for the new file as well as the file name.
 * 
 * @author themis
 */
public class CreateRqsWizardPage extends WizardPage {

	/**
	 * The text object of the container (project).
	 */
	private Text containerText;

	/**
	 * The text object of the name of the file.
	 */
	private Text fileText;

	/**
	 * The current selection.
	 */
	private ISelection selection;

	/**
	 * Constructor for this page.
	 * 
	 * @param selection the current selection
	 */
	public CreateRqsWizardPage(ISelection selection) {
		super("wizardPage");
		setTitle("Requirements Editor New Wizard");
		setDescription("Create a new rqs file");
		this.selection = selection;
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		Label label = new Label(container, SWT.NULL);
		label.setText("&Container:");

		containerText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		containerText.setLayoutData(gd);
		containerText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		Button button = new Button(container, SWT.PUSH);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});
		label = new Label(container, SWT.NULL);
		label.setText("&File name:");

		fileText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fileText.setLayoutData(gd);
		fileText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		initialize();
		dialogChanged();
		setControl(container);
	}

	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */
	private void initialize() {
		if (selection != null && selection.isEmpty() == false && selection instanceof IStructuredSelection) {
			IProject project = ProjectLocator.getProjectOfSelectionList((IStructuredSelection) selection);
			String requirementsFolderLocation = null;
			try {
				requirementsFolderLocation = project.getPersistentProperty(new QualifiedName("",
						"eu.scasefp7.eclipse.core.ui.rqsFolder"));
			} catch (CoreException e) {
				Activator.log("Error retrieving project property (requirements folder location)", e);
			}
			IContainer container = project;
			if (requirementsFolderLocation != null) {
			    IResource requirementsFolder = project.findMember(new Path(requirementsFolderLocation)); 
				if (requirementsFolder != null && requirementsFolder.exists())
					container = (IContainer) requirementsFolder;
			}
			containerText.setText(container.getFullPath().toString());
		}
		fileText.setText("new_file.rqs");
	}

	/**
	 * Uses the standard container selection dialog to choose the new value for
	 * the container field.
	 */
	private void handleBrowse() {
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(), ResourcesPlugin.getWorkspace()
				.getRoot(), false, "Select new file container");
		if (dialog.open() == ContainerSelectionDialog.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				containerText.setText(((Path) result[0]).toString());
			}
		}
	}

	/**
	 * Ensures that both text fields are set.
	 */
	private void dialogChanged() {
		IResource container = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(getContainerName()));
		String fileName = getFileName();

		if (getContainerName().length() == 0) {
			setErrorMessage("File container must be specified");
			setPageComplete(false);
			return;
		}
		if (container == null || (container.getType() & (IResource.PROJECT | IResource.FOLDER)) == 0) {
			setErrorMessage("File container must exist");
			setPageComplete(false);
			return;
		}
		if (!container.isAccessible()) {
			setErrorMessage("Project must be writable");
			setPageComplete(false);
			return;
		}
		if (fileName.length() == 0) {
			setErrorMessage("File name must be specified");
			setPageComplete(false);
			return;
		}
		if (fileName.replace('\\', '/').indexOf('/', 1) > 0) {
			setErrorMessage("File name must be valid");
			setPageComplete(false);
			return;
		}
		if (((IContainer) container).getFile(new Path(fileName)).exists()) {
			setErrorMessage("File name already exists");
			setPageComplete(false);
			return;
		}
		;
		int dotLoc = fileName.lastIndexOf('.');
		if (dotLoc != -1) {
			String ext = fileName.substring(dotLoc + 1);
			if (ext.equalsIgnoreCase("rqs") == false) {
				setErrorMessage("File extension must be \"rqs\"");
				setPageComplete(false);
				return;
			}
		} else {
			setErrorMessage("File extension must be \"rqs\"");
			setPageComplete(false);
			return;
		}
		setErrorMessage(null);
		setPageComplete(true);
	}

	/**
	 * Returns the name of the container (project).
	 * 
	 * @return the name of the container (project).
	 */
	public String getContainerName() {
		return containerText.getText();
	}

	/**
	 * Returns the name of the new file.
	 * 
	 * @return the name of the new file.
	 */
	public String getFileName() {
		return fileText.getText();
	}
}