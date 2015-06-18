package eu.scasefp7.eclipse.reqeditor.wizards;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.internal.resources.File;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.wizards.datatransfer.DataTransferMessages;
import org.eclipse.ui.internal.wizards.datatransfer.FileSystemExportOperation;
import org.eclipse.ui.internal.wizards.datatransfer.IDataTransferHelpContextIds;
import org.eclipse.ui.internal.wizards.datatransfer.WizardFileSystemResourceExportPage1;

import eu.scasefp7.eclipse.reqeditor.helpers.MyFileSystemExporter;

/**
 * The "Export" wizard page that allows exporting rqs files to txt and ann format.
 * 
 * @author themis
 */
@SuppressWarnings("restriction")
public class ExportTxtAnnWizardPage extends WizardFileSystemResourceExportPage1 {

	/**
	 * A button denoting if a txt file must be exported.
	 */
	private Button exportTxtFile;

	/**
	 * A button denoting if an ann file must be exported.
	 */
	private Button exportAnnFile;

	/**
	 * Constructor for this page.
	 * 
	 * @param selection the current selection.
	 */
	public ExportTxtAnnWizardPage(IStructuredSelection selection) {
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

		Group optionsGroup = new Group(composite, SWT.NONE);
		GridLayout layout = new GridLayout();
		optionsGroup.setLayout(layout);
		optionsGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		optionsGroup.setText("Select export format");
		optionsGroup.setFont(composite.getFont());
		exportTxtFile = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
		exportTxtFile.setText("Export requirements in txt format");
		exportTxtFile.setFont(optionsGroup.getFont());
		exportTxtFile.setSelection(true);
		exportAnnFile = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
		exportAnnFile.setText("Export annotations in ann format");
		exportAnnFile.setFont(optionsGroup.getFont());
		exportAnnFile.setSelection(true);

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
	 * The Finish button was pressed. Export the selected rqs files in txt and ann format.
	 * 
	 * @return {@code true} if rqs files are exported correctly, {@code false} otherwise.
	 */
	@Override
	public boolean finish() {
		@SuppressWarnings("unchecked")
		List<File> resourcesToExport = getWhiteCheckedResources();
		if (!ensureTargetIsValid(new java.io.File(getDestinationValue()))) {
			return false;
		}
		boolean exportTxt = exportTxtFile.getSelection();
		boolean exportAnn = exportAnnFile.getSelection();
		if (!exportTxt && !exportAnn) {
			displayErrorDialog("Please select at least one format (txt or ann)");
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

		// Use reflection to change the {@link FileSystemExporter} object of {@link FileSystemExportOperation} with an
		// object of type {@link MyFileSystemExporter}.
		try {
			Field exporter = FileSystemExportOperation.class.getDeclaredField("exporter");
			exporter.setAccessible(true);
			exporter.set(op, new MyFileSystemExporter(exportTxt, exportAnn));
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}

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