package org.pustefixframework.util.javascript.internal;

import org.pustefixframework.util.javascript.Compressor;

import de.schlund.pfixxml.config.EnvironmentProperties;

/**
 * 
 * Locator for finding Javascript compressor implementations in the 
 * classpath. Currently only support the YUI compresssor.
 * 
 * Should be replaced by pluggable locator or configuration mechanism. 
 * 
 * @author mleidig@schlund.de
 *
 */
public class CompressorLocator {

    public static Compressor getCompressor() {
        if("prod".equals(EnvironmentProperties.getProperties().getProperty("mode"))) {
    	if(YUICompressorAdapter.isAvailable()) {
            return new YUICompressorAdapter();
        }
        } else {
        	return new WhitespaceCompressor();
        }
        return null;
    }
    
}
