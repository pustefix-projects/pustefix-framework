/*
 * de.schlund.pfixcore.webservice.monitor.PrefetchingResponseWrapper
 */
package de.schlund.pfixcore.webservice.monitor;

import java.io.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

/**
 * PrefetchingResponseWrapper.java 
 * 
 * Created: 28.07.2004
 * 
 * @author mleidig
 */
public class PrefetchingResponseWrapper {
    
    MyServletOutputStream myOut;
    
    public PrefetchingResponseWrapper(HttpServletResponse res) {
        myOut=new MyServletOutputStream();
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
        
        ByteArrayOutputStream out;
        
        public MyServletOutputStream() {
            out=new ByteArrayOutputStream();
        }
        
        public void write(int b) throws IOException {
            out.write(b);
        }
        
        public byte[] getBytes() {
            return out.toByteArray();
        }
        
    }
    
}
