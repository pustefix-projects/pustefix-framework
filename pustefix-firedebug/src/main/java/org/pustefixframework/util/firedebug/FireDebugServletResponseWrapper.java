package org.pustefixframework.util.firedebug;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * FireDebugServletResponseWrapper
 * 
 * FireDebugServletResponseWrapper is needed to wrap the HttpServletResponse
 * in order to set the response header after Pustefix has finished, because 
 * otherwise Pustefix already sends the response.
 * 
 * @author Holger Rüprich
 */

public class FireDebugServletResponseWrapper extends HttpServletResponseWrapper {
    
    FireDebugServletOutputStream fireDebugOut;
    
    public FireDebugServletResponseWrapper(HttpServletResponse res) throws IOException {
        super(res);
        fireDebugOut = new FireDebugServletOutputStream(res.getOutputStream());
    }
    
    public ServletOutputStream getOutputStream() throws IOException {
        return fireDebugOut;
    }

    public PrintWriter getWriter() throws IOException {
        return new PrintWriter(fireDebugOut);
    }
    
    class FireDebugServletOutputStream extends ServletOutputStream {
        
        OutputStream out;
        
        public FireDebugServletOutputStream(OutputStream reqOut) {
            out = reqOut;
        }
        
        public void write(int b) throws IOException {   
            out.write(b);
        }
        
    }
}
