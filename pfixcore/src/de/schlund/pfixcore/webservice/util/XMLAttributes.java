/*
 * de.schlund.pfixschlund.util.checker.domainapi.XMLAttributes
 */
package de.schlund.pfixcore.webservice.util;

import org.xml.sax.Attributes;
import java.util.ArrayList;

/**
 * XMLAttributes.java 
 * 
 * Created: 10.11.2003
 * 
 * @author mleidig
 */
public class XMLAttributes {

    Attributes attrs;

    public XMLAttributes(Attributes attrs) {
        this.attrs=attrs;
    }
    
    public String getValue(String name) {
        String val=attrs.getValue(name);
        if(val!=null) return val.trim();
        return val;
    }

    public int getLength() {
        return attrs.getLength();
    }
    
    public String getQName(int index) {
        return attrs.getQName(index);
    }
    
    public String getValue(int index) {
        return attrs.getValue(index);
    }
    
}
