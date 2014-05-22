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

import org.pustefixframework.config.contextxmlservice.IWrapperConfig;

import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;


/**
 * Stores configuration for an IWrapper
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class IWrapperConfigImpl implements IWrapperConfig {
    
    private String prefix = null;
    private Class<? extends IWrapper> wrapperClass = null;
    private boolean checkActive = true;
    private boolean dologging = false;
    private IHandler handler;
    private String scope = "singleton";
    private String tenant;
    
    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.IWrapperConfig#getPrefix()
     */
    public String getPrefix() {
        return this.prefix;
    }
    
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    
    public void setWrapperClass(Class<? extends IWrapper> clazz) {
        this.wrapperClass = clazz;
    }
    
    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.IWrapperConfig#getWrapperClass()
     */
    public Class<? extends IWrapper> getWrapperClass() {
        return this.wrapperClass;
    }
    
    public void setCheckActive(boolean checkActive) {
        this.checkActive = checkActive;
    }
    
    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.IWrapperConfig#isCheckActive()
     */
    public boolean doCheckActive() {
        return this.checkActive;
    }
    
    public void setLogging(boolean dologging) {
        this.dologging = dologging;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.IWrapperConfig#getLogging()
     */
    public boolean getLogging() {
        return this.dologging;
    }

    public void setHandler(IHandler handler) {
        this.handler = handler;
    }
    
    public IHandler getHandler() {
        return this.handler;
    }
    
    public void setScope(String scope) {
        this.scope = scope;
    }
    
    public String getScope() {
        return this.scope;
    }
    
    public void setTenant(String tenant) {
        this.tenant = tenant;
    }
    
    public String getTenant() {
        return tenant;
    }
    
}
