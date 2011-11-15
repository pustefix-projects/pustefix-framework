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
 */

package org.pustefixframework.webservices.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author mleidig@schlund.de
 */
public class RecordingOutputStream extends OutputStream {

	ByteArrayOutputStream bytes;
	OutputStream out;
	
	public RecordingOutputStream(OutputStream out) {
		this.out=out;
		bytes=new ByteArrayOutputStream();
	}
	
	public byte[] getBytes() {
		return bytes.toByteArray();
	}
	
	@Override
	public void write(int b) throws IOException {
		bytes.write(b);
		out.write(b);
	}
	
	@Override
	public void flush() throws IOException {
		out.flush();
	}
	
	@Override
	public void write(byte[] b) throws IOException {
		bytes.write(b);
		out.write(b);
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		bytes.write(b,off,len);
		out.write(b, off, len);
	}
	
}
