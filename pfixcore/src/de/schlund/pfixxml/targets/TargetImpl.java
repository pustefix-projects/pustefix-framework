/*
* This file is part of PFIXCORE.
*
* PFIXCORE is free software; you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation; either version 2 of the License, or
* (at your option) any later version.
*
* PFIXCORE is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with PFIXCORE; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*
*/
package de.schlund.pfixxml.targets;

import java.io.File;
import java.io.IOException;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Category;

import de.schlund.pfixxml.XMLException;

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
public abstract class TargetImpl implements TargetRW, Comparable {

    //~ Instance/static variables ..................................................................

    // set in from constructor
    protected TargetType type;
    protected TargetGenerator generator;
    protected String targetkey;
    // only needed to init in constructor for virtual targets
    protected AuxDependencyManager auxdepmanager = null;
    protected TreeMap params = null;
    protected Target xmlsource = null;
    protected Target xslsource = null;
    protected Category CAT = Category.getInstance(this.getClass().getName());
    protected Category TREE = Category.getInstance(this.getClass().getName() + ".TREE");
    // determine if the target has been generated. This affects production mode only, where
    // we do not need to handle that the target is always up to date (expect make generate!!!)
    private boolean onceGenerated = false;
    // store  exception occured during transformation here. 
    protected Exception storedException = null;

    
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

    /**
     * @see de.schlund.pfixxml.targets.Target#getAuxDependencyManager()
     */
    public AuxDependencyManager getAuxDependencyManager() {
        return auxdepmanager;
    }

    /**
     * @see de.schlund.pfixxml.targets.Target#getTargetGenerator()
     */
    public TargetGenerator getTargetGenerator() {
        return generator;
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
    public TreeMap getParams() {
        if (params == null) {
            return null;
        } else {
            synchronized (params) {
                return new TreeMap(params);
            }
        }
    }

   
    /**
     * @see de.schlund.pfixxml.targets.Target#getValue()
     */
    public Object getValue() throws TargetGenerationException {
        // Idea: if skip_getmodtimemaybeupdate is set we do not need to call getModeTimeMaybeUpdate
        // but: if the target is not in memory- and disk-cache (has not been generated) we
        // must call getModTimeMaybeUpdate to make it work
        if (generator.isGetModTimeMaybeUpdateSkipped()) {
            // skip getModTimeMaybeUpdate!
            if (CAT.isDebugEnabled()) {
                CAT.debug("skip_getmodtimemaybeupdate is on. Trying to skip getModTimeMaybeUpdate...");
            }
            if (!onceGenerated) { // Target not in memory- and disc-cache -> getModTimeMaybeUpdate
                if (CAT.isDebugEnabled()) {
                    CAT.debug(
                        "Cant't skip getModTimeMaybeUpdate cause target has not been generated! Generating now !!");
                }
            try {
                getModTimeMaybeUpdate();
                // FIXME FIXME ! Do we really handle the exception here, if getmodtimemaybeupdate is slipped????
            } catch(IOException e1) {
                throw new TargetGenerationException(e1.getClass().getName()+" in getModTimeMaybeUpdate()!", e1);
            } catch(XMLException e2) {
                throw new TargetGenerationException(e2.getClass().getName()+"  in getModTimeMaybeUpdate()", e2);
            } catch (ParserConfigurationException e3) {
                throw new TargetGenerationException(e3.getClass().getName()+" in getModTimeMaybeUpdate()", e3);
            }
            

            } // target generated -> nop 
            else {
                if (CAT.isDebugEnabled()) {
                    CAT.debug("Target has been generated! Skipping getModTimeMaybeUpdate...");
                }
            }
        } // do not skip getModTimeMaybeUpdate 
        else {
            if (CAT.isDebugEnabled()) {
                CAT.debug("Skipping getModTimeMaybeUpdate disabled in TargetGenerator!");
            }
            try {
                getModTimeMaybeUpdate();
            } catch(IOException e1) {
                TargetGenerationException tex = new TargetGenerationException(e1.getClass().getName()+" in getModTimeMaybeUpdate()", e1);
                tex.setTargetkey(getTargetKey());
                throw tex;
            } catch(XMLException e2) {
                TargetGenerationException tex =  new TargetGenerationException(e2.getClass().getName()+" in getModTimeMayUpdate()", e2);
                tex.setTargetkey(getTargetKey());
                throw tex;
            } catch (ParserConfigurationException e3) {
                TargetGenerationException tex =  new TargetGenerationException(e3.getClass().getName()+" in getModTimeMaybeUpdate()", e3);
                tex.setTargetkey(getTargetKey());
                throw tex;
            }
        }
        Object obj = null;
        try {
            obj = getCurrValue();
        } catch (TransformerException e) {
            TargetGenerationException tex = new TargetGenerationException("Exception in getCurrValue !", e);
            tex.setTargetkey(getTargetKey());
            throw tex;
        }
        return obj;
    }

    public abstract void addPageInfo(PageInfo info);

    public abstract TreeSet getPageInfos();

    public abstract void setXMLSource(Target source);

    public abstract void setXSLSource(Target source);

    public abstract void addParam(String key, String val);

    public abstract void storeValue(Object obj);

    public abstract boolean needsUpdate() throws Exception;

    public abstract long getModTime();

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
            synchronized (this) {
                obj = getValueFromSPCache();
                if (obj == null || isDiskCacheNewerThenMemCache()) {
                    if (CAT.isDebugEnabled()) {
                        if (CAT.isDebugEnabled() && isDiskCacheNewerThenMemCache()) {
                            CAT.debug(
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

                    // after the newer file on disk is reread and stored in memory cache it isnt't
                    // newer any more, so set the mod time of the target to the mod time of the
                    // file in disk cache
                    if (isDiskCacheNewerThenMemCache()) {
                        setModTime(new File(getTargetGenerator().getDisccachedir() + getTargetKey()).lastModified());
                    }

                    // now the target is generated
                    onceGenerated = true;
                }
            }
        }
        return obj;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object inobj) {
        Target in = (Target) inobj;
        if (getTargetGenerator().getConfigname().compareTo(in.getTargetGenerator().getConfigname()) != 0) {
            return getTargetGenerator().getConfigname().compareTo(in.getTargetGenerator().getConfigname());
        } else {
            return getTargetKey().compareTo(in.getTargetKey());
        }
    }

    
    public boolean isDiskCacheNewerThenMemCache() {
        long target_mod_time = getModTime();
        File thefile = new File(getTargetGenerator().getDisccachedir() + getTargetKey());
        long disk_mod_time = thefile.lastModified();
        if (CAT.isDebugEnabled()) {
            CAT.debug("File in DiskCache "+ getTargetGenerator().getDisccachedir()
                    + getTargetKey() + " (" + disk_mod_time + ") is "
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
        throws TargetGenerationException, XMLException, ParserConfigurationException, IOException;

    protected abstract void setModTime(long mtime);
   
    /**
     * Sets the storedException.
     * @param storedException The storedException to set
     */
    public void setStoredException(Exception stored) {
        this.storedException = stored;
    }
    
    private void doCacheStatistic(Object value) {
        TargetGenerator tgen = getTargetGenerator();
        if(value == null) {
            SPCacheStatistic.getInstance().registerCacheMiss(this);
        } else {
            SPCacheStatistic.getInstance().registerCacheHit(this);
        }
    }
    
    
    

} // TargetImpl
