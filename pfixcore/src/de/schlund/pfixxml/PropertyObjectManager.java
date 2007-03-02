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

package de.schlund.pfixxml;

import java.util.*;
import org.apache.log4j.*;

/**
 * This class manages shared objects, which are build from properties. 
 * 
 * @author mleidig
 */
public class PropertyObjectManager {

    private        Logger LOG = Logger.getLogger(this.getClass());
    private static PropertyObjectManager instance=new PropertyObjectManager();
    private        Map confMaps;
    
    /**Returns PropertyObjectManager instance.*/
    public static PropertyObjectManager getInstance() {
        return instance;
    }
    
    /**Constructor.*/
    PropertyObjectManager() {
        confMaps = new WeakHashMap();
    }
    
    public ConfigurableObject getConfigurableObject(Object config, String className) throws Exception {
        return getConfigurableObject(config, Class.forName(className));
    }
    
    public ConfigurableObject getConfigurableObject(Object config, Class objClass) throws Exception {
        HashMap        confObjs = null;
        ConfigurableObject confObj = null;
        
        confObjs = (HashMap) confMaps.get(config);
        if (confObjs == null) {
            synchronized (confMaps) {
                confObjs = (HashMap) confMaps.get(config);
                if (confObjs == null) {
                    confObjs = new HashMap();
                    confMaps.put(config, confObjs);
                }
            }
        }

        confObj = (ConfigurableObject) confObjs.get(objClass);
        if (confObj == null) {
            synchronized (confObjs) {
                confObj = (ConfigurableObject) confObjs.get(objClass);
                if (confObj == null) {
                    LOG.warn("******* Creating new ConfigurableObject " + objClass.getName());
                    confObj = (ConfigurableObject) objClass.newInstance();
                    confObj.init(config);
                    confObjs.put(objClass, confObj);
                }
            }
        }
        return confObj;
    }
}
