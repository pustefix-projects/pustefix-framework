/*
 * de.schlund.pfixcore.example.webservices.Data
 */
package de.schlund.pfixcore.example.webservices;

/**
 * Data.java 
 * 
 * Created: 30.06.2004
 * 
 * @author mleidig
 */
public interface Data {
    
    public String exchangeData(String data,int strSize) throws Exception;
    public String[] exchangeDataArray(String[] data,int arrSize,int strSize) throws Exception;
    
}
