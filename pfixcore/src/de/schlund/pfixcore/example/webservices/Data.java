/*
 * de.schlund.pfixcore.example.webservices.Data
 */
package de.schlund.pfixcore.example.webservices;

import de.schlund.pfixcore.example.DataBean;
//import de.schlund.pfixcore.example.ComplexData;

/**
 * Data.java 
 * 
 * Created: 30.06.2004
 * 
 * @author mleidig
 */
public interface Data {
    
    public String getData() throws Exception;
    public String[] getDataArray() throws Exception;
    //public DataBean getDataBean() throws Exception;
    //public DataBean echoDataBean(DataBean data) throws Exception;
    //public ComplexData getComplexData() throws Exception;
    //public ComplexData echoComplexData(ComplexData data) throws Exception;
    //public String getDataSid(String sid) throws Exception;
    //public String[] getDataArraySid(String sid) throws Exception;
    //public DataBean getDataBeanSid(String sid) throws Exception;
    //public DataBean echoDataBeanSid(String sid,DataBean data) throws Exception;
    
}
