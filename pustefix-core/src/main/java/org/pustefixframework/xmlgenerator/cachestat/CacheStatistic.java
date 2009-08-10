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
 */

package org.pustefixframework.xmlgenerator.cachestat;

import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Timer;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.pustefixframework.xmlgenerator.targets.SPCache;
import org.pustefixframework.xmlgenerator.targets.SharedLeaf;
import org.pustefixframework.xmlgenerator.targets.Target;
import org.pustefixframework.xmlgenerator.targets.TargetGenerator;
import org.springframework.beans.factory.InitializingBean;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.schlund.pfixxml.XMLException;
import de.schlund.pfixxml.util.Xml;

/**
 * Class managing information on the hits
 * and misses in the SPCache. Currently it 
 * is used by TargetImpl to register
 * cache hits and misses.
 * @author Joerg Haecker <haecker@schlund.de>
 *  
 */
public class CacheStatistic implements CacheStatisticMBean, InitializingBean {
    
    private final static Logger LOG = Logger.getLogger(CacheStatistic.class);
    private int queueSize = 0;
    private int queueTicks = 0;

    /** Format for hitrate */
    private DecimalFormat hitrateFormat = new DecimalFormat("##0.00");
    /** Timer used for AdvanceCacheStatistic */
    private Timer tickTimer = new Timer(true);
    private String projectName;
    
    private TargetGenerator targetGenerator;
    private AdvanceCacheStatistic cacheStat;
    
    public void afterPropertiesSet() throws Exception {
    	cacheStat = new AdvanceCacheStatistic(tickTimer, queueSize, queueTicks);
        try {
            MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer(); 
            ObjectName objectName = new ObjectName("Pustefix:type=CacheStatistic,project="+projectName);
            if(mbeanServer.isRegistered(objectName)) mbeanServer.unregisterMBean(objectName);
            mbeanServer.registerMBean(this, objectName);
        } catch(Exception x) {
            LOG.error("Can't register SPCacheStatistic MBean!",x);
        }
    }
    
    public void setTargetGenerator(TargetGenerator targetGenerator) {
    	this.targetGenerator = targetGenerator;
    }
    
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setQueueSize(int queueSize) {
    	this.queueSize = queueSize;
    }
    
    public void setQueueTicks(int queueTicks) {
    	this.queueTicks = queueTicks;
    }
   
    /**
     * This is called from outside (TargetImpl) to
     * register a cache miss.
     */
    public void registerCacheMiss(Target target) {
    	cacheStat.registerMiss();
    }

    /**
     * This is called from outside (TargetImpl) to
     * register a cache hit.
     */
    public void registerCacheHit(Target target) {
    	cacheStat.registerHit();
    }

    /**
	 * Create cache-statistic in XML-format.
	 */
    @SuppressWarnings("unchecked")
    public Document getAsXML() {

        Document doc = Xml.createDocument();
        Element top = doc.createElement("spcachestatistic");

        SPCache<Object, Object> currentcache = targetGenerator.getTargetCache();
        
        Element ele_currentcache = doc.createElement("currentcache");
        setCacheAttributesXML(currentcache, ele_currentcache);
        top.appendChild(ele_currentcache);

        Element ele_hitmiss = doc.createElement("products");
        attachTargetGenerator2XML(doc, ele_hitmiss);
        attachShared2XML(doc, ele_hitmiss);
        top.appendChild(ele_hitmiss);

        doc.appendChild(top);

        return doc;
    }
    
    /**
     * Create cache-statistic in special format.
     */
    @SuppressWarnings("unchecked")
    public String getAsString() {
        StringBuffer sb = new StringBuffer(128);
        long totalmisses = cacheStat.getMisses();
        long totalhits = cacheStat.getHits();
        String hitrate = formatHitrate(totalhits, totalmisses);
        sb.append("|" + targetGenerator.getName() + ":" + totalhits + "," + totalmisses + "," + hitrate);
        sb.insert(0, "TOTAL:" + totalhits + "," + totalmisses + "," + hitrate);
        return sb.toString();
    }

    /**
     *  Attach the cachestatistic for all known targetgenerators.
     **/
    private void attachTargetGenerator2XML(Document doc, Element ele_hitmiss) {

            Element ele_tg = doc.createElement("product");

            ele_tg.setAttribute("name", targetGenerator.getName());
            long hits = cacheStat.getHits();
            long misses = cacheStat.getMisses();
            String hitrate = formatHitrate(hits, misses) + "%";
            ele_tg.setAttribute("hitrate", hitrate);
            ele_tg.setAttribute("hits", "" + hits);
            ele_tg.setAttribute("misses", "" + misses);

            attachTargets2XML(doc, ele_tg);
            ele_hitmiss.appendChild(ele_tg);
    }

    /**
     *  Attach all targets belonging to a given TargetGenerator.
     **/
    private void attachTargets2XML(Document doc, Element ele_tg) {
    	SPCache<Object,Object> targetCache = targetGenerator.getTargetCache();
    	Iterator<Object> it = targetCache.getIterator();
    	while(it.hasNext()) {
    		Object key = it.next();
    		Object value = targetCache.getValue(key);
    		if(value instanceof Target) {
                Element entry_ele = doc.createElement("target");
                Target t = (Target)value;
                entry_ele.setAttribute("id", t.getTargetKey());
                ele_tg.appendChild(entry_ele);
            }
    	}   
    }

    /**
     *  Attach the SharedLeafs-targets to the cachestatistic
     */
    private void attachShared2XML(Document doc, Element ele_hitmiss) {
    	SPCache<Object,Object> targetCache = targetGenerator.getTargetCache();
    	Iterator<Object> it = targetCache.getIterator();
    	Element ele_shared = doc.createElement("shared");
    	while(it.hasNext()) {
    		Object key = it.next();
    		Object value = targetCache.getValue(key);
    		if(value instanceof SharedLeaf) {
    			Element entry_ele = doc.createElement("sharedtarget");
                SharedLeaf sleaf = (SharedLeaf)value;
                entry_ele.setAttribute("id", sleaf.getPath().toString());
                ele_shared.appendChild(entry_ele);
            }
            ele_hitmiss.appendChild(ele_shared);
        }
    }

    /**
     * Attach general information about the cache and create the total rates
     * by iterating over all known targetGenerators in the targetGen2AdvanceStatMapping-map.
     */
    private void setCacheAttributesXML(SPCache<Object, Object> currentcache, Element ele_currentcache) {
        ele_currentcache.setAttribute("class", currentcache.getClass().getName());
        ele_currentcache.setAttribute("capacity", "" + currentcache.getCapacity());
        ele_currentcache.setAttribute("size", "" + currentcache.getSize());

        long totalhits = cacheStat.getHits();
        long totalmisses = cacheStat.getMisses();
      
        ele_currentcache.setAttribute("hits", "" + totalhits);
        ele_currentcache.setAttribute("misses", "" + totalmisses);
        String hitrate = formatHitrate(totalhits, totalmisses) + "%";
        ele_currentcache.setAttribute("hitrate", hitrate);
    }

    private String formatHitrate(double hits, double misses) {
        double rate = calcHitrate(hits, misses);
        return hitrateFormat.format(rate);
    }

    private double calcHitrate(double hits, double misses) {
        double rate = 0;
        if (hits != 0 && (hits + misses != 0)) {
            rate = (hits / (misses + hits)) * 100;
            if (rate > 100) {
                rate = 100;
            }
        }
        return rate;
    }
}
