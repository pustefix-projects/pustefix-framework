package de.schlund.pfixxml;

import java.io.File;
import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Category;
import org.xml.sax.SAXException;

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

    private static Category  CAT = Category.getInstance(IncludeDocumentFactory.class.getName());
    private SPCache cache= SPCacheFactory.getInstance().getDocumentCache();
    private static IncludeDocumentFactory instance= new IncludeDocumentFactory();
       
    private IncludeDocumentFactory() {}

    /**
     * Get the document corresponding to the file 
     * specified by the path. 
     */
    // FIXME! Don't do the whole method synchronized!!
    public synchronized IncludeDocument getIncludeDocument(String path, boolean mutable) throws SAXException, IOException, TransformerException  {
        /*
         * CAT.debug("cache is now: "+cache.getClass().getName());
           CAT.debug("cache cap   : "+cache.getCapacity());
           CAT.debug(System.out.println("cache size  : "+cache.getSize());*/
        IncludeDocument includeDocument= null;
        String key = getKey(path, mutable);
        
        // document not in cache->create and store it
        if (!isDocumentInCache(key)) {
         //   CAT.debug(path+"("+mutable+") "+ "not in cache");
            includeDocument= new IncludeDocument();
            includeDocument.createDocument(path, mutable);
            cache.setValue(key, includeDocument);
        }
        //document is in cache but obsolete->recreate and store it
        else if (isDocumentInCacheObsolete(path, key)) {
           // CAT.debug(path+"("+mutable+") "+ "is obsolete");
            includeDocument= new IncludeDocument();
            includeDocument.createDocument(path, mutable);
            cache.setValue(key, includeDocument);
        }
        //document is in cache and up to date->get it from cache
        else {
         //   CAT.debug(path+"("+mutable+") "+"cache hit");
            includeDocument= (IncludeDocument) cache.getValue(key);
        }
        return includeDocument;
    }

    private boolean isDocumentInCache(String path) {
        return cache.getValue(path) != null ? true : false;
    }
    
    private String getKey(String path, boolean mutable) {
        String dummy = "";
        if(mutable)
            dummy = "_m";
        else
            dummy = "_im";
            
        StringBuffer newpath = new StringBuffer();
        newpath.append(path).append(dummy);
        return newpath.toString();
    }

    private boolean isDocumentInCacheObsolete(String path, String newpath) {
        File file= new File(path);
        long savedTime= ((IncludeDocument) cache.getValue(newpath)).getModTime();
        return file.lastModified() > savedTime ? true : false;
    }

    /**
     * The getInstance method of a singleton
     */
    public static IncludeDocumentFactory getInstance() {
        return instance;
    }
}


