package org.pustefixframework.util;

import java.io.File;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.log4j.Logger;

public class FrameworkInfo {

    private static Logger LOG = Logger.getLogger(FrameworkInfo.class);
    
    private static String version;
    
    static {
        try {
            URL url = FrameworkInfo.class.getProtectionDomain().getCodeSource().getLocation();
            if(url != null) {
                if("file".equals(url.getProtocol())) {
                    File file = new File(url.toURI());
                    if(!file.isDirectory()) {
                        JarFile jarFile = new JarFile(file);
                        Manifest manifest = jarFile.getManifest();
                        version = manifest.getMainAttributes().getValue("Specification-Version");
                    }
                }
            }
        } catch(Exception x) {
            LOG.error("Can't get Pustefix framework version", x);
        }
        if(version == null) version = "n/a";
    }
    
    public static String getVersion() {
        return version;
    }

}
