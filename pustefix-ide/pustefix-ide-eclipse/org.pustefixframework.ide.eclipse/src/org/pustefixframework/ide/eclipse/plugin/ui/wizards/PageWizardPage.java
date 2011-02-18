package org.pustefixframework.ide.eclipse.plugin.ui.wizards;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SelectionDialog;

import org.pustefixframework.ide.eclipse.plugin.Activator;
import org.pustefixframework.ide.eclipse.plugin.Logger;
import org.pustefixframework.ide.eclipse.plugin.PustefixNature;
import org.pustefixframework.ide.eclipse.plugin.ui.preferences.PreferenceConstants;
import org.pustefixframework.ide.eclipse.plugin.ui.util.SettingsStore;
import org.pustefixframework.ide.eclipse.plugin.ui.util.StatusInfo;
import org.pustefixframework.ide.eclipse.plugin.util.DependConfig;
import org.pustefixframework.ide.eclipse.plugin.util.ProjectConfig;
import org.pustefixframework.ide.eclipse.plugin.util.ServletConfig;


public class PageWizardPage extends BaseWizardPage {
	
	private static Logger LOG=Activator.getLogger();
	
	private Combo projectCombo;
	private IStatus projectStatus=StatusInfo.OK_STATUS;
	
	private Combo servletCombo;
	private IStatus servletStatus=StatusInfo.OK_STATUS;
	
	private Text nameText;
	private IStatus nameStatus=StatusInfo.OK_STATUS;
	
	private Combo flowCombo;
	private IStatus flowStatus=StatusInfo.OK_STATUS;
	
	private Combo stateCombo;
	private IStatus stateStatus=StatusInfo.OK_STATUS;
	
	private Combo xmlCombo;
	private IStatus xmlStatus=StatusInfo.OK_STATUS;
	
	private String namePatternStr="[a-zA-Z0-9]+";
	private Pattern namePattern=Pattern.compile(namePatternStr);
	
	private IProject project;
	private IFolder projectDir;
	
	private List<String> projectNames;
	
	private ProjectConfig projectConfig;
	private ServletConfig servletConfig;
	private DependConfig dependConfig;
	private List<String> xmlBaseTargets;
	
	public PageWizardPage(ISelection selection) {
		super("newPageWizard",selection);
		setTitle("Create Pustefix page");
		setDescription("This wizard creates a new Pustefix page.");
	}

	
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		
		//Project
		
		Label label = new Label(container, SWT.NULL);
		label.setText("&Project:");
		
		projectCombo=new Combo(container,SWT.NONE);
		projectCombo.setItems(new String[] {});
		GridData gd=new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan=2;
		
