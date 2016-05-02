package org.pustefixframework.agent;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Manages live fallback location lookup for classes
 * based on live.xml or automatic Maven project detection.
 * 
 * @author mleidig@schlund.de
 *
 */
public class LiveInfo {

    private Map<POMInfo, String> liveUrls = new HashMap<POMInfo, String>();
    
    public LiveInfo() throws Exception {

        File live = null;
        
        String userDir = System.getProperty("user.dir");
        if (userDir != null) {
            File dir = new File(userDir);
            File test;
            while(!(test = new File(dir, "live.xml")).exists() && (dir = dir.getParentFile()) != null);
            if(test != null && test.exists()) live = test;
        }
      
        if(live == null) {
            String homeDir = System.getProperty("user.home");
            if(homeDir != null) {
                File dir = new File(homeDir);
                File test = new File(dir, "live.xml");
                if(test.exists()) live = test;
            }
        }
        
        if(live != null) {
            read(live);
        }

    }
    
    public LiveInfo(File liveRootDir, int liveRootMaxDepth) {
        findMavenProjects(liveRootDir, 0, liveRootMaxDepth);
    }
    
    public String getLiveLocation(POMInfo pomInfo) {
        return liveUrls.get(pomInfo);
    }
    
    private void read(File file) throws Exception {
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document doc = dbf.newDocumentBuilder().parse(file);
        List<Element> jarElems = getChildElements(doc.getDocumentElement(), "jar");
        for(Element jarElem: jarElems) {
            Element idElem = getChildElement(jarElem, "id");
            if(idElem != null) {
                Element groupIdElem = getChildElement(idElem, "group");
                Element artifactIdElem = getChildElement(idElem, "artifact");
                Element versionElem = getChildElement(idElem, "version");
                if(groupIdElem != null && artifactIdElem != null && versionElem != null) {
                    POMInfo pomInfo = new POMInfo();
                    pomInfo.setGroupId(groupIdElem.getTextContent().trim());
                    pomInfo.setArtifactId(artifactIdElem.getTextContent().trim());
                    pomInfo.setVersion(versionElem.getTextContent().trim());
                    List<Element> dirElems = getChildElements(jarElem, "directory");
                    for(Element dirElem: dirElems) {
                        String dir = dirElem.getTextContent().trim();
                        int ind = dir.indexOf("/src/main/resources");
                        if(ind > -1) {
                            dir = dir.substring(0, ind) + "/target/classes";
                            liveUrls.put(pomInfo, dir);
                        }
                    }
                }
            }
          
        }
    }
    
    private Element getChildElement(Element element, String name) {
        NodeList nodes = element.getChildNodes();
        for(int i=0; i<nodes.getLength(); i++) {
            if(nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element child = (Element)nodes.item(i);
                if(child.getNodeName().equals(name)) return child;
            }
        }
        return null;
    }
    
    private List<Element> getChildElements(Element element, String name) {
        List<Element> children = new ArrayList<Element>();
        NodeList nodes = element.getChildNodes();
        for(int i=0; i<nodes.getLength(); i++) {
            if(nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element child = (Element)nodes.item(i);
                if(child.getNodeName().equals(name)) children.add(child);
            }
        }
        return children;
    }
    
    private void findMavenProjects(File dir, int level, int maxDepth) {
        
        POMParser pomParser = new POMParser();
        File[] files = dir.listFiles();
        for(File file: files) {
            if(file.isDirectory() && !file.isHidden() && !file.getName().equals("CVS")
                    && !file.getName().equals("target") && !file.getName().equals("src")) {
                if(level < maxDepth) findMavenProjects(file, level + 1, maxDepth);
            } else if(file.isFile() && file.getName().equals("pom.xml") && file.canRead()) {
                
                    POMInfo pomInfo = null;
                    try {
                        pomInfo = pomParser.parse(file);
                    } catch (Exception e) {
                        System.err.println("Error reading POM file '" + file.getAbsolutePath() + "' [" +
                                e.getMessage() + "].");
                    }
                    if(pomInfo != null) {
                        String targetDir = new File(file.getParentFile(), "target/classes").getAbsolutePath();
                        liveUrls.put(pomInfo, targetDir);
                    }
            }
        }
        
    }
    
}