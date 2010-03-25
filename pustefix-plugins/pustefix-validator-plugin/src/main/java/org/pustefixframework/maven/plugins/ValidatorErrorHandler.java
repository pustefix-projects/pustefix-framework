package org.pustefixframework.maven.plugins;

import org.apache.maven.plugin.logging.Log;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class ValidatorErrorHandler implements ErrorHandler {

    private Log log;
    
    private int warnings;
    private int errors;
    private int fatalErrors;
    
    public ValidatorErrorHandler(Log log) {
        this.log = log;
    }
    
    public void warning(SAXParseException exception) throws SAXException {
        warnings++;
        log.warn(getMessage(exception));
    }
    
    public void error(SAXParseException exception) throws SAXException {
        errors++;
        log.error(getMessage(exception));
    }
    
    public void fatalError(SAXParseException exception) throws SAXException {
        fatalErrors++;
        log.error(" !!!FATAL!!! " + getMessage(exception));     
    }
    
    private String getMessage(SAXParseException x) {
        return x.getSystemId() + " (Line " + x.getLineNumber() + ", Column " + x.getColumnNumber() + "): " +
                " " + x.getMessage();
    }
    
    public int getWarnings() {
        return warnings;
    }
    
    public int getErrors() {
        return errors;
    }
    
    public int getFatalErrors() {
        return fatalErrors;
    }
    
}
