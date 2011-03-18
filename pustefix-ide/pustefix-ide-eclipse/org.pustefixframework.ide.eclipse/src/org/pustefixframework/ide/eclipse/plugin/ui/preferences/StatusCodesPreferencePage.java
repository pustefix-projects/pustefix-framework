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

public class StatusCodesPreferencePage extends BasePreferencePage {
	
	final static String PAGE_ID="org.pustefixframework.ide.eclipse.plugin.preferences.statuscodes";
	final static String PROP_PAGE_ID="org.pustefixframework.ide.eclipse.plugin.properties.statuscodes";
	
	Button scodeGenButton;
	
	Text scodeSrcDirWebappText;
	IStatus scodeSrcDirWebappStatus;
	
	Text scodeSrcDirModuleText;
    IStatus scodeSrcDirModuleStatus;
	
	Text scodeTargetDirText;
	IStatus scodeTargetDirStatus;
	
	
	public StatusCodesPreferencePage() {
		super();
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		
		scodeSrcDirWebappStatus = StatusInfo.OK_STATUS;
		scodeSrcDirModuleStatus = StatusInfo.OK_STATUS;
		scodeTargetDirStatus = StatusInfo.OK_STATUS;
	}

	public void init(IWorkbench workbench) {
	}
	
	public Control createSettingsContent(Composite parent) {
		
		Composite composite=createDefaultComposite(parent,1);
		
		scodeGenButton=new Button(composite, SWT.CHECK);
        scodeGenButton.setText("&Generate automatically");
		
		final Group envGroup=new Group(composite,SWT.NONE);
		GridLayout layout=new GridLayout();
		layout.numColumns=2;
		envGroup.setLayout(layout);
		GridData gd=new GridData(GridData.FILL_HORIZONTAL);
		envGroup.setLayoutData(gd);
		envGroup.setText("Directories"); 
		
		Label scodeSrcDirWebappLabel=new Label(envGroup,SWT.NONE);
		scodeSrcDirWebappLabel.setText("&Webapp source dir:");
		gd=new GridData();
		scodeSrcDirWebappLabel.setLayoutData(gd);
		
		scodeSrcDirWebappText=new Text(envGroup,SWT.SINGLE|SWT.BORDER);
		gd=new GridData();
		gd.horizontalAlignment= GridData.FILL;
		gd.grabExcessHorizontalSpace=true;
		gd.horizontalSpan=1;
		scodeSrcDirWebappText.setLayoutData(gd);
		
		scodeSrcDirWebappText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				Text text=(Text)e.getSource();
				scodeSrcDirWebappStatus=validatePath(text.getText());
				updateStatus(scodeSrcDirWebappStatus);
			}
		});
		
		
		Label scodeSrcDirModuleLabel=new Label(envGroup,SWT.NONE);
		scodeSrcDirModuleLabel.setText("&Module source dir:");
		gd=new GridData();
		scodeSrcDirModuleLabel.setLayoutData(gd);
	        
		scodeSrcDirModuleText=new Text(envGroup,SWT.SINGLE|SWT.BORDER);
		gd=new GridData();
		gd.horizontalAlignment= GridData.FILL;
		gd.grabExcessHorizontalSpace=true;
		gd.horizontalSpan=1;
		scodeSrcDirModuleText.setLayoutData(gd);
	        
		scodeSrcDirModuleText.addModifyListener(new ModifyListener() {
		    public void modifyText(ModifyEvent e) {
		        Text text=(Text)e.getSource();
		        scodeSrcDirModuleStatus=validatePath(text.getText());
		        updateStatus(scodeSrcDirModuleStatus);
		    }
		});
		
		
		Label scodeTargetDirLabel=new Label(envGroup,SWT.NONE);
		scodeTargetDirLabel.setText("&Target dir:");
		gd=new GridData();
		scodeTargetDirLabel.setLayoutData(gd);
		
		scodeTargetDirText=new Text(envGroup,SWT.SINGLE|SWT.BORDER);
		gd=new GridData();
		gd.horizontalAlignment= GridData.FILL;
		gd.grabExcessHorizontalSpace=true;
		gd.horizontalSpan=1;
		scodeTargetDirText.setLayoutData(gd);
		
		scodeTargetDirText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				Text text=(Text)e.getSource();
				scodeTargetDirStatus=validatePath(text.getText());
				updateStatus(scodeTargetDirStatus);
			}
		});
		
		return envGroup;
	}
	
	private void updateStatus(IStatus lastStatus) {
		updateStatus(lastStatus,new IStatus[] {scodeSrcDirWebappStatus, scodeSrcDirModuleStatus,
		        scodeTargetDirStatus});
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
		if(store.hasProjectValue(PreferenceConstants.PREF_GENERATESTATUSCODES)||
		        store.hasProjectValue(PreferenceConstants.PREF_SCODESRCDIR_MODULE)||
				store.hasProjectValue(PreferenceConstants.PREF_SCODESRCDIR_WEBAPP)||
				store.hasProjectValue(PreferenceConstants.PREF_SCODETARGETDIR)) return true;
		return false;
	}
	
	public void initValues() {
		SettingsStore store = new SettingsStore(Activator.PLUGIN_ID,null);
		boolean genWrappers=store.getCascadedInstanceValue(PreferenceConstants.PREF_GENERATESTATUSCODES,false);
        scodeGenButton.setSelection(genWrappers);
		String scodeSrcDirWebapp = store.getCascadedInstanceValue(PreferenceConstants.PREF_SCODESRCDIR_WEBAPP,"");
		scodeSrcDirWebappText.setText(scodeSrcDirWebapp);
		String scodeSrcDirModule = store.getCascadedInstanceValue(PreferenceConstants.PREF_SCODESRCDIR_MODULE,"");
        scodeSrcDirModuleText.setText(scodeSrcDirModule);
		String scodeTargetDir = store.getCascadedInstanceValue(PreferenceConstants.PREF_SCODETARGETDIR,"");
		scodeTargetDirText.setText(scodeTargetDir);
	}

	protected void performDefaults() {
		SettingsStore store = new SettingsStore(Activator.PLUGIN_ID,null);
		boolean genWrappers=store.getDefaultValue(PreferenceConstants.PREF_GENERATESTATUSCODES,false);
        scodeGenButton.setSelection(genWrappers);
		String scodeSrcDirWebapp = store.getDefaultValue(PreferenceConstants.PREF_SCODESRCDIR_WEBAPP,"");
		scodeSrcDirWebappText.setText(scodeSrcDirWebapp);
		String scodeSrcDirModule = store.getDefaultValue(PreferenceConstants.PREF_SCODESRCDIR_MODULE,"");
        scodeSrcDirModuleText.setText(scodeSrcDirModule);
		String scodeTargetDir = store.getDefaultValue(PreferenceConstants.PREF_SCODETARGETDIR,"");
		scodeTargetDirText.setText(scodeTargetDir);
	}
	
	public boolean performOk() {
		SettingsStore store=new SettingsStore(Activator.PLUGIN_ID,null);
		store.setInstanceValue(PreferenceConstants.PREF_GENERATESTATUSCODES,scodeGenButton.getSelection());
		store.setInstanceValue(PreferenceConstants.PREF_SCODESRCDIR_WEBAPP, scodeSrcDirWebappText.getText());
		store.setInstanceValue(PreferenceConstants.PREF_SCODESRCDIR_MODULE, scodeSrcDirModuleText.getText());
		store.setInstanceValue(PreferenceConstants.PREF_SCODETARGETDIR, scodeTargetDirText.getText());
		return true;
	}

}