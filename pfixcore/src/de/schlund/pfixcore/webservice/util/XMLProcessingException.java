/*
 * de.schlund.pfixschlund.webfix.util.XMLProcessingException
 */
package de.schlund.pfixcore.webservice.util;

import org.xml.sax.SAXException;

/**
 * XMLProcessingException.java 
 * 
 * Created: 07.11.2003
 * 
 * @author mleidig
 */
public class XMLProcessingException extends SAXException {
    
    public XMLProcessingException(String msg) {
        super(msg);
    }
    
    public XMLProcessingException(String msg,Exception ex) {
        super(msg,ex);
    }

}
