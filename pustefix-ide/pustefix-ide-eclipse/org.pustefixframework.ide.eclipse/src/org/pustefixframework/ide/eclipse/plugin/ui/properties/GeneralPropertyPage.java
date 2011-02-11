package org.pustefixframework.ide.eclipse.plugin.ui.properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.pustefixframework.ide.eclipse.plugin.Activator;
import org.pustefixframework.ide.eclipse.plugin.util.PustefixVersion;
import org.pustefixframework.ide.eclipse.plugin.util.VersionCheck;

public class GeneralPropertyPage extends BasePropertyPage {

	private final static String PAGE_ID="org.pustefixframework.ide.eclipse.plugin.preferences.general";
	private final static String PROP_PAGE_ID="org.pustefixframework.ide.eclipse.plugin.properties.general";
	
	private Label versionLabel;
	
	public GeneralPropertyPage() {}

	@Override
	public void init(IWorkbench workbench) {}
	
	@Override
	public Control createSettingsContent(Composite parent) {
		
		Composite composite=createDefaultComposite(parent,1);
		
		final Group targetGroup=new Group(composite,SWT.NONE);
        GridLayout layout=new GridLayout();
        layout.numColumns=1;
        targetGroup.setLayout(layout);
        GridData gd=new GridData(GridData.FILL_HORIZONTAL);
        targetGroup.setLayoutData(gd);
        targetGroup.setText("Pustefix core"); 
        
        ImageDescriptor desc = Activator.getImageDescriptor("icons/pustefix_logo.gif");
        Image img = desc.createImage();
        
        Label label = new Label(targetGroup, SWT.NONE);
        label.setImage(img);
        
        versionLabel = new Label(targetGroup, SWT.NONE);
		
		return targetGroup;
		
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
	    return true;
	}
	
	@Override
	public void initValues() {
	    PustefixVersion version = VersionCheck.getPustefixVersion(getProject());
        versionLabel.setText("Version: "+version);
	}

	@Override
	protected void performDefaults() {
	
	}
	
	@Override
	public boolean performOk() {
		return true;
	}
	
}