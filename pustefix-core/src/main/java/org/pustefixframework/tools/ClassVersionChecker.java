package org.pustefixframework.tools;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 
 * Checks the class file version compatibility of class
 * files (directly on the file system or within jar files).
 * 
 * @author mleidig@schlund.de
 *
 */
public class ClassVersionChecker {
    
    private static Map<String,String> versions = new HashMap<String,String>();
    
    static {
        versions.put("45.3", "1.1");
        versions.put("46.0", "1.2");
        versions.put("47.0", "1.3");
        versions.put("48.0", "1.4");
        versions.put("49.0", "1.5");
        versions.put("50.0", "1.6");
    }
    
    public static boolean supportsJavaVersion(String javaVersion) {
        return versions.values().contains(javaVersion);
    }

    public static String getJavaVersion(String classVersion) {
        String version = versions.get(classVersion);
        if(version == null) version = "unknown";
        return version;
    }
    
    public static int checkCompatibility(File file, String targetJavaVersion) throws IOException {
        int count = 0;
        if(file.isDirectory()) {
            File[] children = file.listFiles();
            for(File child:children) {
                count += checkCompatibility(child, targetJavaVersion);
            }
        } else {
            if(file.getName().endsWith(".class")) {
                InputStream in = new FileInputStream(file);
                String version = getJavaVersion(in);
                if(version.compareTo(targetJavaVersion)>0) {
                    System.err.println("Incompatible class file version: "+file.getCanonicalPath()+ " " + version + " > " + targetJavaVersion);
                    count++;
                }
            } else if(file.getName().endsWith(".jar")) {
                JarFile jar = new JarFile(file);
                Enumeration<JarEntry> entries = jar.entries();
                while(entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if(!entry.isDirectory()) {
                        if(entry.getName().endsWith(".class")) {
                            InputStream in = jar.getInputStream(entry);
                            String version = getJavaVersion(in);
                            if(version.compareTo(targetJavaVersion)>0) {
                                System.err.println("Incompatible class file version: "+  file.getCanonicalPath() + "@" + entry.toString() + " " + version + " > " + targetJavaVersion);
                                count++;
                            }
                        }
                    }
                }
            }
        }
        return count;
    }
    
    public static String getJavaVersion(InputStream is) throws IOException {
        return getJavaVersion(getClassVersion(is));
    }
 
    public static String getClassVersion(InputStream is) throws IOException {
        DataInputStream in = new DataInputStream(is);
        try {
            int magic = in.readInt();
            if(magic == 0xcafebabe) {
                int minor = in.readUnsignedShort();
                int major = in.readUnsignedShort();
                return major + "." + minor;
            }
        } finally {
            in.close();
        }
        return null;
    }
       
}
