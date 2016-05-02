package org.pustefixframework.util.javascript;

import java.io.Reader;
import java.io.Writer;

/**
 * Interface to be implemented by adapter for Javascript compressor implementation.
 */
public interface Compressor {

    public void compress(Reader reader, Writer writer) throws CompressorException;
    
}
