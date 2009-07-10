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
package org.pustefixframework.http.internal;

import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

import de.schlund.pfixcore.exception.PustefixCoreException;
import de.schlund.pfixxml.config.GlobalConfig;
import de.schlund.pfixxml.config.GlobalConfigurator;

/**
 * This Servlet is just there to have it's init method called on startup of the
 * VM. It starts all VM-global factories by calling their 'init' method from the
 * {@link FactoryInit} interface. These factories are located by analyzing the
 * "servlet.propfile" parameter which points to a file where all factories are
 * listed.
 */
public class PustefixInit {

    /**
     * 
     */
    private static final long serialVersionUID = 3072991705791635451L;

    // ~ Instance/static variables
    // ..................................................................

    private final static Logger LOG = Logger.getLogger(PustefixInit.class);
    
    private static long log4jmtime = -1;
    private static boolean warMode = false;
    
    public static void init(ServletContext servletContext) throws PustefixCoreException {
        
    	Properties properties = new Properties(System.getProperties());
            
    	// old webapps specify docroot -- true webapps don't
    	String docrootstr = servletContext.getInitParameter("pustefix.docroot");
    	if (docrootstr == null || docrootstr.equals("")) {
    		docrootstr = servletContext.getRealPath("/");
    		if (docrootstr == null) {
    			warMode = true;
    		}
    	}
    
    	// Setup global configuration before doing anything else
    	if (docrootstr != null) {
    		if (!docrootstr.equals(GlobalConfig.getDocroot())) {
    			GlobalConfigurator.setDocroot(docrootstr);
    		}
    	}
    	if (warMode) {
    		GlobalConfigurator.setServletContext(servletContext);
    	}
            
    	if (docrootstr != null) {
    		// this is for stuff that can't use the PathFactory. Should not be used
    		// when possible...
    		properties.setProperty("pustefix.docroot", docrootstr);
    	}

    }

}
