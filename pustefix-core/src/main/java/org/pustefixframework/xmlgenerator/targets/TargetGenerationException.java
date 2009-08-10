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

package org.pustefixframework.xmlgenerator.targets;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import de.schlund.pfixxml.util.Xml;

/**
 * Exception subclass which represents errors during generation of a target.
 * <br/>
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */
public class TargetGenerationException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 9097944294112810557L;
    private String targetkey;

    public TargetGenerationException() {
        super();
    }

    public TargetGenerationException(String msg) {
        super(msg);
    }

    public TargetGenerationException(String msg, Throwable cause) {
        super(msg);
        initCause(cause);
    }

    public TargetGenerationException(Throwable cause) {
        initCause(cause);
    }

    /**
     * @return
     */
    public String getTargetkey() {
        return targetkey;
    }

    /**
     * @param string
     */
    public void setTargetkey(String key) {
        targetkey = key;
    }

    public Document toXMLRepresentation() {
        return createErrorTree(this);
    }

    private Document createErrorTree(TargetGenerationException targetex) {
        Document doc = Xml.createDocument();
        Element e0 = doc.createElement("error");
        e0.setAttribute("type", "xslt");
        doc.appendChild(e0);
        printEx(targetex, doc, e0);
        return doc;
    }

    private void insertErrInfo(Element error, String key, String value) {
        Element info = error.getOwnerDocument().createElement("info");
        info.setAttribute("key", key);
        info.setAttribute("value", value);
        error.appendChild(info);
    }

    private void printEx(Throwable e, Document doc, Node root) {
        if (e == null) {
            return;
        }

        Element error = doc.createElement("exception");
        root.appendChild(error);
        error.setAttribute("type", e.getClass().getName());
        insertErrInfo(error, "Message", e.getMessage());

        if (e instanceof SAXParseException) {
            SAXParseException sex = (SAXParseException) e;
            insertErrInfo(error, "Id", sex.getSystemId());
            insertErrInfo(error, "Line", "" + sex.getLineNumber());
            insertErrInfo(error, "Column", "" + sex.getColumnNumber());
            printEx(sex.getException(), doc, root);
        } else if (e instanceof TargetGenerationException) {
            TargetGenerationException tagex = (TargetGenerationException) e;
            insertErrInfo(error, "Key", tagex.getTargetkey());
            printEx(tagex.getCause(), doc, root);
        } else if (e instanceof TransformerException) {
            TransformerException trex = (TransformerException) e;
            insertErrInfo(error, "Location", trex.getLocationAsString());
            printEx(trex.getCause(), doc, root);
        } else if (e instanceof SAXException) {
            SAXException saxex = (SAXException) e;
            printEx(saxex.getException(), doc, root);
        }
    }

    public String toStringRepresentation() {
        StringBuffer sb = new StringBuffer();
        printEx(this, sb, " ");
        return sb.toString();
    }

    private void appendStr(StringBuffer buf, String indent, String key, String value) {
        buf.append("|").append(indent).append(key).append(": ").append(value).append("\n");
    }

    private void printEx(Throwable e, StringBuffer buf, String indent) {
        String br = "\n";
        if (e == null) {
            return;
        }
        if (e instanceof SAXParseException) {
            SAXParseException sex = (SAXParseException) e;
            buf.append("|").append(br);
            appendStr(buf, indent, "Type", sex.getClass().getName());
            appendStr(buf, indent, "Message", sex.getMessage());
            appendStr(buf, indent, "Id", sex.getSystemId());
            appendStr(buf, indent, "Line", ""+sex.getLineNumber());
            appendStr(buf, indent, "Column", ""+sex.getColumnNumber());
        } else if (e instanceof TargetGenerationException) {
            TargetGenerationException tgex = (TargetGenerationException) e;
            buf.append("|").append(br);
            appendStr(buf, indent, "Type", tgex.getClass().getName());
            appendStr(buf, indent, "Message", tgex.getMessage());
            appendStr(buf, indent, "Target", tgex.getTargetkey());
            printEx(tgex.getCause(), buf, indent + " ");
        } else if (e instanceof TransformerException) {
            TransformerException trex = (TransformerException) e;
            buf.append("|").append(br);
            appendStr(buf, indent, "Type", trex.getClass().getName());
            appendStr(buf, indent, "Message", trex.getMessage());
            printEx(trex.getCause(), buf, indent + " ");
        } else {
            buf.append("|").append(br);
            appendStr(buf, indent, "Type", e.getClass().getName());
            appendStr(buf, indent, "Message", e.getMessage());
        }
        //printEx(e, buf, indent + " ");
    }

}
