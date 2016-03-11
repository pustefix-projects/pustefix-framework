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

package org.pustefixframework.config.contextxmlservice;

import java.util.HashSet;
import java.util.Set;

/**
 * Configuration of request parameters which should be preserved when doing
 * redirect to change page name or http scheme in URL.
 *
 */
public class PreserveParams {
    
    private Set<String> params;
    
    public PreserveParams() {
        params = new HashSet<String>();
        //add built-in default parameters
        params.add("__frame");
        //params.add("__lf");
    }
    
    public void addParam(String paramName) {
        params.add(paramName);
    }
    
    public boolean containsParam(String paramName) {
        return params.contains(paramName);
    }
    
    public void removeParam(String paramName) {
    	params.remove(paramName);
    }
    
}
