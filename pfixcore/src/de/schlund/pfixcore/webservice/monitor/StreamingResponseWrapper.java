/*
 * de.schlund.pfixcore.webservice.monitor.StreamingResponseWrapper
 */
package de.schlund.pfixcore.webservice.monitor;

import java.io.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

/**
 * StreamingResponseWrapper.java 
 * 
 * Created: 28.07.2004
 * 
 * @author mleidig
 */
public class StreamingResponseWrapper {
    
    MyServletOutputStream myOut;
    
    public StreamingResponseWrapper(HttpServletResponse res) throws IOException {
        myOut=new MyServletOutputStream(res.getOutputStream());
    }
    
    public ServletOutputStream getOutputStream() throws IOException {
        return myOut;
    }

    public PrintWriter getWriter() throws IOException {
        return new PrintWriter(myOut);
    }
    
    public byte[] getBytes() {
        return myOut.getBytes();
    }
    
    class MyServletOutputStream extends ServletOutputStream {
        
        OutputStream reqOut;
        ByteArrayOutputStream out;
        
        public MyServletOutputStream(OutputStream reqOut) {
            this.reqOut=reqOut;
            out=new ByteArrayOutputStream();
        }
        
        public void write(int b) throws IOException {
            out.write(b);
            reqOut.write(b);
        }
        
        public byte[] getBytes() {
            return out.toByteArray();
        }
        
    }
    
}
