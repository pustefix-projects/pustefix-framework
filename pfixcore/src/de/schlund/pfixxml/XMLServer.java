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

import org.apache.log4j.Category;
import org.w3c.dom.Document;

/**
 *
 *
 */

public class XMLServer extends AbstractXMLServer {
    private Category CAT = Category.getInstance(XMLServer.class.getName());


    protected boolean needsSession() {
        return true;
    }
   
    protected boolean allowSessionCreate() {
        return true;
    }

    public SPDocument getDom(PfixServletRequest req) throws Exception {
        SPDocument   spdoc = new SPDocument();
        String       path  = req.getPathInfo();
        RequestParam style = req.getRequestParam("__style");

        CAT.debug("pathinfo gives: " + path);
        CAT.debug("style    gives: " + style);
        
        if (path == null) {
            XMLException e = new XMLException("\nXMLServer needs a xml target in the PathInfo");
            throw(e);
        }
        if (style == null) {
            XMLException e = new XMLException("\nXMLServer needs a '__style' parameter");
            throw(e);
        }
        
        String   file = path.substring(1);
        Document doc  = (Document) ((Document) generator.createXMLLeafTarget(file).getValue()).cloneNode(true);
        if (generator.getTarget(style.getValue()) == null) {
            generator.createXSLLeafTarget(style.getValue());
        }
        
        spdoc.setXSLKey(style.getValue());
        spdoc.setDocument(doc);
        spdoc.setProperties(null);
        
        if (CAT.isDebugEnabled()) {
            CAT.debug(spdoc.toString());
        }
        
        return(spdoc);
    }
}
