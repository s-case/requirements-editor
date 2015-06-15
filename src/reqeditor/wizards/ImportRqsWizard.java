package reqeditor.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

import reqeditor.Activator;

/**
 * A wizard class for importing an rqs file.
 * 
 * @author themis
 */
public class ImportRqsWizard extends Wizard implements IImportWizard {

	/**
	 * The current workbench.
	 */
	private IWorkbench workbench;

	/**
	 * The current selection.
	 */
	private IStructuredSelection selection;

	/**
	 * The page of this wizard.
	 */
	private ImportRqsWizardPage page;

	/**
	 * Initializes this import wizard using the passed workbench and object
	 * selection.
	 *
	 * @param workbench the current workbench.
	 * @param selection the current object selection.
	 */
	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		this.workbench = workbench;
		this.selection = currentSelection;
		setWindowTitle("Import requirements in rqs format");
		setDefaultPageImageDescriptor(Activator.getImageDescriptor("icons/sample.gif")); //$NON-NLS-1$
		setNeedsProgressMonitor(true);
	}

	/**
	 * Adding the page to the wizard.
	 */
	public void addPages() {
		page = new ImportRqsWizardPage(workbench, selection); //$NON-NLS-1$ //$NON-NLS-2$
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in the wizard. We
	 * call the {@link ImportRqsWizardPage#finish()} function.
	 *
	 * @return {@code true} if it is executed correctly, {@code false} otherwise.
	 */
	public boolean performFinish() {
		return page.finish();
	}

}
