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

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * @author mleidig@schlund.de
 */
public class RecordingWriter extends Writer {

	CharArrayWriter chars;
	Writer writer;
	
	public RecordingWriter(Writer writer) {
		this.writer=writer;
		chars=new CharArrayWriter();
	}

	public char[] getCharacters() {
		return chars.toCharArray();
	}
	
	@Override
	public Writer append(char c) throws IOException {
		chars.append(c);
		return writer.append(c);
	}

	@Override
	public Writer append(CharSequence csq, int start, int end) throws IOException {
		chars.append(csq,start,end);
		return writer.append(csq, start, end);
	}

	@Override
	public Writer append(CharSequence csq) throws IOException {
		chars.append(csq);
		return writer.append(csq);
	}

	@Override
	public void close() throws IOException {
		writer.close();
	}

	@Override
	public void flush() throws IOException {
		writer.flush();
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		chars.write(cbuf,off,len);
		writer.write(cbuf, off, len);
	}

	@Override
	public void write(char[] cbuf) throws IOException {
		chars.write(cbuf);
		writer.write(cbuf);
	}

	@Override
	public void write(int c) throws IOException {
		chars.write(c);
		writer.write(c);
	}

	@Override
	public void write(String str, int off, int len) throws IOException {
		chars.write(str,off,len);
		writer.write(str, off, len);
	}

	@Override
	public void write(String str) throws IOException {
		chars.write(str);
		writer.write(str);
	}
	
}
