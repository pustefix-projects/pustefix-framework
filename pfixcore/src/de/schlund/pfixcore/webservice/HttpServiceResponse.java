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

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import javax.servlet.http.HttpServletResponse;

/**
 * @author mleidig@schlund.de
 */
public class HttpServiceResponse implements ServiceResponse {

	HttpServletResponse httpResponse;
	
	public HttpServiceResponse(HttpServletResponse httpResponse) {
		this.httpResponse=httpResponse;
	}
	
	public Object getUnderlyingResponse() {
		return httpResponse;
	}
	
	public void setContentType(String ctype) {
		 httpResponse.setContentType(ctype);
	}
	
	public void setCharacterEncoding(String encoding) {
		 httpResponse.setCharacterEncoding(encoding);
	}
	
	public String getCharacterEncoding() {
		return httpResponse.getCharacterEncoding();
	}
	
	public void setMessage(String message) throws IOException {
		byte[] bytes=message.getBytes("UTF-8");	
		OutputStream out=httpResponse.getOutputStream();
		out.write(bytes);
		out.flush();
		out.close();
	}
	
	public Writer getMessageWriter() throws IOException {
		return httpResponse.getWriter();
	}
	
	public OutputStream getMessageStream() throws IOException {
		return httpResponse.getOutputStream();
	}
	
}
