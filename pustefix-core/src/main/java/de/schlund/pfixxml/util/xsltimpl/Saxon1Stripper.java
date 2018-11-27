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
package de.schlund.pfixxml.util.xsltimpl;

import java.util.StringTokenizer;

import javax.xml.transform.TransformerException;

import org.xml.sax.Attributes;

import com.icl.saxon.Controller;
import com.icl.saxon.Mode;
import com.icl.saxon.om.Name;
import com.icl.saxon.om.NamePool;
import com.icl.saxon.om.NamespaceException;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.om.Stripper;
import com.icl.saxon.pattern.AnyNodeTest;
import com.icl.saxon.pattern.NameTest;
import com.icl.saxon.pattern.NamespaceTest;

import de.schlund.pfixxml.util.WhiteSpaceStripping;

/**
 * Extends Saxon's whitespace stripper by adding support for custom whitespace
 * settings, not depending on a XSLT stylesheet. Also support the definition of
 * stripping/preserving elements at the document root element to override the
 * global whitespace settings.
 */
public class Saxon1Stripper extends Stripper {

    private WhiteSpaceStripping stripping;
    private Mode stripperMode;
    private NamePool namePool;
    private boolean rootProcessed;

    public Saxon1Stripper(WhiteSpaceStripping stripping) {
        this(new Mode());
        this.stripping = stripping;
    }

    private Saxon1Stripper(Mode stripperRules) {
        super(stripperRules);
        this.stripperMode = stripperRules;
    }

    public void setController(Controller controller) {
        super.setController(controller);
        namePool = controller.getNamePool();
    }

    public void startElement (int nameCode, Attributes atts, int[] namespaces, int nscount)
            throws TransformerException {

        if(!rootProcessed) {
            String stripSpace = atts.getValue("strip-space");
            if(stripSpace != null) {
                preprocess(stripSpace, Boolean.FALSE, namespaces, nscount);
            } else if(stripping.getStripSpaceElements() != null) {
                preprocess(stripping.getStripSpaceElements(), Boolean.FALSE, namespaces, nscount);
            }
            String preserveSpace = atts.getValue("preserve-space");
            if(preserveSpace != null) {
                preprocess(preserveSpace, Boolean.TRUE, namespaces, nscount);
            } else if(stripping.getPreserveSpaceElements() != null) {
                preprocess(stripping.getPreserveSpaceElements(), Boolean.TRUE, namespaces, nscount);
            }
            rootProcessed = true;
        }
        super.startElement(nameCode, atts, namespaces, nscount);
    }

    private void preprocess(String elements, Boolean preserve, int[] namespaces, int nscount)
            throws TransformerException {

        StringTokenizer st = new StringTokenizer(elements);
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            try {
                if (s.equals("*")) {
                    stripperMode.addRule(AnyNodeTest.getInstance(), preserve, 0,-0.5);
                } else if (s.endsWith(":*")) {
                    String prefix = s.substring(0, s.length()-2);
                    stripperMode.addRule(new NamespaceTest(namePool, NodeInfo.ELEMENT,
                            getURICodeForPrefix(prefix, namespaces, nscount)), preserve, 0, -0.25);
                } else {
                    if (!Name.isQName(s)) {
                        throw new RuntimeException("Element name " + s + " is not a valid QName");
                    }
                    stripperMode.addRule(new NameTest(NodeInfo.ELEMENT, makeNameCode(s, false, namespaces, nscount)),
                            preserve, 0, 0);
                }
            } catch (NamespaceException err) {
                throw new TransformerException(err);
            }
        }
    }

    private int makeNameCode(String qname, boolean useDefault, int[] namespaces, int nscount)
            throws NamespaceException {

        String prefix = Name.getPrefix(qname);
        if (prefix.equals("")) {
            short uriCode = 0;
            if (useDefault) {
                uriCode = getURICodeForPrefix(prefix, namespaces, nscount);
            }
            return namePool.allocate(prefix, uriCode, qname);
        } else {
            String localName = Name.getLocalName(qname);
            short uriCode = getURICodeForPrefix(prefix, namespaces, nscount);
            return namePool.allocate(prefix, uriCode, localName);
        }
    }

    private short getURICodeForPrefix(String prefix, int[] namespaces, int nscount) {
        for(int i=0; i<nscount; i++) {
            String currentPrefix = namePool.getPrefixFromNamespaceCode(namespaces[i]);
            if(prefix.equals(currentPrefix)) {
                String currentUri = namePool.getURIFromNamespaceCode(namespaces[i]);
                return namePool.getCodeForURI(currentUri);
            }
        }
        return 0;
    }

}