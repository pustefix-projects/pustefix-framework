package de.schlund.pfixxml;

import java.io.File;
import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Category;
import org.xml.sax.SAXException;

import de.schlund.pfixxml.util.Path;
import de.schlund.pfixxml.targets.SPCache;
import de.schlund.pfixxml.targets.SPCacheFactory;

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

    private static Category               CAT      = Category.getInstance(IncludeDocumentFactory.class.getName());
    private static IncludeDocumentFactory instance = new IncludeDocumentFactory();
    private SPCache                       cache    = SPCacheFactory.getInstance().getDocumentCache();
       
    private IncludeDocumentFactory() {}

    public static IncludeDocumentFactory getInstance() {
        return instance;
    }
    
    // FIXME! Don't do the whole method synchronized!!
    public synchronized IncludeDocument getIncludeDocument(Path path, boolean mutable) throws SAXException, IOException, TransformerException  {

        IncludeDocument includeDocument = null;
        String          key             = getKey(path, mutable);
        
        if (!isDocumentInCache(key) || isDocumentInCacheObsolete(path, key)) {
            includeDocument = new IncludeDocument();
            includeDocument.createDocument(path, mutable);
            cache.setValue(key, includeDocument);
        } else {
            includeDocument = (IncludeDocument) cache.getValue(key);
        }
        return includeDocument;
    }

    private boolean isDocumentInCache(String key) {
        return cache.getValue(key) != null ? true : false;
    }
    
    private String getKey(Path path, boolean mutable) {
        return mutable ? path.getRelative() + "_mutable" : path.getRelative() + "_imutable";
    }

    private boolean isDocumentInCacheObsolete(Path path, String newkey) {
        File file      = path.resolve();
        long savedTime = ((IncludeDocument) cache.getValue(newkey)).getModTime();
        return file.lastModified() > savedTime ? true : false;
    }

    public void reset() {
        SPCacheFactory.getInstance().reset();
        cache = SPCacheFactory.getInstance().getDocumentCache();
    }
    
}

