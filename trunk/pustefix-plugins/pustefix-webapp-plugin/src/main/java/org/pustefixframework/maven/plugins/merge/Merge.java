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

package org.pustefixframework.maven.plugins.merge;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.schlund.pfixxml.util.Generics;
import de.schlund.pfixxml.util.XPath;
import de.schlund.pfixxml.util.Xml;

public class Merge {

    private final InputSource src;
    private final String srcPath;
    private final File dest;
    private final boolean verbose;

    public Merge(InputSource src, String srcPath, File dest) {
        this(src, srcPath, dest, true);
    }

    public Merge(InputSource src, String srcPath, File dest, boolean verbose) {
        this.src = src;
        this.srcPath = srcPath;
        this.dest = dest;
        this.verbose = verbose;
    }

    public void run() throws SAXException, IOException, TransformerException {
        Document destDoc;
        List<Node> nodes;
        int modified;

        if (verbose)
            System.out.println("Merge " + src + " into" + dest);
        destDoc = Xml.parseMutable(dest);
        remove(XPath.select(destDoc, "/include_parts/part/theme[count(*) = 0 and normalize-space(text()) = '']"));
        remove(XPath.select(destDoc, "/include_parts/part[count(theme) = 0]"));
        nodes = XPath.select(Xml.parseMutable(src), srcPath);
        modified = merge(Generics.<Element> convertList(nodes), destDoc);
        if (verbose)
            System.out.println("  merged: " + modified);
        Xml.serialize(destDoc, dest, true, true);
    }

    private int merge(List<Element> src, Document dest) throws TransformerException {
        int merged;

        merged = 0;
        for (Element element : src) {
            if (mergeTheme(element, dest)) {
                merged++;
            }
        }
        return merged;
    }

    private boolean mergeTheme(Element src, Document dest) throws TransformerException {
        Element part;

        ensureElement(src, "theme");
        part = (Element) src.getParentNode();
        ensureElement(part, "part");
        return mergeTheme(getName(part), getName(src), src, dest);
    }

    private boolean mergeTheme(String partName, String themeName, Element src, Document dest)
            throws TransformerException {
        Node all;
        Element destPart;
        Element destTheme;
        Node first;

        all = XPath.selectNode(dest, "/include_parts");
        destPart = getOrCreate(all, "part", partName);
        destTheme = get(destPart, "theme", themeName);
        if (destTheme != null) {
            return false;
        }
        destTheme = (Element) dest.importNode(src, true);
        first = destPart.getFirstChild();
        if (first != null) {
            destPart.insertBefore(destTheme, first);
        } else {
            destPart.appendChild(destTheme);
        }
        return true;
    }

    private void ensureElement(Element ele, String name) {
        if (!name.equals(ele.getTagName())) {
            throw new IllegalArgumentException("illegal element: expected " + name + ", found " + ele.getTagName());
        }
    }

    private Element getOrCreate(Node root, String element, String name) throws TransformerException {
        Element result;

        result = (Element) XPath.selectNode(root, element + "[@name = '" + name + "']");
        if (result == null) {
            result = root.getOwnerDocument().createElement(element);
            result.setAttribute("name", name);
            root.appendChild(result);
        }
        return result;
    }

    private Element get(Node root, String element, String name) throws TransformerException {
        Element result;

        result = (Element) XPath.selectNode(root, element + "[@name = '" + name + "']");
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

    private void remove(List<Node> nodes) {
        Iterator<Node> iter;
        Node node;

        iter = nodes.iterator();
        while (iter.hasNext()) {
            node = iter.next();
            node.getParentNode().removeChild(node);
        }
    }

}
