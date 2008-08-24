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

import java.util.HashSet;
import java.util.Set;

import org.pustefixframework.config.contextxmlservice.ContextXMLServletConfig;
import org.pustefixframework.config.contextxmlservice.SSLOption;

import de.schlund.pfixcore.scriptedflow.ScriptedFlowConfigImpl;
import de.schlund.pfixcore.workflow.ConfigurableState;
import de.schlund.pfixxml.resources.FileResource;

/**
 * Stores configuration for a Pustefix servlet
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ContextXMLServletConfigImpl extends AbstractXMLServletConfigImpl implements ContextXMLServletConfig, SSLOption {
    private final static Class<de.schlund.pfixcore.workflow.app.DefaultIWrapperState> DEFAULT_IHANDLER_STATE = de.schlund.pfixcore.workflow.app.DefaultIWrapperState.class;

    private final static Class<de.schlund.pfixcore.workflow.app.StaticState> DEFAULT_STATIC_STATE = de.schlund.pfixcore.workflow.app.StaticState.class;

    private Class<? extends ConfigurableState> defaultStateClass = DEFAULT_STATIC_STATE;

    private Class<? extends ConfigurableState> defaultIHandlerStateClass = DEFAULT_IHANDLER_STATE;

    private ContextConfigImpl contextConfig;

    private ScriptedFlowConfigImpl scriptedFlowConfig = new ScriptedFlowConfigImpl();

    private Set<FileResource> fileDependencies = new HashSet<FileResource>();

    private long loadTime = 0;

    public void setDefaultStaticState(Class<? extends ConfigurableState> clazz) {
        this.defaultStateClass = clazz;
    }

    public Class<? extends ConfigurableState> getDefaultStaticState() {
        return this.defaultStateClass;
    }

    public void setDefaultIHandlerState(Class<? extends ConfigurableState> clazz) {
        this.defaultIHandlerStateClass = clazz;
    }

    public Class<? extends ConfigurableState> getDefaultIHandlerState() {
        return this.defaultIHandlerStateClass;
    }

    public void setContextConfig(ContextConfigImpl config) {
        this.contextConfig = config;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.ContextXMLServletConfig#getContextConfig()
     */
    public ContextConfigImpl getContextConfig() {
        return this.contextConfig;
    }

    public ScriptedFlowConfigImpl getScriptedFlowConfig() {
        return this.scriptedFlowConfig;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.ContextXMLServletConfig#needsReload()
     */
    public boolean needsReload() {
        for (FileResource file : fileDependencies) {
            if (file.lastModified() > loadTime) {
                return true;
            }
        }
        return false;
    }
}