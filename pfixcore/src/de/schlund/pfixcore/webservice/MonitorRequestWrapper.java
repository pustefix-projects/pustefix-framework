/*
 * de.schlund.pfixcore.webservice.MonitorRequestWrapper
 */
package de.schlund.pfixcore.webservice;

import java.io.*;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletRequest;

/**
 * MonitorRequestWrapper.java 
 * 
 * Created: 28.07.2004
 * 
 * @author mleidig
 */
public class MonitorRequestWrapper extends HttpServletRequestWrapper {

    byte[] bytes;
    MyServletInputStream myIn;
    
    
    public MonitorRequestWrapper(HttpServletRequest req) {
        super(req);
        try {
            bytes=new byte[req.getContentLength()];
            InputStream in=req.getInputStream();
            int len=in.read(bytes);
            in.close();
            myIn=new MyServletInputStream(bytes);
        } catch(IOException x) {
            x.printStackTrace();
        }
    }
        
    public ServletInputStream getInputStream() throws IOException {
        return myIn;
    }
     
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(myIn));
    }
    
    public byte[] getBytes() {
        return bytes;
    }
    
    class MyServletInputStream extends ServletInputStream {
        
        ByteArrayInputStream in;
        
        public MyServletInputStream(byte[] bytes) {
            in=new ByteArrayInputStream(bytes);
        }
        
        public int read() throws IOException {
            return in.read();
        }
        
    }
    
}
