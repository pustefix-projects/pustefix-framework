package de.schlund.pfixxml.testenv;


/**
 * @author jh
 *
 */
public class RecordManagerException extends Exception {

    //~ Instance/static variables ..................................................................

    private String    errorMessage = null;
    private Exception theCause = null;

    //~ Constructors ...............................................................................

    public RecordManagerException(String error, Exception cause) {
        super(error);
        this.errorMessage = error;
        this.theCause     = cause;
    }

    //~ Methods ....................................................................................

    public Exception getCause() {
        return theCause == null ? new Exception("unkonw reason") : theCause;
    }

    public String getMessage() {
        return errorMessage == null ? "" : errorMessage;
    }
}