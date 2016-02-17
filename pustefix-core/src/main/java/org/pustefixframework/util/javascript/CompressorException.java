package org.pustefixframework.util.javascript;

/**
 * General exception thrown by Javascript compressors if
 * compression of Javascript fails. 
 */
public class CompressorException extends Exception {

    private static final long serialVersionUID = -7731783686843608988L;

    public CompressorException(String message) {
        super(message);
    }

    public CompressorException(String message, Throwable cause) {
        super(message, cause);
    }
    
    
}
