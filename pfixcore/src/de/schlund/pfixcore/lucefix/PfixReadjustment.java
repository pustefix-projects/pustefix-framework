/*
 * This file is part of PFIXCORE.
 * 
 * PFIXCORE is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * PFIXCORE is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package de.schlund.pfixcore.lucefix;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.apache.lucene.document.DateField;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;

import de.schlund.pfixcore.editor2.core.dom.IncludePartThemeVariant;
import de.schlund.pfixcore.editor2.core.dom.Project;
import de.schlund.pfixcore.editor2.core.spring.ProjectFactoryService;
import de.schlund.pfixcore.editor2.frontend.util.SpringBeanLocator;
import de.schlund.pfixxml.XMLException;
import de.schlund.pfixxml.config.GlobalConfig;

/**
 * @author schuppi
 * @date Jun 24, 2005
 */
public class PfixReadjustment {
    
    private static PfixReadjustment _instance = new PfixReadjustment();
    
    private static Logger LOG = Logger.getLogger(PfixReadjustment.class);
    
    public static final String LUCENE_DATA = PfixQueueManager.lucene_data_path;
    
    /**
     * @param idletime
     * @throws XMLException
     */
    private PfixReadjustment() {
    }
    
    /**
     * Checks list of include parts for changes and updates search index.
     */
    public void readjust() {
        Collection partsKnownByPustefix = getUsedTripels();
        IndexReader reader = null;
        PfixQueueManager queue;
        boolean jobDone;
        long startLoop, stopLoop, startCollect, stopCollect, startIndexLoop, stopIndexLoop, startAddLoop, stopAddLoop;
        
        long collectTime = 0;
        
        int knownDocsSize, newDocs, deleteDocs, numDocs;
        
        startLoop = stopLoop = startCollect = stopCollect = startIndexLoop = stopIndexLoop = startAddLoop = stopAddLoop = 0;
        newDocs = knownDocsSize = deleteDocs = numDocs = 0;
        
        startLoop = System.currentTimeMillis();
        Set<Tripel> tripelsToIndex = new TreeSet<Tripel>();
        
        queue = PfixQueueManager.getInstance(null);
        try {
            jobDone = false;
            startCollect = System.currentTimeMillis();
            partsKnownByPustefix = getUsedTripels();
            stopCollect = System.currentTimeMillis();
            collectTime = stopCollect - startCollect;
            knownDocsSize = partsKnownByPustefix.size();
            
            try {
                reader = IndexReader.open(LUCENE_DATA);
            } catch (IOException ioe) {
                LOG.warn("broken or nonexistant database -> will queue ALL known parts");
                
                for (Iterator iter = partsKnownByPustefix.iterator(); iter.hasNext();) {
                    Tripel element = (Tripel) iter.next();
                    element.setType(Tripel.Type.INSERT);
                    newDocs++;
                    if (!tripelsToIndex.add(element)) {
                        LOG.debug("duplicated insert");
                    }
                }
                jobDone = true;
            }
            if (!jobDone) {
                numDocs = reader.numDocs();
                startIndexLoop = System.currentTimeMillis();
                docloop: for (int i = 0; i < numDocs; i++) {
                    
                    Document currentdoc;
                    try {
                        currentdoc = reader.document(i);
                    } catch (RuntimeException e) {
                        // this happens if we want to access a deleted
                        // document -> continue
                        continue docloop;
                    }
                    
                    // check if needed
                    String path = currentdoc.get(PreDoc.PATH);
                    Tripel pfixTripel = new Tripel(path, null);
                    
                    if (partsKnownByPustefix.contains(pfixTripel)) {
                        
                        // checkTs
                        File f = new File(GlobalConfig.getDocroot(),currentdoc.get(PreDoc.FILENAME));
                        if (f.lastModified() != DateField.stringToTime(currentdoc.get(PreDoc.LASTTOUCH))) {
                            // ts differs
                            pfixTripel.setType(Tripel.Type.INSERT);
                            LOG.debug("TS differs: " + pfixTripel);
                            newDocs++;
                            if (!tripelsToIndex.add(pfixTripel)) {
                                LOG.debug("duplicated insert " + pfixTripel);
                            }
                        }
                        partsKnownByPustefix.remove(pfixTripel);
                    } else {
                        // part not needed anymore
                        Tripel newTripel = new Tripel(currentdoc.get(PreDoc.PATH), Tripel.Type.DELETE);
                        deleteDocs++;
                        queue.queue(newTripel);
                    }
                    
                }
                stopIndexLoop = System.currentTimeMillis();
                
                // now partsKnownByPustefix only contains parts which are NOT indexed...
                startAddLoop = System.currentTimeMillis();
                for (Iterator iter = partsKnownByPustefix.iterator(); iter.hasNext();) {
                    Tripel element = (Tripel) iter.next();
                    element.setType(Tripel.Type.INSERT);
                    // LOG.debug("adding " + element + " to queue
                    // (INDEX)");
                    newDocs++;
                    if (!tripelsToIndex.add(element)) {
                        LOG.debug("duplicated insert " + element);
                    }
                    // queue.queue(element);
                }
                
                stopAddLoop = System.currentTimeMillis();
            }
        } catch (IOException ioe) {
            LOG.fatal("error reading index", ioe);
        }
        
        // its a treeset, it is already sorted :)
        // Collections.sort(tripelsToIndex);
        // Collections.
        for (Tripel tripel : tripelsToIndex) {
            queue.queue(tripel);
        }
        
        stopLoop = System.currentTimeMillis();
        long needed = stopLoop - startLoop;
        if (newDocs != 0 || deleteDocs != 0) {
            LOG.debug(needed + "ms (getUsedTripels(): " + collectTime + "ms (" + knownDocsSize + "u) indexloop: "
                      + (stopIndexLoop - startIndexLoop) + "|" + (stopAddLoop - startAddLoop) + "ms (" + numDocs
                      + "u), added " + newDocs + "+" + deleteDocs + " queueitems");
        }
        
        try {
            if (reader != null) {
                reader.close();
                reader = null;
            }
        } catch (IOException e) {
            LOG.error("error while closing reader", e);
        }
    }
    
    private Set<Tripel> getUsedTripels() {
        Set<Tripel> retval = new TreeSet<Tripel>();
        
        ProjectFactoryService projectFactory = SpringBeanLocator.getProjectFactoryService();
        for (Iterator i = projectFactory.getProjects().iterator(); i.hasNext();) {
            Project currentproject = (Project) i.next();
            for (Iterator i2 = currentproject.getAllIncludeParts().iterator(); i2.hasNext();) {
                IncludePartThemeVariant element = (IncludePartThemeVariant) i2.next();
                String filename = element.getIncludePart().getIncludeFile().getPath();
                String incname = element.getIncludePart().getName();
                String prodname = element.getTheme().getName();
                retval.add(new Tripel(prodname, incname, filename));
            }
        }
        
        return retval;
    }
    
    /**
     * Returns singleton which must have been initialized ealier.
     * 
     * @return Instance of PfixReadjustment
     * @throws RuntimeException
     *             if singleton has not been initialized yet
     */
    public static synchronized PfixReadjustment getInstance() {
        if (_instance == null) {
            throw new RuntimeException("PfixReadjustment has to be initialized first!");
        }
        return _instance;
    }
    
}