		projectCombo.setLayoutData(gd);
		projectCombo.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				Combo combo=(Combo)e.getSource();
				projectStatus=validateProject(combo.getText());
				updateStatus(projectStatus);
				if(projectStatus.isOK()) {
					updateProject(combo.getText());
				}
				checkComplete();
			}
		});
		
		//Servlet
		
		label = new Label(container, SWT.NULL);
		label.setText("&Handler:");
		
		servletCombo=new Combo(container,SWT.NONE);
		servletCombo.setItems(new String[] {});
		gd=new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan=2;
		
		servletCombo.setLayoutData(gd);
		servletCombo.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				Combo combo=(Combo)e.getSource();
				servletStatus=validateServlet(combo.getText());
				if(servletStatus.isOK()) {
					updateServlet(combo.getText());
				}
				updateStatus(servletStatus);
				checkComplete();
			}
		});
		
		//Page name
		
		label = new Label(container, SWT.NULL);
		label.setText("Page &name:");

		nameText = new Text(container, SWT.BORDER | SWT.SINGLE);
		nameText.setText("");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan=2;
		nameText.setLayoutData(gd);
		nameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				Text text=(Text)e.getSource();
				nameStatus=validatePageName(text.getText());
				updateStatus(nameStatus);
				checkComplete();
			}
		});

		//Pageflow
		
		label=new Label(container,SWT.NONE);
		label.setText("Page&flow:");
		
		flowCombo=new Combo(container,SWT.NONE);
		flowCombo.setItems(new String[] {});
		flowCombo.select(0);
		gd=new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan=2;
		flowCombo.setLayoutData(gd);
		flowCombo.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				Combo combo=(Combo)e.getSource();
				flowStatus=validatePageflow(combo.getText());
				updateStatus(flowStatus);
				checkComplete();
			}
		});
		
		//State
		
		label=new Label(container,SWT.NULL);
		label.setText("&State:");
		
		stateCombo=new Combo(container,SWT.NONE);
		stateCombo.setItems(new String[] {"de.schlund.pfixcore.workflow.app.DefaultIWrapperState","de.schlund.pfixcore.workflow.app.StaticState"});
		stateCombo.select(0);
		gd=new GridData(GridData.FILL_HORIZONTAL);
		stateCombo.setLayoutData(gd);
		stateCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				Combo combo=(Combo)e.getSource();
				stateStatus=validateState(combo.getText());
				updateStatus(stateStatus);
				checkComplete();
			}
		});
		
		final Button stateButton = new Button(container,SWT.None);
		stateButton.setText("Browse");
		stateButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IType it=chooseType(stateCombo.getText());
				if(it!=null) stateCombo.setText(it.getFullyQualifiedName());
			}
		});
		
		
		//XML base target
		
		label=new Label(container,SWT.NONE);
		label.setText("XML &base target:");
		
		xmlCombo=new Combo(container,SWT.NONE);
		xmlCombo.setItems(new String[] {});
		xmlCombo.select(0);
		gd=new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan=2;
		xmlCombo.setLayoutData(gd);
		xmlCombo.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				Combo combo=(Combo)e.getSource();
				xmlStatus=validateXMLBaseTarget(combo.getText());
				updateStatus(xmlStatus);
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
		IResource res=null;
		if(selection!=null && !selection.isEmpty() && selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			if (ssel.size() > 1) return;
			Object obj = ssel.getFirstElement();
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
//			} else {
//				updateProjects();
//				try {
//					List<String> projectNames=getProjects();
//					for(int i=0;i<projectNames.size();i++) {
//						IFolder folder=projectDir.getFolder(projectNames.get(i));
//						if(folder.getFullPath().isPrefixOf(res.getFullPath())) {
//							projectCombo.select(i);
//						}
//					}
//				} catch(CoreException x) {
//					LOG.error(x);
//				}
//			}
		}
	
	}
	
	
	
	private List<String> getProjects() throws CoreException {
		if(projectNames==null) {
			projectNames=new ArrayList<String>();
			IResource[] members=projectDir.members(false);
			for(IResource member:members) {
				if(member.getType()==IResource.FOLDER) {
					IFolder folder=(IFolder)member;
					
					IFile file=folder.getFile("conf/project.xml");
					if(!file.exists()) {
						file=folder.getFile("conf/project.xml.in");
					}
					if(file.exists()) {
						String name=folder.getName();
						projectNames.add(name);
					}
				}
			}
		}
		return projectNames;
	}
	
	private void updateProjects() {
		projectCombo.removeAll();
		try { 
			List<String> projectNames=getProjects();
			String[] items=new String[projectNames.size()];
			items=projectNames.toArray(items);
			projectCombo.setItems(items);
		} catch(CoreException x) {
			LOG.error(x);
		}
	}

	
	private void updateProject(String project) {
		IFile prjFile=projectDir.getFile(project+"/conf/project.xml.in");
		if(!prjFile.exists()) {
			prjFile=projectDir.getFile(project+"/conf/project.xml");
		}
		try {
			projectConfig=new ProjectConfig(prjFile);
			String currentServlet=servletCombo.getText();
			servletCombo.removeAll();
			List<String> servlets=projectConfig.getHandlers();
			String[] servletList=new String[servlets.size()];
			servlets.toArray(servletList);
			servletCombo.setItems(servletList);
			int ind=servlets.indexOf(currentServlet);
			if(ind>-1) servletCombo.select(ind);
			else if(servlets.size()>0) servletCombo.select(0);
			
			//depend.xml
			IFile depFile=projectDir.getFile(project+"/conf/depend.xml");
			dependConfig=new DependConfig(depFile);
			
			//XML base targets
			xmlBaseTargets=new ArrayList<String>();
			IFolder xmlDir=projectDir.getFolder(project+"/xml");
			if(xmlDir.exists()) {
				IResource[] members=xmlDir.members();
				for(IResource member:members) {
					if(member.getType()==IResource.FILE) {
						IFile file=(IFile)member;
						if(file.getName().endsWith(".xml")) {
							xmlBaseTargets.add(project+"/xml/"+file.getName());
						}
					}
				}
			}
			String currentXML=xmlCombo.getText();
			xmlCombo.removeAll();
			String[] xmlList=new String[xmlBaseTargets.size()];
			xmlBaseTargets.toArray(xmlList);
			xmlCombo.setItems(xmlList);
			ind=xmlBaseTargets.indexOf(currentXML);
			if(ind>-1) xmlCombo.select(ind);
			else if(xmlBaseTargets.size()>0) xmlCombo.select(0);
			
		} catch(Exception x) {
			LOG.error(x);
		}
	}
	
	
	private void updateServlet(String servlet) {
		if(projectConfig!=null) {
			try {
				IPath propPath=projectConfig.getPropFile(servlet);
				IFile propFile=projectDir.getFile(propPath);
				servletConfig=new ServletConfig(propFile);
				String currentFlow=flowCombo.getText();
				flowCombo.removeAll();
				List<String> flows=servletConfig.getPageFlows();
				String[] flowList=new String[flows.size()];
				flows.toArray(flowList);
				flowCombo.setItems(flowList);
				int ind=flows.indexOf(currentFlow);
				if(ind>-1) flowCombo.select(ind);
			} catch(Exception x) {
				LOG.error(x);
			}
		}
	}
	
	private void disableControls() {
		projectCombo.setEnabled(false);
		nameText.setEnabled(false);
		flowCombo.setEnabled(false);
		stateCombo.setEnabled(false);
	}
	
	private void updateStatus(IStatus lastStatus) {
		updateStatus(lastStatus,new IStatus[] {projectStatus,nameStatus,flowStatus,stateStatus});
	}


	private IStatus validateState(String name) {
		IStatus status=StatusInfo.OK_STATUS;
		IJavaProject javaProject=JavaCore.create(project);
		try {
			IType type=javaProject.findType(name);
			if(type==null) status=new StatusInfo(IStatus.ERROR,"State class not found.");
			//TODO: check if State implementation
		} catch(JavaModelException x) {
			LOG.error(x);
		}
		return status;
	}
	
	private IStatus validatePageName(String name) {
		IStatus status=StatusInfo.OK_STATUS;
		Matcher matcher=namePattern.matcher(name);
		if(!matcher.matches()) status=new StatusInfo(IStatus.ERROR,"Invalid page name.");
		else {
			if(servletConfig!=null && servletConfig.getPages().contains(name)) 
				status=new StatusInfo(IStatus.ERROR,"The servlet configuration already contains this page.");
			else if(dependConfig!=null && dependConfig.getStandardPages().contains(name)) 
				status=new StatusInfo(IStatus.ERROR,"The page configuration already contains this page.");
		}
		return status;
	}
	
	private IStatus validatePageflow(String flow) {
		IStatus status=StatusInfo.OK_STATUS;
		if(servletConfig!=null) {
			if(!flow.trim().equals("") && !servletConfig.getPageFlows().contains(flow)) 
				status=new StatusInfo(IStatus.ERROR,"Pageflow can't be found.");
		}
		return status;
	}
	
	private IStatus validateXMLBaseTarget(String xml) {
		IStatus status=StatusInfo.OK_STATUS;
		if(xmlBaseTargets!=null) {
			if(!xml.trim().equals("") && !xmlBaseTargets.contains(xml)) 
				status=new StatusInfo(IStatus.ERROR,"XML base target can't be found.");
		}
		return status;
	}
	
	private IStatus validateServlet(String servlet) {
		IStatus status=StatusInfo.OK_STATUS;
		if(projectConfig!=null) {
			if(!projectConfig.getHandlers().contains(servlet)) status=new StatusInfo(IStatus.ERROR,"Servlet can't be found.");
		}
		return status;
	}
	
	private IStatus validateProject(String project) {
		IStatus status=StatusInfo.OK_STATUS;
		try {
			List<String> projectNames=getProjects();
			if(!projectNames.contains(project)) {
				status=new StatusInfo(IStatus.ERROR,"Project doesn't exist.");
			}
		} catch(CoreException x) {
			LOG.error(x);
		}
		return status;
	}
	

	private boolean checkComplete() {
		boolean complete=true;
		IStatus[] status=new IStatus[] {projectStatus,servletStatus,nameStatus,flowStatus,stateStatus,xmlStatus};
		for(IStatus s:status) if(s.getSeverity()==IStatus.ERROR) complete=false;
		if(complete) if(projectCombo.getText().equals("")||servletCombo.getText().equals("")||
				nameText.getText().equals("")||stateCombo.getText().equals("")||xmlCombo.getText().equals("")) complete=false;
		setPageComplete(complete);
		return complete;
	}
	
	public IFolder getProjectDir() {
		return projectDir;
	}
	
	public String getProjectName() {
		return projectCombo.getText();
	}
	
	public String getServletName() {
		return servletCombo.getText();
	}
	
	public String getPageName() {
		return nameText.getText();
	}
	
	public String getFlow() {
		return flowCombo.getText();
	}
	
	public String getState() {
		return stateCombo.getText();
	}
	
	public String getXMLBaseTarget() {
		return xmlCombo.getText();
	}
	
	public ServletConfig getServletConfig() {
		return servletConfig;
	}
	
	public DependConfig getDependConfig() {
		return dependConfig;
	}
	
	private IType chooseType(String typeStr) {
		
		/**
		Class[] acceptedTypes=new Class[] {IProject.class,IFolder.class};
		//ISelectionStatusValidator validator= new TypedElementSelectionValidator(acceptedTypes, false);
		IWorkspaceRoot workspace=getProject().getWorkspace().getRoot();
		IProject[] allProjects= workspace.getProjects();
		
		List<IProject> excludedProjects=new ArrayList<IProject>(allProjects.length);
		IProject currProject= getProject();
		for (int i= 0; i < allProjects.length; i++) {
			if (!allProjects[i].equals(currProject)) {
				excludedProjects.add(allProjects[i]);
			}
		}
		ViewerFilter filter= new TypeViewerFilter(acceptedTypes,excludedProjects.toArray());

		ILabelProvider lp= new WorkbenchLabelProvider();
		ITreeContentProvider cp= new WorkbenchContentProvider();

		IPath path=new Path(pathStr);
		IResource initSelection= null;
		if (path != null) {
			initSelection= currProject.findMember(path);
		}
		*/
		try {
		SelectionDialog dialog=JavaUI.createTypeDialog(getShell(),getContainer(),SearchEngine.createWorkspaceScope(),
				IJavaElementSearchConstants.CONSIDER_ALL_TYPES, false,"",null);
		dialog.setTitle("Select a state");
		dialog.setMessage("Select a state");
		
		if (dialog.open() == Window.OK) {
			return (IType)dialog.getResult()[0];
		}
		} catch(JavaModelException x) {
			LOG.error(x);
		}
		
		
		return null;
	}
	
	
}