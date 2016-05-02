package org.pustefixframework.ide.eclipse.plugin.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import org.pustefixframework.ide.eclipse.plugin.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store=Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.PREF_IWRPSRCDIR, "src/main/java");
		store.setDefault(PreferenceConstants.PREF_IWRPTARGETDIR, "target/generated-sources/iwrappers");
		store.setDefault(PreferenceConstants.PREF_SCODESRCDIR_WEBAPP, "src/main/webapp/dyntxt");
		store.setDefault(PreferenceConstants.PREF_SCODESRCDIR_MODULE, "src/main/resources/PUSTEFIX-INF");
        store.setDefault(PreferenceConstants.PREF_SCODETARGETDIR, "target/generated-sources/statuscodes");
		store.setDefault(PreferenceConstants.PREF_GENERATEIWRAPPERS, true);
		store.setDefault(PreferenceConstants.PREF_GENERATESTATUSCODES, true);
	}

}
