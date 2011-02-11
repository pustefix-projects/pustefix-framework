package org.pustefixframework.ide.eclipse.plugin.ui.wizards;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.pustefixframework.ide.eclipse.plugin.Activator;
import org.pustefixframework.ide.eclipse.plugin.Logger;
import org.pustefixframework.ide.eclipse.plugin.builder.PustefixNature;
import org.pustefixframework.ide.eclipse.plugin.ui.preferences.PreferenceConstants;
import org.pustefixframework.ide.eclipse.plugin.ui.util.SettingsStore;
import org.pustefixframework.ide.eclipse.plugin.ui.util.StatusInfo;
import org.pustefixframework.ide.eclipse.plugin.util.PathValidator;


public class ProjectWizardPage extends BaseWizardPage {
	
	private static Logger LOG=Activator.getLogger();
	
	private Text nameText;
	private IStatus nameStatus=StatusInfo.OK_STATUS;
	
	private Text descText;
	private IStatus descStatus=StatusInfo.OK_STATUS;
	
	private Combo langCombo;
	private IStatus langStatus=StatusInfo.OK_STATUS;
	
	private Combo encCombo;
	private IStatus encStatus=StatusInfo.OK_STATUS;
	
	private Text servletText;
	private IStatus servletStatus=StatusInfo.OK_STATUS;
	
	private String namePatternStr="[a-zA-Z_0-9]+";
	private Pattern namePattern=Pattern.compile(namePatternStr);
	
	private String langPatternStr="[a-zA-Z]+(_[a-zA-Z]+)?";
	private Pattern langPattern=Pattern.compile(langPatternStr);
	
	private String encPatternStr="[a-zA-Z]+(-[a-zA-Z0-9]+)?(-[a-zA-Z0-9]+)?";
	private Pattern encPattern=Pattern.compile(encPatternStr);
	
	private IProject project;
	private IFolder projectDir;
	
	public ProjectWizardPage(ISelection selection) {
		super("newProjectWizard",selection);
		setTitle("Create Pustefix project");
		setDescription("This wizard creates a new Pustefix project.");
	}

	
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 2;
		layout.verticalSpacing = 9;
		
		//Project name
		
		Label label = new Label(container, SWT.NULL);
		label.setText("Project &name:");

