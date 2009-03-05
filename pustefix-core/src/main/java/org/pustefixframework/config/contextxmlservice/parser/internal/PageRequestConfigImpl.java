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

package org.pustefixframework.config.contextxmlservice.parser.internal;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.pustefixframework.config.contextxmlservice.PageRequestConfig;
import org.pustefixframework.config.contextxmlservice.SSLOption;

import de.schlund.pfixcore.auth.AuthConstraint;

/**
 * Stores configuration for a PageRequest
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class PageRequestConfigImpl implements Cloneable, PageRequestConfig, SSLOption {
    
    private String pageName = null;
    private boolean ssl = false;
    private AuthConstraint authConstraint;
    private String defaultFlow = null;
    private String beanName = null;
    private Properties props = new Properties();
    private LinkedHashMap<String, ProcessActionPageRequestConfigImpl> actions = new LinkedHashMap<String, ProcessActionPageRequestConfigImpl>();
    
    public void setPageName(String page) {
        this.pageName = page;
    }
    
    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.PageRequestConfig#getPageName()
     */
    public String getPageName() {
        return this.pageName;
    }
    
    public void setSSL(boolean forceSSL) {
        this.ssl = forceSSL;
    }
    
    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.PageRequestConfig#isSSL()
     */
    public boolean isSSL() {
        return this.ssl;
    }
    
    public AuthConstraint getAuthConstraint() {
    	return authConstraint;
    }
    
    public void setAuthConstraint(AuthConstraint authConstraint) {
    	this.authConstraint = authConstraint;
    }

    public String getDefaultFlow() {
        return defaultFlow;
    }

    public void setDefaultFlow(String defaultFlow) {
        this.defaultFlow = defaultFlow;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public String getBeanName() {
        return this.beanName ;
    }
    
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }
    
    public void setProperties(Properties props) {
        this.props = new Properties();
        Enumeration<?> e = props.propertyNames();
        while (e.hasMoreElements()) {
            String propname = (String) e.nextElement();
            this.props.setProperty(propname, props.getProperty(propname));
        }
    }
    
    public Properties getProperties() {
        return this.props;
    }
    
    public Map<String, ProcessActionPageRequestConfigImpl> getProcessActions() {
        return Collections.unmodifiableMap(this.actions);
    }
    
    public void addProcessAction(String name, ProcessActionPageRequestConfigImpl action) {
        actions.put(name, action);
    }
 }
