/*
 * Created on 13.10.2003
 *
 */
package de.schlund.pfixxml.targets;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import de.schlund.pfixxml.XMLException;
import de.schlund.util.FactoryInit;

/**
 * @author Joerg Haecker <haecker@schlund.de>
 *
 */
public class SPCacheStatistic implements FactoryInit {

    private TargetGeneratorsStatistic targetGenStatistic;
    private HashMap dependXMLToProductnameMapping;
    private CacheHitMissPair hitsAndMissesForWholeCache = new CacheHitMissPair();
    private String productsconf;
    private DecimalFormat hitrateFormat = new DecimalFormat("##0.00");
    
    private static SPCacheStatistic theInstance = new SPCacheStatistic();
    private static int REGISTER_MISS = 0;
    private static int REGISTER_HIT = 1;
    private static DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
    private static String PROP_PRODUCTDATA = "editorproductfactory.productdata";
   
    

    private SPCacheStatistic() {
        targetGenStatistic = new TargetGeneratorsStatistic();
        dependXMLToProductnameMapping = new HashMap();
    }

    public static SPCacheStatistic getInstance() {
        return theInstance;    
    }


    synchronized void registerCacheMiss(Target target) {
        hitsAndMissesForWholeCache.increaseMisses();
        register(target, REGISTER_MISS);
    }

    synchronized void registerCacheHit(Target target) {
        hitsAndMissesForWholeCache.increaseHits();
        register(target, REGISTER_HIT);
    }

    /**
     * Create cache-statistic in XML-format.
     */
     public Document getCacheStatisticAsXML() throws ParserConfigurationException {
        HashMap targetgentoinfomap_clone = targetgentoinfomap_clone = cloneTargetGeneratorToCacheHitMissInfoMapping();
         
        Document doc = dbfac.newDocumentBuilder().newDocument();
        Element top = doc.createElement("spcachestatistic");

        SPCache currentcache = SPCacheFactory.getInstance().getCache();
        
        Element ele_currentcache = doc.createElement("currentcache");
        setCacheAttributes(currentcache, ele_currentcache);
        top.appendChild(ele_currentcache);
        
        TargetsInSPCache targetsincache = new TargetsInSPCache();
        targetsincache.inspectCache();

        Element ele_hitmiss = doc.createElement("products");
        attachTargetGenerators(doc, targetsincache, ele_hitmiss, targetgentoinfomap_clone);
        attachShared(doc, targetsincache, ele_hitmiss);
        top.appendChild(ele_hitmiss);

        doc.appendChild(top);
        
        return doc;     
    }
   
     public String getCacheStatisticAsString() {
        StringBuffer sb= new StringBuffer(128);
        SPCache currentcache = SPCacheFactory.getInstance().getCache();
        sb.append("TOTAL:"+hitsAndMissesForWholeCache.getHits()+","+
                    hitsAndMissesForWholeCache.getMisses()+","+
                    formatHitrate((double)hitsAndMissesForWholeCache.getHits(), (double)hitsAndMissesForWholeCache.getMisses()));
        
        HashMap targetgentoinfomap_clone = cloneTargetGeneratorToCacheHitMissInfoMapping();
       
        for(Iterator i = targetgentoinfomap_clone.keySet().iterator(); i.hasNext(); ) {
            TargetGenerator tgen = (TargetGenerator) i.next();
            CacheHitMissPair hitmiss = (CacheHitMissPair) targetgentoinfomap_clone.get(tgen);
            sb.append("|"+getProductnameForTargetGenerator(tgen)+":"+hitmiss.getHits()+","+
                        hitmiss.getMisses()+","+formatHitrate((double) hitmiss.getHits(), (double)hitmiss.getMisses()));
        }
        
        return sb.toString();
    }
   
     
     
