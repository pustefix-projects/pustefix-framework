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

import de.schlund.pfixcore.workflow.*;
import de.schlund.pfixxml.*;
import de.schlund.pfixxml.targets.*;
import de.schlund.pfixxml.util.*;
import de.schlund.util.FactoryInit;
import java.util.*;
import org.apache.log4j.Category;
import org.w3c.dom.*;


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

    private static Category               LOG           = Category.getInstance(EditorProductFactory.class.getName());
    private static EditorProductFactory   instance      = new EditorProductFactory();

    private TreeMap   knownproducts;
    private String    productfile;
    private boolean   already_read;
    
    public static final String            PROP_PF       = "editorproductfactory.productdata";
    private static final String           PROP_DELAY    = "editorproductfactory.delayinit";
    private static final String           IXSL_PFIX     = "ixsl";
    private static final String           IXSL_URI      = "http://www.w3.org/1999/XSL/Transform";
    private static final String           PFX_PFIX      = "pfx";
    private static final String           PFX_URI       = "http://www.schlund.de/pustefix/core";

    //~ Methods ....................................................................................

    public static EditorProductFactory getInstance() {
        return instance;
    }

    public EditorProductFactory() {
        knownproducts = new TreeMap();
        productfile = null;
        already_read = false;
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
            already_read = true;
        }
        return (EditorProduct[]) knownproducts.values().toArray(new EditorProduct[]{});
    }

    public synchronized void init(Properties properties) throws Exception {
        productfile = properties.getProperty(PROP_PF);
        if (productfile == null) {
            throw new XMLException("Need to have property " + PROP_PF + " set!");
        }
        String delay = properties.getProperty(PROP_DELAY);
        already_read = false;
        if (delay != null && delay.equals("false")) {
            readFile(productfile);
        }
    }

    public void readFile(String filename) throws Exception {
        Document doc = Xml.parseMutable(PathFactory.getInstance().createPath(filename).resolve());
        doc.normalize();
        configure(doc);
    }
    
    public void configure(Document doc) throws Exception {
        already_read = true;
        Element documentation = (Element) XPath.selectNode(doc, "/projects/common/documentation");
        NodeList nl = doc.getElementsByTagName("project");
        for (int i = 0; i < nl.getLength(); i++) {
            Element prj  = (Element) nl.item(i);
            if (XPath.test(prj, "servlet[@useineditor = 'true']")) {
                EditorProduct product = createProduct(prj, documentation);
                knownproducts.put(product.getName(), product);
            }
        }
    }
    
    public static EditorProduct createProduct(Element prj, Element commonDocumentation) throws Exception {
        String  name = prj.getAttribute("name");
        if (name == null) {
            throw new XMLException("Product needs a name!");
        }
        String comment = ((Text) XPath.select(prj, "./comment/text()").get(0)).getNodeValue();
        if (comment == null) {
            throw new XMLException("Product needs a comment element!");
        }
        String depend = ((Text) XPath.select(prj, "./depend/text()").get(0)).getNodeValue();
        if (depend == null) {
            throw new XMLException("Product needs a depend element!");
        }
        Path            dependpath = PathFactory.getInstance().createPath(depend);
        TargetGenerator gen        = TargetGeneratorFactory.getInstance().createGenerator(dependpath);
        Navigation      navi       = NavigationFactory.getInstance().getNavigation(depend);
        NodeList        nlist      = prj.getElementsByTagName("servlet");
        if (nlist.getLength() == 0) {
            throw new XMLException("Product needs to have servlet elements defined!");
        }
        PfixcoreServlet[] servlets = new PfixcoreServlet[nlist.getLength()];
        for (int j = 0; j < nlist.getLength(); j++) {
            Element handler = (Element) nlist.item(j);
            String  hname = handler.getAttribute("name");
            if (hname == null) {
                throw new XMLException("Handler needs a name!");
            }
            servlets[j] = new PfixcoreServlet(hname);
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
        
        EditorDocumentation edit = createDocumentation(commonDocumentation, prj);
        return new EditorProduct(name, comment, dependpath, gen, navi, servlets, nspaces, edit);
    }
    
    private static EditorDocumentation createDocumentation(Element common, Element project) throws Exception {
        String[] argument;
        List lst;

        lst = new ArrayList();
        if (common != null) {
            addDocumentation(common.getElementsByTagName("doc_file"), lst);
        }
        addDocumentation(project.getElementsByTagName("documentation"), lst);
        argument = new String[lst.size()];
        lst.toArray(argument);
        return new EditorDocumentation(argument);
    }

    private static void addDocumentation(NodeList nliste, List result) throws Exception {
        for (int k = 0; k < nliste.getLength(); k++) {
            Element el   = (Element) nliste.item(k);
            String  node = ((Text) XPath.select(el, "./text()").get(0)).getNodeValue();
            node = node.trim();
            if (node.length() > 0) {
                LOG.debug("Documentation found in: " + node);
                result.add(node);
            }
        }
    }
    
}
