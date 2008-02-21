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

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import de.schlund.pfixcore.workflow.ContextResource;
import de.schlund.pfixxml.config.ContextResourceConfig;

/**
 * Stores configuration for a ContextResource
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ContextResourceConfigImpl implements ContextResourceConfig {
    private final static Logger LOG = Logger.getLogger(ContextResourceConfigImpl.class);
    
    private Class<? extends ContextResource> resourceClass = null;
    private HashSet<Class<? extends ContextResource>> interfaces = new HashSet<Class<? extends ContextResource>>();
    private Properties props = new Properties();
    private ContextConfigImpl parent;
    
    public ContextResourceConfigImpl(Class<? extends ContextResource> clazz, ContextConfigImpl parent) {
        this.resourceClass = clazz;
        this.parent = parent;
    }
    
    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.ContextResourceConfig#getContextResourceClass()
     */
    public Class<? extends ContextResource> getContextResourceClass() {
        return this.resourceClass;
    }
    
    public void addInterface(Class<? extends ContextResource> clazz) {
        this.interfaces.add(clazz); 
        ContextResourceConfig oldConfig = this.parent.getContextResourceConfigForInterface(clazz);
        if (oldConfig != null) {
            LOG.warn("Binding interface " + clazz.getName() + " already bound to " + oldConfig.getClass().getName() + " to new class " + this.resourceClass.getName());
        }
        this.parent.interfaceToResource.put(clazz, this);
    }
    
    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.ContextResourceConfig#getInterfaces()
     */
    public Set<Class<? extends ContextResource>> getInterfaces() {
        return this.interfaces;
    }
    
    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.ContextResourceConfig#getProperties()
     */
    public Properties getProperties() {
        return this.props;
    }
}
