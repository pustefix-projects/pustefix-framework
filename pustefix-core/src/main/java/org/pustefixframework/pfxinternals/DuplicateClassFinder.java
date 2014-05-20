package org.pustefixframework.pfxinternals;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

public class DuplicateClassFinder {
    
    public static Map<String, String[]> find(Pattern includePattern, Pattern excludePattern) throws Exception {

        Map<String, String[]> classToLocation= new HashMap<String, String[]>();
        
        URLClassLoader cl = (URLClassLoader)DuplicateClassFinder.class.getClassLoader();
        URL[] urls = cl.getURLs();
        for(URL url: urls) {
            if(url.getProtocol().equals("file")) {
                if(url.getPath().endsWith(".jar")) {
                    JarFile file = new JarFile(new File(url.toURI()));
                    Enumeration<JarEntry> e = file.entries();
                    while(e.hasMoreElements()) {
                        JarEntry entry = e.nextElement();
                        if(entry.getName().endsWith(".class")) {
                            String className = entry.getName();
                            className = className.substring(0, className.length() -6).replace('/', '.');
                            if(includePattern.matcher(className).matches() && !excludePattern.matcher(className).matches()) {
                                String[] locations = classToLocation.get(className);
                                if(locations == null) {
                                    locations = new String[] {url.toString()};
                                } else {
                                    locations = Arrays.copyOf(locations, locations.length + 1);
                                    locations[locations.length -1] = url.toString();
                                }
                                classToLocation.put(className, locations);
                            }
                        }
                    }
                } else {
                    File dir = new File(url.toURI());
                    if(dir.getAbsolutePath().endsWith("/WEB-INF/classes")) {
                        walk(dir, dir, classToLocation, includePattern, excludePattern);
                    }
                    
                }
            }
        }
        
        return classToLocation;
    }
    
    private static void walk(File dir, File baseDir, Map<String, String[]> classToLocation, Pattern includePattern, Pattern excludePattern) {
        File[] files = dir.listFiles();
        for(File file: files) {
            if(file.isDirectory()) {
                walk(file, baseDir, classToLocation, includePattern, excludePattern);
            } else if(file.getName().endsWith(".class")){
                String className = file.getAbsolutePath().substring(baseDir.getAbsolutePath().length() + 1);
                className = className.substring(0, className.length() -6).replace('/', '.');
                if(includePattern.matcher(className).matches() && !excludePattern.matcher(className).matches()) {
                    String[] locations = classToLocation.get(className);
                    if(locations == null) {
                        locations = new String[] {baseDir.getAbsolutePath()};
                    } else {
                        locations = Arrays.copyOf(locations, locations.length + 1);
                        locations[locations.length -1] = baseDir.getAbsolutePath();
                    }
                    classToLocation.put(className, locations);
                }
            }
        }
    }
    
}
