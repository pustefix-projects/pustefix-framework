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

package de.schlund.pfixxml;

import java.util.Date;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Category;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.schlund.util.statuscodes.StatusCode;

/**
 * @author jtl
 *
 *
 */

public class ResultDocument {
    public  static final String PFIXCORE_NS = "http://www.schlund.de/pustefix/core";
    public  static final String IXSL_NS     = "http://www.w3.org/1999/XSL/Transform";
    
    private        Category               CAT   = Category.getInstance(ResultDocument.class.getName());
    private static DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
    
    protected Element    formresult;
    protected Element    formvalues;
    protected Element    formerrors;
    protected Element    formhiddenvals;
    protected ResultForm resultform = null;
    protected Document   doc;
    protected SPDocument spdoc;
    protected boolean    do_continue = false;
    
    public ResultDocument() throws ParserConfigurationException {
        init();
    }

    protected void init() throws ParserConfigurationException {
        spdoc  = new SPDocument();
        dbfac.setNamespaceAware(true);
        doc    = dbfac.newDocumentBuilder().newDocument();
        
        Date date = new Date();

        spdoc.setDocument(doc);
        formresult = doc.createElement("formresult");
        formresult.setAttribute("xmlns:pfx", PFIXCORE_NS);
        formresult.setAttribute("serial", "" + date.getTime());
        doc.appendChild(formresult);
    }

    public void setContinue(boolean do_continue) {
        this.do_continue = do_continue;
    }
    
    public boolean wantsContinue() {
        return do_continue;
    }
    
    public void addUsedNamespace(String prefix, String uri) {
        formresult.setAttribute("xmlns:" + prefix, uri);
    }
    
    public SPDocument getSPDocument() {
        return spdoc;
    }
    
    public void setSPDocument(SPDocument spdoc) {
        this.spdoc = spdoc;
    }

    public Element getRootElement() {
        return formresult;
    }
    
    public ResultForm createResultForm() {
        if (resultform == null) {
            formvalues = doc.createElement("formvalues");
            formresult.appendChild(formvalues);
            
            formerrors = doc.createElement("formerrors");
            formresult.appendChild(formerrors);
            
            formhiddenvals = doc.createElement("formhiddenvals");
            formresult.appendChild(formhiddenvals);
            resultform = new ResultForm(this);
        }
        return resultform;
    }

    protected Element getFormValues() {
        return formvalues;
    }

    protected Element getFormErrors() {
        return formerrors;
    }

    protected Element getFormHiddenvals() {
        return formhiddenvals;
    }
    
    public Element createNode(String name) {
        return createSubNode(formresult, name);
    }

    public Element createSubNode(Element el, String name) {
        Element node = doc.createElement(name);
        el.appendChild(node);
        return node;
    }

    public static Element addTextChild(Element element, String name, String text) {
        Document owner = element.getOwnerDocument();
        if (text == null) {
            return null;
        }
	
        Element tmp = owner.createElement(name);
        tmp.appendChild(owner.createTextNode(text));
        element.appendChild(tmp);
        return tmp;
    }

    public Element createIncludeFromStatusCode(Properties props, StatusCode code) {
        return createIncludeFromStatusCode(props, code, null);
    }
    
    public Element createIncludeFromStatusCode(Properties props, StatusCode code, String[] args) {
        String  incfile = (String) props.get("statuscodefactory.messagefile");
        String  part    = code.getStatusCodeWithDomain();
        Element include = doc.createElementNS(ResultDocument.PFIXCORE_NS, "pfx:include");
        include.setAttribute("href", incfile);
        include.setAttribute("part", part);
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                Element arg   = doc.createElementNS(ResultDocument.PFIXCORE_NS, "pfx:arg");
                arg.setAttribute("value", args[i]);
                include.appendChild(arg);
            }
        }
        return include;
    }
    
    public static Element addTextIncludeChild(Element element, String name,
                                              String incfile, String part) {
        Document owner = element.getOwnerDocument();
        
        Element include = owner.createElementNS(PFIXCORE_NS, "pfx:include");
        include.setAttribute("href", incfile);
        include.setAttribute("part", part);
	
        Element tmp = owner.createElement(name);
        tmp.appendChild(include);
        element.appendChild(tmp);
        return tmp;
    }
}