		nameText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		nameText.setLayoutData(gd);
		nameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				Text text=(Text)e.getSource();
				nameStatus=validateProjectName(text.getText());
				updateStatus(nameStatus);
				checkComplete();
			}
		});
		
		//Project description
		
		label=new Label(container,SWT.NULL);
		label.setText("Project &description:");
		
		descText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		descText.setLayoutData(gd);
		

		//Project default language
		
		label=new Label(container,SWT.NONE);
		label.setText("Default &language:");
		gd=new GridData();
		label.setLayoutData(gd);
		
		langCombo=new Combo(container,SWT.NONE);
		langCombo.setItems(new String[] {"de_DE","en_GB","en_US","es_ES","fr_FR"});
		langCombo.select(2);
		gd=new GridData();
		langCombo.setLayoutData(gd);
		langCombo.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				Combo combo=(Combo)e.getSource();
				langStatus=validateLanguage(combo.getText());
				updateStatus(langStatus);
				checkComplete();
			}
		});
		
		//Project encoding
		
		label=new Label(container,SWT.NULL);
		label.setText("Project &encoding:");
		
		encCombo=new Combo(container,SWT.NONE);
		encCombo.setItems(new String[] {"ISO-8859-1","ISO-8859-15","UTF-8"});
		encCombo.select(2);
		gd=new GridData();
		encCombo.setLayoutData(gd);
		encCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				Combo combo=(Combo)e.getSource();
				encStatus=validateEncoding(combo.getText());
				updateStatus(encStatus);
				checkComplete();
			}
		});
		
		//Main servlet name
		
		label = new Label(container, SWT.NULL);
		label.setText("Main &servlet name:");

		servletText = new Text(container, SWT.BORDER | SWT.SINGLE);
		servletText.setText("servlet");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		servletText.setLayoutData(gd);
		servletText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				Text text=(Text)e.getSource();
				servletStatus=validateServletName(text.getText());
				updateStatus(servletStatus);
				checkComplete();
			}
		});
		
		initialize();
		setControl(container);
		setPageComplete(false);
	}

	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */

	private void initialize() {
		
		boolean isPustefix=false;
		if(selection!=null && !selection.isEmpty() && selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			if (ssel.size() > 1) return;
			Object obj = ssel.getFirstElement();
			IResource res=null;
			if(obj instanceof IResource) res=(IResource)obj;
			else if(obj instanceof IAdaptable) res=(IResource)((IAdaptable)obj).getAdapter(IResource.class);
			if(res!=null) {
				project=res.getProject();
				try {
					isPustefix=project.getDescription().hasNature(PustefixNature.NATURE_ID);
				} catch(CoreException x) {
					LOG.error(x);
				}
			}
		}
		if(!isPustefix) {
			StatusInfo status=new StatusInfo(IStatus.WARNING,"No Pustefix environment in current scope!");
			updateStatus(status);
			disableControls();
			setPageComplete(false);
		} else {
			SettingsStore store=new SettingsStore(Activator.PLUGIN_ID,project);
//			String str=store.getCascadedProjectValue(PreferenceConstants.PREF_PROJECTDIR,"");
//			projectDir=project.getFolder(new Path(str));
//			if(!projectDir.exists()) {
//				StatusInfo status=new StatusInfo(IStatus.WARNING,"Project root directory not found!");
//				updateStatus(status);
//				disableControls();
//				setPageComplete(false);
//			}
		}
	
	}

	
	private void disableControls() {
		nameText.setEnabled(false);
		descText.setEnabled(false);
		langCombo.setEnabled(false);
		encCombo.setEnabled(false);
		servletText.setEnabled(false);
	}
	
	private void updateStatus(IStatus lastStatus) {
		updateStatus(lastStatus,new IStatus[] {nameStatus,descStatus,langStatus,encStatus,servletStatus});
	}


	private IStatus validateProjectName(String name) {
		IStatus status=StatusInfo.OK_STATUS;
		Matcher matcher=namePattern.matcher(name);
		if(!matcher.matches()) status=new StatusInfo(IStatus.ERROR,"Invalid project name.");
		else {
			IPath path=new Path(name);
			if(projectDir.exists(path)) status=new StatusInfo(IStatus.WARNING,"Resource already exists.");
		}
		return status;
	}
	
	private IStatus validateServletName(String name) {
		IStatus status=StatusInfo.OK_STATUS;
		Matcher matcher=namePattern.matcher(name);
		if(!matcher.matches()) status=new StatusInfo(IStatus.ERROR,"Invalid main servlet name.");
		return status;
	}
	
	private IStatus validateEncoding(String enc) {
		IStatus status=StatusInfo.OK_STATUS;
		Matcher matcher=encPattern.matcher(enc);
		if(!matcher.matches()) status=new StatusInfo(IStatus.ERROR,"Invalid project encoding.");
		return status;
	}
	
	private IStatus validateLanguage(String lang) {
		IStatus status=StatusInfo.OK_STATUS;
		Matcher matcher=langPattern.matcher(lang);
		if(!matcher.matches()) status=new StatusInfo(IStatus.ERROR,"Invalid default language.");
		return status;
	}
	

	private boolean checkComplete() {
		boolean complete=true;
		IStatus[] status=new IStatus[] {nameStatus,descStatus,langStatus,encStatus,servletStatus};
		for(IStatus s:status) if(s.getSeverity()==IStatus.ERROR) complete=false;
		if(complete) if(nameText.equals("")||servletText.equals("")||langCombo.getText().equals("")
				||encCombo.getText().equals("")) complete=false;
		setPageComplete(complete);
		return complete;
	}
	
	public IFolder getProjectDir() {
		return projectDir;
	}
	
	public String getProjectName() {
		return nameText.getText();
	}
	
	public String getServletName() {
		return servletText.getText();
	}
	
	public String getDefaultLanguage() {
		return langCombo.getText();
	}
	
	public String getDescription() {
		return descText.getText();
	}
	
}