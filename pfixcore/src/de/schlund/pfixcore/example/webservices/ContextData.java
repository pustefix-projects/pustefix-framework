package de.schlund.pfixcore.example.webservices;

public interface ContextData {
    
    public String exchangeData(String data,int strSize) throws Exception;
    public String[] exchangeDataArray(String[] data,int arrSize,int strSize) throws Exception;
   
}
