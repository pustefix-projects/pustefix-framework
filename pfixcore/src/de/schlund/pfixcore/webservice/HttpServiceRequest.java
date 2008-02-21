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

package de.schlund.pfixcore.webservice;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author mleidig@schlund.de
 */
public class HttpServiceRequest implements ServiceRequest {

	HttpServletRequest httpRequest;
	String serviceName;
    String requestURI;
    String cachedMessage;
	
	public HttpServiceRequest(HttpServletRequest httpRequest) {
		this.httpRequest=httpRequest;
	}
	
    public String getServiceName() {
        if(serviceName==null) {
            serviceName=extractServiceName(httpRequest);
            if(serviceName==null) throw new IllegalArgumentException("No service name found.");
        }
        return serviceName;
    }
    
	public static String extractServiceName(HttpServletRequest httpRequest) {
        String extName=null;		
        String name=httpRequest.getPathInfo();
        if(name!=null) {
            int ind=name.lastIndexOf('/');
            if(ind>-1) {
                if((name.length()-ind)>1) {
                    extName=name.substring(ind+1);
                }
            }
        }
		return extName;
	}
	
	public Object getUnderlyingRequest() {
		return httpRequest;
	}
	
	public String getParameter(String name) {
		return httpRequest.getParameter(name);
	}
	
	public String getCharacterEncoding() {
		return httpRequest.getCharacterEncoding();
	}
	
	public String getMessage() throws IOException {
		if(httpRequest.getContentType().equals(Constants.CONTENT_TYPE_URLENCODED)) {
			return httpRequest.getParameter("message");
		} else {
            if(cachedMessage==null) {
    			//Check if content type is text/plain text/xml application/xml ?
    			String charset=httpRequest.getCharacterEncoding();
    			if(charset==null) charset="UTF-8";
    			BufferedReader in=new BufferedReader(new InputStreamReader(httpRequest.getInputStream(),charset));
    			CharArrayWriter data=new CharArrayWriter();
    			char buf[]=new char[4096];
    			int ret;
    			while((ret=in.read(buf,0,4096))!=-1) data.write(buf,0,ret);
    			cachedMessage=data.toString();
            }
            return cachedMessage;
		}
	}
	
	public Reader getMessageReader() throws IOException {
		if(httpRequest.getContentType().equals(Constants.CONTENT_TYPE_URLENCODED)) {
			String msg=httpRequest.getParameter("message");
			if(msg==null) return null;
			return new StringReader(msg);
		} else {
			//Check if content type is text/plain text/xml application/xml ?
			String charset=httpRequest.getCharacterEncoding();
			if(charset==null) charset="UTF-8";
			return new InputStreamReader(httpRequest.getInputStream(),charset);
		}
	}
	
	public InputStream getMessageStream() throws IOException {
		if(httpRequest.getContentType().equals(Constants.CONTENT_TYPE_URLENCODED)) {
			String msg=httpRequest.getParameter("message");
			if(msg==null) return null;
			String charset=httpRequest.getCharacterEncoding();
			if(charset==null) charset="UTF-8";
			byte[] bytes=msg.getBytes(charset);
			return new ByteArrayInputStream(bytes);
		} else {
			//Check if content type is text/plain text/xml application/xml ?
			return httpRequest.getInputStream();
		}
	}
    
    public String getRequestURI() {
        if(requestURI==null) {
            requestURI=getRequestURI(httpRequest);
        }
        return requestURI;
    }
    
    private static String getRequestURI(HttpServletRequest srvReq) {
        StringBuffer sb=new StringBuffer();
        sb.append(srvReq.getScheme());
        sb.append("://");
        sb.append(srvReq.getServerName());
        sb.append(":");
        sb.append(srvReq.getServerPort());
        sb.append(srvReq.getRequestURI());
        HttpSession session=srvReq.getSession(false);
        if(session!=null) {
            sb.append(Constants.SESSION_PREFIX);
            sb.append(session.getId());
        }
        String s=srvReq.getQueryString();
        if(s!=null&&!s.equals("")) {
            sb.append("?");
            sb.append(s);
        }
        return sb.toString();
    }
	
    public String getServerName() {
        return httpRequest.getServerName();
    }
    
    private void dumpHeaders(StringBuilder sb) {
        Enumeration<?> names=httpRequest.getHeaderNames();
        while(names.hasMoreElements()) {
            String name=(String)names.nextElement();
            Enumeration<?> values=httpRequest.getHeaders(name);
            while(values.hasMoreElements()) {
                String value=(String)values.nextElement();
                sb.append(name+": "+value+"\n");
            }
        }
    }
    
    private void dumpMessage(StringBuilder sb) {
        if(cachedMessage==null) {
            sb.append("[Message not available]");
        } else {
            sb.append(cachedMessage);
        }
    }
    
    public String dump() {
        StringBuilder sb=new StringBuilder();
        sb.append(getRequestURI());
        sb.append("\n\n");
        dumpHeaders(sb);
        sb.append("\n");
        dumpMessage(sb);
        return sb.toString();
    }
    
}
