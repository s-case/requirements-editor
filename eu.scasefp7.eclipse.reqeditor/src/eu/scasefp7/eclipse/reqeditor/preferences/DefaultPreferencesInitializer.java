package eu.scasefp7.eclipse.reqeditor.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import eu.scasefp7.eclipse.reqeditor.Activator;

/**
 * Provides the default preferences for the requirements editor.
 * 
 * @author themis
 */
public class DefaultPreferencesInitializer extends AbstractPreferenceInitializer {

	/**
	 * Initializes this object.
	 */
	public DefaultPreferencesInitializer() {
	}

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault("Entity", "0,0,255");
		store.setDefault("EntityText", "0,255,255");
		store.setDefault("IsActorOf", "0,0,255");
		store.setDefault("ActsOn", "0,0,255");
		store.setDefault("HasProperty", "0,0,255");
		store.setDefault("RelatesTo", "0,0,255");
	}
}