    private HashMap cloneTargetGeneratorToCacheHitMissInfoMapping() {
        HashMap targetgentoinfomap_clone;
        synchronized(this) {
            targetgentoinfomap_clone = (HashMap) targetGenStatistic.createMapClone();
        }
        return targetgentoinfomap_clone;
    }

   
    /* Attach all TargetGenerators */
    private void attachTargetGenerators(Document doc, TargetsInSPCache targetsincache, Element ele_hitmiss, HashMap targetgentoinfomap) {
        
        for(Iterator i = targetgentoinfomap.keySet().iterator(); i.hasNext(); ) {
            TargetGenerator tgen = (TargetGenerator) i.next();
            Element ele_tg = doc.createElement("product");
            
            String dependxml;
            String configattr = getProductnameForTargetGenerator(tgen);
            
            ele_tg.setAttribute("name", configattr);    
            CacheHitMissPair hitmiss = (CacheHitMissPair) targetgentoinfomap.get(tgen);
            ele_tg.setAttribute("hitrate", formatHitrate((double) hitmiss.getHits(), (double) hitmiss.getMisses())+"%");
            ele_tg.setAttribute("hits", ""+hitmiss.getHits());
            ele_tg.setAttribute("misses", ""+hitmiss.getMisses());
        
            attachTargets(doc, targetsincache, tgen, ele_tg);
            ele_hitmiss.appendChild(ele_tg);
        }
    }
    
    private String getProductnameForTargetGenerator(TargetGenerator tgen) {
        String dependxml = tgen.getConfigname();
        String configattr;
        if(dependXMLToProductnameMapping.containsKey(dependxml)) {
            configattr = (String) dependXMLToProductnameMapping.get(dependxml);
        } else {
            String configname = tgen.getConfigname();
            configattr = configname.substring(tgen.getConfigname().indexOf(tgen.getDocroot()), configname.length());
        }
        return configattr;
    }

    private String formatHitrate(double hits, double misses) {
        double rate = calcHitrate(hits, misses);
                
        return hitrateFormat.format(rate);
    }
    
    private double calcHitrate(double hits, double misses) {
        double rate = 0;
        if(hits != 0 && (hits + misses != 0)) {
            rate = (hits / (misses + hits)) * 100;
            if(rate > 100) {
                rate = 100;
            }
        }
        return rate;
    }

