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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import de.schlund.pfixcore.exception.PustefixApplicationException;
import de.schlund.pfixcore.exception.PustefixCoreException;
import de.schlund.pfixcore.exception.PustefixRuntimeException;
import de.schlund.pfixxml.config.ContextConfig;
import de.schlund.pfixxml.config.ContextResourceConfig;
import de.schlund.pfixxml.loader.AppLoader;
import de.schlund.pfixxml.loader.Reloader;
import de.schlund.pfixxml.loader.StateTransfer;

/**
 * Implements the ability to store objects implementing a number of interfaces extending
 * the ContextResource interface
 *
 * @author jtl, thomas
 *
 */

public class ContextResourceManager implements Reloader {
    private final static Logger LOG = Logger.getLogger(ContextResourceManager.class);
    private HashMap<String, ContextResource>  resources = new HashMap<String, ContextResource>();
    
    /**
     * Instanciates the objects and registers the interfaces which
     * should be used from each.
     *
     * In general such an object implements a number of interfaces extending
     * the ContextResource interface. You are able to specify the classes
     * you want to instanciate and to specify which interfaces you want to
     * use from such an object.
     * 
     * The configuration is done by passing properties, each object you want
     * to use must be specified in a single property.
     * <br>
     * The name of this property consists of the prefix
     * <code>context.resource.[A_NUMBER].</code> followed by the
     * full qualified classname of the class.
     * <br>
     * The value of the property specifies the interfaces you want to use
     * from this object. All interfaces are declared by the full qualified
     * classname of the interface and separated by a comma. 
     * <br>
     * An wrong example:<br>
     * <code>context.rescoure.1.de.foo.FooImpl             = Foo</code><br>
     * <code>context.rescoure.2.de.foo.FooAndBarAndBazImpl = Foo,Bar,Baz</code>
     *
     * This example, written as above, would be invalid as no two ContextRessources
     * are allowed to act as an implementation for the same interface(Foo in this case).
     * Note that the classes may implement the same interface, they are just not allowed to act as
     * an implementation for the same interface in a ContextRessource declaration.
     *
     * The correct example could be:<br>
     * <code>context.rescoure.1.de.foo.FooImpl             = Foo</code><br>
     * <code>context.rescoure.2.de.foo.FooAndBarAndBazImpl = Bar,Baz</code>
     *
     * which is correct without any change in the code of the implementing classes.
     * @throws PustefixApplicationException 
     * @throws PustefixCoreException 
     *
     */
    
     public void init(Context context, ContextConfig config) throws PustefixApplicationException, PustefixCoreException {
        LOG.debug("initialize ContextResources...");
        
        Collection<ContextResource> resourcesToInitialize = new ArrayList<ContextResource>();
        Map<String, ContextResource> resourceClassToInstance = new HashMap<String, ContextResource>();
        
        Collection<ContextResourceConfig> resourceConfigs = config.getContextResourceConfigs();
        
        for (Iterator<ContextResourceConfig> i = resourceConfigs.iterator(); i.hasNext();) {
            ContextResourceConfig resourceConfig = i.next();
            ContextResource cr = null;
            String classname = resourceConfig.getContextResourceClass().getName();
            try {
                LOG.debug("Creating object with name [" + classname + "]");
                AppLoader appLoader = AppLoader.getInstance();
                if (appLoader.isEnabled()) {
                    cr = (ContextResource) appLoader.loadClass(classname).newInstance();
                } else {
                    cr = (ContextResource) resourceConfig.getContextResourceClass().newInstance();
                }
            } catch (InstantiationException e) {
                throw new PustefixRuntimeException("Exception while creating object " + classname + ":" + e);
            } catch (IllegalAccessException e) {
                throw new PustefixRuntimeException("Exception while creating object " + classname + ":" + e);
            } catch (ClassNotFoundException e) {
                throw new PustefixRuntimeException("Exception while creating object " + classname + ":" + e);
            }
            
            resourcesToInitialize.add(cr);
            resourceClassToInstance.put(cr.getClass().getName(), cr);
        }
        
        Map<Class, ContextResourceConfig> interfaces = config.getInterfaceToContextResourceMap();
        for (Class clazz : interfaces.keySet()) {
            String interfacename = clazz.getName();
            String resourceclass = interfaces.get(clazz).getContextResourceClass().getName();
            ContextResource cr = resourceClassToInstance.get(resourceclass);
            checkInterface(cr, interfacename);
            LOG.debug("* Registering [" + cr.getClass().getName() + "] for interface [" + interfacename + "]");
            resources.put(interfacename, cr);
        }
        
        for (Iterator i = resourcesToInitialize.iterator(); i.hasNext();) {
            ContextResource resource = (ContextResource) i.next();
            try {
                resource.init(context);
            } catch (Exception e) {
                throw new PustefixApplicationException("Exception while initializing context resource " + resource.getClass(), e);
            }
        }
        
        AppLoader appLoader = AppLoader.getInstance();
        if (appLoader.isEnabled()) appLoader.addReloader(this);   
    }

    /**
     * Returns the stored object which implements the interface,
     * specified by the full qualified classname of the requested interface, or
     * null, if no object for the interface name is found. 
     *
     * @param name the classname of the requested interface
     * @return an object of a class implementing the requested interface, which
               extends <code>ContextResource</code>
     */
    public ContextResource getResource(String name) {
        return  (ContextResource) resources.get(name);
    }

    private void checkInterface(Object obj, String interfacename) throws PustefixCoreException {
        Class wantedinterface = null;
        
        // Get the class of the requested interface and get all
        // implemented interfaces of the object
        try {
            AppLoader appLoader = AppLoader.getInstance();
            if (appLoader.isEnabled()) {
                wantedinterface = appLoader.loadClass(interfacename);
            } else {
                wantedinterface = Class.forName(interfacename) ;
            }
        } catch (ClassNotFoundException e) {
            throw new PustefixRuntimeException("Got ClassNotFoundException for classname " +  interfacename +
                                       "while checking for interface");
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
    
    /**
     * Returns an iterator for all stored objects.
     *
     * @return the <code>Iterator</code>
     */
    public Iterator getResourceIterator() {
        return  resources.values().iterator();
    }
    
    public void reload() {
        HashMap<String, ContextResource>  resNew = new HashMap<String, ContextResource>();
        Iterator it     = resources.keySet().iterator();
        while (it.hasNext()) {
            String str = (String) it.next();
            ContextResource crOld = (ContextResource)resources.get(str);
            ContextResource crNew = (ContextResource)StateTransfer.getInstance().transfer(crOld);
            resNew.put(str,crNew);
        }
        resources = resNew;
    }
}
