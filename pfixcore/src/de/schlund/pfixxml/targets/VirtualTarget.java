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

import de.schlund.pfixxml.*;
import de.schlund.pfixxml.util.*;
import java.io.*;
import java.util.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import org.apache.log4j.NDC;
import org.w3c.dom.Document;

/**
 * VirtualTarget.java
 *
 *
 * Created: Mon Jul 23 19:53:38 2001
 *
 * @author <a href="mailto: "Jens Lautenbacher</a>
 *
 *
 */
public abstract class VirtualTarget extends TargetImpl {
    protected long                 modtime       = 0;
    protected TreeSet              pageinfos     = new TreeSet();
    protected AuxDependencyManager auxdepmanager = null;
    protected boolean              forceupdate   = false;
    
    public AuxDependencyManager getAuxDependencyManager() {
        return auxdepmanager; 
    }
    /**
     * @see de.schlund.pfixxml.targets.TargetRW#addPageInfo(de.schlund.pfixxml.targets.PageInfo)
     */
    public void addPageInfo(PageInfo info) {
        synchronized (pageinfos) {
            pageinfos.add(info);
        }
    }

    /**
     * @see de.schlund.pfixxml.targets.Target#getPageInfos()
     */
    public TreeSet getPageInfos() {
        synchronized (pageinfos) {
            return (TreeSet) pageinfos.clone();
        }
    }

    /**
     * @see de.schlund.pfixxml.targets.TargetRW#setXMLSource(de.schlund.pfixxml.targets.Target)
     */
    public void setXMLSource(Target source) {
        xmlsource = source;
    }

    /**
     * @see de.schlund.pfixxml.targets.TargetRW#setXSLSource(de.schlund.pfixxml.targets.Target)
     */
    public void setXSLSource(Target source) {
        xslsource = source;
    }

    public void setThemes(String[] themes) {
        this.themes = themes;
    }

    /**
     * @see de.schlund.pfixxml.targets.TargetRW#addParam(java.lang.String, java.lang.String)
     */
    public void addParam(String key, String val) {
        synchronized (params) {
            params.put(key, val);
        }
    }

    /**
     * @see de.schlund.pfixxml.targets.Target#getModTime()
     */
    public long getModTime() {
        if (modtime == 0) {
            synchronized (this) {
                if (modtime == 0) {
                    File doc = new File(getTargetGenerator().getDisccachedir().resolve(), getTargetKey());
                    if (doc.exists() && doc.isFile()) {
                        setModTime(doc.lastModified());
                    }
                }
            }
        }
        return modtime;
    }

    /**
     * @see de.schlund.pfixxml.targets.Target#needsUpdate()
     */
    public boolean needsUpdate() throws Exception {
        long    mymodtime = getModTime();
        long    xmlmod;
        long    xslmod;
        boolean xmlup;
        boolean xslup;
        Target  tmp;

        tmp    = getXMLSource();
        xmlup  = tmp.needsUpdate();
        xmlmod = tmp.getModTime();
        tmp    = getXSLSource();
        xslup  = tmp.needsUpdate();
        xslmod = tmp.getModTime();

        if (forceupdate)
            return true;
        if (xslup || xmlup)
            return true;
        if ((xmlmod > mymodtime) || (xslmod > mymodtime) || getAuxDependencyManager().getMaxTimestamp(false) > mymodtime)
            return true;
        return false;
    }

    /**
     * @see de.schlund.pfixxml.targets.TargetRW#storeValue(java.lang.Object)
     */
    public void storeValue(Object obj) {
        SPCache cache = SPCacheFactory.getInstance().getCache();
        cache.setValue(this, obj);
    }

  
    public String toString() {
        return "[TARGET: " + getType() + " " + getTargetKey()
            + "@" + getTargetGenerator().getName()
            + " <" + getXMLSource().getTargetKey() + "> <"
            + getXSLSource().getTargetKey() + ">]";
    }

    /**
     * @see de.schlund.pfixxml.targets.TargetImpl#setModTime(long)
     */
    // still to implement from TargetImpl:
    //protected abstract Object  getValueFromDiscCache() throws Exception;
    protected void setModTime(long mtime) {
        modtime = mtime;
    }

    /**
     * @see de.schlund.pfixxml.targets.TargetImpl#getValueFromSPCache()
     */
    protected Object getValueFromSPCache() {
        SPCache cache = SPCacheFactory.getInstance().getCache();
        return cache.getValue(this);
    }

