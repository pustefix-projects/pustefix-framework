/*
 * de.schlund.pfixcore.webservice.util.XMLTypeConverter
 */
package de.schlund.pfixcore.webservice.util;


/**
 * XMLTypeConverter.java 
 * 
 * Created: 01.10.2004
 * 
 * @author mleidig
 */
public class XMLTypeConverter {

    public static boolean toBoolean(String val) throws XMLTypeConverterException {
        if(val.equalsIgnoreCase("true") || val.equals("1")) return true;
        else if(val.equalsIgnoreCase("false") || val.equals("0")) return false;
        else throw new XMLTypeConverterException(val,boolean.class);
    }
    
    public static int toInteger(String val) throws XMLTypeConverterException {
        try {
            int intVal=Integer.parseInt(val);
            return intVal;
        } catch(NumberFormatException x) {
            throw new XMLTypeConverterException(val,int.class);
        }
    }
    
}
