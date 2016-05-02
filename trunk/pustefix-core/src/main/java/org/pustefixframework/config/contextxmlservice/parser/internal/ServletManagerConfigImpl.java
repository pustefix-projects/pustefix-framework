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

package org.pustefixframework.config.contextxmlservice.parser.internal;

import java.util.Enumeration;
import java.util.Properties;

import org.pustefixframework.config.contextxmlservice.ServletManagerConfig;


/**
 * Stores configuration used by ServletManager 
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ServletManagerConfigImpl implements ServletManagerConfig {

    private boolean ssl;
    private Properties props = new Properties();

    public void setSSL(boolean b) {
        this.ssl = b;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.ServletManagerConfig#isSSL()
     */
    public boolean isSSL() {
        return this.ssl;
    }

    public void setProperties(Properties props) {
        this.props = new Properties();
        Enumeration<?> e = props.propertyNames();
        while (e.hasMoreElements()) {
            String propname = (String) e.nextElement();
            this.props.setProperty(propname, props.getProperty(propname));
        }
    }
    
    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.ServletManagerConfig#getProperties()
     */
    public Properties getProperties() {
        return this.props;
    }
    
    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.ServletManagerConfig#needsReload()
     */
    public boolean needsReload() {
        // Override this method in child implementations
        // which may have to check for changes in more
        // files than the main config file
        return false;
    }
}
