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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.apache.log4j.Category;

import de.schlund.util.FactoryInit;

/**
 * SPCacheFactory.java
 *
 *
 * Created: Mon Jul 23 17:06:56 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a> 
 * @author <a href="mailto:haecker@schlund.de">Joerg Haecker</a>      
 *  
 * This class realises the factory and the singleton pattern and implements the {@link FactoryInit}
 * interface. It is responsible to create and store the caches uses by the PUSTEFIX system. 
 * Currently PUSTEFIX uses one cache for the targets and one for the include-modules. The
 * properties of the caches are passed via the init-method of the FactoryInit-interface. 
 * If the propertries can't be interpreted correctly or the init-method is not called,
 * {@link LRUCache} initialised with default values will be returned.
 *
 */

public class SPCacheFactory implements FactoryInit {
    private static Category CAT= Category.getInstance(SPCacheFactory.class.getName());
    private static SPCacheFactory instance= new SPCacheFactory();

    private static SPCache targetCache= new LRUCache();
    private static SPCache documentCache= new LRUCache();

    private static final String PROP_TARGET_CACHE_CLASS= "targetcache.cacheclass";
    private static final String PROP_TARGET_CACHE_SIZE = "targetcache.cachecapacity";

    private static final String PROP_DOCUMENT_CACHE_CLASS= "includecache.cacheclass";
    private static final String PROP_DOCUMENT_CACHE_SIZE = "includecache.cachecapacity";

    private SPCacheFactory() {};

    /**
     * Implemented from FactoryInit.
     */
    public void init(Properties props) {
        synchronized (targetCache) {
            targetCache.createCache(LRUCache.DEFAULT_SIZE);
            SPCache tmp= getConfiguredCache(props, PROP_TARGET_CACHE_CLASS, PROP_TARGET_CACHE_SIZE);
            if (tmp != null) {
                targetCache= tmp;
            }
        }

        synchronized (documentCache) {
            documentCache.createCache(LRUCache.DEFAULT_SIZE);
            SPCache tmp= getConfiguredCache(props, PROP_DOCUMENT_CACHE_CLASS, PROP_DOCUMENT_CACHE_SIZE);
            if (tmp != null) {
                documentCache= tmp;
            }
        }
        if(CAT.isInfoEnabled()) {
        	CAT.info("SPCacheFactory initialized: ");
        	CAT.info("  TargetCache   : Class="+targetCache.getClass().getName()+" Capacity=" + targetCache.getCapacity() + " Size="+targetCache.getSize());
        	CAT.info("  DocumentCache : Class="+documentCache.getClass().getName()+" Capacity=" + documentCache.getCapacity() + " Size="+documentCache.getSize());
        }
    }

    private SPCache getConfiguredCache(Properties props, String propNameClass, String propNameSize) {
        SPCache tmp= null;
        if (props != null) {
            String classname= props.getProperty(propNameClass);
            String csize= props.getProperty(propNameSize);
            boolean sizeError= false;
            int cachesize= 0;

            if (csize != null) {
                try {
                    cachesize= Integer.parseInt(csize);
                } catch (NumberFormatException e) {
                    sizeError= true;
                    CAT.error("The property " + propNameSize + " is not an int");
                }
            } else {
                sizeError= true;
                CAT.error("The property " + propNameSize + " is null");
            }

            if (classname != null && !sizeError) {

                //CAT.warn("*** Found SPCache classname '" + classname + "' in properties!");

                tmp= getCache(classname);
                if (tmp != null)
                    tmp.createCache(cachesize);
            } else
                CAT.error("Property "+propNameClass+" is null");
            
        } else
            CAT.error("Properties for caches are null");
            
        return tmp;
    }

    private SPCache getCache(String classname) {
        SPCache retval= null;
        try {
            Constructor constr= Class.forName(classname).getConstructor(null);
            retval= (SPCache) constr.newInstance(null);
        } catch (InstantiationException e) {
            CAT.error("unable to instantiate class [" + classname + "]", e);
        } catch (IllegalAccessException e) {
            CAT.error("unable access class [" + classname + "]", e);
        } catch (ClassNotFoundException e) {
            CAT.error("unable to find class [" + classname + "]", e);
        } catch (NoSuchMethodException e) {
            CAT.error("unable to find correct method in [" + classname + "]", e);
        } catch (InvocationTargetException e) {
            CAT.error("unable to invoke correct method in [" + classname + "]", e);
        } catch (ClassCastException e) {
            CAT.error("class [" + classname + "] does not implement the interface IHandler", e);
        }
        return retval;
    }

    /**
     * The getInstance method of a singleton.
     */
    public static SPCacheFactory getInstance() {
        return instance;
    }

    /**
     * Get the cache for targets.
     */
    public synchronized SPCache getCache() {
        synchronized (targetCache) {
            /*CAT.debug("Cache is:          "+targetCache.getClass().getName());
            CAT.debug("Cache capacity is: "+targetCache.getCapacity());
            CAT.debug("Cache size is:     "+targetCache.getSize());*/
            return targetCache;
        }
    }

    /**
     * Get the cache for include-modules.
     */
    public synchronized SPCache getDocumentCache() {
        synchronized (documentCache) {
            /*CAT.debug("DocumentCache is:          "+documentCache.getClass().getName());
            CAT.debug("DocumentCache capacity is: "+documentCache.getCapacity());
            CAT.debug("DocumentCache size is:     "+documentCache.getSize());*/
            return documentCache;
        }
    }

} // SPCacheFactory
