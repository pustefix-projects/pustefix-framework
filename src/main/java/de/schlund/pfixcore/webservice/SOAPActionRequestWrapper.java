/*
 * de.schlund.pfixcore.webservice.SOAPActionRequestWrapper
 */
package de.schlund.pfixcore.webservice;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * SOAPActionRequestWrapper.java 
 * 
 * Created: 16.08.2004
 * 
 * @author mleidig
 */
public class SOAPActionRequestWrapper extends HttpServletRequestWrapper {

    String soapMsg;
    String soapAction="\"\"";
    MyServletInputStream myIn;
    
    public SOAPActionRequestWrapper(HttpServletRequest req) throws IOException {
        super(req);
        if(req.getHeader(Constants.HEADER_SOAP_ACTION)==null) {
            soapMsg=req.getParameter(Constants.PARAM_SOAP_MESSAGE);
            if(soapMsg!=null) {
                myIn=new MyServletInputStream();
            }
        }
    }
    
    public String getHeader(String name) {
        if(name.equals("SOAPAction")) return soapAction;
        return super.getHeader(name);
    }
    
    public ServletInputStream getInputStream() throws IOException {
        return myIn;
    }
     
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(myIn));
    }
    
    class MyServletInputStream extends ServletInputStream {
        
        ByteArrayInputStream in;
        
        public MyServletInputStream() {
            in=new ByteArrayInputStream(soapMsg.getBytes());
        }
        
        public int read() throws IOException {
            return in.read();
        }
        
    }
    
}
