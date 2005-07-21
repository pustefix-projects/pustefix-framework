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

public class SharedLeaf implements Comparable {
    private String  path;
    private String  dir;
    private TreeSet pageinfos = new TreeSet();
    private long    modtime   = 0;
    
    protected SharedLeaf(String path) {
        this.path = path;
        this.dir  = path.substring(0,path.lastIndexOf("/"));
    }

    public String getDir() { return dir; }
    public String getPath() { return path; }
    
    public void addPageInfo(PageInfo info) {
        synchronized (pageinfos) {
            pageinfos.add(info);
        }
    }
    
    public TreeSet getPageInfos() {
        synchronized (pageinfos) {
            return (TreeSet) pageinfos.clone();
        }
    }
    
    public void setModTime(long mtime) {
        modtime = mtime;
    }

    public long getModTime() {
        if (modtime == 0) {
            File doc = new File(getPath());
            if (doc.exists() && doc.isFile()) {
                setModTime(doc.lastModified());
            }
        }
        return modtime;
    }

   
    // comparable interface
    
    public int compareTo(Object inobj) {
        SharedLeaf in = (SharedLeaf) inobj;
        if (dir.compareTo(in.getDir()) != 0) {  
            return dir.compareTo(in.getDir());
        } else { 
            return path.compareTo(in.getPath());
        }
    }

}// SharedLeaf
