package org.pustefixframework.ide.eclipse.plugin.ui.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.pustefixframework.ide.eclipse.plugin.Activator;
import org.pustefixframework.ide.eclipse.plugin.ui.util.SettingsStore;

public class GeneralPreferencePage extends BasePreferencePage {
	
	final static String PAGE_ID="org.pustefixframework.ide.eclipse.plugin.preferences.general";
	final static String PROP_PAGE_ID="org.pustefixframework.ide.eclipse.plugin.properties.general";
	
	Button autoAssignButton;
	
	public GeneralPreferencePage() {
		super();
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	public void init(IWorkbench workbench) {
	}
	
	public Control createSettingsContent(Composite parent) {
		
		Composite composite=createDefaultComposite(parent,1);
		
        autoAssignButton=new Button(composite, SWT.CHECK);
        autoAssignButton.setText("&Automatically assign Pustefix nature to new Pustefix projects");
        
		return composite;
	}
	

	@Override
	protected String getPreferencePageId() {
		return PAGE_ID;
	}
	
	@Override
	protected String getProjectPropertyPageId() {
		return PROP_PAGE_ID;
	}
	
	@Override
	protected boolean hasProjectSpecificOptions(IProject project) {
		return false;
	}
	
	public void initValues() {
		SettingsStore store = new SettingsStore(Activator.PLUGIN_ID,null);
		boolean autoAssign=store.getCascadedInstanceValue(PreferenceConstants.PREF_AUTOASSIGNNATURE,false);
        autoAssignButton.setSelection(autoAssign);
	}

	protected void performDefaults() {
		SettingsStore store = new SettingsStore(Activator.PLUGIN_ID,null);
		boolean autoAssign=store.getDefaultValue(PreferenceConstants.PREF_AUTOASSIGNNATURE,false);
        autoAssignButton.setSelection(autoAssign);
	}
	
	public boolean performOk() {
		SettingsStore store=new SettingsStore(Activator.PLUGIN_ID,null);
		store.setInstanceValue(PreferenceConstants.PREF_AUTOASSIGNNATURE, autoAssignButton.getSelection());
		return true;
	}


}