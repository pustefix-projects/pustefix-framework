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

public class IWrapperPropertyPage extends BasePropertyPage {

	final static String PAGE_ID="org.pustefixframework.ide.eclipse.plugin.preferences.iwrapper";
	final static String PROP_PAGE_ID="org.pustefixframework.ide.eclipse.plugin.properties.iwrapper";
	
	Button iwrpGenButton;
	
	Text iwrpSrcDirText;
	IStatus iwrpSrcDirStatus;
	
	Text iwrpTargetDirText;
	IStatus iwrpTargetDirStatus;
	
	
	public IWrapperPropertyPage() {
		super();
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		iwrpSrcDirStatus = StatusInfo.OK_STATUS;
		iwrpTargetDirStatus = StatusInfo.OK_STATUS;
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
        
		iwrpGenButton = new Button(subComp, SWT.CHECK);
        iwrpGenButton.setText("&Generate automatically");
		
		final Group envGroup = new Group(subComp, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 3;
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
		
		final Button iwrpSrcDirButton = new Button(envGroup,SWT.None);
		iwrpSrcDirButton.setText("Browse");
		iwrpSrcDirButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IContainer ic=chooseContainer(iwrpSrcDirText.getText());
				if(ic!=null) iwrpSrcDirText.setText(ic.getProjectRelativePath().toPortableString());
			}
		});
		
		gd= new GridData();
		gd.horizontalSpan= 1;
		gd.horizontalAlignment= GridData.FILL;
			
		iwrpSrcDirButton.setLayoutData(gd);
		
		
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
		
		final Button iwrpTargetDirButton = new Button(envGroup,SWT.None);
		iwrpTargetDirButton.setText("Browse");
		iwrpTargetDirButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IContainer ic=chooseContainer(iwrpTargetDirText.getText());
				if(ic!=null) iwrpTargetDirText.setText(ic.getProjectRelativePath().toPortableString());
			}
		});
		
		gd= new GridData();
		gd.horizontalSpan= 1;
		gd.horizontalAlignment= GridData.FILL;
			
		iwrpTargetDirButton.setLayoutData(gd);
		
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
		String iwrpSrcDir = store.getCascadedProjectValue(PreferenceConstants.PREF_IWRPSRCDIR,"");
		iwrpSrcDirText.setText(iwrpSrcDir);
		String iwrpTargetDir = store.getCascadedProjectValue(PreferenceConstants.PREF_IWRPTARGETDIR,"");
		iwrpTargetDirText.setText(iwrpTargetDir);
		boolean genWrappers=store.getCascadedProjectValue(PreferenceConstants.PREF_GENERATEIWRAPPERS,false);
        iwrpGenButton.setSelection(genWrappers);
	}

	protected void performDefaults() {
		SettingsStore store=new SettingsStore(Activator.PLUGIN_ID,getProject());
		String iwrpSrcDir = store.getDefaultValue(PreferenceConstants.PREF_IWRPSRCDIR,"");
		iwrpSrcDirText.setText(iwrpSrcDir);
		String iwrpTargetDir = store.getDefaultValue(PreferenceConstants.PREF_IWRPTARGETDIR,"");
		iwrpTargetDirText.setText(iwrpTargetDir);
		boolean genWrappers=store.getDefaultValue(PreferenceConstants.PREF_GENERATEIWRAPPERS,false);
        iwrpGenButton.setSelection(genWrappers);
	}
	
	public boolean performOk() {
		SettingsStore store=new SettingsStore(Activator.PLUGIN_ID,getProject());
		if(isProjectSpecificEnabled()) {
			store.setProjectValue(PreferenceConstants.PREF_IWRPSRCDIR, iwrpSrcDirText.getText());
			store.setProjectValue(PreferenceConstants.PREF_IWRPTARGETDIR, iwrpTargetDirText.getText());
			store.setProjectValue(PreferenceConstants.PREF_GENERATEIWRAPPERS,iwrpGenButton.getSelection());
		} else {
			store.removeProjectValue(PreferenceConstants.PREF_IWRPSRCDIR);
			store.removeProjectValue(PreferenceConstants.PREF_IWRPTARGETDIR);
			store.removeProjectValue(PreferenceConstants.PREF_GENERATEIWRAPPERS);
		}
		return true;
	}
	
}