package de.schlund.util.statuscodes;

public class StatusCodeException extends RuntimeException {

    public StatusCodeException() {
        super();
    }

    public StatusCodeException(String desc) {
        super(desc);
    }
}    
