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

import de.schlund.pfixxml.config.IWrapperConfig;


/**
 * Stores configuration for an IWrapper
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class IWrapperConfigImpl implements IWrapperConfig {
    
    private String prefix = null;
    private Class wrapperClass = null;
    private boolean continueOnSubmit = false;
    private boolean activeIgnore = false;
    private boolean alwaysRetrieve = false;
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
    
    public void setWrapperClass(Class clazz) {
        this.wrapperClass = clazz;
    }
    
    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.IWrapperConfig#getWrapperClass()
     */
    public Class getWrapperClass() {
        return this.wrapperClass;
    }
    
    public void setContinue(boolean continueOnSubmit) {
        this.continueOnSubmit = continueOnSubmit;
    }
    
    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.IWrapperConfig#isContinue()
     */
    public boolean isContinue() {
        return this.continueOnSubmit;
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
    
    public void setAlwaysRetrieve(boolean retrieve) {
        this.alwaysRetrieve = retrieve;
    }
    
    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.IWrapperConfig#isAlwaysRetrieve()
     */
    public boolean isAlwaysRetrieve() {
        return this.alwaysRetrieve;
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
