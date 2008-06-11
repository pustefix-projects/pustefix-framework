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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author mleidig@schlund.de
 */
public class RecordingInputStream extends InputStream {
	
	ByteArrayOutputStream bytes;
	InputStream in;
	
	RecordingInputStream(InputStream in) {
		this.in=in;
		bytes=new ByteArrayOutputStream();
	}
	
	public byte[] getBytes() {
		return bytes.toByteArray();
	}
	
	@Override
	public int available() throws IOException {
		return in.available();
	}
	
	@Override
	public void close() throws IOException {
		in.close();
	}
	
	@Override
	public synchronized void mark(int readlimit) {
		//Not supported
	}
	
	@Override
	public boolean markSupported() {
		return false;
	}
	
	@Override
	public int read() throws IOException {
		int b=in.read();
		if(b!=-1) bytes.write(b);
		return b;
	}
	
	@Override
	public int read(byte[] b) throws IOException {
		int no=in.read(b);
		if(no!=-1) bytes.write(b,0,no);
		return no;
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int no=in.read(b,off,len);
		if(no!=-1) bytes.write(b,off,no);
		return no;
	}
	
	@Override
	public synchronized void reset() throws IOException {
		in.reset();
	}
	
	@Override
	public long skip(long n) throws IOException {
		return in.skip(n);
	}
	
}