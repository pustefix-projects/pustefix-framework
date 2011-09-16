package org.pustefixframework.ide.eclipse.plugin.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import org.pustefixframework.ide.eclipse.plugin.Activator;
import org.pustefixframework.ide.eclipse.plugin.Logger;
import org.pustefixframework.ide.eclipse.plugin.util.DependConfig;
import org.pustefixframework.ide.eclipse.plugin.util.ProjectCreator;
import org.pustefixframework.ide.eclipse.plugin.util.ServletConfig;

public class PageWizard extends Wizard implements INewWizard {

    private Logger LOG = Activator.getLogger();
    
	private PageWizardPage page;
	private ISelection selection;
	
	@Override
	public boolean performFinish() {
		
		final IFolder projectRootDir=page.getProjectDir();
		final String projectName=page.getProjectName();
		final String servletName=page.getServletName();
		final String pageFlow=page.getFlow();
		final String stateClass=page.getState();
		final String pageName=page.getPageName();
		final String xmlBaseTarget=page.getXMLBaseTarget();
		final ServletConfig servletConfig=page.getServletConfig();
		final DependConfig dependConfig=page.getDependConfig();
		
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					monitor.beginTask("Creating new project",3);
					
					servletConfig.addPage(pageName,stateClass,pageFlow);
					servletConfig.save();
					monitor.worked(1);

					dependConfig.addStandardPage(pageName,xmlBaseTarget,"/xml/"+servletName);
					dependConfig.save();
					monitor.worked(1);
					
					ProjectCreator.createXMLPage(projectRootDir, projectName, pageName);
					monitor.worked(1);
					
				} catch (CoreException e) {
					LOG.error(e);
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			LOG.error(realException);
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		}
		return true;
	}
	
	
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub
		this.selection = selection;
		setWindowTitle("New Page");
	}

	@Override
	public void addPages() {
		page=new PageWizardPage(selection);
		addPage(page);
	}
	
}
