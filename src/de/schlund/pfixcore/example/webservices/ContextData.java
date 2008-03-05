package de.schlund.pfixcore.example.webservices;

import de.schlund.pfixcore.workflow.ContextResource;

public interface ContextData extends ContextResource {
    
    public String exchangeData(String data,int strSize) throws Exception;
    public String[] exchangeDataArray(String[] data,int arrSize,int strSize) throws Exception;
   
}
