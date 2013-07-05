package de.schlund.pfixxml.resources;

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

public class ResourceFinder {
    
    public static void findAll(String[] fileExtensions, String[] paths, ResourceVisitor visitor) throws Exception {
        find(fileExtensions, paths, null, visitor);
        ModuleInfo moduleInfo = ModuleInfo.getInstance();
        Set<String> moduleNames = moduleInfo.getModules();
        Iterator<String> it = moduleNames.iterator();
        while(it.hasNext()) {
            find(fileExtensions, paths, it.next(), visitor);
        }
    }
        
    public static void find(String[] fileExtensions, String[] paths, String module, ResourceVisitor visitor) throws Exception {
        for(int i=0; i<fileExtensions.length; i++) {
            if(!fileExtensions[i].startsWith(".")) fileExtensions[i] = "." + fileExtensions[i];
        }
        for(int i=0; i<paths.length; i++) {
            if(paths[i].startsWith("/")) paths[i] = paths[i].substring(1);
            if(paths[i].endsWith("/")) paths[i] = paths[i].substring(0, paths[i].length() - 1);
        }   
        if(module == null) {
            if(GlobalConfig.getDocroot() != null) {
                File docroot = new File(GlobalConfig.getDocroot());
                for(String path: paths) {
                    File folder = new File(docroot, path);
                    if(folder.exists()) find(folder, fileExtensions, visitor);
                }
            } else if(GlobalConfig.getServletContext() != null) {
                for(String path: paths) {
                    find("/" + path + "/", fileExtensions, GlobalConfig.getServletContext(), visitor);
                }
            }
        } else {
        	ModuleInfo moduleInfo = ModuleInfo.getInstance();
            ModuleDescriptor desc = moduleInfo.getModuleDescriptor(module);
            String[] modulePaths = new String[paths.length];
            for(int i=0; i<paths.length; i++) {
                modulePaths[i] = desc.getResourcePath() + "/" + paths[i];
            }
            findInModule(desc, modulePaths, fileExtensions, visitor);
        }
    }
    
    private static void findInModule(ModuleDescriptor desc, String[] modulePaths, String[] fileExtensions, ResourceVisitor visitor) throws Exception {
        
        URL url = desc.getURL().getProtocol().equals("jar") ? getJarURL(desc.getURL()) : getFileUrl(desc.getURL());
        if(url.getProtocol().equals("jar")) {
            JarFileURLConnection con = new JarFileURLConnection(url);
            JarFile jarFile = con.getJarFile();
            Enumeration<JarEntry> entries = jarFile.entries();
            while(entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if(startsWith(entry.getName(), modulePaths)) {
                    if(!entry.isDirectory() && endsWith(entry.getName(), fileExtensions)) {
                        String uri = "module://" + desc.getName() + "/" + entry.getName().substring(13);
                        Resource res = ResourceUtil.getResource(uri);
                        visitor.visit(res);
                    }
                }
            }
        } else if(url.getProtocol().equals("file")) {
            File base = new File(url.toURI());
            for(String modulePath: modulePaths) {
                find(new File(base, modulePath), fileExtensions, visitor);
            }
        }
    }
    
    private static boolean startsWith(String checkPath, String[] paths) {
        for(String path: paths) {
            if(checkPath.startsWith(path)) return true;
        }
        return false;
    }
    
    private static void find(File dir, String[] fileExtensions, ResourceVisitor visitor) {
        if(dir.exists()) {
            File[] files = dir.listFiles();
            for(File file: files) {
                if(!file.isHidden()) {
                    if(file.isDirectory() && !file.getName().equals("CVS")) {
                        find(file, fileExtensions, visitor);
                    } else if(file.isFile() && endsWith(file.getName(), fileExtensions)) {
                        Resource res = ResourceUtil.getResource(file.toURI());
                        visitor.visit(res);
                    }
                }
            }
        }
    }
    
    private static void find(String dirPath, String[] fileExtensions, ServletContext servletContext, ResourceVisitor visitor) {
        @SuppressWarnings("unchecked")
        Set<String> paths = servletContext.getResourcePaths(dirPath);
        if(paths != null) {
            for(String path: paths) {
                if(path.endsWith("/")) {
                    find(path, fileExtensions, servletContext, visitor);
                } else if(endsWith(path, fileExtensions)) {
                    Resource res = ResourceUtil.getResource(path);
                    visitor.visit(res);
                }
            }
        }
    }
    
    private static boolean endsWith(String path, String[] fileExtensions) {
        for(String fileExtension: fileExtensions) {
            if(path.endsWith(fileExtension)) return true;
        }
        return false;
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
