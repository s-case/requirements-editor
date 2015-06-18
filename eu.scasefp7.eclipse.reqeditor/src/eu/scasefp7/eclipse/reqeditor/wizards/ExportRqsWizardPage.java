package eu.scasefp7.eclipse.reqeditor.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.internal.resources.File;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.wizards.datatransfer.DataTransferMessages;
import org.eclipse.ui.internal.wizards.datatransfer.FileSystemExportOperation;
import org.eclipse.ui.internal.wizards.datatransfer.IDataTransferHelpContextIds;
import org.eclipse.ui.internal.wizards.datatransfer.WizardFileSystemResourceExportPage1;

/**
 * The "Export" wizard page that allows exporting rqs files.
 * 
 * @author themis
 */
@SuppressWarnings("restriction")
public class ExportRqsWizardPage extends WizardFileSystemResourceExportPage1 {

	/**
	 * Constructor for this page.
	 * 
	 * @param selection the current selection.
	 */
	public ExportRqsWizardPage(IStructuredSelection selection) {
		super(selection);
		setTitle("Requirements Editor Export Wizard");
		setDescription("Select your requirements file to export");
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
		composite.setFont(parent.getFont());

		createResourcesGroup(composite);
		createDestinationGroup(composite);

		restoreResourceSpecificationWidgetValues(); // ie.- local
		restoreWidgetValues(); // ie.- subclass hook

		updateWidgetEnablements();
		setPageComplete(determinePageCompletion());
		setErrorMessage(null); // should not initially have error message

		setControl(composite);
		giveFocusToDestination();
		PlatformUI.getWorkbench().getHelpSystem()
				.setHelp(getControl(), IDataTransferHelpContextIds.FILE_SYSTEM_EXPORT_WIZARD_PAGE);
	}

	/**
	 * The Finish button was pressed. Export the selected rqs files.
	 * 
	 * @return {@code true} if rqs files are exported correctly, {@code false} otherwise.
	 */
	@Override
	public boolean finish() {
		@SuppressWarnings({ "unchecked" })
		List<File> resourcesToExport = getWhiteCheckedResources();
		if (!ensureTargetIsValid(new java.io.File(getDestinationValue()))) {
			return false;
		}

		for (File file : resourcesToExport) {
			String filename = file.getName();
			int i = filename.lastIndexOf('.');
			if (i <= 0 || !filename.substring(i + 1).equals("rqs")) {
				setErrorMessage("All files exported must have the rqs extension!");
				return false;
			}
		}

		saveDirtyEditors();
		saveWidgetValues();

		FileSystemExportOperation op = new FileSystemExportOperation(null, resourcesToExport, getDestinationValue(),
				this);

		op.setCreateLeadupStructure(false);
		op.setOverwriteFiles(true);

		try {
			getContainer().run(true, true, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			displayErrorDialog(e.getTargetException());
			return false;
		}

		IStatus status = op.getStatus();
		if (!status.isOK()) {
			ErrorDialog.openError(getContainer().getShell(), DataTransferMessages.DataTransfer_exportProblems, null,
					status);
			return false;
		}

		return true;
	}

}
