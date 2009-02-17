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
import java.util.TreeMap;

import de.schlund.pfixxml.resources.Resource;
import de.schlund.pfixxml.util.XsltVersion;

/**
 * SharedLeafFactory.java
 *
 *
 * Created: Fri Jul 13 13:31:32 2001
 *
 * @author <a href="mailto: "Jens Lautenbacher</a>
 *
 *
 */

public class SharedLeafFactory {

    private static SharedLeafFactory instance     = new SharedLeafFactory();
    private TreeMap<String, SharedLeaf> sharedleaves = new TreeMap<String, SharedLeaf>();
    
    private SharedLeafFactory() {}
    
    public static SharedLeafFactory getInstance() {
        return instance;
    }

    public synchronized SharedLeaf getSharedLeaf(XsltVersion xsltVersion,Resource path) {
        SharedLeaf ret = (SharedLeaf) sharedleaves.get(xsltVersion+":"+path);
        if (ret == null) {
            ret =  new SharedLeaf(path);
            sharedleaves.put(xsltVersion+":"+path, ret);
        }
        return ret;
    }
    
    
    public void reset() {
        sharedleaves = new TreeMap<String, SharedLeaf>();
    }

}// SharedLeafFactory
