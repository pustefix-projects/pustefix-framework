package de.schlund.pfixxml.resources;

public class ResourceProviderException extends Exception {
    
    private static final long serialVersionUID = 3110677599653806751L;

    public ResourceProviderException(String msg) {
        super(msg);
    }
    
    public ResourceProviderException(String msg, Throwable throwable) {
        super(msg, throwable);
    }

}
