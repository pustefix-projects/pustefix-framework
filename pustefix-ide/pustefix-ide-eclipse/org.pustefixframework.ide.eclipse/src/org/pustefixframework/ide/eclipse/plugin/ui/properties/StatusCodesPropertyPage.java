package org.pustefixframework.ide.eclipse.plugin.ui.properties;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceSorter;
import org.pustefixframework.ide.eclipse.plugin.Activator;
import org.pustefixframework.ide.eclipse.plugin.ui.preferences.PreferenceConstants;
import org.pustefixframework.ide.eclipse.plugin.ui.settings.FolderSelectionDialog;
import org.pustefixframework.ide.eclipse.plugin.ui.util.SettingsStore;
import org.pustefixframework.ide.eclipse.plugin.ui.util.StatusInfo;
import org.pustefixframework.ide.eclipse.plugin.ui.util.TypeViewerFilter;
import org.pustefixframework.ide.eclipse.plugin.util.PathValidator;

public class StatusCodesPropertyPage extends BasePropertyPage {

	final static String PAGE_ID="org.pustefixframework.ide.eclipse.plugin.preferences.statuscodes";
	final static String PROP_PAGE_ID="org.pustefixframework.ide.eclipse.plugin.properties.statuscodes";
	
	Button scodeGenButton;
	
	Text scodeSrcDirText;
	IStatus scodeSrcDirStatus;
	
	Text scodeTargetDirText;
	IStatus scodeTargetDirStatus;
	
	
	public StatusCodesPropertyPage() {
		super();
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		scodeSrcDirStatus = StatusInfo.OK_STATUS;
		scodeTargetDirStatus = StatusInfo.OK_STATUS;
	}

	public void init(IWorkbench arg0) {
	    // TODO Auto-generated method stub
	    
	}
	
	public Control createSettingsContent(Composite parent) {
		
		Composite composite = createDefaultComposite(parent,1);
		
		Composite subComp = new Composite(composite, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        subComp.setLayout(layout);
        
		scodeGenButton = new Button(subComp, SWT.CHECK);
        scodeGenButton.setText("&Generate automatically");
		
		final Group envGroup = new Group(subComp, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 3;
		envGroup.setLayout(layout);
		GridData gd=new GridData(GridData.FILL_HORIZONTAL);
		envGroup.setLayoutData(gd);
		envGroup.setText("Directories"); 
		
		Label scodeSrcDirLabel=new Label(envGroup,SWT.NONE);
		scodeSrcDirLabel.setText("&Source dir:");
		gd=new GridData();
		scodeSrcDirLabel.setLayoutData(gd);
		
		scodeSrcDirText=new Text(envGroup,SWT.SINGLE|SWT.BORDER);
		gd=new GridData();
		gd.horizontalAlignment= GridData.FILL;
		gd.grabExcessHorizontalSpace=true;
		gd.horizontalSpan=1;
		scodeSrcDirText.setLayoutData(gd);
		
		scodeSrcDirText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				Text text=(Text)e.getSource();
				scodeSrcDirStatus=validatePath(text.getText());
				updateStatus(scodeSrcDirStatus);
			}
		});
		
