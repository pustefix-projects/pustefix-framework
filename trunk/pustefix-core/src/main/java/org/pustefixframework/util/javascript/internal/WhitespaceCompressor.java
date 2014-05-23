package org.pustefixframework.util.javascript.internal;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.Writer;

import org.pustefixframework.util.javascript.Compressor;
import org.pustefixframework.util.javascript.CompressorException;

public class WhitespaceCompressor implements Compressor {

	public void compress(Reader reader, Writer writer) throws CompressorException {
		try {
			BufferedReader br = new BufferedReader(reader);
			String line = null;
			while((line = br.readLine()) != null) {
				line = line.trim();
				if(line.length() > 0) {
					writer.write(line);
					writer.write('\n');
				}
			}
		} catch(Exception x) {
			throw new CompressorException("Error during Javascript whitespace stripping", x);
		}
	}
	
}
