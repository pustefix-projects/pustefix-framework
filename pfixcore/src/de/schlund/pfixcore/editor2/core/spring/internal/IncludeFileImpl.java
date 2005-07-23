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
 */

package de.schlund.pfixcore.editor2.core.spring.internal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import de.schlund.pfixcore.editor2.core.dom.AbstractIncludeFile;
import de.schlund.pfixcore.editor2.core.dom.IncludePart;
import de.schlund.pfixcore.editor2.core.spring.ImageFactoryService;
import de.schlund.pfixcore.editor2.core.spring.IncludeFactoryService;
import de.schlund.pfixcore.editor2.core.spring.ProjectFactoryService;
import de.schlund.pfixcore.editor2.core.spring.ThemeFactoryService;
import de.schlund.pfixcore.editor2.core.spring.VariantFactoryService;
import de.schlund.pfixxml.IncludeDocument;
import de.schlund.pfixxml.util.XPath;

/**
 * Implementation of IncludeFile using a Pustefix IncludeDocument as backend
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class IncludeFileImpl extends AbstractIncludeFile {
    private String path;

    private ProjectFactoryService projectfactory;

    private ThemeFactoryService themefactory;

    private VariantFactoryService variantfactory;

    private Hashtable cache;

    private IncludeDocument pfixIncDoc;

    private IncludeFactoryService includefactory;

    private ImageFactoryService imagefactory;

    public IncludeFileImpl(ProjectFactoryService projectfactory,
            ThemeFactoryService themefactory,
            VariantFactoryService variantfactory,
            IncludeFactoryService includefactory,
            ImageFactoryService imagefactory, String path,
            IncludeDocument pfixIncDoc) {
        this.projectfactory = projectfactory;
        this.themefactory = themefactory;
        this.variantfactory = variantfactory;
        this.includefactory = includefactory;
        this.imagefactory = imagefactory;
        this.path = path;
        this.pfixIncDoc = pfixIncDoc;
        this.cache = new Hashtable();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.IncludeFile#getPath()
     */
    public String getPath() {
        return this.path;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.IncludeFile#getPart(java.lang.String)
     */
    public IncludePart getPart(String name) {
        if (this.cache.containsKey(name)) {
            return (IncludePart) this.cache.get(name);
        }
        synchronized (cache) {
            if (!this.cache.containsKey(name)) {
                Node xml = this.getContentXML();
                if (xml == null) {
                    return null;
                }
                try {
                    if (!XPath.test(xml, "part[@name='" + name + "']")) {
                        return null;
                    }
                } catch (TransformerException e) {
                    // Should NEVER happen
                    Logger.getLogger(this.getClass()).error("XPath error!", e);
                    return null;
                }
                IncludePart incPart = new IncludePartImpl(this.themefactory,
                        this.projectfactory, this.variantfactory,
                        this.includefactory, this.imagefactory, name, this);
                this.cache.put(name, incPart);
            }
        }
        return (IncludePart) this.cache.get(name);
    }

    public void setPfixIncludeDocument(IncludeDocument includeDocumentForFile) {
        this.pfixIncDoc = includeDocumentForFile;
    }

    public Document getContentXML() {
        if (this.pfixIncDoc == null) {
            return null;
        }
        return pfixIncDoc.getDocument();
    }

    public IncludePart createPart(String name) {
        IncludePart part;
        part = this.getPart(name);
        if (part != null) {
            return part;
        }
        synchronized (cache) {
            if (!this.cache.containsKey(name)) {
                IncludePart incPart = new IncludePartImpl(this.themefactory,
                        this.projectfactory, this.variantfactory,
                        this.includefactory, this.imagefactory, name, this);
                this.cache.put(name, incPart);
            }
        }
        return (IncludePart) this.cache.get(name);
    }

    public boolean hasPart(String name) {
        return (this.getPart(name) == null);
    }

    public Collection getParts() {
        // Make sure all physically existing parts are in cache
        Node xml = this.getContentXML().getDocumentElement();
        if (xml != null) {
            try {
                List nlist = XPath.select(xml, "part/@name");
                for (Iterator i = nlist.iterator(); i.hasNext();) {
                    this.getPart(((Node) i.next()).getNodeValue());
                }
            } catch (TransformerException e) {
                // Should NEVER happen
                // Log and go on
                Logger.getLogger(this.getClass()).error("XPath error!", e);
            }
        }
        // Now use cache to return physical and virtual parts
        // Synchronize and copy the values to make sure we return a
        // static version to iterate over.
        HashSet temp = new HashSet();
        synchronized (this.cache) {
            for (Iterator i = this.cache.values().iterator(); i.hasNext();) {
                temp.add(i.next());
            }
        }
        return temp;
    }

}
