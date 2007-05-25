package de.schlund.pfixcore.util;

import java.io.File;
import java.util.Iterator;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.xml.DOMConfigurator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.schlund.pfixxml.IncludeDocumentFactory;
import de.schlund.pfixxml.PathFactory;
import de.schlund.pfixxml.targets.AuxDependencyInclude;
import de.schlund.pfixxml.targets.DependencyType;
import de.schlund.pfixxml.targets.TargetDependencyRelation;
import de.schlund.pfixxml.targets.TargetGenerator;
import de.schlund.pfixxml.util.Path;
import de.schlund.pfixxml.util.XPath;
import de.schlund.pfixxml.util.Xml;


/**
 * DumpText.java
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */

public class DumpText {
    private static DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
    static {
        dbfac.setNamespaceAware(true);
    }
    
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: java de.schlund.util.DumpText <DOCROOT> <DEPEND.XML>");
            System.err.println("       This will create a dump of all include parts");
            System.err.println("       that are used by the project that belongs to the");
            System.err.println("       given DEPEND.XML file. Note that the DEPEND.XML file");
            System.err.println("       must be given as a relative file name to DOCROOT.");            
            System.exit(0);
        }
        String    docroot = args[0];
        String    depend  = args[1];
        PathFactory.getInstance().init(docroot);
        DumpText trans = new DumpText();
        DOMConfigurator.configure("core/conf/generator_quiet.xml");
        trans.generateList(depend);
    }

    
    /**
     * <code>generateList</code> iterates over all includes. It will dump all the include parts
     * that are used in the referenced project (which is identified by the given depend.xml file)
     * into the output file "dump.xml".
     *
     * @param depend a <code>String</code> referencing the depend.xml file to process
     * @exception Exception if an error occurs
     */
    public void generateList(String depend) throws Exception {
        Document list = dbfac.newDocumentBuilder().newDocument();
        Element  root = list.createElement("dumpedincludeparts");
        root.setAttribute("xmlns:pfx", "http://www.schlund.de/pustefix/core");
        root.setAttribute("xmlns:ixsl", "http://www.w3.org/1999/XSL/Transform");
        root.setAttribute("dependfile", depend);
        list.appendChild(root);
        root.appendChild(list.createTextNode("\n"));
        
        TargetGenerator gen  = new TargetGenerator(PathFactory.getInstance().createPath(depend));
        TreeSet         incs = TargetDependencyRelation.getInstance().getProjectDependenciesForType(gen, DependencyType.TEXT);
        for (Iterator i = incs.iterator(); i.hasNext();) {
            AuxDependencyInclude aux  = (AuxDependencyInclude) i.next();
            if (includePartOK(aux)) {
                File file = aux.getPath().resolve();
                if (file.exists()) {
                    System.out.print(".");
                    handleInclude(root, aux);
                }
            }
        }
        root.appendChild(list.createTextNode("\n"));
        Xml.serialize(list, "dump.xml", false, true);
        System.out.print("\n");
    }

    /**
     * The method <code>includePartOK</code> can be overridden in derived classes to
     * select based on the include part given as input if this part should be dumped or not.
     * The default implementation just returns true. 
     * @param aux - The AuxDependencyInclude to check
     * @return true, if the include part should be dunped, false if otherwise
     */
    protected boolean includePartOK(AuxDependencyInclude aux) {
        // String path  = aux.getPath().getRelativePath();
        // String part  = aux.getPart();
        // String theme = aux.getTheme();
        return true;
    }
    
    /**
     * The method <code>retrieveTheme</code> can be overridden to decide which theme
     * to use in the declaration of the dumped content (this is important so you can 
     * change the theme from the current one to another for the import process).
     * The default implementation returns aux.getTheme().  
     * @param aux
     * @return the String to be used in the theme attribute of the dumped 
     */
    protected String retrieveTheme(AuxDependencyInclude aux) {
        // String path  = aux.getPath().getRelativePath();
        // String part  = aux.getPart();
        return aux.getTheme();
    }
    
    private void handleInclude(Element root, AuxDependencyInclude aux) throws Exception {
        Path path = aux.getPath();
        String part          = aux.getPart();
        String theme         = aux.getTheme();
        Document doc         = root.getOwnerDocument();

        IncludeDocumentFactory incfac = IncludeDocumentFactory.getInstance();
        Document incdoc  = incfac.getIncludeDocument(path, true).getDocument();
        Node     extpart = XPath.selectNode(incdoc, "/include_parts/part[@name = '" + part + "']");
        
        if (extpart != null) {

            Element partelem = doc.createElement("USEDINCLUDE");
            partelem.setAttribute("PART", part);
            partelem.setAttribute("PATH", path.getRelative());

            root.appendChild(doc.createTextNode("\n"));
            root.appendChild(doc.createTextNode("  "));
            root.appendChild(partelem);

            NodeList nl = extpart.getChildNodes();
            
            Element themenode = null;
                
            for (int i = 0; i < nl.getLength(); i++) {
                if (nl.item(i) instanceof Element) {
                    Element elem = (Element) nl.item(i);
                    if (elem.getNodeName().equals("theme")) {
                        if (elem.getAttribute("name").equals(theme)) {
                            themenode = elem;
                            break;
                        }
                    }
                }
            }
            if (themenode != null) {
                partelem.setAttribute("THEME", retrieveTheme(aux));
                NodeList nlist = themenode.getChildNodes();
                for (int i = 0; i < nlist.getLength(); i++) {
                    Node node = (Node) doc.importNode(nlist.item(i), true);
                    partelem.appendChild(node);
                }
            } else {
                System.out.println("Didn't find matching theme in part " + part + "@" + path + " for theme " + theme + "!");
            }
        }
    }

}
