package org.pustefixframework.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Manifest;

import org.apache.log4j.Logger;

public class FrameworkInfo {
    
    private static Logger LOG = Logger.getLogger(FrameworkInfo.class);
    
    private static String version;
    
    static {
        try {
            InputStream in = FrameworkInfo.class.getClassLoader().getResourceAsStream("META-INF/MANIFEST.MF");
            Manifest manifest = new Manifest(in);
            version = manifest.getMainAttributes().getValue("Specification-Version");
            in.close();
        } catch(IOException x) {
            LOG.error("Can't get version info", x);
            version = "n/a";
        }
    }
    
    public static String getVersion() {
        return version;
    }

}
