package org.pustefixframework.ide.eclipse.plugin.ui.util;

import org.eclipse.core.runtime.IStatus;

import org.pustefixframework.ide.eclipse.plugin.Activator;

public class StatusInfo implements IStatus {

	private final static IStatus[] children=new IStatus[0];
	private final static int code=0;

	public final static IStatus OK_STATUS=new StatusInfo(); 
	
	private String message;
	private int severity;
	
	public StatusInfo() {
		severity=IStatus.OK;
	}
	
	public StatusInfo(int severity) {
		this.severity=severity;
	}
	
	public StatusInfo(int severity,String message) {
		this.severity=severity;
		this.message=message;
	}
	
	public IStatus[] getChildren() {
		return children;
	}

	public int getCode() {
		return code;
	}

	public Throwable getException() {
		return null;
	}

	public String getMessage() {
		return message;
	}

	public String getPlugin() {
		return Activator.PLUGIN_ID;
	}

	public int getSeverity() {
		return severity;
	}

	public boolean isMultiStatus() {
		return false;
	}

	public boolean isOK() {
		return severity==IStatus.OK;
	}

	public boolean matches(int severityMask) {
		return (severityMask & severity)!=0;
	}
	
	public String toString() {
		StringBuilder sb=new StringBuilder();
		if(severity==IStatus.OK) {
			sb.append("OK");
		} else if(severity==IStatus.ERROR) {
			sb.append("ERROR");
		} else if(severity==IStatus.WARNING) {
			sb.append("WARNING");
		}
		return sb.toString();
	}

}
