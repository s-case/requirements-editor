package reqeditor.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

import reqeditor.Activator;

/**
 * A wizard class for exporting an rqs file into txt and ann files.
 * 
 * @author themis
 */
public class ExportTxtAnnWizard extends Wizard implements IExportWizard {

	/**
	 * The current selection.
	 */
	private IStructuredSelection selection;

	/**
	 * The page of this wizard.
	 */
	private ExportTxtAnnWizardPage page;

	/**
	 * Initializes this export wizard using the passed workbench and object
	 * selection.
	 *
	 * @param workbench the current workbench.
	 * @param selection the current object selection.
	 */
	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		this.selection = currentSelection;
		setWindowTitle("Export requirements in txt/ann format");
		setDefaultPageImageDescriptor(Activator.getImageDescriptor("icons/sample.gif")); //$NON-NLS-1$
		setNeedsProgressMonitor(true);
	}

	/**
	 * Adding the page to the wizard.
	 */
	public void addPages() {
		page = new ExportTxtAnnWizardPage(selection); //$NON-NLS-1$ //$NON-NLS-2$
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in the wizard. We
	 * call the {@link ExportTxtAnnWizardPage#finish()} function.
	 *
	 * @return {@code true} if it is executed correctly, {@code false} otherwise.
	 */
	public boolean performFinish() {
		return page.finish();
	}
}
