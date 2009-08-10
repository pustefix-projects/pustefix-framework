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

package org.pustefixframework.config.directoutputservice.parser.internal;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.pustefixframework.config.contextxmlservice.SSLOption;
import org.pustefixframework.config.contextxmlservice.parser.internal.AbstractPustefixRequestHandlerConfigImpl;
import org.pustefixframework.config.directoutputservice.DirectOutputPageRequestConfig;
import org.pustefixframework.config.directoutputservice.DirectOutputRequestHandlerConfig;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;


/**
 * Stores configuration for a Pustefix servlet
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class DirectOutputRequestHandlerConfigImpl extends AbstractPustefixRequestHandlerConfigImpl implements
        SSLOption, DirectOutputRequestHandlerConfig {
    private boolean sync = true;

    private String authConstraintRef;
    
    private Map<String, ? extends DirectOutputPageRequestConfig> pageRequests;

    private long loadTime = 0;

    public void setSynchronized(boolean sync) {
        this.sync = sync;
    }
    
    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.DirectOutputServletConfig#isSynchronized()
     */
    public boolean isSynchronized() {
        return sync;
    }

    public void setAuthConstraintRef(String authConstraintRef) {
        this.authConstraintRef = authConstraintRef;
    }
    
    public String getAuthConstraintRef() {
        return authConstraintRef;
    }
    
    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.DirectOutputServletConfig#getPageRequest(java.lang.String)
     */
    public DirectOutputPageRequestConfig getPageRequest(String page) {
        return this.pageRequests.get(page);
    }

    public Map<String, ? extends DirectOutputPageRequestConfig> getPageRequests() {
        return pageRequests;
    }

    public void setPageRequests(Map<String, ? extends DirectOutputPageRequestConfig> pageRequests) {
        this.pageRequests = pageRequests;
    }

    public BeanDefinition createBeanDefinition(RuntimeBeanReference directOutputPageMap) {
        BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(DirectOutputRequestHandlerConfigImpl.class);
        beanBuilder.setScope("singleton");
        beanBuilder.addPropertyValue("authConstraintRef", this.getAuthConstraintRef());
        beanBuilder.addPropertyValue("pageRequests", directOutputPageMap);
        beanBuilder.addPropertyValue("properties", this.getProperties());
        beanBuilder.addPropertyValue("SSL", this.isSSL());
        beanBuilder.addPropertyValue("synchronized", this.isSynchronized());
        return beanBuilder.getBeanDefinition();
    }

}
