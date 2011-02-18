package org.pustefixframework.ide.eclipse.plugin.ui.properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.ControlEnableState;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.pustefixframework.ide.eclipse.plugin.ui.preferences.BasePreferencePage;
import org.pustefixframework.ide.eclipse.plugin.ui.util.PageData;

public abstract class BasePropertyPage extends BasePreferencePage implements IWorkbenchPropertyPage {
	
    private Control staticContent;
	private IAdaptable adaptable;
	private IProject project;
	
	private ControlEnableState settingsContentState;
	
	private Button prjSpecButton;
	private Link settingLink;
	
	public void setElement(IAdaptable element) {
		if(element!=null) {
			adaptable=element;
			project=(IProject)element.getAdapter(IResource.class);
		}
	}
	
	public IAdaptable getElement() {
		return adaptable;
	}
	
	public IProject getProject() {
		return project;
	}
	
	protected boolean isWebappProject() {
	    return true;
	}
	
	protected Control createPrologContent(Composite parent) {

		Composite composite=createDefaultComposite(parent,3);
		
		if(getProject()!=null) {
		
			prjSpecButton=new Button(composite,SWT.CHECK);
			prjSpecButton.setText("Enable pr&oject specific settings");
			GridData gd = new GridData();
			gd.horizontalSpan=1;
			gd.horizontalAlignment=SWT.FILL;
			gd.grabExcessHorizontalSpace=true;
		    prjSpecButton.setLayoutData(gd);
			prjSpecButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					boolean enabled=((Button)e.getSource()).getSelection();
					switchSettingsContent(enabled);
				}
			});
		
			settingLink= new Link(composite, SWT.NONE);
			settingLink.setFont(composite.getFont());
			settingLink.setText("<A>" + "Configure Workspace Settings..." + "</A>");  
			settingLink.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					openWorkspacePreferences();
				}
				public void widgetDefaultSelected(SelectionEvent e) {
					openWorkspacePreferences();
				}
			});
			gd = new GridData();
			gd.horizontalSpan=1;
		    gd.horizontalAlignment=SWT.END;
		    gd.grabExcessHorizontalSpace=false;
		    settingLink.setLayoutData(gd);
		    settingLinkControl=settingLink;
		    
		} 
		
	    return composite;
	}
	
	protected boolean isProjectSpecificEnabled() {
		return prjSpecButton.getSelection();
	}
	
	private void openWorkspacePreferences() {
		PageData data= new PageData();
		data.put(OPTION_NOLINK,Boolean.TRUE);
		String id=getPreferencePageId();
		PreferencesUtil.createPreferenceDialogOn(getShell(), id, new String[] { id }, data).open();
	}
	
	
	@Override
	protected Control createContents(Composite parent) {
		
		this.parentComposite=parent;
		
		Composite composite=new Composite(parent,SWT.NONE);
		GridLayout layout=new GridLayout();
		layout.marginHeight=0;
		layout.marginWidth=0;
		composite.setLayout(layout);
		
		createPrologContent(composite);
		
		settingsContent=createSettingsContent(composite);
		if(settingsContent != null) {
		    GridData data=new GridData(SWT.FILL,SWT.FILL,true,true);
		    settingsContent.setLayoutData(data);
		}
		    
		staticContent=createStaticContent(composite);
		if(staticContent != null) {
		    GridData data = new GridData(SWT.FILL,SWT.FILL,true,true);
	        staticContent.setLayoutData(data);
		}
		
		boolean enabled = hasProjectSpecificOptions(getProject());
		prjSpecButton.setSelection(enabled);
		switchSettingsContent(enabled);
        
		initValues();
		
		return composite;
		
	}
	
	protected void switchSettingsContent(boolean enabled) {
	    if(enabled) {
			if(settingsContentState != null) {
				settingsContentState.restore();
				settingsContentState = null;
			}
			settingLink.setEnabled(false);
		} else {
			if(settingsContent != null) {
			    settingsContentState = ControlEnableState.disable(settingsContent);
			}
			settingLink.setEnabled(true);
		}
	}
	
	public Control createSettingsContent(Composite parent) {
	    return null;
	}
	
	public Control createStaticContent(Composite parent) {
	    return null;
	}
	
}
