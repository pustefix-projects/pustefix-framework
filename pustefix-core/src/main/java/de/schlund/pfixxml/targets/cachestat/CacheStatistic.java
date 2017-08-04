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

package de.schlund.pfixxml.targets.cachestat;

import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.schlund.pfixxml.targets.SPCache;
import de.schlund.pfixxml.util.Xml;

/**
 * Class managing information on the hits
 * and misses in the SPCache. Currently it 
 * is used by TargetImpl to register
 * cache hits and misses.
 *
 * <p>
 * An instance registers itself as MBean using the Object Name:
 * Pustefix:type=CacheStatistic,project=PROJECT
 * </p>
 *
 * <p>
 * Upon registration of Caches via {@link #monitor(SPCache, String)}, it registers
 * per a per-Cache MBean using an Object-Name like:<br/>
 * <code>Pustefix:type=DetailedCacheStatistics,project=PROJECT,id=CACHE_ID</code>
 * </p> 
 *
 * @author Joerg Haecker <haecker@schlund.de>
 *
 * @see DetailedCacheStatisticsMBean
 *
 */
public class CacheStatistic implements CacheStatisticMBean, InitializingBean, DisposableBean {
    
    private final static Logger LOG = LoggerFactory.getLogger(CacheStatistic.class);
    private int queueSize = 0;
    private int queueTicks = 0;
    
    /** Format for hitrate */
    private DecimalFormat hitrateFormat = new DecimalFormat("##0.00");
    /** Timer used for AdvanceCacheStatistic */
    private Timer tickTimer;
    
    private String projectName;

    private List<SPCacheStatProxy<?,?>> cacheStatistics = new ArrayList<SPCacheStatProxy<?,?>>();
    
    public void afterPropertiesSet() throws Exception {
        registerMBean(this, "Pustefix:type=CacheStatistic,project=" + projectName);

        tickTimer = new Timer("Timer-CacheStatistic", true);
    }

    public <T1,T2> SPCache<T1, T2> monitor(SPCache<T1, T2> cache, String id) {        
        AdvanceCacheStatistic stat = new AdvanceCacheStatistic(id, tickTimer, queueSize, queueTicks);
        SPCacheStatProxy<T1, T2> proxy = new SPCacheStatProxy<T1, T2>(cache, stat);
        cacheStatistics.add(proxy);

        registerMBean(new DetailedCacheStatistics(proxy), //
                "Pustefix:type=DetailedCacheStatistics,project=" + projectName + ",id=" + stat.getId());

        return proxy;
    }

    private void registerMBean(Object mbean, String name) {
        try {
            ObjectName objectName = new ObjectName(name);
            MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();

            if (mbeanServer.isRegistered(objectName)) {
                mbeanServer.unregisterMBean(objectName);
            }

            mbeanServer.registerMBean(mbean, objectName);

            LOG.info("Registered MBean: " + objectName);
        } catch (Exception e) {
            LOG.error("Can't register MBean: " + name, e);
        }
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
    
    public void destroy() throws Exception {
        if(tickTimer != null) {
            tickTimer.cancel();
            tickTimer = null;
        }
    }

    /**
	 * Create cache-statistic in XML-format.
	 */
    public Document getAsXML() {

        Document doc = Xml.createDocument();
        Element root = doc.createElement("cachestatistic");    
        doc.appendChild(root);
        Iterator<SPCacheStatProxy<?,?>> it = cacheStatistics.iterator();
        while(it.hasNext()) {
            SPCacheStatProxy<?,?> statProxy = it.next();
            AdvanceCacheStatistic stat = statProxy.getStatistic();
            Element elem = doc.createElement("cache");
            elem.setAttribute("id",  stat.getId());
            elem.setAttribute("size", String.valueOf(statProxy.getSize()));
            elem.setAttribute("capacity", String.valueOf(statProxy.getCapacity()));
            elem.setAttribute("saturation", formatSaturation(statProxy.getSize(), statProxy.getCapacity()));
            long hits = stat.getHits();
            long misses = stat.getMisses();
            String hitrate = formatHitrate(hits, misses);
            elem.setAttribute("hitrate", hitrate);
            elem.setAttribute("hits", "" + hits);
            elem.setAttribute("misses", "" + misses);
            root.appendChild(elem);
        }
        return doc;
    }
    
    /**
     * Create cache-statistic in special format.
     */
    public String getAsString() {
        StringBuilder sb = new StringBuilder();
        Iterator<SPCacheStatProxy<?,?>> it = cacheStatistics.iterator();
        while(it.hasNext()) {
            SPCacheStatProxy<?,?> statProxy = it.next();
            AdvanceCacheStatistic stat = statProxy.getStatistic();
            long hits = stat.getHits();
            long misses = stat.getMisses();
            String hitrate = formatHitrate(hits, misses);
            String saturation = formatSaturation(statProxy.getSize(), statProxy.getCapacity());
            sb.append(stat.getId()).append(",").append(statProxy.getSize()).append(",")
                .append(statProxy.getCapacity()).append(",").append(saturation).append(",")
                .append(hits).append(",").append(misses).append(",").append(hitrate);
            if(it.hasNext()) sb.append("|");
        }
        return sb.toString();
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
    
    private String formatSaturation(int size, int capacity) {
        double rate = (double)size / capacity  * 100;
        return hitrateFormat.format(rate);
    }
    
    public void reset() {
    	cacheStatistics.clear();
    }
}