		final Button scodeSrcDirButton = new Button(envGroup,SWT.None);
		scodeSrcDirButton.setText("Browse");
		scodeSrcDirButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IContainer ic=chooseContainer(scodeSrcDirText.getText());
				if(ic!=null) scodeSrcDirText.setText(ic.getProjectRelativePath().toPortableString());
			}
		});
		
		gd= new GridData();
		gd.horizontalSpan= 1;
		gd.horizontalAlignment= GridData.FILL;
			
		scodeSrcDirButton.setLayoutData(gd);
		
		
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
		
		final Button scodeTargetDirButton = new Button(envGroup,SWT.None);
		scodeTargetDirButton.setText("Browse");
		scodeTargetDirButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IContainer ic=chooseContainer(scodeTargetDirText.getText());
				if(ic!=null) scodeTargetDirText.setText(ic.getProjectRelativePath().toPortableString());
			}
		});
		
		gd= new GridData();
		gd.horizontalSpan= 1;
		gd.horizontalAlignment= GridData.FILL;
			
		scodeTargetDirButton.setLayoutData(gd);
		
		return subComp;
		
	}

	
	private IStatus validatePath(String pathStr) {
		IStatus status=PathValidator.validate(pathStr);
		if(status.isOK()) {
			IPath path=new Path(pathStr);
			IFolder file=getProject().getFolder(path);
			if(!file.exists()) status=new StatusInfo(IStatus.WARNING,"Folder doesn't exist: "+pathStr);
		}
		return status;
	}
	
	private void updateStatus(IStatus lastStatus) {
		updateStatus(lastStatus,new IStatus[] {scodeSrcDirStatus, scodeTargetDirStatus});
	}
	
	
	@Override
	protected String getPreferencePageId() {
		return PAGE_ID;
	}
	
	@Override
	protected String getProjectPropertyPageId() {
		return PROP_PAGE_ID;
	}
	
	
	private IContainer chooseContainer(String pathStr) {
		
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
		
		FolderSelectionDialog dialog= new FolderSelectionDialog(getShell(), lp, cp, true);
		
		//ElementTreeSelectionDialog dialog=new ElementTreeSelectionDialog(parentComposite.getShell(),lp,cp);
		dialog.setTitle("Folder Selection"); 
		//dialog.setValidator(validator);
		dialog.setMessage("Select a folder:");
		dialog.setAllowMultiple(false);
		dialog.setEmptyListMessage("No folders available");
		dialog.addFilter(filter);
		dialog.setInput(workspace);
		dialog.setInitialSelection(initSelection);
		dialog.setSorter(new ResourceSorter(ResourceSorter.NAME));
		
		
		
		if (dialog.open() == Window.OK) {
			return (IContainer)dialog.getFirstResult();
		}
		
		return null;
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
		SettingsStore store=new SettingsStore(Activator.PLUGIN_ID,getProject());
		String pref = isWebappProject()?PreferenceConstants.PREF_SCODESRCDIR_WEBAPP:PreferenceConstants.PREF_SCODESRCDIR_MODULE;
		String scodeSrcDir = store.getCascadedProjectValue(pref,"");
		scodeSrcDirText.setText(scodeSrcDir);
		String scodeTargetDir = store.getCascadedProjectValue(PreferenceConstants.PREF_SCODETARGETDIR,"");
		scodeTargetDirText.setText(scodeTargetDir);
		boolean genScodes = store.getCascadedProjectValue(PreferenceConstants.PREF_GENERATESTATUSCODES,false);
        scodeGenButton.setSelection(genScodes);
	}

	protected void performDefaults() {
		SettingsStore store=new SettingsStore(Activator.PLUGIN_ID,getProject());
		String pref = isWebappProject()?PreferenceConstants.PREF_SCODESRCDIR_WEBAPP:PreferenceConstants.PREF_SCODESRCDIR_MODULE;
		String scodeSrcDir = store.getDefaultValue(pref,"");
		scodeSrcDirText.setText(scodeSrcDir);
		String scodeTargetDir = store.getDefaultValue(PreferenceConstants.PREF_SCODETARGETDIR,"");
		scodeTargetDirText.setText(scodeTargetDir);
		boolean genScodes = store.getDefaultValue(PreferenceConstants.PREF_GENERATESTATUSCODES,false);
        scodeGenButton.setSelection(genScodes);
	}
	
	public boolean performOk() {
		SettingsStore store=new SettingsStore(Activator.PLUGIN_ID,getProject());
		if(isProjectSpecificEnabled()) {
		    String pref = isWebappProject()?PreferenceConstants.PREF_SCODESRCDIR_WEBAPP:PreferenceConstants.PREF_SCODESRCDIR_MODULE;
			store.setProjectValue(pref, scodeSrcDirText.getText());
			store.setProjectValue(PreferenceConstants.PREF_SCODETARGETDIR, scodeTargetDirText.getText());
			store.setProjectValue(PreferenceConstants.PREF_GENERATESTATUSCODES, scodeGenButton.getSelection());
		} else {
		    String pref = isWebappProject()?PreferenceConstants.PREF_SCODESRCDIR_WEBAPP:PreferenceConstants.PREF_SCODESRCDIR_MODULE;
			store.removeProjectValue(pref);
			store.removeProjectValue(PreferenceConstants.PREF_SCODETARGETDIR);
			store.removeProjectValue(PreferenceConstants.PREF_GENERATESTATUSCODES);
		}
		return true;
	}
	
}