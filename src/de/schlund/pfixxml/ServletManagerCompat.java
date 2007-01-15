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
 */

package de.schlund.pfixxml;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;

import org.xml.sax.SAXException;

import de.schlund.pfixxml.config.ServletManagerConfig;
import de.schlund.pfixxml.config.XMLPropertiesUtil;
import de.schlund.pfixxml.config.impl.ServletManagerConfigImpl;
import de.schlund.pfixxml.resources.FileResource;

/**
 * This servlet is a compatibility layer for legacy servlets based on the old
 * ServletManager. It uses a configuration file in the standardprops XML 
 * format and provides the properties defined in this file to the child 
 * implementation.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public abstract class ServletManagerCompat extends ServletManager {
    private ServletManagerConfigImpl config;

    protected ServletManagerConfig getServletManagerConfig() {
        return config;
    }

    protected void reloadServletConfig(FileResource configFile, Properties globalProperties) throws ServletException {
        Properties props = new Properties(globalProperties);
        if (configFile != null) {
            try {
                XMLPropertiesUtil.loadPropertiesFromXMLFile(configFile, props);
            } catch (SAXException e) {
                throw new ServletException("Parsing error while reading configuration file " + configFile.toString());
            } catch (IOException e) {
                throw new ServletException("Could not read configuration file " + configFile.toString());
            }
        }
        this.config = new ServletManagerConfigImpl();
        this.config.setProperties(props);
        String needs_ssl = props.getProperty("servlet.needsSSL");
        if (needs_ssl != null && (needs_ssl.equals("true") || needs_ssl.equals("yes") || needs_ssl.equals("1"))) {
            this.config.setSSL(true);
        } else {
            this.config.setSSL(false);
        }
    }
    
    /**
     * Compatibility method as old ServletManager used to be confingured using
     * properties and child implementations might depend on this.
     * 
     * @return Properties defined in the configuration file
     */
    protected Properties getProperties() {
        return this.config.getProperties();
    }
}
