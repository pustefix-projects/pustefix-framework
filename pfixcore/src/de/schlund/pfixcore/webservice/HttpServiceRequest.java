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

import javax.servlet.http.HttpServletRequest;

/**
 * @author mleidig@schlund.de
 */
public class HttpServiceRequest implements ServiceRequest {

	HttpServletRequest httpRequest;
	String serviceName;
	
	public HttpServiceRequest(HttpServletRequest httpRequest) {
		this.httpRequest=httpRequest;
	}
	
	public String getServiceName() {
		if(serviceName==null) {
			String name=httpRequest.getPathInfo();
			if(name==null) throw new IllegalArgumentException("No service name found.");
			int ind=name.lastIndexOf('/');
			if(ind<0) throw new IllegalArgumentException("No service name found.");
			else {
				if(!((name.length()-ind)>1)) throw new IllegalArgumentException("No service name found.");
				serviceName=name.substring(ind+1);
			}
		}
		return serviceName;
	}
	
	public Object getServiceObject() {
		// TODO Auto-generated method stub
		return null;
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
			//Check if content type is text/plain text/xml application/xml ?
			String charset=httpRequest.getCharacterEncoding();
			if(charset==null) charset="UTF-8";
			BufferedReader in=new BufferedReader(new InputStreamReader(httpRequest.getInputStream(),charset));
			CharArrayWriter data=new CharArrayWriter();
			char buf[]=new char[4096];
			int ret;
			while((ret=in.read(buf,0,4096))!=-1) data.write(buf,0,ret);
			return data.toString();
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
	
}
