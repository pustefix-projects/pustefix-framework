/*
 * de.schlund.pfixcore.example.webservices.DataImpl
 */
package de.schlund.pfixcore.example.webservices;

import org.pustefixframework.webservices.AbstractService;

/**
 * DataImpl.java 
 * 
 * Created: 30.06.2004
 * 
 * @author mleidig
 */
public class DataImpl extends AbstractService implements Data {
    
    public String exchangeData(String data,int strSize) throws Exception {
        ContextData ctx=(ContextData)getContextResourceManager().getResource(ContextData.class.getName());
        return ctx.exchangeData(data,strSize);
    }
    
    public String[] exchangeDataArray(String[] data,int arrSize,int strSize) throws Exception {
        ContextData ctx=(ContextData)getContextResourceManager().getResource(ContextData.class.getName());
        return ctx.exchangeDataArray(data,arrSize,strSize);
    }

}
