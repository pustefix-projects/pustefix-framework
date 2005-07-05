package de.schlund.lucefix.core;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Category;
import org.apache.lucene.document.DateField;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;

import de.schlund.pfixcore.editor.EditorProduct;
import de.schlund.pfixcore.editor.EditorProductFactory;
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

    private static PfixReadjustment _instance     = null;
    private static Category         LOG           = Category.getInstance(PfixReadjustment.class);
    public static final String      WAITMS_PROP   = "lucefix.pfixreadjustidle";
    public static final String      LUCENE_DATA   = PfixQueueManager.lucene_data_path;

    // TODO: das muessen wir woanders herholen...
    private static final String     PROJECTS_PATH = "/home/schuppi/workspace/pfixschlund/projects/"; ;

    private int                     waitms        = -1;


    /**
     * @param p
     * @throws XMLException
     */
    public PfixReadjustment(Properties p) throws XMLException {
        String waitprop = p.getProperty(WAITMS_PROP);
        if (waitprop == null) throw new XMLException("property " + WAITMS_PROP + " needed but not set!");

        try {
            waitms = Integer.parseInt(waitprop);
        } catch (NumberFormatException e) {
            throw new XMLException(waitprop + " is not a valid integer");
        }
    }

    /*
     * @see java.lang.Runnable#run()
     */
    public void run() {
        Collection partsKnownByPustefix;
        IndexReader reader = null;
        PfixQueueManager queue;
        boolean jobDone;
        long startLoop, stopLoop;
        while (true) {
            try {
                Thread.sleep(waitms);
            } catch (InterruptedException e) {}
            startLoop = System.currentTimeMillis();
            try {
                jobDone = false;
                try {
                    partsKnownByPustefix = getUsedTripels();
                } catch (Exception e1) {
                    LOG.error("error while getting known tripels", e1);
                    e1.printStackTrace();
                    continue;
                }

                queue = PfixQueueManager.getInstance(null);
                try {
                    reader = IndexReader.open(LUCENE_DATA);
                } catch (IOException ioe) {
                    LOG.warn("broken or nonexistant database -> will queue ALL known parts");

                    for (Iterator iter = partsKnownByPustefix.iterator(); iter.hasNext();) {
                        Tripel element = (Tripel) iter.next();
                        element.setType(TripelImpl.INDEX);
                        queue.queue(element);
                    }
                    jobDone = true;
                }
                if (!jobDone) {
                    int numDocs = reader.numDocs();
                    docloop: for (int i = 0; i < numDocs; i++) {
                        Document currentdoc;
                        try {
                            currentdoc = reader.document(i);
                        } catch (RuntimeException e) {
                            // TODO Auto-generated catch block
                            // this happens if we want to access a deleted
                            // document -> continue
                            continue docloop;
                        }

                        // check if needed
                        String path = currentdoc.get(PreDoc.PATH);
                        Tripel pfixTripel = null;
                        loop: for (Iterator iter = partsKnownByPustefix.iterator(); iter.hasNext();) {
                            Tripel element = (Tripel) iter.next();
                            if (element.getPath().equals(path)) {
                                pfixTripel = element;
                                break loop;
                            }
                        }

                        if (pfixTripel != null) {
                            // checkTs
                            File f = new File(currentdoc.get(PreDoc.FILENAME));
                            if (f.lastModified() != DateField.stringToTime(currentdoc.get(PreDoc.LASTTOUCH))) {
                                // ts differs
                                pfixTripel.setType(TripelImpl.INDEX);
                                queue.queue(pfixTripel);
                            }
                        } else {
                            // part not needed anymore
                            Tripel newTripel = new TripelImpl(currentdoc.get(PreDoc.PATH), TripelImpl.DELETE);
                            queue.queue(newTripel);
                        }
                        partsKnownByPustefix.remove(pfixTripel);
                    }
                    // now partsKnownByPustefix only contains parts which are
                    // NOT indexed...
                    for (Iterator iter = partsKnownByPustefix.iterator(); iter.hasNext();) {
                        Tripel element = (Tripel) iter.next();
                        element.setType(TripelImpl.INDEX);
                        queue.queue(element);
                    }
                }
            } catch (XMLException e) {
                LOG.error("xmlexception in " + this.getClass().getName(), e);
            } catch (IOException ioe) {
                LOG.fatal("error reading index", ioe);
            }
            stopLoop = System.currentTimeMillis();
            LOG.debug("one loop done, needed " + (stopLoop - startLoop) + "ms");
        }
    }


    private Set getUsedTripels() throws Exception {
        // TODO an pustefix ranploeppen -> jtl
        Set retval = new HashSet();
//        Set retval = new TreeSet(new TripelImpl.TripelComparator());
        EditorProductFactory factory = EditorProductFactory.getInstance();
        EditorProduct[] allProducts = factory.getAllEditorProducts();
        for (int i = 0; i < allProducts.length; i++) {
            EditorProduct currentproduct = allProducts[i];
            Collection targets = currentproduct.getTargetGenerator().getAllTargets().values();
            for (Iterator iter = targets.iterator(); iter.hasNext();) {
                Target currenttarget = (Target) iter.next();
//                LOG.debug("READAUX: " + currenttarget);
                if (currenttarget instanceof VirtualTarget) {
                    VirtualTarget vtarget = (VirtualTarget) currenttarget;
                    Collection depc = vtarget.getAuxDependencyManager().getChildren();
                    for (Iterator iterator = depc.iterator(); iterator.hasNext();) {
                        AuxDependency currentdependency = (AuxDependency) iterator.next();
//                        LOG.debug("READAUXdep: " + currentdependency);
                        if (currentdependency.getType() == DependencyType.TEXT) {
                            retval.add(new TripelImpl(currentdependency.getProduct(), currentdependency.getPart(),
                                    currentdependency.getPath().getRelative()));
                            // LOG.debug("new auxdep: " +
                            // currentdependency.getPath().getRelative() + "|" +
                            // currentdependency.getPart() + "|" +
                            // currentdependency.getProduct());
                            // LOG.debug("found auxdep: " +
                            // currentdependency.getPath().getRelative() + " | "
                            // + currentdependency.getPart() + " | " +
                            // currentdependency.getProduct());
                        }
                        recurse(retval, currentdependency, vtarget);
                    }
                    // }else if(currenttarget instanceof LeafTarget){
                    // LeafTarget ltarget = (LeafTarget) currenttarget;
                    // ltarget.g
                }

            }

        }
        //
        //
        // retval.add(new TripelImpl("default", "content", PROJECTS_PATH
        // + "euecommon/txt/pages/main_RootSSLDomainSelect.xml"));
        // retval.add(new TripelImpl("default", "box_title", PROJECTS_PATH
        // + "euecommon/txt/pages/main_RootSSLDomainSelect.xml"));
        // retval.add(new TripelImpl("default", "box_main", PROJECTS_PATH
        // + "euecommon/txt/pages/main_RootSSLDomainSelect.xml"));
        // retval.add(new TripelImpl("default", "box_help", PROJECTS_PATH
        // + "euecommon/txt/pages/main_RootSSLDomainSelect.xml"));
        return retval;
    }

    private void recurse(Set set, AuxDependency currentdependency, VirtualTarget vtarget) {
        TreeSet children = null;
        if (vtarget != null)
            children = currentdependency.getChildren(vtarget);
        else
            children = currentdependency.getChildrenForAllThemes();

        if (children != null)
            for (Iterator iter = children.iterator(); iter.hasNext();) {
                AuxDependency element = (AuxDependency) iter.next();
//                LOG.debug("READAUXelem: " + element);
                if (element.getType() == DependencyType.TEXT)
                    set.add(new TripelImpl(element.getProduct(), element.getPart(), element.getPath().getRelative()));
                recurse(set, element, vtarget);
            }
    }

    /**
     * @param p
     * @return
     * @throws XMLException
     */
    public static synchronized PfixReadjustment getInstance(Properties p) throws XMLException {
        if (_instance == null) _instance = new PfixReadjustment(p);
        return _instance;
    }

}
