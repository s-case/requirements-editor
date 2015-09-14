package eu.scasefp7.eclipse.reqeditor.editors;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.*;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.IDE;

import eu.scasefp7.eclipse.reqeditor.ui.RequirementsReader;
import eu.scasefp7.eclipse.reqeditor.ui.annotationseditor.AnnotatedTextWithActions;
import eu.scasefp7.eclipse.reqeditor.ui.requirementseditor.RequirementsTextEditor;

/**
 * The multi-page editor for this Eclipse plugin.
 * 
 * @author themis
 */
public class MyReqEditor extends MultiPageEditorPart implements IResourceChangeListener {

	/**
	 * The text editor that lies within this multi-page editor. It is not shown to the user.
	 */
	private TextEditor editor;

	/**
	 * A text editor that allows adding, removing, and modifying requirements.
	 */
	private RequirementsTextEditor requirementsEditor;

	/**
	 * The UI editor that allows adding, removing, and modifying annotations.
	 */
	private AnnotatedTextWithActions annotationsEditor;

	/**
	 * The rqs reader that handles the requirements and the annotations.
	 */
	private RequirementsReader reader;

	/**
	 * Initializes this object and creates the {@code editor} and the {@code reader}.
	 */
	public MyReqEditor() {
		super();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
		editor = new TextEditor();
		reader = new RequirementsReader(editor);
	}

	/**
	 * Creates the editor that is used to modify the file. Note that this editor is NOT shown anywhere. By convention,
	 * page 0 is selected as a page that is not shown.
	 */
	void createPage0() {
		try {
			// int index = addPage(editor, getEditorInput());
			// setPageText(index, editor.getTitle());
			IEditorSite site = createSite(editor);
			editor.init(site, getEditorInput());
			setPartName(getEditorInput().getName());
		} catch (PartInitException e) {
			ErrorDialog.openError(getSite().getShell(), "Error creating nested text editor", null, e.getStatus());
		}
	}

	/**
	 * Creates the page for the requirements editor and sets the events for modifying the content of the editor.
	 */
	void createPage1() {
		Composite composite = new Composite(getContainer(), SWT.NONE);
		FillLayout layout = new FillLayout();
		composite.setLayout(layout);
		requirementsEditor = new RequirementsTextEditor(composite, SWT.H_SCROLL | SWT.V_SCROLL);
		int index = addPage(composite);
		setPageText(index, "Requirements");
	}

	/**
	 * Creates the page for the UI annotations editor.
	 */
	void createPage2() {
		Composite composite = new Composite(getContainer(), SWT.NONE);
		FillLayout layout = new FillLayout();
		composite.setLayout(layout);
		annotationsEditor = new AnnotatedTextWithActions(composite, SWT.H_SCROLL | SWT.V_SCROLL);
		annotationsEditor.setEditable(false);
		int index = addPage(composite);
		setPageText(index, "Annotated Requirements");
	}

	/**
	 * Creates the pages of this multi-page editor.
	 */
	protected void createPages() {
		createPage0();
		createPage1();
		createPage2();
	}

	/**
	 * Disposes all editors.
	 */
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
	}

	/**
	 * Saves the multi-page editor's document.
	 */
	public void doSave(IProgressMonitor monitor) {
		editor.doSave(monitor);
	}

	/**
	 * Saves the multi-page editor's document as another file.
	 */
	public void doSaveAs() {
		editor.doSaveAs();
	}

	/**
	 * Sets the active page given its index.
	 * 
	 * @param pageIndex the index of the page to be activated.
	 */
	public void setActivePage(int pageIndex) {
		super.setActivePage(pageIndex);
	}

	/*
	 * (non-Javadoc) Method declared on IEditorPart
	 */
	public void gotoMarker(IMarker marker) {
		setActivePage(0);
		IDE.gotoMarker(getEditor(0), marker);
	}

	/**
	 * Checks that the input is an instance of {@code IFileEditorInput}.
	 * 
	 * @param site the site for which this part is being created.
	 * @param input the input on which this editor should be created.
	 * @throws PartInitException
	 */
	public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
		if (!(editorInput instanceof IFileEditorInput))
			throw new PartInitException("Invalid Input: Must be IFileEditorInput");
		super.init(site, editorInput);
	}

	/*
	 * (non-Javadoc) Method declared on IEditorPart.
	 */
	public boolean isSaveAsAllowed() {
		return true;
	}

	/**
	 * Calculates the contents of the page that is activated.
	 * 
	 * @param pageIndex the index of the page that is activated.
	 */
	protected void pageChange(int newPageIndex) {
		super.pageChange(newPageIndex);
		reader.parseFromEditor();
		if (newPageIndex == 0) {
			requirementsEditor.setText(reader);
			// requirementsEditor.setEditable(!reader.hasAnnotations());
		}
		if (newPageIndex == 1) {
			annotationsEditor.setTextAndAnnotations(reader);
		}
	}

	/**
	 * Closes all project files on project close.
	 * 
	 * @param event an event denoting the resource has changed.
	 */
	public void resourceChanged(final IResourceChangeEvent event) {
		if (event.getType() == IResourceChangeEvent.PRE_CLOSE || event.getType() == IResourceChangeEvent.PRE_DELETE) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
					for (int i = 0; i < pages.length; i++) {
						if (((FileEditorInput) editor.getEditorInput()).getFile().getProject()
								.equals(event.getResource())) {
							IEditorPart editorPart = pages[i].findEditor(editor.getEditorInput());
							pages[i].closeEditor(editorPart, true);
						}
					}
				}
			});
		}
		else if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
			IResourceDelta delta = event.getDelta();
			if (delta != null) {
				delta = delta.findMember(((FileEditorInput) editor.getEditorInput()).getFile().getFullPath());
				if (delta != null && delta.getKind() == IResourceDelta.REMOVED) {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
							for (int i = 0; i < pages.length; i++) {
								IEditorPart editorPart = pages[i].findEditor(editor.getEditorInput());
								pages[i].closeEditor(editorPart, true);
							}
						}
					});
				}
			}
		}
	}

}
