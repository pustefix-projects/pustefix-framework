package de.schlund.pfixxml.util;

import javax.xml.transform.TransformerException;


public class XsltResourceNotFoundException extends TransformerException {

    private static final long serialVersionUID = 615355005652872119L;

    public XsltResourceNotFoundException(String msg) {
        super(msg);
    }
    
    public XsltResourceNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }
    
    
}
