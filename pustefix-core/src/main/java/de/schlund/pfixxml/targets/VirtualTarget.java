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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.NDC;
import org.w3c.dom.Document;

import de.schlund.pfixxml.XMLException;
import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.util.Xslt;

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
    protected long modtime = 0;

    protected TreeSet<PageInfo> pageinfos = new TreeSet<PageInfo>();

    protected boolean forceupdate = false;

    /**
     * @see de.schlund.pfixxml.targets.TargetRW#addPageInfo(de.schlund.pfixxml.targets.PageInfo)
     */
    @Override
    public void addPageInfo(PageInfo info) {
        synchronized (pageinfos) {
            pageinfos.add(info);
        }
    }

    /**
     * @see de.schlund.pfixxml.targets.Target#getPageInfos()
     */
    @Override
    public TreeSet<PageInfo> getPageInfos() {
        synchronized (pageinfos) {
            return new TreeSet<PageInfo>(pageinfos);
        }
    }

    /**
     * @see de.schlund.pfixxml.targets.TargetRW#setXMLSource(de.schlund.pfixxml.targets.Target)
     */
    @Override
    public void setXMLSource(Target source) {
        xmlsource = source;
    }

    /**
     * @see de.schlund.pfixxml.targets.TargetRW#setXSLSource(de.schlund.pfixxml.targets.Target)
     */
    @Override
    public void setXSLSource(Target source) {
        xslsource = source;
    }

    /**
     * @see de.schlund.pfixxml.targets.TargetRW#addParam(java.lang.String, java.lang.String)
     */
    @Override
    public void addParam(String key, Object val) {
        synchronized (params) {
            params.put(key, val);
        }
    }
    
    public Object removeParam(String key) {
        synchronized(params) {
            return params.remove(key);
        }
    }

    /**
     * @see de.schlund.pfixxml.targets.Target#getModTime()
     */
    @Override
    public long getModTime() {
        if (modtime == 0) {
            synchronized (this) {
                if (modtime == 0) {
                    FileResource doc = ResourceUtil.getFileResource(getTargetGenerator().getDisccachedir(), getTargetKey());
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
    @Override
    public synchronized boolean needsUpdate() throws Exception {
        long mymodtime = getModTime();
        long xmlmod;
        long xslmod;
        long depmod = 0;
        boolean xmlup;
        boolean xslup;
        boolean depup = false;
        Target tmp;

        tmp = getXMLSource();
        xmlup = tmp.needsUpdate();
        xmlmod = tmp.getModTime();
        tmp = getXSLSource();
        xslup = tmp.needsUpdate();
        xslmod = tmp.getModTime();

        for (Iterator<AuxDependency> i = this.getAuxDependencyManager().getChildren()
                .iterator(); i.hasNext();) {
            AuxDependency aux = i.next();
            if (aux.getType() == DependencyType.TARGET) {
                Target auxtarget = ((AuxDependencyTarget) aux).getTarget();
                depmod = Math.max(auxtarget.getModTime(), depmod);
                if (auxtarget.needsUpdate()) {
                    depup = true;
                }
            }
        }

        if (forceupdate)
            return true;
        if (xslup || xmlup || depup)
            return true;
        if ((xmlmod > mymodtime) || (xslmod > mymodtime)
                || getAuxDependencyManager().getMaxTimestamp() > mymodtime
                || depmod > mymodtime)
            return true;
        return false;
    }

    /**
     * @see de.schlund.pfixxml.targets.TargetRW#storeValue(java.lang.Object)
     */
    @Override
    public void storeValue(Object obj) {
        SPCache<Object,Object> cache = null;
        if(TargetGenerator.isRenderKey(getTargetKey())) {
            cache = generator.getCacheFactory().getRenderCache();
        } else {
            cache = generator.getCacheFactory().getCache();
        }
        cache.setValue(this, obj);
    }

    @Override
    public String toString() {
        if (getXMLSource() != null && getXSLSource() != null) {
            return "[TARGET: " + getType() + " " + getTargetKey() + "[" + themes.getId()
                    + "]" + " <" + getXMLSource().getTargetKey() + "> <"
                    + getXSLSource().getTargetKey() + ">]";
        } else {
            return "[TARGET: " + getType() + " " + getTargetKey() + "[" + themes.getId()
                    + "]]";
        }
    }

    /**
     * @see de.schlund.pfixxml.targets.TargetImpl#setModTime(long)
     */
    // still to implement from TargetImpl:
    //protected abstract Object  getValueFromDiscCache() throws Exception;
    @Override
    protected void setModTime(long mtime) {
        modtime = mtime;
    }

    /**
     * @see de.schlund.pfixxml.targets.TargetImpl#getValueFromSPCache()
     */
    @Override
    protected Object getValueFromSPCache() {
        SPCache<Object,Object> cache = null;
        if(TargetGenerator.isRenderKey(getTargetKey())) {
            cache = generator.getCacheFactory().getRenderCache();
        } else {
            cache = generator.getCacheFactory().getCache();
        }
        return cache.getValue(this);
    }

    /**
     * @see de.schlund.pfixxml.targets.TargetImpl#getModTimeMaybeUpdate()
     */
    @Override
    protected long getModTimeMaybeUpdate() throws TargetGenerationException,
            XMLException, IOException {
        // long currmodtime = getModTime();
        long maxmodtime = 0;
        long tmpmodtime;
        NDC.push("    ");
        if(TREE.isDebugEnabled()) {
            TREE.debug("> " + getTargetKey());
        }
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
        tmpmodtime = getAuxDependencyManager().getMaxTimestamp();
        // if (tmpmodtime > currmodtime) {
        //     CAT.warn("### AUX of "  + getTargetKey() + " is newer! " + tmpmodtime + ">" + currmodtime);
        // }
        maxmodtime = Math.max(tmpmodtime, maxmodtime);

        // check target dependencies
        for (Iterator<AuxDependency> i = this.getAuxDependencyManager().getChildren()
                .iterator(); i.hasNext();) {
            AuxDependency aux = i.next();
            if (aux.getType() == DependencyType.TARGET) {
                Target auxtarget;
                try {
                    auxtarget = ((AuxDependencyTarget) aux).getTarget();
                } catch (Exception e) {
                    throw new TargetGenerationException("Nested exception", e);
                }
                if (auxtarget instanceof TargetImpl) {
                    tmpmodtime = ((TargetImpl) auxtarget)
                            .getModTimeMaybeUpdate();
                } else {
                    tmpmodtime = auxtarget.getModTime();
                }
                maxmodtime = Math.max(tmpmodtime, maxmodtime);
            }
        }
        
        // check target generator config 
        tmpmodtime = getTargetGenerator().getConfigMaxModTime();
        maxmodtime = Math.max(tmpmodtime, maxmodtime);

        if ((maxmodtime > getModTime()) || forceupdate) {
            synchronized (this) {
                if ((maxmodtime > getModTime()) || forceupdate) {
                    try {
                        generateValue();
                        if(TREE.isDebugEnabled()) {
                            TREE.debug("  [" + getTargetKey() + ": generated...]");
                        }
                    } catch (TransformerException e) {
                        LOG.error("Error when generating: " + getTargetKey()
                                + " from " + getXMLSource().getTargetKey()
                                + " and " + getXSLSource().getTargetKey(), e);
                        // Now we invalidate the mem- and disc cache to force
                        // a complete rebuild of this target the next try
                        storeValue(null);
                        setModTime(-1);

                        TransformerException tex = e;
                        TargetGenerationException targetex = null;
                        if (storedException != null) {
                            targetex = new TargetGenerationException(
                                    "Caught transformer exception when doing getModtimeMaybeUpdate",
                                    storedException);
                        } else {
                            targetex = new TargetGenerationException(
                                    "Caught transformer exception when doing getModtimeMaybeUpdate",
                                    tex);
                        }
                        targetex.setTargetkey(targetkey);
                        throw targetex;
                    }
                }
            }
        } else {
            if(TREE.isDebugEnabled()) {
                TREE.debug("  [" + getTargetKey() + ": skipping...]");
            }
        }
        NDC.pop();
        return getModTime();
    }

    private void generateValue() throws XMLException, TransformerException,
            IOException {
        String key = getTargetKey();
        Target tmpxmlsource = getXMLSource();
        Target tmpxslsource = getXSLSource();
        FileResource cachepath = getTargetGenerator().getDisccachedir();
        FileResource cachefile = ResourceUtil.getFileResource(cachepath, key);
        cachefile.getParentAsFileResource().mkdirs();
        if (LOG.isDebugEnabled()) {
            LOG.debug(key + ": Getting " + getType() + " by XSLTrafo ("
                    + tmpxmlsource.getTargetKey() + " / "
                    + tmpxslsource.getTargetKey() + ")");
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
        Document xmlobj = (Document) ((TargetRW) tmpxmlsource).getCurrValue();
        Templates templ = (Templates) ((TargetRW) tmpxslsource).getCurrValue();
        if (xmlobj == null)
            throw new XMLException("**** xml source "
                    + tmpxmlsource.getTargetKey() + " ("
                    + tmpxmlsource.getType() + ") doesn't have a value!");
        if (templ == null)
            throw new XMLException("**** xsl source "
                    + tmpxslsource.getTargetKey() + " ("
                    + tmpxslsource.getType() + ") doesn't have a value!");
        TreeMap<String, Object> tmpparams = getParams();
        tmpparams.put("themes", themes.getId());

        // Store output in temporary object and overwrite cache file only
        // when transformation was sucessfully finished
        ByteArrayOutputStream temp = new ByteArrayOutputStream();
        Xslt.transform(xmlobj, templ, tmpparams, new StreamResult(temp));
        OutputStream out = cachefile.getOutputStream();
        out.write(temp.toByteArray());
        out.close();
        
        // Load the target in memcache to make sure all load time
        // dependencies are being registered
        this.getCurrValue();
        
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
