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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.log4j.Category;
import org.apache.lucene.document.DateField;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;

import de.schlund.pfixcore.editor2.core.dom.IncludePart;
import de.schlund.pfixcore.editor2.core.dom.IncludePartThemeVariant;
import de.schlund.pfixcore.editor2.core.dom.Project;
import de.schlund.pfixcore.editor2.core.spring.ProjectFactoryService;
import de.schlund.pfixcore.editor2.frontend.util.SpringBeanLocator;
import de.schlund.pfixxml.PathFactory;
import de.schlund.pfixxml.XMLException;
import de.schlund.pfixxml.targets.AuxDependency;
import de.schlund.pfixxml.targets.DependencyType;
import de.schlund.pfixxml.targets.Target;
import de.schlund.pfixxml.targets.VirtualTarget;

/**
 * @author schuppi
 * @date Jun 24, 2005
 */
public class PfixReadjustment implements Runnable {

    private static PfixReadjustment _instance   = null;
    private static Category         LOG         = Category.getInstance(PfixReadjustment.class);
    public static final String      LUCENE_DATA = PfixQueueManager.lucene_data_path;

    private int                     waitms      = -1;


    /**
     * @param idletime
     * @throws XMLException
     */
    public PfixReadjustment(Integer idletime) throws XMLException {
        waitms = idletime;
    }

    /*
     * @see java.lang.Runnable#run()
     */
    public void run() {
        Collection partsKnownByPustefix;
        IndexReader reader = null;
        PfixQueueManager queue;
        boolean jobDone;
        long startLoop, stopLoop, startCollect, stopCollect, startIndexLoop, stopIndexLoop, startAddLoop, stopAddLoop;

        long collectTime = 0;

        int indexSize, knownDocsSize, newDocs, deleteDocs, numDocs;

        while (true) {
            try {
                Thread.sleep(waitms);
            } catch (InterruptedException e) {}
            
            synchronized (PfixQueueManager.getInstance(null).mutex) {
                // prevents PfixReadjustment to feed the queue while
                // PfixQueuemanager is still working

                startLoop = stopLoop = startCollect = stopCollect = startIndexLoop = stopIndexLoop = startAddLoop = stopAddLoop = 0;
                indexSize = newDocs = knownDocsSize = deleteDocs = numDocs = 0;

                startLoop = System.currentTimeMillis();
                List<Tripel> tripelsToIndex = new Vector<Tripel>();

                queue = PfixQueueManager.getInstance(null);
                try {
                    jobDone = false;
                    try {
                        startCollect = System.currentTimeMillis();
                        partsKnownByPustefix = getUsedTripels();
                        stopCollect = System.currentTimeMillis();
                        collectTime = stopCollect - startCollect;
                        knownDocsSize = partsKnownByPustefix.size();
                    } catch (Exception e1) {
                        LOG.error("error while getting known tripels", e1);
                        e1.printStackTrace();
                        continue;
                    }

                    try {
                        reader = IndexReader.open(LUCENE_DATA);
                    } catch (IOException ioe) {
                        LOG
                                .warn("broken or nonexistant database -> will queue ALL known parts");

                        for (Iterator iter = partsKnownByPustefix.iterator(); iter
                                .hasNext();) {
                            Tripel element = (Tripel) iter.next();
                            element.setType(Tripel.Type.INSERT);
                            newDocs++;
                            tripelsToIndex.add(element);
                            // queue.queue(element);
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
                            Tripel pfixTripel = null;
                            loop: for (Iterator iter = partsKnownByPustefix
                                    .iterator(); iter.hasNext();) {
                                Tripel element = (Tripel) iter.next();
                                if (element.getPath().equals(path)) {
                                    pfixTripel = element;
                                    break loop;
                                }
                            }

                            if (pfixTripel != null) {
                                // checkTs
                                File f = PathFactory.getInstance().createPath(
                                        currentdoc.get(PreDoc.FILENAME))
                                        .resolve();

                                if (f.lastModified() != DateField
                                        .stringToTime(currentdoc
                                                .get(PreDoc.LASTTOUCH))) {
                                    // ts differs
                                    pfixTripel.setType(Tripel.Type.INSERT);
                                    newDocs++;
                                    tripelsToIndex.add(pfixTripel);
                                    // queue.queue(pfixTripel);
                                }
                            } else {
                                // part not needed anymore
                                Tripel newTripel = new Tripel(currentdoc
                                        .get(PreDoc.PATH), Tripel.Type.DELETE);
                                deleteDocs++;
                                queue.queue(newTripel);
                            }
                            partsKnownByPustefix.remove(pfixTripel);
                        }
                        stopIndexLoop = System.currentTimeMillis();

                        // now partsKnownByPustefix only contains parts which
                        // are
                        // NOT indexed...
                        startAddLoop = System.currentTimeMillis();
                        for (Iterator iter = partsKnownByPustefix.iterator(); iter
                                .hasNext();) {
                            Tripel element = (Tripel) iter.next();
                            element.setType(Tripel.Type.INSERT);
                            // LOG.debug("adding " + element + " to queue
                            // (INDEX)");
                            newDocs++;
                            tripelsToIndex.add(element);
                            // queue.queue(element);
                        }

                        stopAddLoop = System.currentTimeMillis();
                    }
                } catch (IOException ioe) {
                    LOG.fatal("error reading index", ioe);
                }
                

                Collections.sort(tripelsToIndex);
                for (Tripel tripel : tripelsToIndex) {
                    queue.queue(tripel);
                }

                
                
                stopLoop = System.currentTimeMillis();
                long needed = stopLoop - startLoop;
                if (newDocs != 0 || deleteDocs != 0) {
                    LOG.debug(needed + "ms (getUsedTripels(): " + collectTime
                            + "ms (" + knownDocsSize + "u) indexloop: "
                            + (stopIndexLoop - startIndexLoop) + "|"
                            + (stopAddLoop - startAddLoop) + "ms (" + numDocs
                            + "u), added " + newDocs + "+" + deleteDocs
                            + " queueitems");
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
        }
    }


    private Set<Tripel> getUsedTripels() throws Exception {
        Set<Tripel> retval = new HashSet<Tripel>();

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
     * @param idletime
     * @return
     * @throws XMLException
     */
    public static synchronized PfixReadjustment getInstance(Integer idletime) throws XMLException {
        if (_instance == null) _instance = new PfixReadjustment(idletime);
        return _instance;
    }

}
