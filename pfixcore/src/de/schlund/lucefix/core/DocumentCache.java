package de.schlund.lucefix.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.xml.sax.SAXException;

import de.schlund.lucefix.core.Tripel;
import de.schlund.pfixxml.PathFactory;

/**
 * @author schuppi
 * @date Jun 14, 2005
 */
public class DocumentCache {
    private Map cache;
    
    public DocumentCache(){
        cache = new Hashtable();
    }
    public Document getDocument(Tripel tripel) throws FileNotFoundException, IOException, SAXException{
        return getDocument(tripel.getPath());
    }
    public Document getDocument(String path) throws FileNotFoundException, IOException, SAXException{
        // look in cache
        Document retval = lookup(path);
        if (retval == null){
            String filename = stripAddition(path);
            // file was not scanned (?)
            Collection newest = PfxDocument.getDocumentsFromFileAsCollection(PathFactory.getInstance().createPath(filename).resolve());
            for (Iterator iter = newest.iterator(); iter.hasNext();) {
                Document element = (Document) iter.next();
                cache.put(element.get("path"),element);
                if (path.equals(element.get("path"))){
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
        int vorletzterSlash = path.lastIndexOf("/", letzterSlash-1);
        return path.substring(0,vorletzterSlash);
    }
    /**
     * @param path
     * @return
     */
    private Document lookup(String path) {
        return (Document)cache.get(path);
//        for (Iterator iter = cache.iterator(); iter.hasNext();) {
//            Document element = (Document) iter.next();
//            if (path.equals(element.get("path")))
//                return element;
//        }
//        return null;
    }
    public boolean remove(Document doc){
        if (doc == null) return false;
        return cache.remove(doc.get("path")) != null;
    }
    public Collection getRest(){
        return cache.values();
    }
    public void flush(){
        cache.clear();
    }
}
