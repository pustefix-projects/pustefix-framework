/*
 * de.schlund.pfixcore.webservice.monitor.PrefetchingRequestWrapper
 */
package de.schlund.pfixcore.webservice.monitor;

import java.io.*;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

/**
 * PrefetchingRequestWrapper.java 
 * 
 * Created: 28.07.2004
 * 
 * @author mleidig
 */
public class PrefetchingRequestWrapper extends MonitorRequestWrapper {

    byte[] bytes;
    MyServletInputStream myIn;
    
    public PrefetchingRequestWrapper(HttpServletRequest req) throws IOException {
        super(req);
        bytes=new byte[req.getContentLength()];
        InputStream in=req.getInputStream();
        int len=in.read(bytes);
        in.close();
        myIn=new MyServletInputStream(bytes);
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
