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

    private HashMap targetGenStatistic;
    private HashMap dependXMLToProductnameMapping;
    private static SPCacheStatistic theInstance = new SPCacheStatistic();
    private static int REGISTER_MISS = 0;
    private static int REGISTER_HIT = 1;
    private static DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
    private static String PROP_PRODUCTDATA = "editorproductfactory.productdata";
    private long allHits = 0;
    private long allMisses = 0;
    private String productsconf;
    private DecimalFormat hitrateFormat = new DecimalFormat("##0.00");
    

    private SPCacheStatistic() {
        targetGenStatistic = new HashMap();
        dependXMLToProductnameMapping = new HashMap();
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
     public Document getCacheStatisticAsXML() throws ParserConfigurationException {
        HashMap targetgentoinfomap_clone = targetgentoinfomap_clone = cloneTargetGeneratorToCacheHitMissInfoMapping();
         
        Document doc = dbfac.newDocumentBuilder().newDocument();
        Element top = doc.createElement("spcachestatistic");

        SPCache currentcache = SPCacheFactory.getInstance().getCache();
        
        Element ele_currentcache = doc.createElement("currentcache");
        setCacheAttributes(currentcache, ele_currentcache);
        top.appendChild(ele_currentcache);
        
        TargetsInSPCache targetsincache = getTargetsForTargetGeneratorFromSPCache();

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
        sb.append("Cache class="+currentcache.getClass().getName()+" capacity="+currentcache.getCapacity()+
                  " size="+currentcache.getSize()+" hits="+allHits+" misses="+allMisses+
                  " hitrate="+formatHitrate((double)allHits, (double)allMisses)+"|");
        
        HashMap targetgentoinfomap_clone = cloneTargetGeneratorToCacheHitMissInfoMapping();
       
        for(Iterator i = targetgentoinfomap_clone.keySet().iterator(); i.hasNext(); ) {
            TargetGenerator tgen = (TargetGenerator) i.next();
            CacheHitMissPair hitmiss = (CacheHitMissPair) targetgentoinfomap_clone.get(tgen);
            sb.append("|product name="+getProductnameForTargetGenerator(tgen)+
                        " hitrate="+formatHitrate((double) hitmiss.hits, (double) hitmiss.misses)+
                        " hits="+hitmiss.hits+" misses="+hitmiss.misses);
        }
        
        return sb.toString();
    }
   
     
     
    private HashMap cloneTargetGeneratorToCacheHitMissInfoMapping() {
        HashMap targetgentoinfomap_clone;
        synchronized(this) {
            targetgentoinfomap_clone = (HashMap) targetGenStatistic.clone();
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
            ele_tg.setAttribute("hitrate", formatHitrate((double) hitmiss.hits, (double) hitmiss.misses));
            ele_tg.setAttribute("hits", ""+hitmiss.hits);
            ele_tg.setAttribute("misses", ""+hitmiss.misses);
        
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
        double rate = 0;
        if(hits != 0 && misses != 0) {
            rate = (hits / (misses + hits)) * 100;
            if(rate > 100) {
                rate = 100;
            }
        }
                
        return hitrateFormat.format(rate)+"%";
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
        ele_currentcache.setAttribute("hitrate", formatHitrate((double)allHits, (double)allMisses));
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
        allHits = 0;
        allMisses = 0;
        HashMap targetgentoinfomap_clone = cloneTargetGeneratorToCacheHitMissInfoMapping();
        for(Iterator i = targetgentoinfomap_clone.keySet().iterator(); i.hasNext(); ) {
            CacheHitMissPair misshit = (CacheHitMissPair) targetgentoinfomap_clone.get(i.next());
            misshit.misses = 0;
            misshit.hits = 0;
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
