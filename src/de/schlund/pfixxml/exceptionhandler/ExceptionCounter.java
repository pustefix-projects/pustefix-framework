package de.schlund.pfixxml.exceptionhandler;

public class ExceptionCounter {
    private ExceptionCounter() {}
    private static ExceptionCounter instance = new ExceptionCounter();
    private long count = 0;
    
    public static ExceptionCounter getInstance() {
        return instance;
    }
    
    public void increase() {
        count++;
    }
    
    public long getNumberOfExceptions() {
        return count;
    }

}
