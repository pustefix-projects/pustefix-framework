package org.pustefixframework.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.apache.log4j.Logger;

public class FrameworkInfo {

    private final static Logger LOG = Logger.getLogger(FrameworkInfo.class);
    
    private final static String version = detectVersion();
    private final static String scmUrl = detectSCMUrl();
    
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
    
    private static String detectSCMUrl() {
        String detected = null;
        try {
            URL url = FrameworkInfo.class.getProtectionDomain().getCodeSource().getLocation();
            if(url != null) {
                if("file".equals(url.getProtocol())) {
                    File file = new File(url.toURI());
                    if(!file.isDirectory()) {
                        JarFile jarFile = new JarFile(file);
                        ZipEntry entry = jarFile.getEntry("META-INF/SVN_REVISION");
                        if(entry != null) {
                            InputStream in = jarFile.getInputStream(entry);
                            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "utf-8"));
                            String line = null;
                            while((line = reader.readLine()) != null && detected == null) {
                                if(line.startsWith("URL:") && line.length()>4) {
                                    detected = line.substring(5).trim();
                                }
                            }
                            reader.close();
                        }
                    }
                }
            }
        } catch(Exception x) {
            LOG.error("Can't get Pustefix SCM URL", x);
        }
        if(detected == null) detected = "n/a";
        return detected;
    }
    
    public static String getVersion() {
        return version;
    }
    
    public static String getSCMUrl() {
        return scmUrl;
    }

}
