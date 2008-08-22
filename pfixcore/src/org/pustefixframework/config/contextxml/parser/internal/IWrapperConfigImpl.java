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

package org.pustefixframework.config.contextxml.parser.internal;

import org.pustefixframework.config.contextxml.IWrapperConfig;

import de.schlund.pfixcore.generator.IWrapper;


/**
 * Stores configuration for an IWrapper
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class IWrapperConfigImpl implements IWrapperConfig {
    
    private String prefix = null;
    private Class<? extends IWrapper> wrapperClass = null;
    private boolean activeIgnore = false;
    private boolean dologging = false;
    
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
    
    public void setActiveIgnore(boolean ignore) {
        this.activeIgnore = ignore;
    }
    
    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.IWrapperConfig#isActiveIgnore()
     */
    public boolean isActiveIgnore() {
        return this.activeIgnore;
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
}
