/*
 * de.schlund.pfixcore.example.webservices.TypeTestImpl
 */
package de.schlund.pfixcore.example.webservices;

import java.util.Date;
import javax.xml.parsers.*;
import org.w3c.dom.*;

/**
 * TypeTestImpl.java 
 * 
 * Created: 30.07.2004
 * 
 * @author mleidig
 */
public class TypeTestImpl implements TypeTest {
    
    public int echo(int val) {
        return val;
    }
    
    public int[] echo(int[] vals) {
        return vals;
    }
    
    public float echo(float val) {
        return val;
    }
    
    public float[] echo(float[] vals) {
        return vals;
    }
    
    public double echo(double val) {
        return val;
    }
    
    public double[] echo(double[] vals) {
        return vals;
    }
    
    public Date echo(Date date) {
        return date;
    }
    
    public Date[] echo(Date[] dates) {
        return dates;
    }
    
    public String echo(String str) {
        return str;
    }
    
    public String[] echo(String[] strs) {
        return strs;
    }
    
    public Element getElement() throws Exception {
        DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
        DocumentBuilder db=dbf.newDocumentBuilder();
        Document doc=db.newDocument();
        Element elem=doc.createElement("test");
        elem.setAttribute("id","1");
        Element subElem=doc.createElement("foo");
        elem.appendChild(subElem);
        subElem.appendChild(doc.createTextNode("fasdfasdfasdf"));
        return elem;
    }
    
}
