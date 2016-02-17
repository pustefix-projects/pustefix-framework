package org.pustefixframework.util.javascript.internal;

import org.pustefixframework.util.javascript.Compressor;

/**
 * Locator for finding Javascript compressor implementations in the 
 * classpath. It will try to find the YUI Javascript Compressor first,
 * if not found it will look for the Google Closure Compiler.
 */
public class CompressorLocator {

    private static Compressor compressor;
    
    static {
        if(YUICompressorAdapter.isAvailable()) {
            compressor = new YUICompressorAdapter();
        } else {
            try {
                Class.forName("com.google.javascript.jscomp.Compiler");
                compressor = new ClosureCompilerAdapter();
            } catch (ClassNotFoundException e) {
                //ignore
            }
        }
    }
    
    public static Compressor getCompressor() {
        return compressor;
    }
    
}
