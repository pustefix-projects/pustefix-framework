/*
 * de.schlund.pfixcore.example.webservices.TypeTest
 */
package de.schlund.pfixcore.example.webservices;

import java.util.Date;

import org.w3c.dom.Element;

import de.schlund.pfixcore.example.DataBean;


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
    
    public float echoFloat(float val);
    
    public float[] echoFloatArray(float[] vals);
    
    public double echoDouble(double val);
    
    public double[] echoDoubleArray(double[] vals);
    
    public Date echoDate(Date date);
    
    public Date[] echoDateArray(Date[] dates);
    
    public String echoString(String str);
    
    public String[] echoStringArray(String[] strs);
    
    public Element echoElement(Element elem) throws Exception;
    
    public DataBean echoDataBean(DataBean data);
    
    public DataBean[] echoDataBeanArray(DataBean[] data);
    
}
