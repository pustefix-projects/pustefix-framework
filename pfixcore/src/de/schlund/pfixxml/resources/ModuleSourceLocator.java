package de.schlund.pfixxml.resources;

import java.io.File;
import java.net.URL;

/**
 * Implementors can provide a source or unpacked directory for a pustefix module 
 * specified by the jar file's URL. Thus resources are loaded from this location
 * instead of the jar file. 
 * 
 * @author mleidig@schlund.de
 *
 */
public interface ModuleSourceLocator {

    public File getLocation(URL jarUrl);
    
}
