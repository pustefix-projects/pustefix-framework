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

package de.schlund.pfixxml;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.ThrowableInformation;

import de.schlund.pfixcore.util.PropertiesUtils;

public class FactoryInitUtil {
    private final static Logger LOG = Logger.getLogger(FactoryInitUtil.class);
    
    private static boolean initialized = false;
    
    public static boolean isInitialized() {
        return initialized;
    }
    
    public static synchronized void initialize(Properties properties) throws FactoryInitException {
        if (initialized) {
            return;
        }
        
        HashMap<String, String> factoryProps = PropertiesUtils.selectProperties(properties,
                "factory.initialize");
        if (factoryProps != null) {
            // sort key to initialize the factories in defined order
            TreeSet<String> keys = new TreeSet<String>(factoryProps.keySet());
            for (Iterator<String> i = keys.iterator(); i.hasNext();) {
                String key = i.next();
                String factoryClassName = factoryProps.get(key);
                try {
                    LOG.debug(">>>> Init key: [" + key + "] class: [" + factoryClassName + "] <<<<");
                    long start = 0;
                    long stop = 0;
                    Class<?> clazz = Class.forName(factoryClassName);
                    Method meth = clazz.getMethod("getInstance");
                    if(!Modifier.isStatic(meth.getModifiers())) throw new Exception("Method 'getInstance()' of "+
                            "class '"+clazz.getName()+"' isn't static.");
                    Object factory = meth.invoke(null, (Object[])null );
                    LOG.debug("     Object ID: " + factory);
                    start = System.currentTimeMillis();
                    clazz.getMethod("init", new Class[] { Properties.class }).invoke(factory, new Object[] { properties });
                    stop = System.currentTimeMillis();
                    LOG.debug("Init of " + factory + " took " + (stop - start) + " ms");
                } catch (Exception e) {
                    LOG.error(e.toString());
                    
                    Throwable cause = e;
                    if (e instanceof InvocationTargetException && e.getCause() != null) 
                        cause=e.getCause();
                    
                    FactoryInitException initException = new FactoryInitException(factoryClassName, cause);

                    ThrowableInformation info = new ThrowableInformation(e);
                    String[] trace = info.getThrowableStrRep();
                    StringBuffer strerror = new StringBuffer();
                    for (int ii = 0; ii < trace.length; ii++) {
                        strerror.append("->" + trace[ii] + "\n");
                    }
                    LOG.error(strerror.toString());
                    
                    throw initException;
                }
            }
        }
        
        initialized = true;
    }
}
