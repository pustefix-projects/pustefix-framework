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

package de.schlund.util.statuscodes;

import de.schlund.pfixcore.util.PropertiesUtils;
import de.schlund.util.FactoryInit;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import org.apache.log4j.Category;

/**
 * factory object that produces StatusCode objects for all framework classes
 *
 * @author Wolfram M�ller, jtl
 */

public class StatusCodeFactory implements FactoryInit {
    private final static String            PROP_SCFILE = "statuscodefactory.propertyfile";
    private final static String            PROP_SCODE  = "statuscodefactory.statuscode";
    private final static Category          LOG         = Category.getInstance(StatusCodeFactory.class.getName());
    private final static HashMap           allscodes   = new HashMap();
    private final static HashSet           propurls    = new HashSet();
    private final static StatusCodeFactory instance    = new StatusCodeFactory();
    private final static Object            LOCK        = new Object();
    
    private final String localdomain;

    public  static StatusCodeFactory getInstance() {
        return instance;
    }
    
    public HashMap getAllSCodes() {
        synchronized (LOCK) {
            return new HashMap(allscodes);
        }
    }
    
    public void init (Properties props) throws Exception {
        LOG.debug( ">>> StatusCodeFactory: initializing ..." );
        HashMap propfiles = PropertiesUtils.selectProperties(props,  PROP_SCFILE);
        for (Iterator i = propfiles.values().iterator(); i.hasNext(); ) {
            String name = (String) i.next();
            URL    url  = null;
            if (name.startsWith("/")) {
                url = new URL("file:" + name);
            } else {
                url = getClass().getClassLoader().getResource(name);
            }
            if (url != null) {
                LOG.debug("    Loading '" + name + "' from URL '" + url + "'");
                addSCResource(url);
            } else {
                LOG.warn("*** Couldn't find system resource for name '" + name + "'");
            }
        }
    }

    public static void addSCResource(URL url) throws Exception {
        synchronized (LOCK) {
            if (propurls.contains(url)) {
                LOG.debug("*** Already loaded resource from url '" + url + "'");
                return;
            }

            Properties props = new Properties();
            try {
                props.load(url.openStream());
            } catch (Exception e) {
                LOG.error(">> Error << ",e);
                throw e;
            }
        
            HashMap scprops = PropertiesUtils.selectProperties(props, PROP_SCODE);
            for (Iterator i = scprops.keySet().iterator(); i.hasNext(); ) {
                String fullsc = (String) i.next();
                String defmsg = (String) scprops.get(fullsc);
                String scode  = ""; // StatusCode ohne Domain
                String domain = ""; // StatusCodeDomain ohne letzten "."
                
                if (fullsc.indexOf(".") >= 0) {
                    domain = fullsc.substring(0, fullsc.lastIndexOf("."));
                    scode  = fullsc.substring(fullsc.lastIndexOf(".") + 1);
                } else {
                    domain = "";  
                    scode = fullsc;
                }
                synchronized (allscodes) {
                    if (!allscodes.containsKey(fullsc)) {
                        StatusCode sc = new StatusCode(domain, scode, defmsg);
                        allscodes.put(fullsc, sc);
                    } else {
                        throw new RuntimeException("Duplicate status code identifier [" + fullsc +"]");
                    }
                }
            }
            propurls.add(url);
        }
    }

    /**
     * Checks, if a StatusCode exists.
     *
     * @param code name of the requested StatusCode full qualified with it's domain 
     * @exception StatusCodeException if domain is not set with setLocalDomain()
     */
    public boolean statusCodeExists(String code) throws StatusCodeException {
        synchronized (allscodes) {
            return allscodes.containsKey(code);
	    }
    }
    
    /**
     * gets an StatusCode in the defined domain 
     * @param code name of the requested StatusCode without domain name
     * @exception StatusCodeException if StatusCode can't be found  
     */
    public StatusCode getStatusCode(String code) throws StatusCodeException {
        String name;
        StatusCode scode;
        
        if (localdomain.equals("")) { 
            name = code;
        } else {
            name = localdomain + "." + code;
        }
        synchronized (LOCK) {
            scode = (StatusCode) allscodes.get(name);
        }
        
        if (scode == null) {
            throw new StatusCodeException("StatusCodeFactory, StatusCode [" + name + "] not defined");
        }
        
        return scode;
    }

    public StatusCodeFactory() {
        this("");
    }

    public StatusCodeFactory(String localdomain) {
        this.localdomain = localdomain;
    }
}