    /* Attach all SharedLeafs */
    private void attachShared(Document doc, TargetsInSPCache targetsincache, Element ele_hitmiss) {
        if(! targetsincache.getSharedTargets().isEmpty()) {
            Element ele_shared = doc.createElement("shared");
            for(Iterator i = targetsincache.getSharedTargets().iterator(); i.hasNext(); ) {
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
        if(targetsincache.containsTargetGenerator(tgen)) {
            List targets = (List) targetsincache.getTargetsForTargetGenerator(tgen);
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
        ele_currentcache.setAttribute("hits", ""+hitsAndMissesForWholeCache.getHits());
        ele_currentcache.setAttribute("misses", ""+hitsAndMissesForWholeCache.getMisses());
        ele_currentcache.setAttribute("hitrate", formatHitrate((double)hitsAndMissesForWholeCache.getHits(), 
                                                                (double)hitsAndMissesForWholeCache.getMisses())+"%");
    }

    
    

    /* Register a cache-hit or cache-miss */
    private void register(Target target, int mode) {
        TargetGenerator tgen = target.getTargetGenerator();
        if(targetGenStatistic.containsHitMissPairForTargetGenerator(tgen)) {
            CacheHitMissPair hitmiss = targetGenStatistic.getHitMissPairForTargetGenerator(tgen);
            if(mode == REGISTER_HIT) {
                hitmiss.increaseHits();
            } else {
                hitmiss.increaseMisses();
            }
        } else {
            CacheHitMissPair hitmiss = new CacheHitMissPair();
            if(mode == REGISTER_HIT) {
                hitmiss.increaseHits();
            } else {
                hitmiss.increaseMisses();
            }
            targetGenStatistic.setHitMissPairForTargetGenerator(hitmiss, tgen);
        }
    }

    /**
     * @see de.schlund.util.FactoryInit#init(java.util.Properties)
     */
    public void init(Properties props) throws Exception {
        String productdatafile = props.getProperty(PROP_PRODUCTDATA);
        if(productdatafile == null || productdatafile.equals("")) {
            throw new XMLException("Need property '"+PROP_PRODUCTDATA+"' for retrieving product data.");
        }
        DocumentBuilder docbuilder = dbfac.newDocumentBuilder();
        Document doc = docbuilder.parse(new File(productdatafile));
        
        NodeList nl = XPathAPI.selectNodeList(doc, "/projects/project");
        for(int i=0; i<nl.getLength(); i++) {
            Node project_node = nl.item(i);
            String prjname = ((Element) project_node).getAttribute("name");
            Node dependxml_node = XPathAPI.selectSingleNode(project_node, "./depend");
            String dependname = ((Text)((Element) dependxml_node).getFirstChild()).getData();
            dependXMLToProductnameMapping.put(dependname, prjname);
        }
    }
    
    public void reset() {
        hitsAndMissesForWholeCache.resetHits();
        hitsAndMissesForWholeCache.resetMisses();
        HashMap targetgentoinfomap_clone = cloneTargetGeneratorToCacheHitMissInfoMapping();
        for(Iterator i = targetgentoinfomap_clone.keySet().iterator(); i.hasNext(); ) {
            CacheHitMissPair misshit = (CacheHitMissPair) targetgentoinfomap_clone.get(i.next());
            misshit.resetMisses();
            misshit.resetHits();
        }
    }
   
}

final class CacheHitMissPair {
    private long hits = 0;
    private long misses = 0;
    
    void increaseHits() {
        hits++;
    }
    
    void increaseMisses() {
        misses++;
    }
    
    long getHits() {
        return hits;
    }
    
    long getMisses() {
        return misses;
    }
    
    void resetHits() {
        hits = 0;
    }
    
    void resetMisses() {
        misses = 0;
    }
}


final class TargetGeneratorsStatistic {
    private HashMap targetGenStatistic = new HashMap();
    
    void setHitMissPairForTargetGenerator(CacheHitMissPair hitmiss, TargetGenerator tgen) {
        targetGenStatistic.put(tgen, hitmiss);
    }
    
    boolean containsHitMissPairForTargetGenerator(TargetGenerator tgen) {
        return targetGenStatistic.containsKey(tgen);
    }
    
    CacheHitMissPair getHitMissPairForTargetGenerator(TargetGenerator tgen) {
        return (CacheHitMissPair) targetGenStatistic.get(tgen);
    }

    HashMap createMapClone() {
        return (HashMap) targetGenStatistic.clone();
    }
}





final class TargetsInSPCache {
    /* Maps tgen as key to List with targets as value */
    private HashMap targettgenMapping = new HashMap();
    /* Includes all SharedLeafs in SPCache */
    private ArrayList sharedTargets = new ArrayList();
    
    
    
    void inspectCache() {
        getTargetsForTargetGeneratorFromSPCache(); 
    }
    
    List getSharedTargets() {
       return sharedTargets;
    }
    
    List getTargetsForTargetGenerator(TargetGenerator tgen) {
        return (List) targettgenMapping.get(tgen);
    }
    
    boolean containsTargetGenerator(TargetGenerator tgen) {
        return targettgenMapping.containsKey(tgen);
    }
    
    private void addSharedTarget(SharedLeaf leaf) {
        sharedTargets.add(leaf);
    }
    
    private void setTargetsForTargetGenerator(TargetGenerator tgen, List targets) {
        targettgenMapping.put(tgen, targets);
    }
    
    
    /* Inspect SPCache and collect all Targets belonging to a TargetGenerator
     * and all Shared Leafs */
    private void getTargetsForTargetGeneratorFromSPCache() {
        SPCache cache = SPCacheFactory.getInstance().getCache();
       
        for(Iterator i = cache.getIterator(); i.hasNext();) {
            Object obj = i.next();
            if(obj instanceof Target) { 
                Target target = (Target) obj;
                TargetGenerator tgen = target.getTargetGenerator();
                if(containsTargetGenerator(tgen)) {
                    List list = (List)getTargetsForTargetGenerator(tgen);
                    list.add(target);
                } else {
                    ArrayList list = new ArrayList();
                    list.add(target);
                    setTargetsForTargetGenerator(tgen, list);    
                }        
            } else if(obj instanceof SharedLeaf) {
                addSharedTarget((SharedLeaf)obj);                   
            }
        }
    }
}
