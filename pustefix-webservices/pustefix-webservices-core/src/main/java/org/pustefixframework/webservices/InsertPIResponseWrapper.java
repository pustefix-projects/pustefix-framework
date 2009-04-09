/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
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
    
    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return myOut;
    }

    @Override
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
        
        @Override
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
