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

import de.schlund.pfixxml.*;
import de.schlund.pfixcore.util.*;
import java.lang.*;
import java.lang.reflect.*;
import java.util.*;
import javax.servlet.*;
import org.apache.log4j.*;

/**
 * Implements the ability to store objects implementing a number of interfaces extending
 * the ContextResource interface
 *
 * @author jtl, thomas
 *
 *
 */

public class ContextResourceManager {
    private Category CAT = Category.getInstance(ContextResourceManager.class.getName());
    private HashMap  resources = new HashMap();

    private final static String PROP_RESOURCE = "context.resource";
    private final static String SEPERATOR     = ", ";
    
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
     * An <b>wrong</b> example:<br>
     * <code>context.rescoure.1.de.foo.FooImpl=Foo</code><br>
     * <code>context.rescoure.2.de.foo.FooAndBarAndBazImpl=Foo,Bar,Baz</code>
     *
     * This example, written as above, would be invalid as no two ContextRessources
     * are allowed to act as an implementation for the same interface(Foo in this case).
     * Note that the classes may implement the same interface, they are just not allowed to act as
     * an implementation for the same interface in a ContextRessource declaration.
     *
     * The <b>correct</b> example could be:<br>
     * <code>context.rescoure.1.de.foo.FooImpl=Foo</code><br>
     * <code>context.rescoure.2.de.foo.FooAndBarAndBazImpl=Bar,Baz</code>
     *
     * which is correct without any change in the code of the implementing classes.
     *
     * @param props a <code>Properties</code> value
     * @exception ServletException if an error occurs
     */
    public void init(Context context) throws Exception{
        CAT.debug("initialize ContextResources...");
	// CAT.debug("Properties:\n" + context.getProperties());

	// Getting all Properties beginning with PROP_RESOURCE
	TreeMap cr_create = PropertiesUtils.selectPropertiesSorted(context.getProperties(), PROP_RESOURCE);
        HashSet cr_init   = new HashSet();
	// Hope, I got properties
	if (cr_create != null && !cr_create.isEmpty()) {
	    // For each property...
	    for (Iterator i = cr_create.keySet().iterator(); i.hasNext(); ) {
		// Get the classname and create a tokenizer to traverse the
		// list of interfaces
		String resourcename   = (String) i.next();
		int    classnameIndex = resourcename.indexOf(".") + 1;
		if (classnameIndex == -1) {
		    throw new ServletException("Not the needed format for " +
					       " ContextResource-Property !");
		}
		String          classname     = resourcename.substring(classnameIndex);  
		String          interfacelist = (String) cr_create.get(resourcename);
		StringTokenizer tokenizer     = new StringTokenizer(interfacelist, SEPERATOR); 
		ContextResource cr            = null;

		// Now create an object of the requested class 
		try {
		    CAT.debug("Creating object with name [" + classname + "]");
		    cr = (ContextResource) Class.forName(classname).newInstance();
		} catch (Exception e) {
		    throw new ServletException("Exception while creating object " +
					       classname + ":" + e);
		}

		// initialize it...
		// cr.init(context);
                cr_init.add(cr);
                
		if (tokenizer.countTokens() == 0) {
		    throw new ServletException("No interfaces given for object of class [" + classname + "]"); 
		}
		
		while (tokenizer.hasMoreTokens()) {
		    String interfacename = tokenizer.nextToken().trim();
		    checkInterface(cr, interfacename);
		    CAT.debug("* Registering [" + classname + "] for interface [" + interfacename + "]");
		    resources.put(interfacename, cr);
		}
	    }
            for (Iterator i = cr_init.iterator(); i.hasNext(); ) {
                ContextResource cr = (ContextResource) i.next();
                cr.init(context);
            }
	} else {
	    CAT.debug("No Properties with prefix " + PROP_RESOURCE + " found! ");
	}
        
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
    
    private void checkInterface(Object obj, String interfacename) throws ServletException {
	Class   wantedinterface       = null;
	Class[] implementedinterfaces = null;

	// Get the class of the requested interface and get all
	// implemented interfaces of the object
	try {
	    wantedinterface       = Class.forName(interfacename) ;
	    implementedinterfaces = obj.getClass().getInterfaces();
	} catch (ClassNotFoundException e) {
	    throw new ServletException("Got ClassNotFoundException for classname " +  interfacename +
				       "while checking for interface");
	}
	
	CAT.debug("Check if requested interface [" + interfacename + 
		  "] is implemented by [" + obj.getClass().getName() + "]");
	boolean gotcha = false;
        
	// Check for all implemented interfaces, if it equals the interface that
	// we want, than break.
	for (int i = 0; i < implementedinterfaces.length; i++) {
	    if (implementedinterfaces[i].equals(wantedinterface)) {
		CAT.debug("Got requested interface " + interfacename + "! Bingo!");
		gotcha = true;
		break;
	    } else {
		CAT.debug("Got interface [" + implementedinterfaces[i].getName() + "], this doesn't match");
	    }
	}
        
	if (!gotcha) {
	    // Uh, the requested interface is not implemented by the
	    // object, that's not nice!
	    throw new ServletException("The class [" + obj.getClass().getName() +
				       "] doesn't implemented requested interface " +
				       interfacename);
	}
        
	// Now check if the interface is already registered...
	if (resources.containsKey(interfacename)) {
	    throw new ServletException("Interface [" + interfacename +
				       "] already registered for instance of [" +
				       resources.get(interfacename).getClass().getName()
				       + "]");
	}
    }
    
    /**
     * Returns an iterator for all stored objects.
     *
     * @return the <code>Iterator</code>
     */
    protected Iterator getResourceIterator() {
	return  resources.values().iterator();
    }
}
