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
public class StreamingRequestWrapper {

    MyServletInputStream myIn;
    
    public StreamingRequestWrapper(HttpServletRequest req) throws IOException {
        myIn=new MyServletInputStream(req);
    }
        
    public ServletInputStream getInputStream() throws IOException {
        return myIn;
    }
     
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(myIn));
    }
    
    public byte[] getBytes() throws IOException {
        return myIn.getBytes();
    }
    
    class MyServletInputStream extends ServletInputStream {
        
        InputStream reqIn;
        ByteArrayOutputStream out;
        boolean wasRead;
        HttpServletRequest req;
        
        public MyServletInputStream(HttpServletRequest req) {
            this.req=req;
            out=new ByteArrayOutputStream();
        }
        
        public int read() throws IOException {
            if(reqIn==null) {
                System.out.println("read");
                reqIn=req.getInputStream();
            }
            int b=reqIn.read();
            out.write(b);
            return b;
        }
        
        public byte[] getBytes() throws IOException {
            //Read input if not already done
            int ch=-1;
            while((ch=read())!=-1) {};
            return out.toByteArray();
        }
        
    }
    
}
