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

package de.schlund.pfixcore.workflow;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import de.schlund.pfixcore.beans.InitResource;
import de.schlund.pfixcore.exception.PustefixApplicationException;
import de.schlund.pfixcore.exception.PustefixCoreException;
import de.schlund.pfixcore.exception.PustefixRuntimeException;
import de.schlund.pfixxml.config.ContextConfig;
import de.schlund.pfixxml.config.ContextResourceConfig;

/**
 * Implements the ability to store objects implementing a number of interfaces
 *
 * @author jtl, thomas
 *
 */

public class ContextResourceManagerImpl implements ContextResourceManager {
    private final static Logger LOG = Logger.getLogger(ContextResourceManagerImpl.class);
    private HashMap<String, Object>  resources = new HashMap<String, Object>();
    
    /**
     * Instantiates the objects and registers the interfaces which
     * should be used from each.
     *
     * In general such an object implements a number of interfaces. You are able to specify the classes
     * you want to instantiate and to specify which interfaces you want to
     * use from such an object.
     * 
     * @throws PustefixApplicationException 
     * @throws PustefixCoreException 
     *
     */
    @SuppressWarnings("deprecation")
     public void init(Context context, ContextConfig config) throws PustefixApplicationException, PustefixCoreException {
        LOG.debug("Initializing Resources...");
        
        Collection<Object> resourcesToInitialize = new ArrayList<Object>();
        Map<String, Object> resourceClassToInstance = new HashMap<String, Object>();
        
        for (ContextResourceConfig resourceConfig : config.getContextResourceConfigs()) {
            Object cr = null;
            String classname = resourceConfig.getContextResourceClass().getName();
            try {
                LOG.debug("Creating object with name [" + classname + "]");
                cr = resourceConfig.getContextResourceClass().newInstance();
            } catch (InstantiationException e) {
                throw new PustefixRuntimeException("Exception while creating object " + classname + ":" + e);
            } catch (IllegalAccessException e) {
                throw new PustefixRuntimeException("Exception while creating object " + classname + ":" + e);
            }
            
            resourcesToInitialize.add(cr);
            resourceClassToInstance.put(cr.getClass().getName(), cr);
        }
        
        Map<Class<?>, ? extends ContextResourceConfig> interfaces = config.getInterfaceToContextResourceMap();
        for (Class<?> clazz : interfaces.keySet()) {
            String interfacename = clazz.getName();
            String resourceclass = interfaces.get(clazz).getContextResourceClass().getName();
            Object cr = resourceClassToInstance.get(resourceclass);
            checkInterface(cr, interfacename);
            LOG.debug("* Registering [" + cr.getClass().getName() + "] for interface [" + interfacename + "]");
            resources.put(interfacename, cr);
        }
        
        for (Iterator<Object> i = resourcesToInitialize.iterator(); i.hasNext();) {
            Object resource = i.next();

            if (resource instanceof ContextResource) {
                try {
                    LOG.debug("***** Resource implements ContextResource => calling init(Context) of " + resource.getClass().getName());
                    ((ContextResource) resource).init(context);
                } catch (Exception e) {
                    throw new PustefixApplicationException("Exception while initializing context resource " + resource.getClass(), e);
                }
            } else {
                for (Method m : resource.getClass().getMethods()) {
                    if (m.isAnnotationPresent(InitResource.class)) {
                        try {
                            Class<?>[] params = m.getParameterTypes();
                            if (params.length == 0) {
                                LOG.debug("***** Found @InitResource for " + m.getName() + "() of " + resource.getClass().getName());
                                m.invoke(resource, new Object[]{});
                            } else if (params.length == 1 && (params[0].isAssignableFrom(Context.class))) {
                                LOG.debug("***** Found @InitResource for " + m.getName() + "(Context) of " + resource.getClass().getName());
                                m.invoke(resource, context);
                            } else {
                                throw new PustefixApplicationException("Annotated '@InitResource' method must either take " + 
                                        "no parameters or only one of type 'Context'or a superclass of it.");
                            } 
                        } catch (Exception e) {
                            throw new PustefixApplicationException("Exception while initializing context resource " + resource.getClass(), e);
                        }
                        break;
                    }
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixcore.workflow.ContextResourceManager#getResource(java.lang.String)
     */
    public Object getResource(String name) {
        return  resources.get(name);
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixcore.workflow.ContextResourceManager#getResource(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public <T> T getResource(Class<T> clazz) {
        return (T) resources.get(clazz.getName());
    }
    
    private void checkInterface(Object obj, String interfacename) throws PustefixCoreException {
        Class<?> wantedinterface = null;
        
        // Get the class of the requested interface and get all
        // implemented interfaces of the object
        try {
            wantedinterface = Class.forName(interfacename) ;
        } catch (ClassNotFoundException e) {
            throw new PustefixRuntimeException("Got ClassNotFoundException for classname " +  interfacename + "while checking for interface");
        }
	
        LOG.debug("Check if requested interface [" + interfacename + 
                  "] is implemented by [" + obj.getClass().getName() + "]");
        
        // Check for all implemented interfaces, if it equals the interface that
        // we want, than break.
        
        if (wantedinterface.isInstance(obj)) {
            LOG.debug("Got requested interface " + interfacename);
        } else {
            // Uh, the requested interface is not implemented by the
            // object, that's not nice!
            throw new PustefixCoreException("The class [" + obj.getClass().getName() +
                                       "] doesn't implemented requested interface " +
                                       interfacename);
        }
        
        // Now check if the interface is already registered...
        if (resources.containsKey(interfacename)) {
            throw new PustefixCoreException("Interface [" + interfacename +
                                       "] already registered for instance of [" +
                                       resources.get(interfacename).getClass().getName() + "]");
        }
    }
    
    /* (non-Javadoc)
     * @see de.schlund.pfixcore.workflow.ContextResourceManager#getResourceIterator()
     */
    public Iterator<Object> getResourceIterator() {
        return  resources.values().iterator();
    }
    
}
