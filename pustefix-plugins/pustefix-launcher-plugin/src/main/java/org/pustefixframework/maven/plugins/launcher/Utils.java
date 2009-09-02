package org.pustefixframework.maven.plugins.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class Utils {

    public static String getBundleSymbolicNameFromJar(File file) {
        try {
            JarFile jarFile = new JarFile(file);
            Manifest manifest = jarFile.getManifest();
            String bundleSymbolicName = manifest.getMainAttributes().getValue("Bundle-SymbolicName");
            return bundleSymbolicName;
        } catch(IOException x) {
            throw new RuntimeException("Error reading MANIFEST.MF attribute from: " + file.getAbsolutePath(), x);
        }
    }
    
    public static String getBundleSymbolicNameFromProject(File dir) {
        File file = new File(dir, "target/classes/META-INF/MANIFEST.MF");
        try {
            Manifest manifest = new Manifest(new FileInputStream(file));
            String bundleSymbolicName = manifest.getMainAttributes().getValue("Bundle-SymbolicName");
            return bundleSymbolicName;
        } catch(IOException x) {
            throw new RuntimeException("Error reading MANIFEST.MF attribute from: " + file.getAbsolutePath(), x);
        }
    }
  
}
