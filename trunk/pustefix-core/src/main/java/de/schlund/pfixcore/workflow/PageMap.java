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

package de.schlund.pfixcore.workflow;

import java.util.Iterator;
import java.util.Map;

public class PageMap {

    private Map<String, ? extends State> pagemap;
    
    public void setMap(Map<String, ? extends State> map) {
        this.pagemap = map;
    }

    public State getState(PageRequest page) {
        return getState(page.getName());
    }

    public State getState(String pagename) {
        return (State) pagemap.get(pagename);
    }

    @Override
    public String toString() {
        String ret = "";
        for (Iterator<String> i = pagemap.keySet().iterator(); i.hasNext(); ) {
            if (ret.length() > 0) {
                ret += ", ";
            }
            String key = i.next();
            ret += key + " -> " + pagemap.get(key).getClass().getName();
        }
        return ret;
    }
    
}
