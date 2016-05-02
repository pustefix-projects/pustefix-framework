package de.schlund.pfixxml;

public class IncludePartsInfoParsingException extends Exception {

    private static final long serialVersionUID = -2611908324885912529L;

    public IncludePartsInfoParsingException(String location, Throwable cause) {
        super("Error parsing include parts from " + location, cause);
    }
    
}
