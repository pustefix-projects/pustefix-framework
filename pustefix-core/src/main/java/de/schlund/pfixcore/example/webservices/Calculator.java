/*
 * de.schlund.pfixcore.example.webservices.Calculator
 */
package de.schlund.pfixcore.example.webservices;

/**
 * Calculator.java 
 * 
 * Created: 30.06.2004
 * 
 * @author mleidig
 */
public interface Calculator {

    public int add(int value1,int value2);
    public int invert(int value);
    public int subtract(int value1,int value2); 
    public int multiply(int value1,int value2);
    public int divide(int value1,int value2);
    
}
