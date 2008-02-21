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

package de.schlund.pfixxml.config.impl;

import java.util.Enumeration;
import java.util.Properties;

import de.schlund.pfixcore.workflow.DirectOutputState;
import de.schlund.pfixxml.config.DirectOutputPageRequestConfig;


/**
 * Stores configuration for a DirectOutputServlet PageRequest
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class DirectOutputPageRequestConfigImpl implements DirectOutputPageRequestConfig {

    private String pageName = null;

    private Class<? extends DirectOutputState> stateClass = null;
    
    private Properties properties = new Properties();

    public void setPageName(String page) {
        this.pageName = page;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.DirectOutputPageRequestConfig#getPageName()
     */
    public String getPageName() {
        return this.pageName;
    }

    public void setState(Class<? extends DirectOutputState> clazz) {
        this.stateClass = clazz;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.DirectOutputPageRequestConfig#getState()
     */
    public Class<? extends DirectOutputState> getState() {
        return this.stateClass;
    }
    
    public void setProperties(Properties props) {
        this.properties = new Properties();
        Enumeration<?> e = props.propertyNames();
        while (e.hasMoreElements()) {
            String propname = (String) e.nextElement();
            this.properties.setProperty(propname, props.getProperty(propname));
        }
    }
    
    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.DirectOutputPageRequestConfig#getProperties()
     */
    public Properties getProperties() {
        return this.properties;
    }
}
