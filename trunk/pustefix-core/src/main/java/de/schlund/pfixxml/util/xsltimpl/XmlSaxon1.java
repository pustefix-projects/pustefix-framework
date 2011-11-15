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

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.output.SaxonOutputKeys;

import de.schlund.pfixxml.util.XmlSupport;
import de.schlund.pfixxml.util.Xslt;
import de.schlund.pfixxml.util.XsltVersion;

/**
 * @author mleidig@schlund.de
 */
public class XmlSaxon1 implements XmlSupport {
    
    public Document createInternalDOM(Source input) throws TransformerException {
        Transformer trans  = Xslt.createIdentityTransformer(XsltVersion.XSLT1);
        DOMResult   result = new DOMResult();
        trans.transform(input, result);
        return (Document) result.getNode();
    }
    
    public boolean isInternalDOM(Node node) {
        return node instanceof NodeInfo;
    }
    
    public String getIndentOutputKey() {
        return SaxonOutputKeys.INDENT_SPACES;
    }
    
}
