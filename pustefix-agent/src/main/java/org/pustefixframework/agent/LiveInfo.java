package org.pustefixframework.agent;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class LiveInfo {

    private Map<POMInfo, String> liveUrls = new HashMap<POMInfo, String>();
    
    public LiveInfo() {

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
            try {
                read(live);
            } catch(Exception e) {
                e.printStackTrace();
                //TODO: log
            }
        }

    }
    
    public String getLiveLocation(POMInfo pomInfo) {
        return liveUrls.get(pomInfo);
    }
    
    public void read(File file) throws Exception {
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
    
    public static void find(File dir, int level, int maxDepth) {
        
        File[] files = dir.listFiles();
        for(File file: files) {
            if(file.isDirectory() && !file.isHidden() && !file.getName().equals("CVS")) {
                if(level < maxDepth) find(file, level + 1, maxDepth);
            } else if(file.isFile() && file.getName().equals("pom.xml") && file.canRead()) {
                System.out.println(dir.getAbsolutePath());
            }
        }
        
    }
    
    public static void main(String[] args) {
        //LiveInfo li = new LiveInfo();
        File dir = new File("/data/checkouts/pustefix.svn.sourceforge.net");
        LiveInfo.find(dir, 0, 4);
    }
    
}
