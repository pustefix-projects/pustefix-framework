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

package org.pustefixframework.webservices.utils;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

/**
 * @author mleidig@schlund.de
 */
public class RecordingReader extends Reader {
	
	CharArrayWriter chars;
	Reader reader;
	
	public RecordingReader(Reader reader) {
		this.reader=reader;
		chars=new CharArrayWriter();
	}

	public char[] getCharacters() {
		return chars.toCharArray();
	}
	
	public void close() throws IOException {
		reader.close();
	}

	public void mark(int readAheadLimit) throws IOException {
		//Not supported
	}

	public boolean markSupported() {
		return false;
	}

	public int read() throws IOException {
		int ch=reader.read();
		if(ch!=-1) chars.write(ch);
		return ch;
	}

	public int read(char[] cbuf, int off, int len) throws IOException {
		return reader.read(cbuf, off, len);
	}

	public int read(char[] cbuf) throws IOException {
		int no=reader.read(cbuf);
		if(no!=-1) chars.write(cbuf,0,no);
		return no;
	}

	public int read(CharBuffer target) throws IOException {
		int oldPos=target.position();
		int no=reader.read(target);
		if(no>-1) {
			char[] c=new char[no];
			int newPos=target.position();
			target.position(oldPos);
			for(int i=0;i<no;i++) c[i]=target.charAt(i);
			chars.write(c);
			target.position(newPos);
		}
		return no;
	}

	public boolean ready() throws IOException {
		return reader.ready();
	}

	public void reset() throws IOException {
		reader.reset();
	}

	public long skip(long n) throws IOException {
		return reader.skip(n);
	}
	
}
