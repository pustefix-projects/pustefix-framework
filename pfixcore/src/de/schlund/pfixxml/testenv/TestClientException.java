package de.schlund.pfixxml.testenv;

/**
 * @author jh
 */
public class TestClientException extends Exception {

    private String errorMessage = null;
    private Exception theCause = null;
    
    public TestClientException(String error, Exception cause) {
        super(error);
        this.errorMessage = error;
        this.theCause = cause;
    }
    
    public Exception getExceptionCause() {
        if(theCause == null)
            return new Exception("unkown reason");
        return theCause;
    }
    
    public String getMessage() {
        if(errorMessage == null)
            return "";
        return errorMessage;
    }
}
