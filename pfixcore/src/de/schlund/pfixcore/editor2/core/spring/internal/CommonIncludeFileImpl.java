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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import de.schlund.pfixcore.editor2.core.dom.AbstractIncludeFile;
import de.schlund.pfixcore.editor2.core.dom.IncludePart;
import de.schlund.pfixcore.editor2.core.spring.FileSystemService;
import de.schlund.pfixcore.editor2.core.spring.PathResolverService;
import de.schlund.pfixxml.util.XPath;

/**
 * Class implementing functions common to IncludeFiles and DynIncludeFiles
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public abstract class CommonIncludeFileImpl extends AbstractIncludeFile {
    private String path;

    private HashMap cache;

    private FileSystemService filesystem;

    private PathResolverService pathresolver;

    private long lastModTime;

    private Document xmlCache;

    protected abstract IncludePart createIncludePartInstance(String name);

    public CommonIncludeFileImpl(FileSystemService filesystem,
            PathResolverService pathresolver, String path) {
        this.filesystem = filesystem;
        this.pathresolver = pathresolver;
        this.path = path;
        this.cache = new HashMap();
        this.lastModTime = 0;
        this.xmlCache = null;
    }

    protected FileSystemService getFileSystemService() {
        return this.filesystem;
    }

    protected PathResolverService getPathResolverService() {
        return this.pathresolver;
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
        synchronized (cache) {
            if (this.cache.containsKey(name)) {
                return (IncludePart) this.cache.get(name);
            } else {
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
                IncludePart incPart = createIncludePartInstance(name);
                this.cache.put(name, incPart);
                return incPart;
            }
        }
    }

    public Document getContentXML() {
        return this.getContentXML(false);
    }

    public Document getContentXML(boolean forceUpdate) {
        File xmlFile = new File(this.pathresolver.resolve(this.getPath()));
        if (!forceUpdate && this.xmlCache != null) {
            synchronized (this.xmlCache) {
                if (xmlFile.lastModified() <= this.lastModTime) {
                    return this.xmlCache;
                }
            }
        }
        synchronized (this.filesystem.getLock(xmlFile)) {
            try {
                return this.filesystem.readXMLDocumentFromFile(xmlFile);
            } catch (SAXException e) {
                Logger.getLogger(this.getClass()).warn(e);
                return null;
            } catch (IOException e) {
                Logger.getLogger(this.getClass()).warn(e);
                return null;
            }
        }
    }

    public IncludePart createPart(String name) {
        synchronized (cache) {
            if (!this.cache.containsKey(name)) {
                IncludePart incPart = createIncludePartInstance(name);
                this.cache.put(name, incPart);
            }
            return (IncludePart) this.cache.get(name);
        }   
    }

    public boolean hasPart(String name) {
        return (this.getPart(name) == null);
    }

    public Collection getParts() {
        // Make sure all physically existing parts are in cache
        synchronized (this.cache) {
            Document xmlDoc = this.getContentXML();
            Node xml = null;
            if (xmlDoc != null) {
                xml = xmlDoc.getDocumentElement();
            }
            if (xml != null) {
                try {
                    List nlist = XPath.select(xml, "part/@name");
                    for (Iterator i = nlist.iterator(); i.hasNext();) {
                        String partName = ((Node) i.next()).getNodeValue();
                        if (!this.cache.containsKey(partName)) {
                            IncludePart incPart = createIncludePartInstance(partName);
                            this.cache.put(partName, incPart);
                        }
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
            HashSet temp = new HashSet(this.cache.values());
            return temp;
        }
    }

}
