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
import java.util.*;
import java.io.*;
import org.apache.log4j.*;

import de.schlund.pfixxml.XMLException;

/**
 * LeafTarget.java
 *
 *
 * Created: Mon Jul 23 19:53:38 2001
 *
 * @author <a href="mailto: "Jens Lautenbacher</a>
 *
 *
 */

public abstract class LeafTarget extends TargetImpl {
    // Set this in the Constructor of derived classes!
    protected SharedLeaf sharedleaf;
    
    public void setXMLSource(Target source) {
        throw new RuntimeException("Can't add a XMLSource to a leaf");
    }

    public void setXSLSource(Target source) {
        throw new RuntimeException("Can't add a XSLSource to a leaf");
    }
    
    public void addParam(String key, String val) {
        throw new RuntimeException("Can't add a stylesheet parameter to a leaf");
    }
    
    public TreeSet getPageInfos() {
        return sharedleaf.getPageInfos();
    }

    public void addPageInfo(PageInfo info) {
        sharedleaf.addPageInfo(info);
    }
    
    public long getModTime() {
        synchronized (sharedleaf) {
            return sharedleaf.getModTime();
        }
    }

    
    public boolean needsUpdate() throws Exception  {
        synchronized (sharedleaf) {
            long mymodtime = sharedleaf.getModTime();
            File doc       = new File(getTargetGenerator().getDocroot(), getTargetKey());
            if (doc.lastModified() > mymodtime) {
                return true;
            }
            return false;
        }
    }

    public void storeValue(Object obj) {
        synchronized (sharedleaf) {
            SPCache cache = SPCacheFactory.getInstance().getCache();
            cache.setValue(sharedleaf, obj);
        }
    }

    public String toString() {
        return "[TARGET: " + getType() + " " + getTargetKey() + "@" + getTargetGenerator().getConfigname() + "]";
    }

    // still to implement from TargetImpl:
    //protected abstract Object  getValueFromDiscCache() throws Exception;

    protected void setModTime(long mtime) {
        synchronized (sharedleaf) {
            sharedleaf.setModTime(mtime);
        }
    }

    protected Object getValueFromSPCache() {
        synchronized (sharedleaf) {
            return SPCacheFactory.getInstance().getCache().getValue(sharedleaf);
        }
    }

    protected long getModTimeMaybeUpdate() throws TargetGenerationException, XMLException, IOException {
        long mymodtime  = getModTime(); 
        long maxmodtime = new File(getTargetGenerator().getDocroot(), getTargetKey()).lastModified(); 
        NDC.push("    ");
        TREE.debug("> " + getTargetKey());
        
        if (maxmodtime > mymodtime) {
            try {
                // invalidate Memcache:
                storeValue(null);
                TREE.debug("  [" + getTargetKey() + ": updated leaf node...]");
                setModTime(maxmodtime);
            } catch (Exception e) {
                CAT.error("Error when updating", e);
            }
        } else {
            TREE.debug("  [" + getTargetKey() + ": leaf node...]");
        }
        NDC.pop();
        return getModTime();
    }
    
}// LeafTarget
