package de.schlund.pfixxml.testenv;


/**
 * Exception class for handling errors for {@link RecordManager}
 * <br/>
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */
public class RecordManagerException extends Exception {

    //~ Instance/static variables ..................................................................

    private String    errorMessage = null;
    private Exception theCause = null;

    //~ Constructors ...............................................................................

    /**
     * Create a new RecordManagerException
     * @param error an error message
     * @param cause an exception which is the cause for this exception
     */
    public RecordManagerException(String error, Exception cause) {
        super(error);
        this.errorMessage = error;
        this.theCause     = cause;
    }

    //~ Methods ....................................................................................

    /**
     * Get the cause of this exception
     * @return the cause
     */
    public Exception getExceptionCause() {
        return theCause == null ? new Exception("unkown reason") : theCause;
    }


    /**
     * Get the error message
     * @return the message
     */
    public String getMessage() {
        return errorMessage == null ? "" : errorMessage;
    }
}
