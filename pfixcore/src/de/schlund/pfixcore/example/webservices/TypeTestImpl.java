/*
 * de.schlund.pfixcore.example.webservices.TypeTestImpl
 */
package de.schlund.pfixcore.example.webservices;

import java.util.Calendar;
import org.w3c.dom.*;

import de.schlund.pfixcore.example.DataBean;

/**
 * TypeTestImpl.java 
 * 
 * Created: 30.07.2004
 * 
 * @author mleidig
 */
public class TypeTestImpl implements TypeTest {
    
    public String info() {
        return "TypeTest";
    }
    
    public int echoInt(int val) {
        return val;
    }
    
    public int[] echoIntArray(int[] vals) {
        return vals;
    }
    
    public float echoFloat(float val) {
        return val;
    }
    
    public float[] echoFloatArray(float[] vals) {
        return vals;
    }
    
    public double echoDouble(double val) {
        return val;
    }
    
    public double[] echoDoubleArray(double[] vals) {
        return vals;
    }
    
    public Calendar echoDate(Calendar date) {
        return date;
    }
    
    public Calendar[] echoDateArray(Calendar[] dates) {
        return dates;
    }
    
    public String echoString(String str) {
        return str;
    }
    
    public String[] echoStringArray(String[] strs) {
        return strs;
    }
    
    public Object[] echoObjectArray(Object[] objs) {
        return objs;
    }
    
    public Element echoElement(Element elem) throws Exception {
        return elem;
    }
    
    public DataBean echoDataBean(DataBean data) {
        return data;
    }
    
    public DataBean[] echoDataBeanArray(DataBean[] data) {
        return data;
    }
    
}
