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
import java.io.*;
import java.util.*;
import org.apache.log4j.*;
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
    protected long    modtime   = 0l;
    protected TreeSet pageinfos = new TreeSet();

    public void addPageInfo(PageInfo info) {
        synchronized(pageinfos) {
            pageinfos.add(info);
        }
    }

    public TreeSet getPageInfos() {
        synchronized(pageinfos) {
            return (TreeSet) pageinfos.clone();
        }
    }

    public void setXMLSource(Target source) {
        xmlsource = source;
    }

    public void setXSLSource(Target source) {
        xslsource = source;
    }

    public void addParam(String key, String val) {
        synchronized(params) {
            params.put(key, val);
        }
    }

    public long getModTime() {
        if(modtime == 0l) {
            synchronized(this) {
                if(modtime == 0l) {
                    File doc = new File(getTargetGenerator().getDisccachedir() + getTargetKey());
                    if(doc.exists() && doc.isFile()) {
                        setModTime(doc.lastModified());
                    }
                }
            }
        }
        return modtime;
    }

    public boolean needsUpdate() throws Exception {
        long mymodtime = getModTime();
        long xmlmod;
        long xslmod;
        boolean xmlup;
        boolean xslup;
        Target tmp;
        tmp            = getXMLSource();
        xmlup          = tmp.needsUpdate();
        xmlmod         = tmp.getModTime();
        tmp            = getXSLSource();
        xslup          = tmp.needsUpdate();
        xslmod         = tmp.getModTime();
        if(xslup || xmlup)
            return true;
        if((xmlmod > mymodtime) || (xslmod > mymodtime)
           || getAuxDependencyManager().getMaxTimestamp() > mymodtime)
            return true;
        return false;
    }

    public void storeValue(Object obj) {
        SPCache cache = SPCacheFactory.getInstance().getCache();
        cache.setValue(this, obj);
    }

    public String toString() {
        return "[TARGET: " + getType() + " " + getTargetKey() + "@"
               + getTargetGenerator().getConfigname() + " <" + getXMLSource().getTargetKey()
               + "> <" + getXSLSource().getTargetKey() + ">]";
    }

    // still to implement from TargetImpl:
    //protected abstract Object  getValueFromDiscCache() throws Exception;
    protected void setModTime(long mtime) {
        modtime = mtime;
    }

    protected Object getValueFromSPCache() {
        SPCache cache = SPCacheFactory.getInstance().getCache();
        return cache.getValue(this);
    }

    protected long getModTimeMaybeUpdate() throws Exception {
        long maxmodtime = 0l;
        long tmpmodtime;
        NDC.push("    ");
        TREE.debug("> " + getTargetKey());
        tmpmodtime      = ((TargetImpl) getXMLSource()).getModTimeMaybeUpdate();
        maxmodtime      = Math.max(tmpmodtime, maxmodtime);
        tmpmodtime      = ((TargetImpl) getXSLSource()).getModTimeMaybeUpdate();
        maxmodtime      = Math.max(tmpmodtime, maxmodtime);

        // check all the auxilliary sources from auxsource
        maxmodtime = Math.max(getAuxDependencyManager().getMaxTimestamp(), maxmodtime);
        if(maxmodtime > getModTime()) {
            synchronized(this) {
                if(maxmodtime > getModTime()) {
                    try {
                        generateValue();
                        TREE.debug("  [" + getTargetKey() + ": generated...]");
                    } catch(Exception e) {
                        StringBuffer b = new StringBuffer(100);
                        b.append("Error when generating: ").append(getTargetKey()).append(" from ").
                            append(getXMLSource().getTargetKey()).append(" and ").append(getXSLSource().getTargetKey());
                        CAT.error(b.toString(), e);
                    }
                }
            }
        } else {
            TREE.debug("  [" + getTargetKey() + ": skipping...]");
        }
        NDC.pop();
        return getModTime();
    }

    private void generateValue() throws Exception {
        PustefixXSLTProcessor xsltproc  = TraxXSLTProcessor.getInstance();
        String                key       = getTargetKey();
        Target                xmlsource = getXMLSource();
        Target                xslsource = getXSLSource();
        File                  cachefile = new File(getTargetGenerator().getDisccachedir() + key);
        new File(cachefile.getParent()).mkdirs();
        CAT.debug(key + ": Getting " + getType() + " by XSLTrafo (" + xmlsource.getTargetKey()
                  + " / " + xslsource.getTargetKey() + ")");

        // we reset the auxilliary dependencies here, as they will be rebuild now, too 
        getAuxDependencyManager().reset();
        // as the file will be rebuild in the disc cache, we need to make sure that we will load it again
        // when we need it by invalidating the Memcache;
        storeValue(null);
        //  Ok, the value of the xml and the xsl dependency may still be null
        //  (as we defer loading until we actually need the doc, which is now).
        //  But the modtime has been taken into account, so those files exists in the disc cache and
        //  are up-to-date: getCurrValue() will finally load these values.
        Object xmlobj = ((TargetRW) xmlsource).getCurrValue();
        Object xslobj = ((TargetRW) xslsource).getCurrValue();
        if (xmlobj == null)
            throw new XMLException("**** xml source " + xmlsource.getTargetKey() +
                                   xmlsource.getType() + " doesn't have a value!");
        if (xslobj == null)
            throw new XMLException("**** xsl source " + xslsource.getTargetKey() +
                                   xslsource.getType() + " doesn't have a value!");

        //FIXME!!! Do we want to do this right HERE????
        xsltproc.applyTrafoForOutput(xmlobj, xslobj, getParams(), new FileOutputStream(cachefile));
        // Now we need to save the current value of the auxdependencies
        getAuxDependencyManager().saveAuxdepend();
        // and let's update the modification time.
        setModTime(cachefile.lastModified());
    }
} // VirtualTarget
