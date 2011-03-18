package org.pustefixframework.ide.eclipse.plugin.ui.preferences;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PreferencesUtil;

import org.pustefixframework.ide.eclipse.plugin.ui.settings.ProjectSelectionDialog;
import org.pustefixframework.ide.eclipse.plugin.ui.util.PageData;

public abstract class BasePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	protected Control settingsContent;
	private PageData dataMap;
	
	protected final static String OPTION_NOLINK="nolink";

	protected Composite parentComposite;
	
	protected Control settingLinkControl;
	
	protected Control createPrologContent(Composite parent) {
		
		Composite composite=createDefaultComposite(parent,3);
		
		Link settingLink= new Link(composite, SWT.NONE);
		settingLink.setFont(composite.getFont());
		settingLink.setText("<A>" + "Configure Project Specific Settings..." + "</A>");  
		settingLink.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				openProjectSettings();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				openProjectSettings();
			}
		});
		GridData gd = new GridData();
		gd.horizontalSpan=3;
		gd.horizontalAlignment=SWT.END;
		gd.grabExcessHorizontalSpace=true;
		settingLink.setLayoutData(gd);
		settingLinkControl=composite;
			
		return composite;
	}
	
	protected void openProjectSettings() {
		PageData data= new PageData();
		data.put(OPTION_NOLINK,Boolean.TRUE);
		Set<IJavaProject> projectsWithSpecifics= new HashSet<IJavaProject>();
		try {
			IJavaProject[] projects= JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()).getJavaProjects();
			for (int i= 0; i < projects.length; i++) {
				IJavaProject curr= projects[i];
				if (hasProjectSpecificOptions(curr.getProject())) {
					projectsWithSpecifics.add(curr);
				}
			}
		} catch (JavaModelException e) {
			// ignore
		}
		ProjectSelectionDialog dialog= new ProjectSelectionDialog(getShell(), projectsWithSpecifics);
		if (dialog.open() == Window.OK) {
			IJavaProject res= (IJavaProject) dialog.getFirstResult();
			if(res!=null) openProjectProperties(res.getProject(), data);
		}
	}
	
	protected final void openProjectProperties(IProject project, Object data) {
		String id=getProjectPropertyPageId();
		if (id != null) {
			PreferencesUtil.createPropertyDialogOn(getShell(), project, id, new String[] { id }, data).open();
		}
	}
	
	protected abstract String getPreferencePageId();
	protected abstract String getProjectPropertyPageId();
	
	protected abstract boolean hasProjectSpecificOptions(IProject project);
	
	protected Composite createDefaultComposite(Composite parent,int gridColNo) {
		Composite composite=new Composite(parent,SWT.NULL);
		GridLayout layout=new GridLayout();
		layout.numColumns=gridColNo;
		layout.marginHeight=0;
		layout.marginWidth=0;
		composite.setLayout(layout);
		GridData gd=new GridData();
		gd.grabExcessHorizontalSpace=true;
		gd.horizontalAlignment=SWT.FILL;
		composite.setLayoutData(gd);
		return composite;
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
		GridData data=new GridData(SWT.FILL,SWT.FILL,true,true);
		settingsContent.setLayoutData(data);
		
		initValues();
		
		return composite;
		
	}
	
	public abstract void initValues();
	
	public abstract Control createSettingsContent(Composite parent);
	

	
	@Override
	public void applyData(Object data) {
		if(data!=null) {
			if(data instanceof PageData) {
				dataMap=(PageData)data;
				Object value=dataMap.get(OPTION_NOLINK);
				if(Boolean.TRUE.equals(value)) hideLink();
			}
		}
	}
	
	private void hideLink() {
		if(settingLinkControl!=null) {
			settingLinkControl.dispose();
			parentComposite.layout(true,true);
		}
	}
	
	protected IStatus getDisplayStatus(IStatus lastStatus,IStatus[] statusList) {
		if(lastStatus.getSeverity()==IStatus.ERROR) return lastStatus;
		IStatus impStatus=null;
		for(IStatus status:statusList) {
			if(status.getSeverity()==IStatus.ERROR) return status;
			else if(impStatus==null && status.getSeverity()==IStatus.WARNING) impStatus=status;
		}
		return impStatus;
	}
	
	protected void updateStatus(IStatus lastStatus,IStatus[] statusList) {
		IStatus impStatus=getDisplayStatus(lastStatus,statusList);
		setMessage(null);
		setErrorMessage(null);
		if(impStatus==null||impStatus.isOK()) {
			setValid(true);
		} else if(impStatus.getSeverity()==IStatus.ERROR) {
			setValid(false);
			setErrorMessage(impStatus.getMessage());
		} else if(impStatus.getSeverity()==IStatus.WARNING) {
			setValid(true);
			setMessage(impStatus.getMessage(),WARNING);
		} else {
			setValid(true);
			setMessage(impStatus.getMessage(),INFORMATION);
		}	
	}
	
}
