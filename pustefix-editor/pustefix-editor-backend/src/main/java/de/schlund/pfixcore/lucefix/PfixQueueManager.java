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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.DateField;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.xml.sax.SAXException;

import de.schlund.pfixxml.XMLException;
import de.schlund.pfixxml.config.GlobalConfig;

/**
 * @author schuppi
 * @date Jun 24, 2005
 */
public class PfixQueueManager implements Runnable {

    private static PfixQueueManager _instance = null;
    private final static Logger LOG = Logger.getLogger((PfixQueueManager.class));
    public static final String WAITMS_PROP = "lucefix.queueidle";
    public static String lucene_data_path;
    private Queue<Tripel> queue = new LinkedList<Tripel>();
    private DocumentCache cache = null;
    private IndexReader reader = null;
    private IndexSearcher searcher = null;
    private IndexWriter writer = null;
    private Collection<Document> documents2write = null;
    private int waitms = -1;
    private Analyzer analyzer = PreDoc.ANALYZER;
    protected Object mutex = new Object();

    /**
     * @param p
     * @throws XMLException
     */
    public PfixQueueManager(Integer idletime) {

        waitms = idletime;
        lucene_data_path = (new File(GlobalConfig.getDocroot(), ".index")).getAbsolutePath();

        documents2write = new Vector<Document>();
    }

    /*
     * @see java.lang.Runnable#run()
     */
    public void run() {
        Tripel current;
        long startLoop, stopLoop;
        int added, updated, removed, size;
        cache = new DocumentCache();
        while (true) {
                startLoop = System.currentTimeMillis();
                added = updated = removed = size = 0;
                queueloop: while ((current = queue.poll()) != null) {
                    try {
                        if (current.getType() == Tripel.Type.INSERT
                                || current.getType() == Tripel.Type.EDITORUPDATE) {
                            try {
                                if (reader == null)
                                    reader = IndexReader.open(lucene_data_path);
                            } catch (IOException e) {
                                createDB();
                                reader = IndexReader.open(lucene_data_path);
                            }
                            if (size == 0)
                                size = reader.numDocs();
                            if (searcher == null)
                                searcher = new IndexSearcher(reader);

                            Term term = new Term("path", current.getPath());
                            TermQuery query = new TermQuery(term);
                            Hits hits = searcher.search(query);
                            if (hits.length() == 0) {
                                // current queued is NOT indexed
                                Document newdoc = cache.getDocument(current);
                                if (newdoc == null) {
//                                	this just happens too often to log it
//                                    LOG.debug("wanted to work on " + current + " but there is no part for it...");
                                    continue queueloop;
                                }
                                documents2write.add(newdoc);
                                added++;
                                cache.remove(newdoc);
                            } else if (hits.length() == 1) {
                                File f = new File(GlobalConfig.getDocroot(),
                                        current.getFilename());

                                // File f = new File(current.getPath());
                                if (f.lastModified() == DateField
                                        .stringToTime(hits.doc(0).get(
                                                "lasttouch"))){
                                    cache.remove(hits.doc(0));
                                    LOG.debug("TS is ok, discarding action: " + term);
                                } else {
                                    // ts differs, remove outdaten from index
                                    // and
                                    // add the new
                                    Document newDoc = cache
                                            .getDocument(current);
                                    reader.delete(term);
                                    if (newDoc == null) {
                                        continue queueloop;
                                    }
                                    documents2write.add(newDoc);
                                    cache.remove(newDoc);
                                    updated++;
                                }
                            } else {
                                LOG.error("multihit for unique term: " + term);
                            }
                        } else if (current.getType() == Tripel.Type.DELETE) {
                            if (reader == null)
                                reader = IndexReader.open(lucene_data_path);
                            if (size == 0)
                                size = reader.numDocs();
                            reader.delete(new Term("path", current.getPath()));
                            removed++;
                        } else {
                            LOG.error("unsupported tripeltype, discarding");
                        }
                    } catch (IOException e) {
                        LOG.error("error in " + getClass(), e);
                    } catch (SAXException e) {
                        LOG.error("error parsing " + current.getPath(), e);
                    }
                }

                try {
                    if (searcher != null)
                        searcher.close();
                    searcher = null;
                    if (reader != null)
                        reader.close();
                    reader = null;

                    if (documents2write.size() > 0) {
                        if (writer == null)
                            writer = new IndexWriter(lucene_data_path,
                                    analyzer, false);
                        
                        for (Document doc : documents2write) {
                            writer.addDocument(doc);
                        }
                        writer.optimize();
                        writer.close();
                        writer = null;
                    }

                } catch (IOException e) {
                    LOG.error("error writing new index", e);
                    try {
                        if (writer != null)
                            writer.close();
                    } catch (IOException e1) {
                        LOG.error("unable to close indexwriter...", e1);
                        e1.printStackTrace();
                    }
                }

                documents2write.clear();
                cache.flush();
                size += added - removed;
                size = Math.abs(size);
                stopLoop = System.currentTimeMillis();
                long needed = stopLoop - startLoop;
                if (added != 0 || updated != 0 || removed != 0) {
                    LOG.debug(needed + "ms | " + added + " new docs, "
                            + updated + " updated docs, " + removed
                            + " deleted docs | indexsize: " + (size)
                            + " | cacheratio: " + cache.getFound() + "/"
                            + cache.getMissed());
                }
                cache.resetStatistic();
//            }
            try {
                Thread.sleep(waitms);
            } catch (InterruptedException e) {}
        }
    }

    private void createDB() throws IOException {
        LOG.debug("created db");
        writer = new IndexWriter(lucene_data_path, analyzer, true);
        writer.optimize();
        writer.close();
        writer = null;
    }

    /**
     * @param p
     * @return
     * @throws XMLException
     */
    public static synchronized PfixQueueManager getInstance(Integer idletime) {
        if (_instance == null)
            _instance = new PfixQueueManager(idletime);
        return _instance;
    }

    public void queue(Tripel newTripel) {
        synchronized (queue) {
            queue.offer(newTripel);
        }
    }
}