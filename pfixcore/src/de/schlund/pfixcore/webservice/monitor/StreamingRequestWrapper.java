/*
 * de.schlund.pfixcore.webservice.monitor.StreamingRequestWrapper
 */
package de.schlund.pfixcore.webservice.monitor;

import java.io.*;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

/**
 * StreamingRequestWrapper.java 
 * 
 * Created: 28.07.2004
 * 
 * @author mleidig
 */
public class StreamingRequestWrapper extends MonitorRequestWrapper {

    MyServletInputStream myIn;
    
    public StreamingRequestWrapper(HttpServletRequest req) throws IOException {
        super(req);
        myIn=new MyServletInputStream(req.getInputStream());
    }
        
    public ServletInputStream getInputStream() throws IOException {
        return myIn;
    }
     
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(myIn));
    }
    
    public byte[] getBytes() {
        return myIn.getBytes();
    }
    
    class MyServletInputStream extends ServletInputStream {
        
        InputStream reqIn;
        ByteArrayOutputStream out;
        
        public MyServletInputStream(InputStream reqIn) {
            this.reqIn=reqIn;
            out=new ByteArrayOutputStream();
        }
        
        public int read() throws IOException {
            int b=reqIn.read();
            out.write(b);
            return b;
        }
        
        public byte[] getBytes() {
        	return out.toByteArray();
        }
        
    }
    
}
