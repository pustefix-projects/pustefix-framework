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

import org.apache.log4j.Logger;

import de.schlund.pfixxml.IncludeDocument;

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

public class SPCacheFactory {
    private final static Logger LOG = Logger.getLogger(SPCacheFactory.class);
    private static SPCacheFactory instance= new SPCacheFactory();

    private SPCache<Object, Object> targetCache= new LRUCache<Object, Object>();
    private SPCache<String, IncludeDocument> documentCache= new LRUCache<String, IncludeDocument>();

    private static final String PROP_TARGET_CACHE_CLASS= "targetcache.cacheclass";
    private static final String PROP_TARGET_CACHE_SIZE = "targetcache.cachecapacity";

    private static final String PROP_DOCUMENT_CACHE_CLASS= "includecache.cacheclass";
    private static final String PROP_DOCUMENT_CACHE_SIZE = "includecache.cachecapacity";

    private SPCacheFactory() {}

    /**
     * Implemented from FactoryInit.
     */
    public void init(Properties props) {
        synchronized (targetCache) {
            targetCache.createCache(LRUCache.DEFAULT_SIZE);
            SPCache<Object, Object> tmp= getConfiguredCache(props, PROP_TARGET_CACHE_CLASS, PROP_TARGET_CACHE_SIZE);
            if (tmp != null) {
                targetCache= tmp;
            }
        }
        
        synchronized (documentCache) {
            documentCache.createCache(LRUCache.DEFAULT_SIZE);
            SPCache<String, IncludeDocument> tmp= getConfiguredCache(props, PROP_DOCUMENT_CACHE_CLASS, PROP_DOCUMENT_CACHE_SIZE);
            if (tmp != null) {
                documentCache= tmp;
            }
        }
        if(LOG.isInfoEnabled()) {
        	LOG.info("SPCacheFactory initialized: ");
        	LOG.info("  TargetCache   : Class="+targetCache.getClass().getName()+" Capacity=" + targetCache.getCapacity() + " Size="+targetCache.getSize());
        	LOG.info("  DocumentCache : Class="+documentCache.getClass().getName()+" Capacity=" + documentCache.getCapacity() + " Size="+documentCache.getSize());
        }
    }

    private <T1, T2> SPCache<T1, T2> getConfiguredCache(Properties props, String propNameClass, String propNameSize) {
        SPCache<T1, T2> tmp= null;
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
                    LOG.error("The property " + propNameSize + " is not an int");
                }
            } else {
                sizeError= true;
                LOG.error("The property " + propNameSize + " is null");
            }

            if (classname != null && !sizeError) {

                //LOG.warn("*** Found SPCache classname '" + classname + "' in properties!");

                tmp= getCache(classname);
                if (tmp != null)
                    tmp.createCache(cachesize);
            } else
                LOG.error("Property "+propNameClass+" is null");
            
        } else
            LOG.error("Properties for caches are null");
            
        return tmp;
    }

    @SuppressWarnings("unchecked")
    private <T1, T2> SPCache<T1, T2> getCache(String classname) {
        SPCache<T1, T2> retval= null;
        try {
            Constructor<? extends SPCache> constr= Class.forName(classname).asSubclass(SPCache.class).getConstructor((Class[]) null);
            retval= constr.newInstance((Object[]) null);
        } catch (InstantiationException e) {
            LOG.error("unable to instantiate class [" + classname + "]", e);
        } catch (IllegalAccessException e) {
            LOG.error("unable access class [" + classname + "]", e);
        } catch (ClassNotFoundException e) {
            LOG.error("unable to find class [" + classname + "]", e);
        } catch (NoSuchMethodException e) {
            LOG.error("unable to find correct method in [" + classname + "]", e);
        } catch (InvocationTargetException e) {
            LOG.error("unable to invoke correct method in [" + classname + "]", e);
        } catch (ClassCastException e) {
            LOG.error("class [" + classname + "] does not implement the interface SPCache", e);
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
    public synchronized SPCache<Object, Object> getCache() {
        synchronized (targetCache) {
            /*LOG.debug("Cache is:          "+targetCache.getClass().getName());
            LOG.debug("Cache capacity is: "+targetCache.getCapacity());
            LOG.debug("Cache size is:     "+targetCache.getSize());*/
            return targetCache;
        }
    }

    /**
     * Get the cache for include-modules.
     */
    public synchronized SPCache<String, IncludeDocument> getDocumentCache() {
        synchronized (documentCache) {
            /*LOG.debug("DocumentCache is:          "+documentCache.getClass().getName());
            LOG.debug("DocumentCache capacity is: "+documentCache.getCapacity());
            LOG.debug("DocumentCache size is:     "+documentCache.getSize());*/
            return documentCache;
        }
    }
    
    /**
     * To be used with care! If you need it, take care to throw away your old instance of SPCache retrieved 
     * through getCache() and getDocumentCache()!
     */
     public void reset() {
         targetCache= new LRUCache<Object, Object>();
         documentCache= new LRUCache<String, IncludeDocument>();
     }
    

} // SPCacheFactory
