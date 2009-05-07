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
package de.schlund.pfixcore.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.schlund.pfixcore.exception.PustefixRuntimeException;
import de.schlund.pfixxml.resources.ModuleSourceLocator;

/**
 * 
 * @author mleidig@schlund.de
 *
 */
public class LiveJarInfo implements ModuleSourceLocator {
    
    private static Logger LOG = Logger.getLogger(LiveJarInfo.class);
    
    private static LiveJarInfo instance = new LiveJarInfo();
    
    private Map<String, Entry> entries;
    private Map<URL, File> urlToLocation;
    private Set<URL> urlsWithNoLocation;
    
    public static LiveJarInfo getInstance() {
        return instance;
    }
    
    private LiveJarInfo() {
        try {
            read();
            LOG.info(toString());
        } catch(Exception x) {
            throw new PustefixRuntimeException(x);
        }
    }
    
    private void read() throws Exception {
        String homeDir = System.getProperty("user.home");
        File file = new File(homeDir + "/.m2/live.xml");
        if(!file.exists()) {
            file = new File(homeDir + "/.m2/life.xml"); //support old misspelled name
        }
        if(file.exists()) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);
            Element root = document.getDocumentElement();
            if(root.getLocalName().equals("live") || root.getLocalName().equals("life")) { //support old misspelled name
                entries = new HashMap<String, Entry>();
                urlToLocation = new HashMap<URL, File>();
                urlsWithNoLocation = new HashSet<URL>();
                List<Element> jarElems = getChildElements(root, "jar");
                for(Element jarElem:jarElems) {
                    Entry entry = new Entry();
                    Element idElem = getSingleChildElement(jarElem, "id", true);
                    Element groupElem = getSingleChildElement(idElem, "group", true);
                    entry.groupId = groupElem.getTextContent().trim();
                    Element artifactElem = getSingleChildElement(idElem, "artifact", true);
                    entry.artifactId = artifactElem.getTextContent().trim();
                    Element versionElem = getSingleChildElement(idElem, "version", true);
                    entry.version = versionElem.getTextContent().trim();
                    List<Element> dirElems = getChildElements(jarElem, "directory");
                    if(dirElems.size() == 0) dirElems = getChildElements(jarElem, "directorie"); //support old misspelled name
                    for(Element dirElem:dirElems) {
                        File dir = new File(dirElem.getTextContent().trim());
                        entry.directories.add(dir);
                    }
                    entries.put(entry.getId(), entry);
                }
            }
        } else {
            LOG.info("No live jar configuration found in local maven repository.");
        }
    }
    
    public boolean hasEntries() {
        return entries!=null && entries.size()>0;
    }
    
    private static Element getSingleChildElement(Element parent, String localName, boolean mandatory) throws Exception {
        Element elem = null;
        NodeList nodes = parent.getChildNodes();
        for(int i=0; i<nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if(node.getNodeType() == Node.ELEMENT_NODE && node.getLocalName().equals(localName)) {
                if(elem != null) throw new Exception("Multiple '" + localName + "' child elements aren't allowed."); 
                elem = (Element)node;
            }
        }
        if(mandatory && elem == null) throw new Exception("Missing '" + localName + "' child element.");
        return elem;
    }
    
    private static List<Element> getChildElements(Element parent, String localName) {
        List<Element> elems = new ArrayList<Element>();
        NodeList nodes = parent.getChildNodes();
        for(int i=0; i<nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if(node.getNodeType() == Node.ELEMENT_NODE && node.getLocalName().equals(localName)) {
                elems.add((Element)node);
            }
        }
        return elems;
    }
    
    public File getLocation(URL jarUrl) {
        
        File location = urlToLocation.get(jarUrl);
        if(location != null || urlsWithNoLocation.contains(jarUrl)) return location;
        
        String path = jarUrl.getPath();
        int ind = path.indexOf('!');
        path = path.substring(0, ind);
        ind = path.lastIndexOf('/');
        path = path.substring(ind+1);
        ind = path.lastIndexOf('.');
        path = path.substring(0, ind);
        //look for entry by jar file name
        Entry entry = entries.get(path);
        if(entry != null) {
            for(File dir:entry.directories) {
                if(dir.getName().equals("resources")) {
                    if(LOG.isDebugEnabled()) LOG.debug("Found live location by jar file name: " + path);
                    urlToLocation.put(jarUrl, dir);
                    return dir;
                }
            }
        }
        //look for entry by artifact name and MANIFEST attributes
        try {
            URL manifestUrl = new URL(jarUrl, "/META-INF/MANIFEST.MF");
            URLConnection con = manifestUrl.openConnection();
            if(con != null) {
                InputStream in = con.getInputStream();
                if(in != null) {
                    Manifest manifest = new Manifest(in);
                    Attributes attrs = manifest.getMainAttributes();
                    String groupId = attrs.getValue("Implementation-Vendor-Id");
                    String version = attrs.getValue("Implementation-Version");
                    if(groupId != null && version != null) {
                        int endInd = path.indexOf(version);
                        if(endInd > 2) {
                            String artifactId = path.substring(0,endInd-1);
                            String entryKey = groupId + "+" + artifactId + "+" + version;
                            entry = entries.get(entryKey);
                            if(entry != null) {
                                for(File dir:entry.directories) {
                                    if(dir.getName().equals("resources")) {
                                        if(LOG.isDebugEnabled()) 
                                            LOG.debug("Found live location by artifact name and MANIFEST attributes: " + entryKey);
                                        urlToLocation.put(jarUrl, dir);
                                        return dir;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch(FileNotFoundException x) {
            LOG.warn("Module contains no MANIFEST.MF: "+ jarUrl.toString());
        } catch(MalformedURLException x) {
            LOG.warn("Illegal module URL: " + jarUrl.toString(), x);
        } catch(IOException x) {
            LOG.warn("IO error reading module data: " + jarUrl.toString(), x);
        }
        if(LOG.isDebugEnabled()) LOG.debug("Found no live location: " + jarUrl.toString());
        urlsWithNoLocation.add(jarUrl);
        return null;
    }
    
  
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Live jar information - ");
        int no = (entries == null ? 0 :entries.size());
        sb.append("Detected " + no + " live jar entr" + (no==1?"y":"ies"));
        return sb.toString();
    }
    
    private class Entry {
        
        String groupId;
        String artifactId;
        String version;
        List<File> directories = new ArrayList<File>();
        
        String getId() {
            return groupId + "+" + artifactId + "+" + version;
        }
        
    }
   
}
