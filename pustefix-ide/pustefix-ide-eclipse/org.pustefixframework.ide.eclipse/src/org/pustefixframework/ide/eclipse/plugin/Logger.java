package org.pustefixframework.ide.eclipse.plugin;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class Logger {

	public boolean isDebugEnabled() {
		return false;
	}
	
	public void debug(String msg) {
		if(isDebugEnabled()) {
			ILog log=Activator.getDefault().getLog();
			log.log(new Status(IStatus.INFO,Activator.PLUGIN_ID,IStatus.OK,msg,null));
		}
	}
	
	public void info(String msg) {
		ILog log=Activator.getDefault().getLog();
		log.log(new Status(IStatus.INFO,Activator.PLUGIN_ID,IStatus.OK,msg,null));
	}
	
	public void error(Throwable throwable) {
		ILog log=Activator.getDefault().getLog();
		log.log(new Status(IStatus.ERROR,Activator.PLUGIN_ID,IStatus.OK,throwable.getMessage(),throwable));
	}
	
	public void error(String msg,Throwable throwable) {
		ILog log=Activator.getDefault().getLog();
		log.log(new Status(IStatus.ERROR,Activator.PLUGIN_ID,IStatus.OK,msg,throwable));
	}
	
}
