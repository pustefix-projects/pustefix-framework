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

package de.schlund.pfixcore.webservice.beans.metadata;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mleidig@schlund.de
 */
public class Beans {

    Map<String,Bean> beans;
    
    public Beans() {
        beans=new HashMap<String,Bean>();
    }
    
    public Bean getBean(String className) {
        return beans.get(className);
    }
    
    public void setBean(Bean bean) {
        beans.put(bean.getClassName(),bean);
    }
    
}
