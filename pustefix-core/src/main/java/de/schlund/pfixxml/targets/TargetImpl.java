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
package de.schlund.pfixxml.targets;

import java.io.IOException;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.pustefixframework.resource.Resource;

import de.schlund.pfixxml.XMLException;
import de.schlund.pfixxml.targets.cachestat.CacheStatistic;

/**
 * TargetImpl.java
 *
 *
 * Created: Mon Jul 23 16:24:53 2001
 *
 * @author <a href="mailto: "Jens Lautenbacher</a>
 *
 *
 */
public abstract class TargetImpl implements TargetRW, Comparable<Target> {

    //~ Instance/static variables ..................................................................

    // set in from constructor
    protected TargetType           type;
    protected TargetGenerator      generator;
    protected String               targetkey;
    protected Resource             targetRes;
    protected Resource             targetAuxRes;
    protected Themes               themes          = null;
    protected TreeMap<String, Object> params       = null;
    protected Target               xmlsource       = null;
    protected Target               xslsource       = null;
    protected Logger               LOG             = Logger.getLogger(this.getClass());
    protected Logger               TREE            = Logger.getLogger(this.getClass().getName() + ".TREE");
    // determine if the target has been generated. This affects production mode only, where
    // we do not need to handle if the target is really up to date (except make generate!!!)
    private   boolean              onceLoaded      = false;
    // store  exception occured during transformation here. 
    protected Exception            storedException = null;

    protected AuxDependencyManager auxdepmanager   = null;
    
    //~ Methods ....................................................................................

    // Target interface
   /**
    * @see de.schlund.pfixxml.targets.Target#getType()
    */
    public TargetType getType() {
        return type;
    }
    
    /**
     * @see de.schlund.pfixxml.targets.Target#getTargetKey()
     */
    public String getTargetKey() {
        return targetkey;
    }
    
    public Resource getTargetResource() {
    	return targetRes;
    }
    
    public Resource getTargetAuxResource() {
    	return targetAuxRes;
    }

    public String getFullName() {
        return generator.getName() + "@" + targetkey;
    }
    
    /**
     * @see de.schlund.pfixxml.targets.Target#getTargetGenerator()
     */
    public TargetGenerator getTargetGenerator() {
        return generator;
    }

    public Themes getThemes() {
        return themes;
    }
    
    /**
     * @see de.schlund.pfixxml.targets.Target#getXMLSource()
     */
    public Target getXMLSource() {
        return xmlsource;
    }

    /**
     * @see de.schlund.pfixxml.targets.Target#getXSLSource()
     */
    public Target getXSLSource() {
        return xslsource;
    }

    /**
     * @see de.schlund.pfixxml.targets.Target#getParams()
     */
    public TreeMap<String, Object> getParams() {
        if (params == null) {
            return null;
        } else {
            synchronized (params) {
                return new TreeMap<String, Object>(params);
            }
        }
    }

    public void resetParams() {
        if (params != null) {
            params.clear();
        }
    }
    
    /**
     * @see de.schlund.pfixxml.targets.Target#getValue()
     */
    public Object getValue() throws TargetGenerationException {
        // Idea: if skip_getmodtimemaybeupdate is set we do not need to call getModeTimeMaybeUpdate
        // but: if the target is not in disk-cache (has not been generated) we must call
        // getModTimeMaybeUpdate once to generate it. After that, it can be loaded in getCurrValue()
        // which sets onceLoaded to true so we don't have to make this check again.
        if (generator.isGetModTimeMaybeUpdateSkipped()) {
            // skip getModTimeMaybeUpdate!
            LOG.debug("skip_getmodtimemaybeupdate is true. Trying to skip getModTimeMaybeUpdate...");
            if (!onceLoaded) {
                // do test for exists here!
                if (!targetRes.exists()) { // Target has not been loaded once and it doesn't exist in disk cache
                    LOG.debug("Cant't skip getModTimeMaybeUpdated because it has not been loaded " +
                              "and doesn't even exist in disk cache! Generating now !!");
                    try {
                        getModTimeMaybeUpdate();
                        // FIXME FIXME ! Do we really handle the exception here, if getmodtimemaybeupdate is slipped????
                    } catch(IOException e1) {
                        throw new TargetGenerationException(e1.getClass().getName()+ " in getModTimeMaybeUpdate()!", e1);
                    } catch(XMLException e2) {
                        throw new TargetGenerationException(e2.getClass().getName()+ " in getModTimeMaybeUpdate()", e2);
                    }
                } else {
                    LOG.debug("Target exists in disc cache, using it...");
                }
            } else { // target generated -> nop 
                LOG.debug("Target has already been loaded, reusing it...");
            }
        } else { // do not skip getModTimeMaybeUpdate 
            LOG.debug("Skipping getModTimeMaybeUpdate disabled in TargetGenerator!");
            try {
                getModTimeMaybeUpdate();
            } catch(IOException e1) {
                TargetGenerationException tex = new TargetGenerationException(e1.getClass().getName() +
                                                                              " in getModTimeMaybeUpdate()", e1);
                tex.setTargetkey(getTargetKey());
                throw tex;
            } catch(XMLException e2) {
                TargetGenerationException tex = new TargetGenerationException(e2.getClass().getName() +
                                                                               " in getModTimeMayUpdate()", e2);
                tex.setTargetkey(getTargetKey());
                throw tex;
            }
        }
        Object obj = null;
        try {
            obj = getCurrValue();
        } catch (TransformerException e) {
            TargetGenerationException tex = new TargetGenerationException("Exception in getCurrValue (xml=" +
                                                                          getXMLSource() + ", xsl=" + getXSLSource() +")!", e);
            tex.setTargetkey(getTargetKey());
            throw tex;
        }
        return obj;
    }

