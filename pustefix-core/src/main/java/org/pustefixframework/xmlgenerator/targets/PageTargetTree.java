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

package org.pustefixframework.xmlgenerator.targets;
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

    protected void addEntry(String pageName, String variant, Target target) {
    	PageInfo pageinfo = new PageInfo(pageName, variant);
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
    
    protected void removeEntry(String pageName, String variant) {
    	PageInfo pageInfo = new PageInfo(pageName, variant);
    	synchronized (toplevels) {
    		toplevels.remove(pageInfo);
    		pageinfos.remove(pageInfo.getName());
    	}
    }

    public boolean containsPage(String name) {
    	synchronized (pageinfos) {
    		return pageinfos.get(name)!=null;
    	}
    }
    
    public Target getTargetForPage(String pageName, String variant) {
    	PageInfo pageInfo = new PageInfo(pageName, variant);
        synchronized (toplevels) {
            return toplevels.get(pageInfo);
        }
    }

}// PageTargetTree