    /**
     * @see de.schlund.pfixxml.targets.TargetImpl#getModTimeMaybeUpdate()
     */
    protected long getModTimeMaybeUpdate() throws TargetGenerationException, XMLException, IOException {
        // long currmodtime = getModTime();
        long maxmodtime  = 0;
        long tmpmodtime;
        NDC.push("    ");
        TREE.debug("> " + getTargetKey());
        maxmodtime = ((TargetImpl) getXMLSource()).getModTimeMaybeUpdate();
        // if (maxmodtime > currmodtime) {
        //     CAT.warn("### XMLSource of "  + getTargetKey() + " is newer! " + maxmodtime + ">" + currmodtime);
        // }
        tmpmodtime = ((TargetImpl) getXSLSource()).getModTimeMaybeUpdate();
        // if (tmpmodtime > currmodtime) {
        //     CAT.warn("### XSLSource of "  + getTargetKey() + " is newer! " + tmpmodtime + ">" + currmodtime);
        // }
        maxmodtime = Math.max(tmpmodtime, maxmodtime);
        storedException = null;
        // check all the auxilliary sources from auxsource
        tmpmodtime = getAuxDependencyManager().getMaxTimestamp(true);
        // if (tmpmodtime > currmodtime) {
        //     CAT.warn("### AUX of "  + getTargetKey() + " is newer! " + tmpmodtime + ">" + currmodtime);
        // }
        maxmodtime = Math.max(tmpmodtime, maxmodtime);
        if ((maxmodtime > getModTime()) || forceupdate) {
            synchronized (this) {
                if ((maxmodtime > getModTime()) || forceupdate) {
                    try {
                        generateValue();
                        TREE.debug("  [" + getTargetKey() + ": generated...]");
                    } catch (TransformerException e) {
                        CAT.error("Error when generating: " + getTargetKey() + " from "
                                  + getXMLSource().getTargetKey() + " and "
                                  + getXSLSource().getTargetKey(), e);
                        // Now we invalidate the mem- and disc cache to force
                        // a complete rebuild of this target the next try
                        storeValue(null);
                        setModTime(-1);
                        File cachefile = new File(getTargetGenerator().getDisccachedir().resolve(), getTargetKey());
                        if (cachefile.exists()) {
                            cachefile.delete();
                        }
                        
                        TransformerException tex = e;
                        TargetGenerationException targetex = null;
                        if (storedException != null) {
                            targetex = new TargetGenerationException("Caught transformer exception when doing getModtimeMaybeUpdate", storedException);
                        } else {
                            targetex = new TargetGenerationException("Caught transformer exception when doing getModtimeMaybeUpdate", tex);
                        }
                        targetex.setTargetkey(targetkey);
                        throw targetex;
                    }
                }
            }
        } else {
            TREE.debug("  [" + getTargetKey() + ": skipping...]");
        }
        NDC.pop();
        return getModTime();
    }

    private void generateValue() throws XMLException, TransformerException, IOException {
        String key          = getTargetKey();
        Target tmpxmlsource = getXMLSource();
        Target tmpxslsource = getXSLSource();
        Path   cachepath    = getTargetGenerator().getDisccachedir();
        File   cachefile    = new File(cachepath.resolve(), key);
        new File(cachefile.getParent()).mkdirs();
        if (CAT.isDebugEnabled()) {
            CAT.debug(key + ": Getting " + getType() + " by XSLTrafo (" +
                      tmpxmlsource.getTargetKey() + " / " + tmpxslsource.getTargetKey() + ")");
        }
        // we reset the auxilliary dependencies here, as they will be rebuild now, too 
        getAuxDependencyManager().reset();
        // as the file will be rebuild in the disc cache, we need to make sure that we will load it again
        // when we need it by invalidating the Memcache;
        storeValue(null);
        //  Ok, the value of the xml and the xsl dependency may still be null
        //  (as we defer loading until we actually need the doc, which is now).
        //  But the modtime has been taken into account, so those files exists in the disc cache and
        //  are up-to-date: getCurrValue() will finally load these values.
        Document  xmlobj    = (Document)  ((TargetRW) tmpxmlsource).getCurrValue();
        Templates templ     = (Templates) ((TargetRW) tmpxslsource).getCurrValue();
        if (xmlobj == null) 
            throw new XMLException("**** xml source " +
                                   tmpxmlsource.getTargetKey() + " (" + tmpxmlsource.getType() + ") doesn't have a value!");
        if (templ == null) 
            throw new XMLException("**** xsl source " +
                                   tmpxslsource.getTargetKey() + " (" + tmpxslsource.getType() + ") doesn't have a value!");
        TreeMap   tmpparams = getParams();
        Xslt.transform(xmlobj, templ, tmpparams, new StreamResult(new FileOutputStream(cachefile)));
        // Now we need to save the current value of the auxdependencies
        getAuxDependencyManager().saveAuxdepend();
        // and let's update the modification time.
        setModTime(cachefile.lastModified());
        forceupdate = false;
    }


    public void setForceUpdate() {
        forceupdate = true;
    }

                         
} // VirtualTarget
