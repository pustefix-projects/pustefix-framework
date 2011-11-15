package org.pustefixframework.ide.eclipse.plugin.ui.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.pustefixframework.ide.eclipse.plugin.Activator;
import org.pustefixframework.ide.eclipse.plugin.ui.util.SettingsStore;
import org.pustefixframework.ide.eclipse.plugin.ui.util.StatusInfo;
import org.pustefixframework.ide.eclipse.plugin.util.PathValidator;

public class IWrapperPreferencePage extends BasePreferencePage {
	
	final static String PAGE_ID="org.pustefixframework.ide.eclipse.plugin.preferences.iwrapper";
	final static String PROP_PAGE_ID="org.pustefixframework.ide.eclipse.plugin.properties.iwrapper";
	
	Button iwrpGenButton;
	
	Text iwrpSrcDirText;
	IStatus iwrpSrcDirStatus;
	
	Text iwrpTargetDirText;
	IStatus iwrpTargetDirStatus;
	
	
	public IWrapperPreferencePage() {
		super();
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		
		iwrpSrcDirStatus = StatusInfo.OK_STATUS;
		iwrpTargetDirStatus = StatusInfo.OK_STATUS;
	}

	public void init(IWorkbench workbench) {
	}
	
	public Control createSettingsContent(Composite parent) {
		
		Composite composite=createDefaultComposite(parent,1);
		
		iwrpGenButton=new Button(composite, SWT.CHECK);
        iwrpGenButton.setText("&Generate automatically");
		
		final Group envGroup=new Group(composite,SWT.NONE);
		GridLayout layout=new GridLayout();
		layout.numColumns=2;
		envGroup.setLayout(layout);
		GridData gd=new GridData(GridData.FILL_HORIZONTAL);
		envGroup.setLayoutData(gd);
		envGroup.setText("Directories"); 
		
		Label iwrpSrcDirLabel=new Label(envGroup,SWT.NONE);
		iwrpSrcDirLabel.setText("&Source dir:");
		gd=new GridData();
		iwrpSrcDirLabel.setLayoutData(gd);
		
		iwrpSrcDirText=new Text(envGroup,SWT.SINGLE|SWT.BORDER);
		gd=new GridData();
		gd.horizontalAlignment= GridData.FILL;
		gd.grabExcessHorizontalSpace=true;
		gd.horizontalSpan=1;
		iwrpSrcDirText.setLayoutData(gd);
		
		iwrpSrcDirText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				Text text=(Text)e.getSource();
				iwrpSrcDirStatus=validatePath(text.getText());
				updateStatus(iwrpSrcDirStatus);
			}
		});
		
		Label iwrpTargetDirLabel=new Label(envGroup,SWT.NONE);
		iwrpTargetDirLabel.setText("&Target dir:");
		gd=new GridData();
		iwrpTargetDirLabel.setLayoutData(gd);
		
		iwrpTargetDirText=new Text(envGroup,SWT.SINGLE|SWT.BORDER);
		gd=new GridData();
		gd.horizontalAlignment= GridData.FILL;
		gd.grabExcessHorizontalSpace=true;
		gd.horizontalSpan=1;
		iwrpTargetDirText.setLayoutData(gd);
		
		iwrpTargetDirText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				Text text=(Text)e.getSource();
				iwrpTargetDirStatus=validatePath(text.getText());
				updateStatus(iwrpTargetDirStatus);
			}
		});
		
		return envGroup;
	}
	
	private void updateStatus(IStatus lastStatus) {
		updateStatus(lastStatus,new IStatus[] {iwrpSrcDirStatus, iwrpTargetDirStatus});
	}
	

	@Override
	protected String getPreferencePageId() {
		return PAGE_ID;
	}
	
	@Override
	protected String getProjectPropertyPageId() {
		return PROP_PAGE_ID;
	}
	
	private IStatus validatePath(String path) {
		return PathValidator.validate(path);
	}
	
	@Override
	protected boolean hasProjectSpecificOptions(IProject project) {
		SettingsStore store=new SettingsStore(Activator.PLUGIN_ID,project);
		if(store.hasProjectValue(PreferenceConstants.PREF_IWRPSRCDIR)||
				store.hasProjectValue(PreferenceConstants.PREF_IWRPTARGETDIR)||
				store.hasProjectValue(PreferenceConstants.PREF_GENERATEIWRAPPERS)) return true;
		return false;
	}
	
	public void initValues() {
		SettingsStore store = new SettingsStore(Activator.PLUGIN_ID,null);
		boolean genWrappers=store.getCascadedInstanceValue(PreferenceConstants.PREF_GENERATEIWRAPPERS,false);
        iwrpGenButton.setSelection(genWrappers);
		String iwrpSrcDir = store.getCascadedInstanceValue(PreferenceConstants.PREF_IWRPSRCDIR,"");
		iwrpSrcDirText.setText(iwrpSrcDir);
		String iwrpTargetDir = store.getCascadedInstanceValue(PreferenceConstants.PREF_IWRPTARGETDIR,"");
		iwrpTargetDirText.setText(iwrpTargetDir);
	}

	protected void performDefaults() {
		SettingsStore store = new SettingsStore(Activator.PLUGIN_ID,null);
		boolean genWrappers=store.getDefaultValue(PreferenceConstants.PREF_GENERATEIWRAPPERS,false);
        iwrpGenButton.setSelection(genWrappers);
		String iwrpSrcDir = store.getDefaultValue(PreferenceConstants.PREF_IWRPSRCDIR,"");
		iwrpSrcDirText.setText(iwrpSrcDir);
		String iwrpTargetDir = store.getDefaultValue(PreferenceConstants.PREF_IWRPTARGETDIR,"");
		iwrpTargetDirText.setText(iwrpTargetDir);
	}
	
	public boolean performOk() {
		SettingsStore store=new SettingsStore(Activator.PLUGIN_ID,null);
		store.setInstanceValue(PreferenceConstants.PREF_GENERATEIWRAPPERS,iwrpGenButton.getSelection());
		store.setInstanceValue(PreferenceConstants.PREF_IWRPSRCDIR, iwrpSrcDirText.getText());
		store.setInstanceValue(PreferenceConstants.PREF_IWRPTARGETDIR, iwrpTargetDirText.getText());
		return true;
	}

}