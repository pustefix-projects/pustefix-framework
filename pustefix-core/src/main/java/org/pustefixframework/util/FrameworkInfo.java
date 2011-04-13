package org.pustefixframework.util;

import java.io.File;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.log4j.Logger;

public class FrameworkInfo {

    private final static Logger LOG = Logger.getLogger(FrameworkInfo.class);
    
    private final static String version = detectVersion();
    
    private static String detectVersion() {
        String detected = null;
        try {
            URL url = FrameworkInfo.class.getProtectionDomain().getCodeSource().getLocation();
            if(url != null) {
                if("file".equals(url.getProtocol())) {
                    File file = new File(url.toURI());
                    if(!file.isDirectory()) {
                        JarFile jarFile = new JarFile(file);
                        Manifest manifest = jarFile.getManifest();
                        detected = manifest.getMainAttributes().getValue("Specification-Version");
                    }
                }
            }
        } catch(Exception x) {
            LOG.error("Can't get Pustefix framework version", x);
        }
        if(detected == null) detected = "n/a";
        return detected;
    }
    
    public static String getVersion() {
        return version;
    }

}
