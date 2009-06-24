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

package org.pustefixframework.config.customization;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.schlund.pfixxml.resources.ResourceUtil;

/**
 * Provides easy access to properties stored at buildtime 
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class RuntimeProperties {
    private static final String PATH = "META-INF/pustefix/runtime.properties";
    private final static Log LOGGER = LogFactory.getLog(RuntimeProperties.class);
    
    private static Properties properties = new Properties();

    static {
        properties.setProperty("mode", "prod");
        properties.setProperty("machine", getMachine());
        properties.setProperty("fqdn", getFQDN());
        properties.setProperty("uid", getUID());
        InputStream inputStream = RuntimeProperties.class.getClassLoader().getResourceAsStream(PATH);
        if (inputStream != null) {
            try {
                properties.load(inputStream);
            } catch (IOException e) {
                LOGGER.warn("Could not load runtime properties from resource " + PATH, e);
            }
        }
    }
    
    public static Properties getProperties() {
        return properties;
    }
    
    private static String getMachine() {
        String str;
        int idx;
        
        str = getFQDN();
        idx = str.indexOf('.');
        return idx == -1 ? str : str.substring(0, idx);
    }

    private static String getFQDN() {
        try {
            String fqdn = InetAddress.getLocalHost().getCanonicalHostName();
            if (fqdn.matches("[0-9][0-9]?[0-9]?\\.[0-9][0-9]?[0-9]?\\.[0-9][0-9]?[0-9]?\\.[0-9][0-9]?[0-9]?")) {
                LOGGER.warn("Could not determine hostname of local machine");
                return "localhost";
            }
            return fqdn;
        } catch (UnknownHostException e) {
            LOGGER.warn("Could not determine hostname of local machine", e);
            return "localhost";
        }
    }
    
    private static String getUID() {
        String username = System.getProperty("user.name");
        if (username == null) {
            username = "nobody";
        }
        return username;
    }
    
    public static void generate(Properties props, String mode, String machine, String fqdn, String uid) throws IOException {
        props.setProperty("mode", mode);
        props.setProperty("machine", machine);
        props.setProperty("fqdn", fqdn);
        props.setProperty("uid", uid);
        props.store(ResourceUtil.getFileResourceFromDocroot(PATH).getOutputStream(), "Properties used at buildtime");
    }
}
