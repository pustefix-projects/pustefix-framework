/*
 * de.schlund.pfixschlund.webfix.util.XMLTypeConverterException
 */
package de.schlund.pfixcore.webservice.util;

/**
 * XMLTypeConverterException.java 
 * 
 * Created: 07.11.2003
 * 
 * @author mleidig
 */
public class XMLTypeConverterException extends XMLProcessingException {
    
    public XMLTypeConverterException(String val,Class type) {
        super("Can't convert '"+val+"' to "+type.getName());
    }

}
