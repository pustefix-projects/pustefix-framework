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

    private        Category CAT = Category.getInstance(this.getClass());
    private static PropertyObjectManager instance=new PropertyObjectManager();
    private        Map propMaps;
    private        Map confMaps;
    
    /**Returns PropertyObjectManager instance.*/
    public static PropertyObjectManager getInstance() {
        return instance;
    }
    
    /**Constructor.*/
    PropertyObjectManager() {
        propMaps = new WeakHashMap();
        confMaps = new WeakHashMap();
    }
    
    /**Returns PropertyObject according to Properties and Class parameters. If it doesn't already exist, it will be created.*/
    public PropertyObject getPropertyObject(Properties props,String className) throws Exception {
        return getPropertyObject(props,Class.forName(className));
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
                    CAT.warn("******* Creating new ConfigurableObject " + objClass.getName());
                    confObj = (ConfigurableObject) objClass.newInstance();
                    confObj.init(config);
                    confObjs.put(objClass, confObj);
                }
            }
        }
        return confObj;
    }
    
    /**Returns PropertyObject according to Properties and Class parameters. If it doesn't already exist, it will be created.*/
    public PropertyObject getPropertyObject(Properties props,Class propObjClass) throws Exception {
        HashMap        propObjs = null;
        PropertyObject propObj  = null;
        
        propObjs = (HashMap) propMaps.get(props);
        if (propObjs == null) {
            synchronized (propMaps) {
                propObjs = (HashMap) propMaps.get(props);
                if (propObjs == null) {
                    propObjs = new HashMap();
                    propMaps.put(props,propObjs);
                }
            }
        }

        propObj = (PropertyObject) propObjs.get(propObjClass);
        if (propObj == null) {
            synchronized (propObjs) {
                propObj = (PropertyObject) propObjs.get(propObjClass);
                if (propObj == null) {
                    CAT.warn("******* Creating new PropertyObject " + propObjClass.getName());
                    propObj = (PropertyObject) propObjClass.newInstance();
                    propObj.init(props);
                    propObjs.put(propObjClass,propObj);
                }
            }
        }
        return propObj;

        /*
        synchronized (propMaps) {
            propObjs = (HashMap) propMaps.get(props);
            if (propObjs == null) {
                propObjs = new HashMap();
                propMaps.put(props,propObjs);
            }
        }
        synchronized (propObjs) {
            PropertyObject propObj = (PropertyObject) propObjs.get(propObjClass);
            if (propObj == null) {
            CAT.warn("******* Creating new PropertyObject " + propObjClass.getName());
                propObj = (PropertyObject) propObjClass.newInstance();
                propObj.init(props);
                propObjs.put(propObjClass,propObj);
            }
            return propObj;
         }
        */
    }
    
    /**Removes PropertyObjects for Properties.They are newly created on demand, i.e. as a result of subsequent getPropertyObject calls.*/
    public void resetPropertyObjects(Properties props) {
        synchronized (propMaps) {
            propMaps.remove(props);
        }
    }
}
