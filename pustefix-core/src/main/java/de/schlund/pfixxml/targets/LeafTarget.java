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
import java.util.Iterator;
import java.util.TreeSet;

import de.schlund.pfixxml.XMLException;
import de.schlund.pfixxml.util.ResourceUtils;


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

    @Override
    public void setXMLSource(Target source) {
        throw new RuntimeException("Can't add a XMLSource to a leaf");
    }

    @Override
    public void setXSLSource(Target source) {
        throw new RuntimeException("Can't add a XSLSource to a leaf");
    }

    @Override
    public void addParam(String key, Object val) {
        throw new RuntimeException("Can't add a stylesheet parameter to a leaf");
    }

    @Override
    public TreeSet<PageInfo> getPageInfos() {
        return sharedleaf.getPageInfos();
    }

    @Override
    public void addPageInfo(PageInfo info) {
        sharedleaf.addPageInfo(info);
    }

    @Override
    public long getModTime() {
        synchronized (sharedleaf) {
            return sharedleaf.getModTime();
        }
    }

    @Override
    public boolean needsUpdate() throws Exception {
        synchronized (sharedleaf) {
            long mymodtime = sharedleaf.getModTime();
            long maxmodtime = ResourceUtils.lastModified(targetRes);
            boolean depup = true;

            for (Iterator<AuxDependency> i = this.getAuxDependencyManager().getChildren()
                    .iterator(); i.hasNext();) {
                AuxDependency aux = i.next();
                if (aux.getType() == DependencyType.TARGET) {
                    Target auxtarget = ((AuxDependencyTarget) aux).getTarget();
                    maxmodtime = Math.max(auxtarget.getModTime(), maxmodtime);
                    if (auxtarget.needsUpdate()) {
                        depup = true;
                    }
                }
            }

            if (depup || maxmodtime > mymodtime) {
                return true;
            }
            return false;
        }
    }

    @Override
    public void storeValue(Object obj) {
        synchronized (sharedleaf) {
            SPCache<Object, Object> cache = SPCacheFactory.getInstance().getCache();
            cache.setValue(sharedleaf, obj);
        }
    }

    @Override
    public String toString() {
        return "[TARGET: " + getType() + " " + getTargetKey() + "@"
                + getTargetGenerator().getName() + "]";
    }

    @Override
    protected void setModTime(long mtime) {
        synchronized (sharedleaf) {
            sharedleaf.setModTime(mtime);
        }
    }

    @Override
    protected Object getValueFromSPCache() {
        synchronized (sharedleaf) {
            return SPCacheFactory.getInstance().getCache().getValue(sharedleaf);
        }
    }

    @Override
    protected long getModTimeMaybeUpdate() throws TargetGenerationException,
            XMLException, IOException {
        long mymodtime = getModTime();
        long maxmodtime = ResourceUtils.lastModified(targetRes);
        TREE.debug("> " + getTargetKey());

        for (Iterator<AuxDependency> i = this.getAuxDependencyManager().getChildren()
                .iterator(); i.hasNext();) {
            AuxDependency aux = i.next();
            if (aux.getType() == DependencyType.TARGET) {
                long tmpmodtime = 0;
                Target auxtarget = ((AuxDependencyTarget) aux).getTarget();
                if (auxtarget instanceof TargetImpl) {
                    tmpmodtime = ((TargetImpl) auxtarget)
                            .getModTimeMaybeUpdate();
                } else {
                    tmpmodtime = auxtarget.getModTime();
                }
                maxmodtime = Math.max(tmpmodtime, maxmodtime);
            }
        }

        if (maxmodtime > mymodtime) {
            try {
                // invalidate Memcache:
                storeValue(null);
                TREE.debug("  [" + getTargetKey() + ": updated leaf node...]");
                setModTime(maxmodtime);
            } catch (Exception e) {
                LOG.error("Error when updating", e);
            }
        } else {
            TREE.debug("  [" + getTargetKey() + ": leaf node...]");
        }
        return getModTime();
    }

}// LeafTarget
