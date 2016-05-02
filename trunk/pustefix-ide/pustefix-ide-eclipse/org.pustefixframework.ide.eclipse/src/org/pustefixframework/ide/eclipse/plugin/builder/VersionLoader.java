package org.pustefixframework.ide.eclipse.plugin.builder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;
import java.util.TreeSet;

import org.pustefixframework.ide.eclipse.plugin.util.PustefixVersion;

public class VersionLoader {
    
    private TreeSet<PustefixVersion> versions = new TreeSet<PustefixVersion>();
    
    public VersionLoader() throws IOException {
        InputStream in = getClass().getResourceAsStream("versions.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line = null;
        while((line = reader.readLine()) != null) {
            line = line.trim();
            PustefixVersion version = PustefixVersion.parseVersion(line);
            versions.add(version);
        }
    }
    
    public URL loadResource(String resource, PustefixVersion version) { 
        Iterator<PustefixVersion> it = versions.descendingIterator();
        while(it.hasNext()) {
            PustefixVersion pv = it.next();
            if(version.compareTo(pv) >= 0) {
                String pkg = "v" + pv.getMajorVersion() + "_" + pv.getMinorVersion() + "_" + pv.getMicroVersion();
                URL url = getClass().getResource(pkg + "/" + resource);
                if(url != null) return url;
            }
        }
        return null;
    }
    
    public Class<?> loadClass(String name, PustefixVersion version) { 
        Iterator<PustefixVersion> it = versions.descendingIterator();
        while(it.hasNext()) {
            PustefixVersion pv = it.next();
            if(version.compareTo(pv) >= 0) {
                String pkg = "v" + pv.getMajorVersion() + "_" + pv.getMinorVersion() + "_" + pv.getMicroVersion();
                String pkgStart = getClass().getName();
                pkgStart = pkgStart.substring(0, pkgStart.lastIndexOf('.'));
                String className = pkgStart + "." + pkg + "." + name;
                try {
                    return Class.forName(className);
                } catch (ClassNotFoundException e) {
                    //ignore
                }
            }
        }
        return null;
    }
    
    
    public static void main(String[] args) throws Exception {
        VersionLoader vl = new VersionLoader();
        PustefixVersion v = PustefixVersion.parseVersion("0.15.0");
        System.out.println(vl.loadResource("iwrapper.xsl", v));
        System.out.println(vl.loadClass("StatusCodeGenerator", v));
    }

}
