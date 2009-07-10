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

package de.schlund.pfixxml;

import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.pustefixframework.resource.InputStreamResource;
import org.pustefixframework.resource.Resource;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import de.schlund.pfixxml.util.Xml;
import de.schlund.pfixxml.util.XsltVersion;


/**
 * IncludeDocument.java
 * 
 * 
 * Created: 20021031
 * 
 * @author <a href="mailto:haecker@schlund.de">Joerg Haecker</a>
 * 
 * 
 * This class encapsulates an include-module of the PUSTEFIX-system.
 * A IncludeDocument stores a Document created from a file. Currently
 * there are two types of Documents: mutable and immutable. The user
 * of this class must know which type he wants. 
 * Various administrative data like modification time
 * of the file from which it is created from and more are stored.  
 */
public class IncludeDocument {

    //~ Instance/static variables ..................................................................

    private Document              doc;
    private long                  modTime = 0;
    // private static final Logger   LOG     = Logger.getLogger(IncludeDocument.class);
    
    //~ Methods ....................................................................................

    /**
     * Create the internal document.
     * @param path the path in the filesystem to create the document from.
     * @param mutable determine if the document is mutable or not. Any attempts
     * to modify an immutable document will cause an exception.
     */
    public void createDocument(XsltVersion xsltVersion, Resource path, boolean mutable) throws SAXException, IOException, TransformerException {
        modTime  = path.lastModified();

        if (mutable) {
            doc = Xml.parseMutable((InputStreamResource)path);
        } else {
            //TODO: avoid exception by providing an appropriate method signature
            if(xsltVersion==null) throw new IllegalArgumentException("XsltVersion is required!");
            doc = Xml.parse(xsltVersion, (InputStreamResource)path);
        }
    }

    public Document getDocument() {
        return doc;
    }

    public long getModTime() {
        return modTime;
    }

    public void resetModTime() {
        modTime -= 1L;
    }
}
