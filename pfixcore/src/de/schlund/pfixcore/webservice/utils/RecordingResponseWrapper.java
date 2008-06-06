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

package de.schlund.pfixcore.webservice.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import de.schlund.pfixcore.webservice.ServiceResponse;
import de.schlund.pfixcore.webservice.ServiceResponseWrapper;

/**
 * @author mleidig@schlund.de
 */
public class RecordingResponseWrapper extends ServiceResponseWrapper {
	
	String message;
	RecordingOutputStream recStream;
	RecordingWriter recWriter;

	public RecordingResponseWrapper(ServiceResponse response) {
		super(response);
	}
	
	@Override
	public void setMessage(String message) throws IOException {
		this.message=message;
		super.setMessage(message);
	}
	
	@Override
	public Writer getMessageWriter() throws IOException {
		recWriter=new RecordingWriter(super.getMessageWriter());
		return recWriter;
	}
	
	@Override
	public OutputStream getMessageStream() throws IOException {
		recStream=new RecordingOutputStream(super.getMessageStream());
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
		if(recWriter!=null) {
			char[] chars=recWriter.getCharacters();
			return new String(chars);
		}
		return null;
	}
	
	public void setRecordedMessage(String message) {
		this.message=message;
	}
	
}
