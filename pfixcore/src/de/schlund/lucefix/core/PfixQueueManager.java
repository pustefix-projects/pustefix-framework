package de.schlund.lucefix.core;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Category;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.document.DateField;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.xml.sax.SAXException;

import de.schlund.pfixxml.PathFactory;
import de.schlund.pfixxml.XMLException;

/**
 * @author schuppi
 * @date Jun 24, 2005
 */
public class PfixQueueManager implements Runnable {

    private static PfixQueueManager _instance       = null;
    private static Category         LOG             = Category.getInstance(PfixQueueManager.class);
    public static final String      WAITMS_PROP     = "lucefix.queueidle";
    public static String      lucene_data_path;

    private Queue                   queue           = null;
    private DocumentCache           cache           = null;
    private IndexReader             reader          = null;
    private IndexSearcher           searcher        = null;
    private IndexWriter             writer          = null;
    private Collection              documents2write = null;
    private int                     waitms          = -1;

    /**
     * @param p
     * @throws XMLException
     */
    public PfixQueueManager(Properties p) throws XMLException {
        String waitprop = p.getProperty(WAITMS_PROP);
        if (waitprop == null) throw new XMLException("property " + WAITMS_PROP + " not defined in factory.prop");

        try {
            waitms = Integer.parseInt(waitprop);
        } catch (NumberFormatException e) {
            throw new XMLException("property " + WAITMS_PROP + " is not a valid integer");
        }
        lucene_data_path = PathFactory.getInstance().createPath(".index").resolve().getAbsolutePath();
        
        queue = new Queue();
        documents2write = new Vector();
    }

    /*
     * @see java.lang.Runnable#run()
     */
    public void run() {
        Tripel current;
        long startLoop, stopLoop;
        int added, updated, removed,size;
        cache = new DocumentCache();
        while (true) {
            startLoop = System.currentTimeMillis();
            added = updated = removed = size = 0;
            while ((current = queue.next()) != null) {
                try {
                    switch (current.getType()) {
                        case TripelImpl.INDEX :
                            
                            try {
                                if (reader == null) reader = IndexReader.open(lucene_data_path);
                            } catch (IOException e) {
                                createDB();
                                reader = IndexReader.open(lucene_data_path);
                            }
                            if (size == 0) size = reader.numDocs();
                            if (searcher == null) searcher = new IndexSearcher(reader);
                            
                            Term term = new Term("path", current.getPath());
                            TermQuery query = new TermQuery(term);
                            Hits hits = searcher.search(query);
                            if (hits.length() == 0){
                                // current queued is NOT indexed
                                Document newdoc = cache.getDocument(current);
                                if (newdoc == null){
                                    LOG.warn("wanted to work on " + current + " but there is no part for it...");
                                    break;
                                }
                                documents2write.add(newdoc);
                                added++;
                                cache.remove(newdoc);
                            }else if (hits.length() == 1){
                                File f = PathFactory.getInstance().createPath(current.getFilename()).resolve();
                                
//                                File f = new File(current.getPath());
                                if (f.lastModified() == DateField.stringToTime(hits.doc(0).get("lasttouch")))
                                    cache.remove(hits.doc(0));
                                else{
                                    // ts differs, remove outdaten from index and add the new
                                    Document newDoc = cache.getDocument(current);
                                    documents2write.add(newDoc);
                                    cache.remove(newDoc);
                                    reader.delete(term);
                                    updated++;
                                }
                            }

                            break;

                        case TripelImpl.DELETE :
                            if (reader == null) reader = IndexReader.open(lucene_data_path);
                            if (size == 0) size = reader.numDocs();
                            reader.delete(new Term("path",current.getPath()));
                            removed++;
                            break;
                        default :
                            LOG.error("unsupported tripeltype, discarding");
                    }
                } catch (IOException e) {
                    LOG.error("error in " + getClass(), e);
                } catch (SAXException e){
                    LOG.error("error parsing " + current.getPath(), e);
                }
            }
            
            try {
                if (searcher != null) searcher.close();
                searcher = null;
                if (reader != null) reader.close();
                reader = null;
                
                if (documents2write.size() > 0) {
                    if (writer == null) writer = new IndexWriter(lucene_data_path, new GermanAnalyzer(), false);
                    for (Iterator iter = documents2write.iterator(); iter.hasNext();) {
                        Document element = (Document) iter.next();
                        if (element != null) writer.addDocument(element);
                    }
                    writer.optimize();
                    writer.close();
                    writer = null;
                }
                
            } catch (IOException e) {
                LOG.error("error writing new index", e);
                try {
                    if (writer != null) writer.close();
                } catch (IOException e1) {
                    LOG.error("unable to close indexwriter...", e1);
                    e1.printStackTrace();
                }
            }
            
            documents2write.clear();
            cache.flush();
            size += added-removed;
            size = Math.abs(size);
            stopLoop = System.currentTimeMillis();
            long needed = stopLoop-startLoop;
//            if (needed > 10)
                LOG.debug(needed + "ms | " + added + " new docs, " + updated + " updated docs, " + removed + " deleted docs | indexsize: " + (size));
            
            try {
                Thread.sleep(waitms);
            } catch (InterruptedException e) {}
        }
    }

    private void createDB() throws IOException {
        LOG.debug("created db");
        writer = new IndexWriter(lucene_data_path, new GermanAnalyzer(), true);
        writer.optimize();
        writer.close();
        writer = null;
    }

    /**
     * @param p
     * @return
     * @throws XMLException
     */
    public static synchronized PfixQueueManager getInstance(Properties p) throws XMLException {
        if (_instance == null) _instance = new PfixQueueManager(p);
        return _instance;
    }
    public void queue(Tripel newTripel){
        synchronized (queue) {
            queue.add(newTripel);
        }
    }
}