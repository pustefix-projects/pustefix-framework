package de.schlund.pfixxml;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.servlet.ServletContext;

import de.schlund.pfixcore.exception.PustefixRuntimeException;
import de.schlund.pfixcore.util.JarFileURLConnection;
import de.schlund.pfixcore.util.ModuleDescriptor;
import de.schlund.pfixcore.util.ModuleInfo;
import de.schlund.pfixxml.config.GlobalConfig;
import de.schlund.pfixxml.resources.Resource;
import de.schlund.pfixxml.resources.ResourceUtil;

public class IncludeFileFinder {
    
    public static void find(IncludeFileVisitor visitor) throws Exception {
        
        //Find render includes in webapp
        if(GlobalConfig.getDocroot() != null) {
            File docroot = new File(GlobalConfig.getDocroot());
            File folder = new File(docroot, "txt");
            if(folder.exists()) find(folder, visitor);
            folder = new File(docroot, "xml");
            if(folder.exists()) find(folder, visitor);
        } else if(GlobalConfig.getServletContext() != null) {
            find("/txt/", GlobalConfig.getServletContext(), visitor);
            find("/xml/", GlobalConfig.getServletContext(), visitor);
        }
        
        //Find render includes in modules
        ModuleInfo moduleInfo = ModuleInfo.getInstance();
        Set<String> moduleNames = moduleInfo.getModules();
        Iterator<String> it = moduleNames.iterator();
        while(it.hasNext()) {
            ModuleDescriptor desc = moduleInfo.getModuleDescriptor(it.next());
            URL url = desc.getURL().getProtocol().equals("jar") ? getJarURL(desc.getURL()) : getFileUrl(desc.getURL());
            if(url.getProtocol().equals("jar")) {
                JarFileURLConnection con = new JarFileURLConnection(url);
                JarFile jarFile = con.getJarFile();
                Enumeration<JarEntry> entries = jarFile.entries();
                while(entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if(entry.getName().startsWith("PUSTEFIX-INF/txt") ||
                            entry.getName().startsWith("PUSTEFIX-INF/xml")) {
                        if(!entry.isDirectory()) {
                            String uri = "module://" + desc.getName() + "/" + entry.getName().substring(13);
                            Resource res = ResourceUtil.getResource(uri);
                            visitor.visit(res);
                        }
                    }
                }
            }
        }
        
    }
    
    private static void find(File dir, IncludeFileVisitor visitor) throws IncludePartsInfoParsingException {
        File[] files = dir.listFiles();
        for(File file: files) {
            if(!file.isHidden()) {
                if(file.isDirectory() && !file.getName().equals("CVS")) {
                    find(file, visitor);
                } else if(file.isFile() && file.getName().endsWith(".xml")) {
                    Resource res = ResourceUtil.getResource(file.toURI());
                    visitor.visit(res);
                }
            }
        }
    }
    
    private static void find(String dirPath, ServletContext servletContext, IncludeFileVisitor visitor) throws IncludePartsInfoParsingException {
        @SuppressWarnings("unchecked")
        Set<String> paths = servletContext.getResourcePaths(dirPath);
        if(paths != null) {
            for(String path: paths) {
                if(path.endsWith("/")) {
                    find(path, servletContext, visitor);
                } else if(path.endsWith(".xml")) {
                    Resource res = ResourceUtil.getResource(path);
                    visitor.visit(res);
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
