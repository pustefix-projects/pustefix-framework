/*
 * de.schlund.pfixcore.webservice.MonitorMessage
 */
package de.schlund.pfixcore.webservice;

/**
 * MonitorMessage.java 
 * 
 * Created: 28.07.2004
 * 
 * @author mleidig
 */
public class MonitorMessage {
    
    private byte[] reqBody;
    private byte[] resBody;
    private String uri;
    
    public MonitorMessage(String uri) {
        this.uri=uri;
    }
    
    public String getURI() {
        return uri;
    }
    
    public byte[] getRequestBody() {
        return reqBody;
    }
    
    public void setRequestBody(byte[] reqBody) {
        this.reqBody=reqBody;
    }
    
    public byte[] getResponseBody() {
        return resBody;
    }
    
    public void setResponseBody(byte[] resBody) {
        this.resBody=resBody;
    }

}
