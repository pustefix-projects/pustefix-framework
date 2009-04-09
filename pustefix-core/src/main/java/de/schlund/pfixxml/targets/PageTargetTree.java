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
 *
 */

package de.schlund.pfixxml.targets;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * PageTargetTree.java
 *
 *
 * Created: Fri Jul 20 13:38:09 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class PageTargetTree {
    TreeMap<PageInfo, Target> toplevels = new TreeMap<PageInfo, Target>();
    TreeMap<String, TreeSet<PageInfo>> pageinfos = new TreeMap<String, TreeSet<PageInfo>>();

    protected void addEntry(PageInfo pageinfo, Target target) {
        synchronized (toplevels) {
            if (toplevels.get(pageinfo) == null) {
                toplevels.put(pageinfo, target);
                String name = pageinfo.getName();
                if (pageinfos.get(name) == null) {
                    pageinfos.put(name, new TreeSet<PageInfo>());
                }
                TreeSet<PageInfo> pinfos = pageinfos.get(name);
                pinfos.add(pageinfo);
            } else {
                throw new RuntimeException("Can't have another top-level target '" +
                                           target.getTargetKey() + "' for the same page '" +
                                           pageinfo.getName() + "' variant: '" + pageinfo.getVariant() + "'");
            }
        }
    }

    public TreeSet<PageInfo> getPageInfoForPageName(String name) {
        synchronized (pageinfos) {
            return pageinfos.get(name);
        }
    }

    
    public TreeSet<PageInfo> getPageInfos() {
        synchronized (toplevels) {
            return new TreeSet<PageInfo>(toplevels.keySet());
        }
    }

    public TreeSet<Target> getToplevelTargets() {
        synchronized (toplevels) {
            return new TreeSet<Target>(toplevels.values());
        }
    }

    public Target getTargetForPageInfo(PageInfo pinfo) {
        synchronized (toplevels) {
            return toplevels.get(pinfo);
        }
    }
    
    public void initTargets() {
        synchronized (toplevels) {
            for (Iterator<PageInfo> i = toplevels.keySet().iterator(); i.hasNext(); ) {
                PageInfo pageinfo = i.next();
                Target   top      = toplevels.get(pageinfo);
                addPageInfoToTarget(pageinfo, top);
            }
        }
    }
    
    private void addPageInfoToTarget(PageInfo page, Target target) {
        ((TargetImpl) target).addPageInfo(page);
        Target xmlsource = target.getXMLSource();
        if (xmlsource != null) {
            addPageInfoToTarget(page, xmlsource);
        }
        
        Target xslsource = target.getXSLSource();
        if (xslsource != null) {
            addPageInfoToTarget(page, xslsource);
        }
    }

}// PageTargetTree
