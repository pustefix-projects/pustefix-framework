/*
 * de.schlund.pfixcore.webservice.MonitorResponseWrapper
 */
package de.schlund.pfixcore.webservice;

import java.io.*;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * MonitorResponseWrapper.java 
 * 
 * Created: 28.07.2004
 * 
 * @author mleidig
 */
public class MonitorResponseWrapper extends HttpServletResponseWrapper {
    
    MyServletOutputStream myOut;
    
    public MonitorResponseWrapper(HttpServletResponse res) {
        super(res);
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
