package de.schlund.pfixxml.testenv;

/**
 * @author jh
 *
 */
public class RecordManagerException extends Exception {
	private String errorMessage = null;
	private Exception theCause = null;
	
	public RecordManagerException(String error, Exception cause) {
		super(error);
		this.errorMessage = error;
		this.theCause = cause;
	}
	
	public Exception getCause() {
		return theCause==null ? new Exception("unkonw reason") : theCause;
	}

	public String getMessage() {
		return errorMessage == null ? "" : errorMessage;
	}

}
