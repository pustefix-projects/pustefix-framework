/*
* This file is part of PFIXCORE.
*
* PFIXCORE is free software; you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation; either version 2 of the License, or
* (at your option) any later version.
*
* PFIXCORE is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with PFIXCORE; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*
*/
package de.schlund.pfixcore.editor;

import de.schlund.pfixcore.workflow.Navigation;
import de.schlund.pfixcore.workflow.NavigationFactory;

import de.schlund.pfixxml.XMLException;
import de.schlund.pfixxml.targets.TargetGenerator;
import de.schlund.pfixxml.targets.TargetGeneratorFactory;

import de.schlund.util.FactoryInit;

import java.io.File;
import java.io.FileInputStream;

import java.util.Properties;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Category;

import org.apache.xpath.XPathAPI;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


/**
 * EditorProductFactory.java
 *
 *
 * Created: Fri Nov 23 23:08:28 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */
public class EditorProductFactory implements FactoryInit {

    //~ Instance/static variables ..................................................................

    private static DocumentBuilderFactory dbfac         = DocumentBuilderFactory.newInstance();
    private static TreeMap                knownproducts = new TreeMap();
    private static Category               LOG           = Category.getInstance(EditorProductFactory.class.getName());
    private static EditorProductFactory   instance      = new EditorProductFactory();
    private static String                 productfile   = null;
    private static boolean                already_read  = false;
    private static final String           PROP_DELAY    = "editorproductfactory.delayinit";
    private static final String           PROP_PF       = "editorproductfactory.productdata";
    private static final String           IXSL_PFIX     = "ixsl";
    private static final String           IXSL_URI      = "http://www.w3.org/1999/XSL/Transform";
    private static final String           PFX_PFIX      = "pfx";
    private static final String           PFX_URI       = "http://www.schlund.de/pustefix/core";

    //~ Methods ....................................................................................

    public static EditorProductFactory getInstance() {
        return instance;
    }

    public synchronized EditorProduct getEditorProduct(String name) throws Exception {
        if (! already_read) {
            readFile(productfile);
        }
        return (EditorProduct) knownproducts.get(name);
    }

    public synchronized EditorProduct[] getAllEditorProducts() throws Exception {
        if (! already_read) {
            readFile(productfile);
        }
        return (EditorProduct[]) knownproducts.values().toArray(new EditorProduct[]{});
    }

    public synchronized void init(Properties properties) throws Exception {
        productfile = properties.getProperty(PROP_PF);
        if (productfile == null) {
            throw new XMLException("Need to have property " + PROP_PF + " set!");
        }
        String delay = properties.getProperty(PROP_DELAY);
        if (delay != null && delay.equals("false")) {
            readFile(productfile);
            already_read = true;
        } else {
            already_read = false;
        }
    }

    private void readFile(String filename) throws Exception {
        DocumentBuilder domp = dbfac.newDocumentBuilder();
        Document        doc = domp.parse(filename);
        doc.normalize();
        NodeList nl = doc.getElementsByTagName("project");
        for (int i = 0; i < nl.getLength(); i++) {
            Element prj  = (Element) nl.item(i);
            String  name = prj.getAttribute("name");
            if (name == null) {
                throw new XMLException("Product needs a name!");
            }
            String comment = ((Text) XPathAPI.selectNodeList(prj, "./comment/text()").item(0)).getNodeValue();
            if (comment == null) {
                throw new XMLException("Product needs a comment element!");
            }
            String depend = ((Text) XPathAPI.selectNodeList(prj, "./depend/text()").item(0)).getNodeValue();
            if (depend == null) {
                throw new XMLException("Product needs a depend element!");
            }
            TargetGenerator gen   = TargetGeneratorFactory.getInstance().createGenerator(depend);
            Navigation      navi  = NavigationFactory.getInstance().getNavigation(depend);
            NodeList        nlist = prj.getElementsByTagName("handler");
            if (nlist.getLength() == 0) {
                throw new XMLException("Product needs to have handler elements defined!");
            }
            PfixcoreServlet[] servlets = new PfixcoreServlet[nlist.getLength()];
            for (int j = 0; j < nlist.getLength(); j++) {
                Element handler = (Element) nlist.item(j);
                String  hname = handler.getAttribute("name");
                if (hname == null) {
                    throw new XMLException("Handler needs a name!");
                }
                String config = ((Text) XPathAPI.selectNodeList(handler, "./properties/text()").item(
                                         0)).getNodeValue();
                if (config == null) {
                    throw new XMLException("Handler needs a properties element!");
                }
                Properties hprop = new Properties();
                hprop.load(new FileInputStream(new File(config)));
                servlets[j] = new PfixcoreServlet(hname, hprop);
            }
            nlist = prj.getElementsByTagName("namespace");
            PfixcoreNamespace[] nspaces = new PfixcoreNamespace[nlist.getLength() + 2];
            nspaces[0] = new PfixcoreNamespace(IXSL_PFIX, IXSL_URI);
            nspaces[1] = new PfixcoreNamespace(PFX_PFIX, PFX_URI);
            for (int j = 0; j < nlist.getLength(); j++) {
                Element nspace = (Element) nlist.item(j);
                String  prefix = nspace.getAttribute("prefix");
                if (prefix == null) {
                    throw new XMLException("Namespace needs a prefix attribute!");
                }
                String uri = nspace.getAttribute("uri");
                if (uri == null) {
                    throw new XMLException("Namespace needs an uri attribute!");
                }
                nspaces[j + 2] = new PfixcoreNamespace(prefix, uri);
            }
            NodeList nliste = doc.getElementsByTagName("documentation");
            nliste = prj.getElementsByTagName("documentation");
            String[] argument = new String[nliste.getLength()];
            for (int k = 0; k < nliste.getLength(); k++) {
                Element el   = (Element) nliste.item(k);
                String  node = ((Text) XPathAPI.selectNodeList(el, "./text()").item(0)).getNodeValue();
                LOG.debug("Documentation found in: " + node);
                argument[k] = node;
            }
            EditorDocumentation edit    = new EditorDocumentation(argument);
            EditorProduct       product = new EditorProduct(name, comment, depend, gen, navi, 
                                                            servlets, nspaces, edit);
            LOG.debug("Init Product: " + product.toString());
            knownproducts.put(name, product);
        }
    }
}