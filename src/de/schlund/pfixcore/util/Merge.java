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
package de.schlund.pfixcore.util;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import de.schlund.pfixxml.util.XPath;
import de.schlund.pfixxml.util.Xml;

public class Merge {
    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            throw new IOException("expected 2 arguments, got " + args.length);
        }
        new Merge(new File(args[0]), new File(args[1]), args[2], args[3]).run();
    }
    
    //-- 
    
    private final File envFile;
    private final File coreFile;
    private final String removePath;
    private final String addPath;
    
    public Merge(File envFile, File coreFile, String removePath, String addPath) throws IOException {
        this.envFile = envFile;
        this.coreFile = coreFile;
        this.removePath = removePath;
        this.addPath = addPath;
    }
    
    public void run() throws SAXException, IOException, TransformerException {
        Document envDoc;
        Document coreDoc;
        List tmp;
        int overwrite;
        
        System.out.println("Merge " + coreFile + " into" + envFile);
        envDoc = Xml.parseMutable(envFile);
        coreDoc = Xml.parseMutable(coreFile);
        tmp = XPath.select(envDoc, removePath);
        System.out.println("  remove " + tmp.size());
        remove(tmp);
        remove(XPath.select(envDoc, "/include_parts/part/product[count(lang) = 0]"));
        remove(XPath.select(envDoc, "/include_parts/part[count(product) = 0]"));
        tmp = XPath.select(coreDoc, addPath);
        overwrite = add(tmp, envDoc);
        System.out.println("  add " + tmp.size() + ", overwriting "+ overwrite);
        Xml.serialize(envDoc, envFile, true, true);
    }

    private int add(List lst, Document dest) throws TransformerException {
        Iterator iter;
        int overwrite;
        
        overwrite = 0;
        iter = lst.iterator();
        while (iter.hasNext()) {
            if (addLang((Element) iter.next(), dest)) {
                overwrite++;
            }
        }
        return overwrite;
    }
    
    private boolean addLang(Element lang, Document dest) throws TransformerException {
        Element part;
        Element product;
        
        product = (Element) lang.getParentNode();
        part = (Element) product.getParentNode();
        return addLang(getName(part), getName(product), lang, dest);
    }

    private boolean addLang(String partName, String productName, Element lang, Document dest) throws TransformerException {
        Node all;
        Element destPart;
        Element destProduct;
        Node newNode;
        Node first;
        boolean overwrite;
        
        all = XPath.selectNode(dest, "/include_parts");
        destPart = getElement(all, "part", partName);
        destProduct = getElement(destPart, "product", productName);
        overwrite = removeLang(destProduct, getName(lang));
        newNode = dest.importNode(lang, true);
        first = destProduct.getFirstChild();
        if (first != null) {
            destProduct.insertBefore(newNode, first);
        } else {
            destProduct.appendChild(newNode);
        }
        return overwrite;
    }

    private boolean removeLang(Element product, String langName) throws TransformerException {
        Node lang;
        
        lang = XPath.selectNode(product, "lang[@name = '" + langName + "']");
        if (lang != null) {
            lang.getParentNode().removeChild(lang);
            return true;
        }
        return false;
    }

    private Element getElement(Node root, String element, String name) throws TransformerException {
        Element result;
        
        result = (Element) XPath.selectNode(root, element + "[@name = '" + name + "']");
        if (result == null) {
            result = root.getOwnerDocument().createElement(element);
            result.setAttribute("name", name);
            root.appendChild(result);
        }
        return result;
    }
    
    private static String getName(Element ele) throws TransformerException {
        String name;
        
        name = ele.getAttribute("name");
        if (name == null) {
            throw new TransformerException("missing name: " + Xml.serialize(ele, true, false));
        }
        return name;
    }
    
    private void remove(List nodes) {
        Iterator iter;
        Node node;
        
        iter = nodes.iterator();
        while (iter.hasNext()) {
            node = (Node) iter.next();
            node.getParentNode().removeChild(node);
        }
    }

    private static String serialize(Node node) {
        return Xml.serialize(node, true, false);
    }
}