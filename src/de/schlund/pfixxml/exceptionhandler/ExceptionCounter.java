package de.schlund.pfixxml.exceptionhandler;

public class ExceptionCounter {
    private ExceptionCounter() {}
    private static ExceptionCounter instance = new ExceptionCounter();
    private long countRuntime = 0;
    private long countChecked = 0;
    private long countErrors = 0;
    
    public static ExceptionCounter getInstance() {
        return instance;
    }
    
    public void increaseRuntime() {
        countRuntime++;
    }
    
    public void increaseChecked() {
        countChecked++;
    }
    
    public void increaseErrors() {
        countErrors++;
    }
    
    public long getNumberOfRuntimeExceptions() {
        return countRuntime;
    }
    
    public long getNumberOfCheckedExceptions() {
        return countChecked;
    }
    
    public long getNumberOfErrors() {
        return countErrors;
    }

}