    public abstract void addPageInfo(PageInfo info);
    
    public abstract TreeSet<PageInfo> getPageInfos();
    
    public abstract void setXMLSource(Target source);

    public abstract void setXSLSource(Target source);
    
    public abstract void addParam(String key, Object val);
    
    public abstract void storeValue(Object obj);

    public abstract boolean needsUpdate() throws Exception;

    public abstract long getModTime();

    @Override
    public abstract String toString();

    /**
     * @see de.schlund.pfixxml.targets.TargetRW#getCurrValue()
     */
    public Object getCurrValue() throws TransformerException {
        Object obj = getValueFromSPCache();
        // add cache access to cache statistic
        doCacheStatistic(obj);
        // look if the target exists in memory cache and if the file in disk cache is newer.
        if (obj == null || isDiskCacheNewerThenMemCache()) {
            synchronized (this) {   // TODO: double-checked locking is broken ...
                obj = getValueFromSPCache();
                if (obj == null || isDiskCacheNewerThenMemCache()) {
                    if (LOG.isDebugEnabled()) {
                        if (LOG.isDebugEnabled() && isDiskCacheNewerThenMemCache()) {
                            LOG.debug(
                                "File in disk cache is newer then in memory cache. Rereading target from disk...");
                        }
                    }
                    
                    obj = getValueFromDiscCache();
                    // Caution! setCacheValue is not guaranteed to store anything at all, so it's NOT
                    // guaranteed that the sequence
                    //           storeValue(tmp); tmp2 = getValueFromSPCache; return tmp2
                    // will return a tmp2 == tmp.
                    // Example: A NullCache will silently ignore all store requests, so a call to this method
                    //          will always trigger getValueFromDiscCache().
                    storeValue(obj);

                    // after the newer file on disk is reread and stored in memory cache it isn't
                    // newer any more, so set the mod time of the target to the mod time of the file
                    // in disk cache
                    if (isDiskCacheNewerThenMemCache()) {
                    	try {
                    		setModTime(targetRes.lastModified());
                    	} catch(IOException x) {
                    		throw new TransformerException("Can't get modification time: " + targetRes.toString(), x);
                    	}
                    }

                    // now the target is generated
                    onceLoaded = true;
                }
            }
        }
        return obj;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Target in) {
        if (getTargetGenerator().getName().compareTo(in.getTargetGenerator().getName()) != 0) {
            return getTargetGenerator().getName().compareTo(in.getTargetGenerator().getName());
        } else {
            return getTargetKey().compareTo(in.getTargetKey());
        }
    }

    
    public boolean isDiskCacheNewerThenMemCache() {
        long target_mod_time = getModTime();
        long disk_mod_time;
        try {
            disk_mod_time = targetRes.lastModified();
        } catch(IOException x) {
        	throw new RuntimeException("Error getting resource modification time: " + targetRes.toString(), x);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("File in DiskCache "+ targetRes.toString()
                    + " (" + disk_mod_time + ") is "
                    + (disk_mod_time > target_mod_time ? " newer " : "older")
                    + " than target(" + target_mod_time + ")");
        }
        // return true if file in diskcache newer than target
        return disk_mod_time > target_mod_time ? true : false;
    }

    
    //
    // implementation
    //
    protected abstract Object getValueFromSPCache();

    protected abstract Object getValueFromDiscCache() throws TransformerException;

    protected abstract long getModTimeMaybeUpdate()
        throws TargetGenerationException, XMLException, IOException;

    protected abstract void setModTime(long mtime);
   
    /**
     * Sets the storedException.
     * @param storedException The storedException to set
     */
    public void setStoredException(Exception stored) {
        this.storedException = stored;
    }
    
    private void doCacheStatistic(Object value) {
        if (value == null) {
            CacheStatistic.getInstance().registerCacheMiss(this);
        } else {
            CacheStatistic.getInstance().registerCacheHit(this);
        }
    }
    
    public AuxDependencyManager getAuxDependencyManager() {
        return auxdepmanager;
    }

} // TargetImpl
