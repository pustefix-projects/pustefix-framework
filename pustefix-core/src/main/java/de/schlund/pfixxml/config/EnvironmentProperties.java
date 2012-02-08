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

package de.schlund.pfixxml.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Provides easy access to environment dependent properties
 */
public class EnvironmentProperties extends Properties {
    
    private static final long serialVersionUID = -8915762987670154888L;
    
    public static final Set<String> AUTODETECT_PROPERTIES = new HashSet<String>();

    static {
        AUTODETECT_PROPERTIES.add("fqdn");
        AUTODETECT_PROPERTIES.add("machine");
        AUTODETECT_PROPERTIES.add("mode");
        AUTODETECT_PROPERTIES.add("uid");
        AUTODETECT_PROPERTIES.add("logroot");
    }
    
    private static Properties props = new EnvironmentProperties();

    public EnvironmentProperties() {
        super(System.getProperties());
    }
    
    public EnvironmentProperties(Properties props) {
        super(props);
    }
    
    @Override
    public String getProperty(String name) {
        String value = super.getProperty(name);
        if(value == null && AUTODETECT_PROPERTIES.contains(name)) {
            if(name.equals("fqdn")) {
                value = getFQDN();
            } else if(name.equals("machine")) {
                value = getMachine();
            } else if(name.equals("mode")) {
                value = getMode();
            } else if(name.equals("uid")) {
                value = getUID();
            } else if(name.equals("logroot")) {
                value = getLogroot();
            }
            setProperty(name, value);
        }
        return value;
    }
    
    public static Properties getProperties() {
        return props;
    }

    public static void setProperties(Properties p) {
        props = new EnvironmentProperties(p);
    }
    
    private static String getFQDN() {
        String fqdn = null;
        try {
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            fqdn = InetAddress.getLocalHost().getCanonicalHostName();
            if(fqdn.equals(hostAddress)) fqdn = InetAddress.getLocalHost().getHostName();
            if(!fqdn.equals(hostAddress)) {
                int ind = fqdn.indexOf('.');
                if(ind > -1) fqdn = fqdn.substring(ind);
                fqdn = getMachine() + fqdn;
            }
        } catch(UnknownHostException x) {
            throw new RuntimeException("Error getting FQDN", x);
        }
        return fqdn;
    }
    
    private static String getMachine() {
        String machine = System.getenv("MACHINE");
        if(machine == null || machine.trim().equals("")) {
            try {
                String hostAddress = InetAddress.getLocalHost().getHostAddress();
                machine = InetAddress.getLocalHost().getHostName();
                if(machine.equals(hostAddress)) machine = InetAddress.getLocalHost().getCanonicalHostName();
                if(!machine.equals(hostAddress)) {
                    int ind = machine.indexOf('.');
                    if(ind > -1) machine = machine.substring(0, ind);
                }
            } catch(UnknownHostException x) {
                throw new RuntimeException("Error getting machine name", x);
            }
        }
        if(machine == null) throw new RuntimeException("Can't get machine name");
        return machine;
    }
    
    private static String getMode() {
        String mode = System.getenv("MAKE_MODE");
        if(mode == null || mode.trim().equals("")) mode = "prod";
        return mode;
    }
    
    private static String getUID() {
        String uid = System.getProperty("user.name");
        if(uid == null || uid.trim().equals("")) uid = "servlet";
        return uid;
    }
    
    private static String getLogroot() {
        String logroot = System.getProperty("catalina.base");
        if(logroot == null) {
            logroot = "applogs";
        } else {
            logroot = logroot.trim();
            if(!logroot.endsWith("/") && logroot.length()>1) logroot += "/";
            logroot += "applogs";
        }
        return logroot;
    }
    
}
