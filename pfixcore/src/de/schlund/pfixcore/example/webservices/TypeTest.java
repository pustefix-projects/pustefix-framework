/*
 * de.schlund.pfixcore.example.webservices.TypeTest
 */
package de.schlund.pfixcore.example.webservices;

import java.util.Calendar;

import org.w3c.dom.Element;
import java.util.HashMap;

/**
 * TypeTest.java 
 * 
 * Created: 30.07.2004
 * 
 * @author mleidig
 */
public interface TypeTest {
    
    public String info();
    
    public int echoInt(int val);
    
    public int[] echoIntArray(int[] vals);
    
    public long echoLong(long val);
    
    public float echoFloat(float val);
    
    public float[] echoFloatArray(float[] vals);
    
    public double echoDouble(double val);
    
    public double[] echoDoubleArray(double[] vals);
    
    public boolean echoBoolean(boolean val);
    
    public boolean[] echoBooleanArray(boolean[] vals);
    
    public Boolean echoBooleanObject(Boolean val);
    
    public Calendar echoDate(Calendar date);
    
    public Calendar[] echoDateArray(Calendar[] dates);
    
    public String echoString(String str);
    
    public String[] echoStringArray(String[] strs);
    
    public String[][] echoStringMultiArray(String[][] strs); 
    
    public Object echoObject(Object obj);
    
    public Object[] echoObjectArray(Object[] objs);
    
    public Element echoElement(Element elem);
    
    public Element[] echoElementArray(Element[] elems);
    
    public DataBean echoDataBean(DataBean data);
    
    public DataBean[] echoDataBeanArray(DataBean[] data);
    
    public HashMap echoHashMap(HashMap map);
    
}
