package de.schlund.pfixcore.example;

import de.schlund.pfixcore.example.webservices.*;

public interface ContextData {
    
    public String getData();
    public String[] getDataArray();
    public DataBean getDataBean();
    public ComplexDataBean getComplexDataBean();
    
}
