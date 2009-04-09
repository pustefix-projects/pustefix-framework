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
import java.util.TreeSet;

import de.schlund.pfixxml.resources.Resource;

/**
 * SharedLeaf.java
 *
 *
 * Created: Tue Jul 24 02:18:20 2001
 *
 * @author <a href="mailto: "Jens Lautenbacher</a>
 *
 *
 */

public class SharedLeaf implements Comparable<SharedLeaf> {
    
    private Resource path;
    private TreeSet<PageInfo> pageinfos = new TreeSet<PageInfo>();
    private long    modtime   = 0;
    
    protected SharedLeaf(Resource path) {
        this.path = path;
    }

    public Resource getPath() { return path; }
    
    public void addPageInfo(PageInfo info) {
        synchronized (pageinfos) {
            pageinfos.add(info);
        }
    }
    
    public TreeSet<PageInfo> getPageInfos() {
        synchronized (pageinfos) {
            return new TreeSet<PageInfo>(pageinfos);
        }
    }
    
    public void setModTime(long mtime) {
        modtime = mtime;
    }

    public long getModTime() {
        if (modtime == 0) {
            if (path.exists() && path.isFile()) {
                setModTime(path.lastModified());
            }
        }
        return modtime;
    }

   
    // comparable interface
    
    public int compareTo(SharedLeaf in) {
        return path.compareTo(in.path);
    }

}// SharedLeaf
