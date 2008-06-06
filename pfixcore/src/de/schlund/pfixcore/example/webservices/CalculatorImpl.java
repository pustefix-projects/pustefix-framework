/*
 * de.schlund.pfixcore.example.webservices.CalculatorImpl
 */
package de.schlund.pfixcore.example.webservices;

/**
 * CalculatorImpl.java 
 * 
 * Created: 30.06.2004
 * 
 * @author mleidig
 */
public class CalculatorImpl implements Calculator {

    public int add(int value1,int value2) {
        return value1+value2; 
    }

    public int invert(int value) {
        return -value;
    }
    
    public int subtract(int value1,int value2) {
        return value1-value2;
    }
    
    public int multiply(int value1,int value2) {
        return value1*value2;
    }
    
    public int divide(int value1,int value2) {
        return value1/value2;
    }
    
}
