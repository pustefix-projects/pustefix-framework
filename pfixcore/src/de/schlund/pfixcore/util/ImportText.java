package de.schlund.pfixcore.util;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.xml.DOMConfigurator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.schlund.pfixxml.PathFactory;
import de.schlund.pfixxml.util.XPath;
import de.schlund.pfixxml.util.Xml;


/**
 * ImportText.java
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */

public class ImportText {
    private static DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();

    static {
        dbfac.setNamespaceAware(true);
    }
    
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: java de.schlund.pfixcore.util.ImportText <DOCROOT> <DUMP.XML>");
            System.err.println("       This will import all include parts from the given DUMP.XML file");
            System.err.println("       that must be given as a relative file name to DOCROOT.");            
            System.exit(0);
        }
        String    docroot = args[0];
        String    dumpxml = args[1];
        PathFactory.getInstance().init(docroot);
        ImportText trans = new ImportText();
        DOMConfigurator.configure("core/conf/generator_quiet.xml");
        trans.importList(dumpxml);
    }

    
    public void importList(String dump) throws Exception {
        Document dumpeddoc     = Xml.parse(new File(dump));
        List     dumpedinclude = XPath.select(dumpeddoc, "/dumpedincludeparts/USEDINCLUDE");

        String oldpath  = null;
        Document incdoc = null;
        File incfile = null;
        
        for (Iterator i = dumpedinclude.iterator(); i.hasNext();) {
            Element usedinc  = (Element) i.next();

            String path = usedinc.getAttribute("PATH");
            // We serialize the old incdoc, because a new include file has to be parsed
            incfile = PathFactory.getInstance().createPath(path).resolve();
            if (incfile.exists()) {
                if (!path.equals(oldpath) || incdoc == null) {
                    if (incdoc != null) {
                        Xml.serialize(incdoc, oldpath, false, true);
                    }
                    incdoc = Xml.parseMutable(incfile);
                }
                System.out.print(".");
                handleInclude(incdoc, usedinc, path);
            } else { 
                System.out.println("*** Missing include file " + path);
            }
            oldpath = path;
        }
        // Make sure to serialize the last incdoc
        if (incdoc != null) {
            Xml.serialize(incdoc, incfile, false, true);
        }
        System.out.println("\n");
    }
    
    private void handleInclude(Document incdoc, Element usedinc, String path) throws Exception {    
        String part = usedinc.getAttribute("PART");
        String theme = usedinc.getAttribute("THEME");
        String origcheck = usedinc.getAttribute("CHECK");
        Element themeelem = (Element) XPath.selectNode(incdoc, "/include_parts/part[@name = '" + part + "']/theme[@name = '" + theme + "']");
        Element themepart = (Element) XPath.selectNode(incdoc, "/include_parts/part[@name = '" + part + "']");
        if (themeelem != null) {
            NodeList oldcontent = themeelem.getChildNodes();
            int count = oldcontent.getLength();
            for (int i = 0; i < count; i++) {
                themeelem.removeChild(oldcontent.item(0));
            }
        } else if (themepart != null) {
            themeelem = incdoc.createElement("theme");
            themeelem.setAttribute("name", theme);
            themepart.appendChild(incdoc.createTextNode("  "));
            themepart.appendChild(themeelem);
            themepart.appendChild(incdoc.createTextNode("\n"));
        } else {
            System.out.println("*** Didn't find part '" + part + "@" + path + "'. Ignoring.");
            return;
        }
        String check = DumpText.md5ForNode(usedinc);
        if (check != null && !check.equals(origcheck)) {
            System.out.print("\nInfo: CHECK differs for '" + theme + "@" + part + "@" + path + "'");
        }
        NodeList content = usedinc.getChildNodes();
        for (int i = 0; i < content.getLength(); i++) {
            Node newnode = incdoc.importNode(content.item(i), true);
            themeelem.appendChild(newnode);
        }
    }
}
