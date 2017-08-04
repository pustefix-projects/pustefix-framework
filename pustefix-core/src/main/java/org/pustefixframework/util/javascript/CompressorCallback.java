package org.pustefixframework.util.javascript;

import java.io.StringReader;
import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.pustefixframework.util.javascript.internal.CompressorLocator;

/**
 * XSLT extension functions for Javascript compression.
 */
public class CompressorCallback {

    private final static Logger LOG = LoggerFactory.getLogger(CompressorCallback.class);
    
    public static String compressJavascript(String javascript) {
        Compressor compressor = CompressorLocator.getCompressor();
        if(compressor != null) {
            try {
                StringReader reader = new StringReader(javascript);
                StringWriter writer = new StringWriter();
                compressor.compress(reader, writer);
                return writer.toString();
            } catch(CompressorException x) {
                LOG.error("Failed to compress inline javascript: " + javascript, x);
            }
        }
        return javascript;
    }
    
}
