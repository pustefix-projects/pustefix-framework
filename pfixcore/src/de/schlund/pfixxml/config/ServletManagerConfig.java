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

package de.schlund.pfixxml.config;

import java.util.Enumeration;
import java.util.Properties;

/**
 * Stores configuration used by ServletManager 
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ServletManagerConfig {

    private boolean ssl;
    private Properties props = new Properties();

    public void setSSL(boolean b) {
        this.ssl = b;
    }

    public boolean isSSL() {
        return this.ssl;
    }

    public void setProperties(Properties props) {
        this.props = new Properties();
        Enumeration e = props.propertyNames();
        while (e.hasMoreElements()) {
            String propname = (String) e.nextElement();
            this.props.setProperty(propname, props.getProperty(propname));
        }
    }
    
    public Properties getProperties() {
        return this.props;
    }
    
    public boolean needsReload() {
        // Override this method in child implementations
        // which may have to check for changes in more
        // files than the main config file
        return false;
    }
}
