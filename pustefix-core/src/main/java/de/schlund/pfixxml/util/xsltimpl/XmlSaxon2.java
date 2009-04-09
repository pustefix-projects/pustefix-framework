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

import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.event.SaxonOutputKeys;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.tinytree.TinyBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.schlund.pfixxml.util.XmlSupport;
import de.schlund.pfixxml.util.Xslt;
import de.schlund.pfixxml.util.XsltVersion;

/**
 * @author mleidig@schlund.de
 */
public class XmlSaxon2 implements XmlSupport {

    public Document createInternalDOM(Source input) throws TransformerException {
        TinyBuilder builder = new TinyBuilder();
        Transformer t = Xslt.createIdentityTransformer(XsltVersion.XSLT2);
        t.transform(input, builder);
        NodeInfo node = builder.getCurrentRoot();
        return (Document) NodeOverNodeInfo.wrap(node);
    }

    public boolean isInternalDOM(Node node) {
        return node instanceof NodeOverNodeInfo;
    }

    public String getIndentOutputKey() {
        return SaxonOutputKeys.INDENT_SPACES;
    }

}
