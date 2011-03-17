package de.schlund.pfixxml;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import de.schlund.pfixcore.exception.PustefixRuntimeException;
import de.schlund.pfixcore.util.JarFileURLConnection;
import de.schlund.pfixcore.util.ModuleDescriptor;
import de.schlund.pfixcore.util.ModuleInfo;

public class IncludeFileFinder {
    
    public static void main(String[] args) throws Exception {
        ModuleInfo moduleInfo = ModuleInfo.getInstance();
        Set<String> moduleNames = moduleInfo.getModules();
        Iterator<String> it = moduleNames.iterator();
        while(it.hasNext()) {
            ModuleDescriptor desc = moduleInfo.getModuleDescriptor(it.next());
            URL url = desc.getURL().getProtocol().equals("jar") ? getJarURL(desc.getURL()) : getFileUrl(desc.getURL());
            if(url.getProtocol().equals("jar")) {
                System.out.println(url);
                JarFileURLConnection con = new JarFileURLConnection(url);
                JarFile jarFile = con.getJarFile();
                Enumeration<JarEntry> entries = jarFile.entries();
                while(entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if(entry.getName().startsWith("PUSTEFIX-INF/txt") ||
                            entry.getName().startsWith("PUSTEFIX-INF/xml")) {
                        System.out.println(entry.getName());
                    }
                }
            }
        }
    }
    
    private static URL getFileUrl(URL url) {
        if (!url.getProtocol().equals("file"))
            throw new PustefixRuntimeException("Invalid protocol: " + url);
        String urlStr = url.toString();
        int ind = urlStr.indexOf("META-INF");
        if (ind > -1) {
            urlStr = urlStr.substring(0, ind);
        } else
            throw new PustefixRuntimeException("Unexpected module descriptor URL: " + url);
        try {
            return new URL(urlStr);
        } catch (MalformedURLException x) {
            throw new PustefixRuntimeException("Invalid module URL: " + urlStr);
        }
    }

    private static URL getJarURL(URL url) {
        if(!url.getProtocol().equals("jar")) throw new PustefixRuntimeException("Invalid protocol: "+url);
        String urlStr = url.toString();
        int ind = urlStr.indexOf('!');
        if(ind > -1 && urlStr.length() > ind + 1)  {
            urlStr = urlStr.substring(0, ind+2);
        } else throw new PustefixRuntimeException("Unexpected module descriptor URL: "+url);
        try {
            return new URL(urlStr);
        } catch(MalformedURLException x) {
            throw new PustefixRuntimeException("Invalid module URL: "+urlStr);
        }
    }

}
