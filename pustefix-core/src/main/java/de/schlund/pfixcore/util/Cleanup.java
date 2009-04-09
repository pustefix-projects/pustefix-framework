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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.schlund.pfixxml.IncludeDocument;
import de.schlund.pfixxml.IncludeDocumentFactory;
import de.schlund.pfixxml.config.GlobalConfigurator;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.util.XPath;
import de.schlund.pfixxml.util.Xml;

/**
 * Cleanup.java
 *
 *
 * Created: Tue Apr 29 14:40:15 2003
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */

public class Cleanup{
    private final static String CLEANUP = "Cleanup.xml";
    private final static DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
    static {
        dbfac.setNamespaceAware(true);
    }
    private HashMap<String, Document> changed = new HashMap<String, Document>();
    
    public Cleanup() {}

    public static void main(String[] args) throws Exception {
        String pwd = new File(".").getCanonicalPath();
        GlobalConfigurator.setDocroot(pwd);
        Cleanup cleanup = new Cleanup();
        cleanup.clean();
    }

    private void clean() throws Exception {
        Document input = Xml.parseMutable(new File(CLEANUP));
        Element  root  = input.getDocumentElement();
        NodeList nl    = root.getChildNodes();
        for (int j = 0; j < nl.getLength(); j++) {
            if (nl.item(j) instanceof Element) {
                Element clean = (Element) nl.item(j);
                String  name = clean.getNodeName();
                if (name.equals("clean")) { 
                    String type = clean.getAttribute("type");
                    String path = clean.getAttribute("path");
                    String part = clean.getAttribute("part");
                    String theme = clean.getAttribute("theme");
                    
                    Document doc = (Document) changed.get(path);
                    if (doc == null && (type.equals("part") || type.equals("theme"))) {
                        IncludeDocument incdoc = IncludeDocumentFactory.getInstance().
                            getIncludeDocument(null, ResourceUtil.getFileResourceFromDocroot(path), true);
                        doc                    = incdoc.getDocument();
                        System.out.println(doc.hashCode());
                        doc.getDocumentElement().removeAttribute("incpath");
                        changed.put(path, doc);
                    }
                    if (type.equals("part")) {
                        cleanPart(doc, path, part);
                    } else if (type.equals("theme")) {
                        cleanTheme(doc, path, part, theme);
                    } else if (type.equals("file")) {
                        cleanFile(path);
                    } else {
                        System.out.println("ERROR! type is " + type);
                    }
                } else {
                    System.out.println("No clean element! " + name);
                }
            }
        }

        for (Iterator<String> i = changed.keySet().iterator(); i.hasNext();) {
            String path = i.next();
            Document doc = changed.get(path);
            System.out.println("Saving " + path);

            Xml.serialize(doc, path, false, true);
        }
    }

    private void cleanPart(Document doc, String path, String part) throws Exception {
        System.out.println("Killing " + path + " => " + part);
        List<Node> nl  = XPath.select(doc, "/include_parts/part[@name = '" + part + "']");
        if (nl.size() == 1) {
            Element partel = (Element) nl.get(0);
            Node    parent = partel.getParentNode();
            parent.removeChild(partel);
        } else {
            System.out.println("More than one matching part " + path + "/" + part);
        }
    }
    
    private void cleanTheme(Document doc, String path, String part, String theme) throws Exception {
        System.out.println("Killing " + path + " => " + part + " => " + theme);
        List<Node> nl  = XPath.select(doc, "/include_parts/part[@name = '" + part + "']/theme[@name = '" + theme + "']");
        if (nl.size() == 1) {
            Element themeel = (Element) nl.get(0);
            Node    parent = themeel.getParentNode();
            parent.removeChild(themeel);
        } else {
            System.out.println("More than one matching theme " + path + "/" + part + "/" + theme);
        }
    }

    private void cleanFile(String path) throws Exception {
        System.out.println("Removing file " + path);
        new File(path).delete();
    }
}
