/*
 * Created on 13.10.2003
 *
 */
package de.schlund.pfixxml.targets;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Joerg Haecker <haecker@schlund.de>
 *
 */
public class SPCacheStatistic {

    private HashMap targetGenStatistic;
    private static SPCacheStatistic theInstance = new SPCacheStatistic();
    private static int REGISTER_MISS = 0;
    private static int REGISTER_HIT = 1;
    private static DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
    private long allHits = 0;
    private long allMisses = 0;

    private SPCacheStatistic() {
        targetGenStatistic = new HashMap();
    }

    public static SPCacheStatistic getInstance() {
        return theInstance;    
    }


    synchronized void registerCacheMiss(Target target) {
        allMisses++;
        register(target, REGISTER_MISS);
    }

    synchronized void registerCacheHit(Target target) {
        allHits++;
        register(target, REGISTER_HIT);
    }

    /**
     * Create cache-statistic in XML-format.
     */
    synchronized public Document getXMLCacheStatistic() throws ParserConfigurationException {
        Document doc = dbfac.newDocumentBuilder().newDocument();
        Element top = doc.createElement("spcachestatistic");

        SPCache currentcache = SPCacheFactory.getInstance().getCache();
        
        Element ele_currentcache = doc.createElement("currentcache");
        setCacheAttributes(currentcache, ele_currentcache);
        top.appendChild(ele_currentcache);
        
        TargetsInSPCache targetsincache = getTargetsForTargetGeneratorFromSPCache();

        Element ele_hitmiss = doc.createElement("targetgenerators");
        attachTargetGenerators(doc, targetsincache, ele_hitmiss);
        attachShared(doc, targetsincache, ele_hitmiss);
        top.appendChild(ele_hitmiss);

        doc.appendChild(top);
        
        return doc;     
    }

    /* Attach all TargetGenerators */
    private void attachTargetGenerators(Document doc, TargetsInSPCache targetsincache, Element ele_hitmiss) {
        DecimalFormat df = new DecimalFormat("##0.00");
        for(Iterator i = targetGenStatistic.keySet().iterator(); i.hasNext(); ) {
            TargetGenerator tgen = (TargetGenerator) i.next();
            Element ele_tg = doc.createElement("targetgenerator");
            
            ele_tg.setAttribute("config", tgen.getConfigname());    
            CacheHitMissPair hitmiss = (CacheHitMissPair) targetGenStatistic.get(tgen);
            double rate = ((double)hitmiss.hits / (hitmiss.hits + hitmiss.misses)) * 100;
            ele_tg.setAttribute("hitrate", df.format(rate)+"%");
            ele_tg.setAttribute("hits", ""+hitmiss.hits);
            ele_tg.setAttribute("misses", ""+hitmiss.misses);
        
            attachTargets(doc, targetsincache, tgen, ele_tg);
            ele_hitmiss.appendChild(ele_tg);
        }
    }

    /* Attach all SharedLeafs */
    private void attachShared(Document doc, TargetsInSPCache targetsincache, Element ele_hitmiss) {
        if(! targetsincache.sharedTargets.isEmpty()) {
            Element ele_shared = doc.createElement("shared");
            for(Iterator i = targetsincache.sharedTargets.iterator(); i.hasNext(); ) {
                Element entry_ele = doc.createElement("sharedtarget");
                SharedLeaf sleaf = (SharedLeaf) i.next();
                entry_ele.setAttribute("id", sleaf.getPath());
                ele_shared.appendChild(entry_ele);
            }
            ele_hitmiss.appendChild(ele_shared);
        }
    }

    /* Attach targets belonging to a TargetGenerator */
    private void attachTargets(Document doc, TargetsInSPCache targetsincache, TargetGenerator tgen, Element ele_tg) {
        if(targetsincache.targettgenMapping.containsKey(tgen)) {
            List targets = (List) targetsincache.targettgenMapping.get(tgen);
            for(Iterator j = targets.iterator(); j.hasNext(); ) {
                Element entry_ele = doc.createElement("target");
                Target t = (Target) j.next();
                entry_ele.setAttribute("id", t.getTargetKey());
                ele_tg.appendChild(entry_ele);
            }
        }
    }

    private void setCacheAttributes(SPCache currentcache, Element ele_currentcache) {
        ele_currentcache.setAttribute("class", currentcache.getClass().getName());
        ele_currentcache.setAttribute("capacity", ""+currentcache.getCapacity());
        ele_currentcache.setAttribute("size", ""+currentcache.getSize());
        ele_currentcache.setAttribute("hits", ""+allHits);
        ele_currentcache.setAttribute("misses", ""+allMisses);
    }

    
    /* Inspect SPCache and collect all Targets belonging to a TargetGenerator
     * and all Shared Leafs */
    private TargetsInSPCache getTargetsForTargetGeneratorFromSPCache() {
        SPCache cache = SPCacheFactory.getInstance().getCache();
        TargetsInSPCache tincache = new TargetsInSPCache();
        for(Iterator i = cache.getIterator(); i.hasNext();) {
            Object obj = i.next();
            if(obj instanceof Target) { 
                Target target = (Target) obj;
                TargetGenerator tgen = target.getTargetGenerator();
                if(tincache.targettgenMapping.containsKey(tgen)) {
                    List list = (List)tincache.targettgenMapping.get(tgen);
                    list.add(target);
                } else {
                    ArrayList list = new ArrayList();
                    list.add(target);
                    tincache.targettgenMapping.put(tgen, list);    
                }        
            } else if(obj instanceof SharedLeaf) {
                tincache.sharedTargets.add((SharedLeaf)obj);                   
            }
        }
        return tincache;
    }

    /* Register a cache-hit or cache-miss */
    private void register(Target target, int mode) {
        TargetGenerator tgen = target.getTargetGenerator();
        if(targetGenStatistic.containsKey(tgen)) {
            CacheHitMissPair hitmiss = (CacheHitMissPair) targetGenStatistic.get(tgen);
            if(mode == REGISTER_HIT) {
                hitmiss.hits++;
            } else {
                hitmiss.misses++;
            }
        } else {
            CacheHitMissPair hitmiss = new CacheHitMissPair();
            if(mode == REGISTER_HIT) {
                hitmiss.hits++;
            } else {
                hitmiss.misses++;
            }
            targetGenStatistic.put(tgen, hitmiss);
        }
    }
    
   
}

final class CacheHitMissPair {
    long hits = 0;
    long misses = 0;
}

final class TargetsInSPCache {
    /* Maps tgen as key to List with targets as value */
    HashMap targettgenMapping = new HashMap();
    /* Includes all SharedLeafs in SPCache */
    ArrayList sharedTargets = new ArrayList();
}
