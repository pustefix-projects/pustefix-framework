package org.pustefixframework.util.javascript;

import java.io.Reader;
import java.io.Writer;

/**
 * 
 * Interface to Javascript compression implementations.
 * 
 * @author mleidig@schlund.de
 *
 */
public interface Compressor {

    public void compress(Reader reader, Writer writer) throws CompressorException;
    
}
