package eu.scasefp7.eclipse.reqeditor.preferences;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import eu.scasefp7.eclipse.reqeditor.Activator;

/**
 * A color preference page used to set the colors of the requirements editor.
 * 
 * @author themis
 */
public class ColorPreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	/**
	 * Initializes this object.
	 */
	public ColorPreferencesPage() {
		super(GRID);
	}

	@Override
	public void createFieldEditors() {
		addField(new ColorFieldEditor("Entity", "Entity Annotation Color", getFieldEditorParent()));
		addField(new ColorFieldEditor("EntityText", "Entity Text Highlight Color", getFieldEditorParent()));
		addField(new ColorFieldEditor("IsActorOf", "IsActorOf Annotation Color", getFieldEditorParent()));
		addField(new ColorFieldEditor("ActsOn", "ActsOn Annotation Color", getFieldEditorParent()));
		addField(new ColorFieldEditor("HasProperty", "HasProperty Annotation Color", getFieldEditorParent()));
		addField(new ColorFieldEditor("RelatesTo", "RelatesTo Annotation Color", getFieldEditorParent()));
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Set the colors of Requirements Editor");
	}

}
