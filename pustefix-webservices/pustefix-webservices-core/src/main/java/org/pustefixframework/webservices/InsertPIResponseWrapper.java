/*
 * de.schlund.pfixcore.webservice.monitor.InsertPIResponseWrapper
 */
package org.pustefixframework.webservices;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * InsertPIResponseWrapper.java 
 * 
 * Created: 28.07.2004
 * 
 * @author mleidig
 */
public class InsertPIResponseWrapper extends HttpServletResponseWrapper {
    
    MyServletOutputStream myOut;
    
    public InsertPIResponseWrapper(HttpServletResponse res) throws IOException {
        super(res);
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
        
        boolean inserted=false;
        int opened=0;
        String pi="<?xml-stylesheet type=\"text/css\" href=\"blank.css\"?>";
        OutputStream reqOut;
        ByteArrayOutputStream out;
        
        public MyServletOutputStream(OutputStream reqOut) {
            this.reqOut=reqOut;
            out=new ByteArrayOutputStream();
        }
        
        public void write(int b) throws IOException {
            if(!inserted) {
                if(b=='<') opened++;
                if(opened==2) {
                    reqOut.write(pi.getBytes());
                    inserted=true;
                }
            }       
            out.write(b);
            reqOut.write(b);
        }
        
        public byte[] getBytes() {
            return out.toByteArray();
        }
        
    }
    
}
