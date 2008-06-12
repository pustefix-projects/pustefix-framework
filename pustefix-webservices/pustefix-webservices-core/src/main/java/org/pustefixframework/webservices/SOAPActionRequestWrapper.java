/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package org.pustefixframework.webservices;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * SOAPActionRequestWrapper.java 
 * 
 * Created: 16.08.2004
 * 
 * @author mleidig
 */
public class SOAPActionRequestWrapper extends HttpServletRequestWrapper {

    String soapMsg;
    String soapAction="\"\"";
    MyServletInputStream myIn;
    
    public SOAPActionRequestWrapper(HttpServletRequest req) throws IOException {
        super(req);
        if(req.getHeader(Constants.HEADER_SOAP_ACTION)==null) {
            soapMsg=req.getParameter(Constants.PARAM_SOAP_MESSAGE);
            if(soapMsg!=null) {
                myIn=new MyServletInputStream();
            }
        }
    }
    
    public String getHeader(String name) {
        if(name.equals("SOAPAction")) return soapAction;
        return super.getHeader(name);
    }
    
    public ServletInputStream getInputStream() throws IOException {
        return myIn;
    }
     
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(myIn));
    }
    
    class MyServletInputStream extends ServletInputStream {
        
        ByteArrayInputStream in;
        
        public MyServletInputStream() {
            in=new ByteArrayInputStream(soapMsg.getBytes());
        }
        
        public int read() throws IOException {
            return in.read();
        }
        
    }
    
}
