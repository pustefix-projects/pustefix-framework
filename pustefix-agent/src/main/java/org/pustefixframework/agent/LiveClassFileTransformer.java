package org.pustefixframework.agent;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class LiveClassFileTransformer implements ClassFileTransformer {
    
    private LiveInfo liveInfo;
    
    private Map<String, String> locationToLive = new HashMap<String, String>();
    private Set<URL> noLiveLocations = new HashSet<URL>();
    
    public LiveClassFileTransformer(LiveInfo liveInfo) {
        this.liveInfo = liveInfo;
    }
    
    public byte[] transform(ClassLoader loader, String className,
            Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
            byte[] classfileBuffer) throws IllegalClassFormatException {
       
        
        
        URL location = protectionDomain.getCodeSource().getLocation();
        if(location != null && location.getProtocol().equals("file")) {
            if(location.getPath().endsWith(".jar") && location.getPath().contains("WEB-INF/lib")) {
                
                String liveLocation = locationToLive.get(location.toExternalForm());
                if(liveLocation == null && !noLiveLocations.contains(location)) {
                    try {
                        File file = new File(location.toURI());
                        JarFile jarFile = new JarFile(file);
                        Manifest manifest = jarFile.getManifest();
                        if(manifest != null) {
                            Attributes attrs = manifest.getMainAttributes();
                            String groupId = attrs.getValue("Implementation-Vendor-Id");
                            String version = attrs.getValue("Implementation-Version");
                            if (groupId != null && version != null) {
                                String fileName = file.getName();
                                int endInd = fileName.indexOf(version);
                                if (endInd > 2) {
                                    String artifactId = fileName.substring(0, endInd - 1);
                                    POMInfo pomInfo = new POMInfo(groupId, artifactId, version);
                                    liveLocation = liveInfo.getLiveLocation(pomInfo);
                                    if(liveLocation != null) {
                                        locationToLive.put(location.toExternalForm(), liveLocation);
                                    } else {
                                        noLiveLocations.add(location);
                                    }
                                }
                            } 
                        }
                    } catch(Exception x) {
                        x.printStackTrace();
                    }
                }
                if(liveLocation != null) {
                    String cpath = liveLocation + "/" + className + ".class";
                    return loadClass(new File(cpath));
                }
            } else if(location.getPath().endsWith(".class") && location.getPath().contains("WEB-INF/classes")) {
                String path = location.getPath();
                int ind = path.indexOf("/WEB-INF/classes");
                path = path.substring(0, ind);
                ind = path.lastIndexOf('/');
                if(ind > -1) {
                    path = path.substring(0, ind);
                    if(path.endsWith("/target")) {
                        path += "/classes";
                        String cpath = path + "/" +className + ".class";
                        return loadClass(new File(cpath));
                    }
                }
                
            }
        }
        
        return classfileBuffer;
        
    }
    
    public String getLiveLocation(String jarPath) {
        return locationToLive.get(jarPath);
    }
    
    private byte[] loadClass(File clFile) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = new FileInputStream(clFile);
            byte[] buffer = new byte[4096];
            int no = 0;
            try {
                while ((no = in.read(buffer)) != -1)
                    out.write(buffer, 0, no);
            } finally {
                in.close();
                out.close();
            }
            return out.toByteArray();
        } catch(Exception x) {
            throw new RuntimeException(x);
        }
    }

}
