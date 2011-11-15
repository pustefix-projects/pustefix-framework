package org.pustefixframework.live;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class LiveUtils {

    private static Logger LOG = Logger.getLogger(LiveUtils.class);

    public static String getArtifactFromPom(File pomFile) throws Exception {
    	Element root = getRootFromPom(pomFile);
        Element artifactElem = getSingleChildElement(root, "artifactId", true);
        String artifactId = artifactElem.getTextContent().trim();
        return artifactId;
    }

    public static String getKeyFromPom(File pomFile) throws Exception {
        Element root = getRootFromPom(pomFile);
        Element groupElem = getSingleChildElement(root, "groupId", true);
        String groupId = groupElem.getTextContent().trim();
        Element artifactElem = getSingleChildElement(root, "artifactId", true);
        String artifactId = artifactElem.getTextContent().trim();
        Element versionElem = getSingleChildElement(root, "version", true);
        String version = versionElem.getTextContent().trim();
        String entryKey = groupId + "+" + artifactId + "+" + version;
        return entryKey;
    }
    
    public static LiveJarInfo.Entry getEntryFromPom(File pomFile) throws Exception {
        Element root = getRootFromPom(pomFile);
        Element groupElem = getSingleChildElement(root, "groupId", true);
        String groupId = groupElem.getTextContent().trim();
        Element artifactElem = getSingleChildElement(root, "artifactId", true);
        String artifactId = artifactElem.getTextContent().trim();
        Element versionElem = getSingleChildElement(root, "version", true);
        String version = versionElem.getTextContent().trim();
        LiveJarInfo.Entry entry = new LiveJarInfo.Entry();
        entry.setGroupId(groupId);
        entry.setArtifactId(artifactId);
        entry.setVersion(version);
        return entry;
    }

    public static Element getRootFromPom(File pomFile) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(pomFile);
        Element root = document.getDocumentElement();
        return root;
    }
    
    public static File guessPom(String docroot) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Guessing pom.xml for " + docroot);
        }

        /*
         * Here is a project layout including a pustefix editor. The editor has its own pom.xml blow META-INF. Otherwise
         * we walk up the parent path till we find the project pom.xml.
         * 
         * <pre> pfixui |-- pom.xml <-- pom of application |-- target |-- pfixui-0.2.29-SNAPSHOT <-- docroot of
         * application |-- WEB-INF |-- editor <-- docroot of editor |-- META-INF |-- maven |--
         * org.pustefixframework.editor |-- pustefix-editor-webui |-- pom.xml <-- pom of editor
         * 
         * </pre>
         */

        // search pom.xml below META-INF
        File docrootDir = new File(docroot);
        if (docrootDir.exists() && docrootDir.isDirectory()) {
            File metaInfDir = new File(docrootDir, "META-INF");
            if (LOG.isTraceEnabled())
                LOG.trace(metaInfDir);
            if (metaInfDir.exists() && metaInfDir.isDirectory()) {
                File mavenDir = new File(metaInfDir, "maven");
                if (LOG.isTraceEnabled())
                    LOG.trace(mavenDir);
                if (mavenDir.exists() && mavenDir.isDirectory()) {
                    File[] groupIdDirs = mavenDir.listFiles();
                    for (File groupIdDir : groupIdDirs) {
                        if (LOG.isTraceEnabled())
                            LOG.trace(groupIdDir);
                        if (groupIdDir.exists() && groupIdDir.isDirectory()) {
                            File[] artifactIdDirs = groupIdDir.listFiles();
                            for (File artifiactIdDir : artifactIdDirs) {
                                if (LOG.isTraceEnabled())
                                    LOG.trace(artifiactIdDir);
                                if (artifiactIdDir.exists() && artifiactIdDir.isDirectory()) {
                                    File pomFile = new File(artifiactIdDir, "pom.xml");
                                    if (LOG.isTraceEnabled())
                                        LOG.trace(pomFile);
                                    if (pomFile.exists() && pomFile.isFile()) {
                                        return pomFile;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // search project pom.xml
        File pomDir = new File(docroot);
        while (pomDir != null && pomDir.exists() && pomDir.isDirectory()) {
            File pomFile = new File(pomDir, "pom.xml");
            if (pomFile != null && pomFile.exists() && pomFile.isFile()) {
                return pomFile;
            }
            pomDir = pomDir.getParentFile();
        }
        return null;
    }

    public static Element getSingleChildElement(Element parent, String localName, boolean mandatory) throws Exception {
        Element elem = null;
        NodeList nodes = parent.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && node.getLocalName().equals(localName)) {
                if (elem != null)
                    throw new Exception("Multiple '" + localName + "' child elements aren't allowed.");
                elem = (Element) node;
            }
        }
        if (mandatory && elem == null)
            throw new Exception("Missing '" + localName + "' child element.");
        return elem;
    }

    public static List<Element> getChildElements(Element parent, String localName) {
        List<Element> elems = new ArrayList<Element>();
        NodeList nodes = parent.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && node.getLocalName().equals(localName)) {
                elems.add((Element) node);
            }
        }
        return elems;
    }
}
