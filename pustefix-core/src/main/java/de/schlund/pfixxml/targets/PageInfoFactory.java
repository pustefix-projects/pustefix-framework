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
import java.util.TreeMap;


/**
 * PageInfoFactory.java
 *
 *
 * Created: Mon Jul 23 19:23:34 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class PageInfoFactory {
    private static PageInfoFactory instance = new PageInfoFactory(); 
    private TreeMap<String, PageInfo> pagemap = new TreeMap<String, PageInfo>();
    
    private PageInfoFactory (){}

    public static PageInfoFactory getInstance() {
        return instance;
    }

    public PageInfo getPage(TargetGenerator gen, String name, String variant) {
        // System.out.println(name + " :: " + variant);
        String   key = gen.getName() + "@" + name + "::" + variant;
        PageInfo ret = (PageInfo) pagemap.get(key);
        if (ret == null) {
            ret = new PageInfo(gen, name, variant);
            pagemap.put(key, ret);
        }
        return ret;
    }
    
    
    public void reset() {
        pagemap = new TreeMap<String, PageInfo>();
    }
    
}// PageInfoFactory
