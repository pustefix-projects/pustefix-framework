package org.pustefixframework.ide.eclipse.plugin.ui.wizards;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;

public abstract class BaseWizardPage extends WizardPage {

	protected ISelection selection;
	
	public BaseWizardPage(String pageName,ISelection selection) {
		super(pageName);
		this.selection = selection;
	}
	
	protected IStatus getDisplayStatus(IStatus lastStatus,IStatus[] statusList) {
		if(lastStatus.getSeverity()==IStatus.ERROR) return lastStatus;
		IStatus impStatus=lastStatus;
		for(IStatus status:statusList) {
			if(status.getSeverity()==IStatus.ERROR) return status;
			if(status.getSeverity()==IStatus.WARNING && impStatus.getSeverity()!=IStatus.WARNING) impStatus=status;
		}
		return impStatus;
	}
	
	protected void updateStatus(IStatus lastStatus,IStatus[] statusList) {
		
		IStatus impStatus=getDisplayStatus(lastStatus,statusList);
		
		setMessage(null);
		setErrorMessage(null);
		if(impStatus==null||impStatus.isOK()) {
			
		} else if(impStatus.getSeverity()==IStatus.ERROR) {
			
			setErrorMessage(impStatus.getMessage());
		} else if(impStatus.getSeverity()==IStatus.WARNING) {
			
			setMessage(impStatus.getMessage(),WARNING);
		} else {
			
			setMessage(impStatus.getMessage(),INFORMATION);
		}	
	}
	
}
