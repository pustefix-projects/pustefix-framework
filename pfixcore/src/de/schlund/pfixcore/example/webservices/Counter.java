/*
 * test.Counter
 */
package de.schlund.pfixcore.example.webservices;

/**
 * Counter.java 
 * 
 * Created: 28.06.2004
 * 
 * @author mleidig
 */
public interface Counter {

    public int getValue() throws Exception;
    public boolean setValue(int value) throws Exception;
    public int addValue(int value) throws Exception;
    public int subtractValue(int value) throws Exception;
    public boolean reset() throws Exception;
    
}
