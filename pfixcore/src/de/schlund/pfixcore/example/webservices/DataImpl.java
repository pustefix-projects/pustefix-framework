/*
 * de.schlund.pfixcore.example.webservices.DataImpl
 */
package de.schlund.pfixcore.example.webservices;

import de.schlund.pfixcore.webservice.*;
import de.schlund.pfixcore.example.ContextData;
import de.schlund.pfixcore.example.DataBean;
//import de.schlund.pfixcore.example.ComplexData;

/**
 * DataImpl.java 
 * 
 * Created: 30.06.2004
 * 
 * @author mleidig
 */
public class DataImpl extends AbstractService implements Data {
    
    public String getData() throws Exception {
        ContextData data=(ContextData)getContextResourceManager().getResource(ContextData.class.getName());
        return data.getData();
    }
    
    public String[] getDataArray() throws Exception {
        ContextData data=(ContextData)getContextResourceManager().getResource(ContextData.class.getName());
        return data.getDataArray();
    }
    
    public DataBean getDataBean() throws Exception {
        ContextData data=(ContextData)getContextResourceManager().getResource(ContextData.class.getName());
        return data.getDataBean();
    }
    
    public DataBean echoDataBean(DataBean data) throws Exception {
        return data;
    }
    
    /**
    public ComplexData getComplexData() throws Exception {
        ContextData data=(ContextData)getContextResourceManager().getResource(ContextData.class.getName());
        return data.getComplexData();
    }
    
    public ComplexData echoComplexData(ComplexData data) throws Exception {
        return data;
    }
    */
    
    public String getDataSid(String sid) throws Exception {
        ContextData data=(ContextData)getContextResource(sid,ContextData.class.getName());
        return data.getData();
    }
    
    public String[] getDataArraySid(String sid) throws Exception {
        ContextData data=(ContextData)getContextResource(sid,ContextData.class.getName());
        return data.getDataArray();
    }
    
    public DataBean getDataBeanSid(String sid) throws Exception {
        ContextData data=(ContextData)getContextResource(sid,ContextData.class.getName());
        return data.getDataBean();
    }
    
    public DataBean echoDataBeanSid(String sid,DataBean data) throws Exception {
        return data;
    }

}
