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
 *
 */

package de.schlund.pfixcore.lucefix;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.lucene.document.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import de.schlund.pfixcore.lucefix.Tripel.Type;
import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;

/**
 * @author schuppi
 * @date Jun 14, 2005
 */
public class DocumentCache {
    private Map<String, Document> cache;
    // private static Logger LOG = Logger.getLogger(DocumentCache.class);

    public DocumentCache() {
        cache = new HashMap<String, Document>();
    }

    public Document getDocument(Tripel tripel) throws FileNotFoundException, IOException, SAXException {
        return getDocument(tripel.getPath(), tripel.getType());
    }

    public Document getDocument(String path, Type type) throws FileNotFoundException, IOException, SAXException {
        // look in cache
//        LOG.debug("looking for " + path);
        Document retval = lookup(path);
        if (retval != null) {
            found++;
        }else{
            missed++;
            String filename = stripAddition(path);
            // file was not scanned
            flush(); 
            Collection<Document> newest = DocumentCache.getDocumentsFromFileAsCollection(ResourceUtil.getFileResourceFromDocroot(
                    filename));
            for (Iterator<Document> iter = newest.iterator(); iter.hasNext();) {
                Document element = iter.next();
                if (type != Type.EDITORUPDATE){
                    cache.put(element.get("path"), element);
                }
                if (path.equals(element.get("path"))) {
                    retval = element;
                    break;
                }
            }
        }
        return retval;
    }

    /**
     * @param path
     * @return
     */
    private static String stripAddition(String path) {
        int letzterSlash = path.lastIndexOf("/");
        int vorletzterSlash = path.lastIndexOf("/", letzterSlash - 1);
        return path.substring(0, vorletzterSlash);
    }

    /**
     * @param path
     * @return
     */
    private Document lookup(String path) {
        return (Document) cache.get(path);
    }

    public boolean remove(Document doc) {
        if (doc == null) return false;
        return cache.remove(doc.get("path")) != null;
    }

    public Collection<Document> getRest() {
        return cache.values();
    }

    public void flush() {
        cache.clear();
    }

    private static Collection<Document> getDocumentsFromFileAsCollection(FileResource f) throws FileNotFoundException, IOException,
            SAXException {
        XMLReader xmlreader = XMLReaderFactory.createXMLReader();
        IncludeFileHandler handler = new IncludeFileHandler(f);
        xmlreader.setContentHandler(handler);
        xmlreader.setDTDHandler(handler);
        xmlreader.setEntityResolver(handler);
        xmlreader.setErrorHandler(handler);
        xmlreader.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
        try {
            xmlreader.parse(new InputSource(f.getInputStream()));
        } catch (Exception e) {
//            org.apache.log4j.Logger.getLogger(DocumentCache.class).warn("bad xml: " + f);
            return new Vector<Document>();
        }
        return handler.getScannedDocumentsAsVector();
    }
    
    // statistic stuff
    private int found = 0, missed = 0;
    protected void resetStatistic(){
        found = missed = 0;
    }

    protected int getFound() {
        return found;
    }

    protected int getMissed() {
        return missed;
    }
    
}
