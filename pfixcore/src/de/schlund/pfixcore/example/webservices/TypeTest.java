/*
 * de.schlund.pfixcore.example.webservices.TypeTest
 */
package de.schlund.pfixcore.example.webservices;

import java.util.Date;

import org.w3c.dom.Element;

/**
 * TypeTest.java 
 * 
 * Created: 30.07.2004
 * 
 * @author mleidig
 */
public interface TypeTest {
    
    public int echo(int val);
    
    public int[] echo(int[] vals);
    
    public float echo(float val);
    
    public float[] echo(float[] vals);
    
    public double echo(double val);
    
    public double[] echo(double[] vals);
    
    public Date echo(Date date);
    
    public Date[] echo(Date[] dates);
    
    public String echo(String str);
    
    public String[] echo(String[] strs);
    
    public Element getElement() throws Exception;
    
}
