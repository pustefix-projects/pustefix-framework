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

package de.schlund.pfixxml;

import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import de.schlund.pfixxml.resources.Resource;
import de.schlund.pfixxml.targets.SPCache;
import de.schlund.pfixxml.targets.SPCacheFactory;
import de.schlund.pfixxml.util.XsltVersion;

/**
 * IncludeDocumentFactory.java  
 * 
 * 
 * Created: 20021029
 * 
 * @author <a href="mailto:haecker@schlund.de">Joerg Haecker</a>
 * 
 * 
 * This class realises the factory and the singleton pattern. It is responsible to 
 * create and store objects of type {@link IncludeDocument}  in a {@link SPCache} 
 * cache created by  {@link SPCacheFactory}. If a requested IncludeDocument is found 
 * in the cache it will be returned, otherwise it will be created and stored in the cache. 
 * If the source file of a requested IncludeDocument is newer then the one stored in the cache, 
 * it will be recreated and be stored in the cache. 
 */
public class IncludeDocumentFactory {

    // private static Logger                 LOG      = Logger.getLogger(IncludeDocumentFactory.class);
    private static IncludeDocumentFactory instance = new IncludeDocumentFactory();
    private SPCache<String, IncludeDocument> cache = SPCacheFactory.getInstance().getDocumentCache();
       
    private IncludeDocumentFactory() {}

    public static IncludeDocumentFactory getInstance() {
        return instance;
    }
    
    // FIXME! Don't do the whole method synchronized!!
    public synchronized IncludeDocument getIncludeDocument(XsltVersion xsltVersion, Resource path, boolean mutable) throws SAXException, IOException, TransformerException  {
        //TODO: change method signature (create multiple methods) to reflect mutable vs. immutable document creation
        if(xsltVersion==null&&!mutable) throw new IllegalArgumentException("XsltVersion has to be specified to create a immutable document.");
        IncludeDocument includeDocument = null;
        String          key             = getKey(xsltVersion, path, mutable);
        
        if (!isDocumentInCache(key) || isDocumentInCacheObsolete(path, key)) {
            includeDocument = new IncludeDocument();
            includeDocument.createDocument(xsltVersion, path, mutable);
            cache.setValue(key, includeDocument);
        } else {
            includeDocument = (IncludeDocument) cache.getValue(key);
        }
        return includeDocument;
    }

    private boolean isDocumentInCache(String key) {
        return cache.getValue(key) != null ? true : false;
    }
    
    private String getKey(XsltVersion xsltVersion, Resource path, boolean mutable) {
        return mutable ? path.toURI().toString() + "_mutable" : path.toURI().toString() + "_imutable"+"_"+xsltVersion;
    }

    private boolean isDocumentInCacheObsolete(Resource path, String newkey) {
        long savedTime = ((IncludeDocument) cache.getValue(newkey)).getModTime();
        return path.lastModified() > savedTime ? true : false;
    }

    public void reset() {
        SPCacheFactory.getInstance().reset();
        cache = SPCacheFactory.getInstance().getDocumentCache();
    }
    
}

