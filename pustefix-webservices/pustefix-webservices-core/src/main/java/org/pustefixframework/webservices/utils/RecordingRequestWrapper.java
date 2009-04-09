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

package org.pustefixframework.webservices.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.pustefixframework.webservices.ServiceRequest;
import org.pustefixframework.webservices.ServiceRequestWrapper;


/**
 * @author mleidig@schlund.de
 */
public class RecordingRequestWrapper extends ServiceRequestWrapper {
	
	String message;
	RecordingInputStream recStream;
	RecordingReader recReader;
	
	public RecordingRequestWrapper(ServiceRequest request) {
		super(request);
	}
	
	@Override
	public String getMessage() throws IOException {
		message=super.getMessage();
		return message;
	}
	
	@Override
	public Reader getMessageReader() throws IOException {
		recReader=new RecordingReader(super.getMessageReader());
		return recReader;
	}
	
	@Override
	public InputStream getMessageStream() throws IOException {
		recStream=new RecordingInputStream(super.getMessageStream());
		return recStream;
	}
	
	public String getRecordedMessage() {
		if(message!=null) return message;
		if(recStream!=null) {
			byte[] bytes=recStream.getBytes();
			String charset=super.getCharacterEncoding();
			if(charset==null) charset="UTF-8";
			try {
				return new String(bytes,charset);
			} catch(UnsupportedEncodingException x) {
				throw new RuntimeException("Encoding '"+charset+"' not supported.");
			}
		}
		if(recReader!=null) {
			char[] chars=recReader.getCharacters();
			return new String(chars);
		}
		return null;
	}

	public void setRecordedMessage(String message) {
		this.message=message;
	}
	
}
