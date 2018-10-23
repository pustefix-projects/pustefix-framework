/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.pustefixframework.agent;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

/**
 * Replaces bytecode of class by bytecode loaded from live fallback location.
 */
public class LiveClassFileTransformer implements ClassFileTransformer {

    private LiveInfo liveInfo;

    private Map<String, String> locationToLive = new HashMap<String, String>();
    private Set<URL> noLiveLocations = new HashSet<URL>();

    public LiveClassFileTransformer(LiveInfo liveInfo) {
        this.liveInfo = liveInfo;
    }

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, 
            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

        URL location = protectionDomain.getCodeSource().getLocation();
        if(location != null && location.getProtocol().equals("file")) {
            if(location.getPath().endsWith(".jar") && location.getPath().contains("WEB-INF/lib")) {

                String liveLocation = locationToLive.get(location.toExternalForm());
                //no "findbugs : DMI_COLLECTION_OF_URLS" here, because only file URLs in use
                if(liveLocation == null && !noLiveLocations.contains(location)) {
                    JarFile jarFile = null;
                    try {
                        File file = new File(location.toURI());
                        jarFile = new JarFile(file);
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
                                        //no "findbugs : DMI_COLLECTION_OF_URLS" here, because only file URLs in use
                                        noLiveLocations.add(location);
                                    }
                                }
                            } 
                        }
                    } catch(Exception x) {
                        System.err.println("Error reading JAR manifest [" + x.getMessage() + "]");
                    } finally {
                        if(jarFile != null) {
                            try {
                                jarFile.close();
                            } catch (IOException e) {
                                //ignore
                            }
                        }
                    }
                }
                if(liveLocation != null) {
                    String cpath = liveLocation + "/" + className + ".class";
                    try {
                        return loadClass(new File(cpath));
                    } catch(IOException x) {
                        System.err.println("Can't load live class '" + cpath + "' [" + x.getMessage() + "]");
                    }
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
                        try {
                            return loadClass(new File(cpath));
                        } catch(IOException x) {
                            System.err.println("Can't load live class '" + cpath + "' [" + x.getMessage() + "]");
                        }
                    }
                }

            }
        }

        return classfileBuffer;
    }

    public String getLiveLocation(String jarPath) {
        return locationToLive.get(jarPath);
    }

    private byte[] loadClass(File clFile) throws IOException {
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
    }

}
