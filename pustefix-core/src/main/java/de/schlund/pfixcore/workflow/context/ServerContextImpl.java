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

package de.schlund.pfixcore.workflow.context;

import java.util.Properties;

import org.pustefixframework.config.contextxmlservice.ContextConfig;
import org.pustefixframework.config.project.ProjectInfo;

import de.schlund.pfixcore.workflow.PageMap;
import de.schlund.pfixcore.workflow.VariantManager;
import de.schlund.pfixxml.Variant;

/**
 * Provides server-wide context information for the {@link de.schlund.pfixxml.ContextXMLServlet}.
 * <b>Should not be used by other classes than ContextXMLServlet</b>  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
//TODO: remove ServerContextImpl
public class ServerContextImpl {
	
    private ContextConfig config;
    
    private PageFlowManager pageflowmanager;
    private VariantManager variantmanager;
    private PageMap pagemap;
    private ProjectInfo projectInfo;
    
    public void init() throws Exception {
        if (config == null || pagemap == null) {
            throw new IllegalStateException("Properties have to be set before calling init().");
        }
        
        variantmanager  = (VariantManager) new VariantManager(config);
        pageflowmanager = new PageFlowManager(config, variantmanager);
    }
    
    public void setConfig(ContextConfig config) {
        this.config = config;
    }
    
    public void setPageMap(PageMap pageMap) {
        this.pagemap = pageMap;
    }
    
    public Properties getProperties() {
        return config.getProperties();
    }

    public Properties getPropertiesForContextResource(Object res) {
        return config.getContextResourceConfig(res.getClass()).getProperties();
    }
    
    public ContextConfig getContextConfig() {
        return config;
    }
        
    public PageFlowManager getPageFlowManager() {
        return pageflowmanager;
    }
    
    public VariantManager getVariantManager() {
        return variantmanager;
    }
    
    public PageMap getPageMap() {
        return pagemap;
    }
    
    public String getPageMatchingVariant(String pagename, Variant variant) {
        if (variant != null & variant.getVariantFallbackArray() != null && variantmanager != null) {
            return variantmanager.getVariantMatchingPageRequestName(pagename, variant);
        } else {
            return pagename;
        }
    }
    
    public ProjectInfo getProjectInfo() {
        return projectInfo;
    }
    
    public void setProjectInfo(ProjectInfo projectInfo) {
        this.projectInfo = projectInfo;
    }
    
}
