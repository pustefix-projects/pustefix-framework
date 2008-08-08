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

package de.schlund.pfixcore.workflow.context;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import de.schlund.pfixcore.workflow.ContextInterceptor;
import de.schlund.pfixcore.workflow.ContextInterceptorFactory;
import de.schlund.pfixcore.workflow.PageMap;
import de.schlund.pfixcore.workflow.VariantManager;
import de.schlund.pfixxml.Variant;
import de.schlund.pfixxml.config.ContextConfig;

/**
 * Provides server-wide context information for the {@link de.schlund.pfixxml.ContextXMLServlet}.
 * <b>Should not be used by other classes than ContextXMLServlet</b>  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
//TODO: remove ServerContextImpl
public class ServerContextImpl {
	
    private ContextConfig config;
    
    private String name;
    
    private PageFlowManager pageflowmanager;
    private VariantManager variantmanager;
    private PageMap pagemap;
    
    private ContextInterceptor[] startInterceptors;
    private ContextInterceptor[] endInterceptors;
    
    public ServerContextImpl() {
        
    }
    
    //TODO: inject dependencies
    public void init(ContextConfig config, String contextName) throws Exception {
        this.config = config;
        this.name = contextName;
        
        variantmanager  = (VariantManager) new VariantManager(config);
        pageflowmanager = new PageFlowManager(config, variantmanager);
        pagemap         = new PageMap(config);
        
        createInterceptors();
    }
    
    private void createInterceptors() throws Exception {
        ArrayList<ContextInterceptor> list = new ArrayList<ContextInterceptor>();
        for (Iterator<Class<? extends ContextInterceptor>> i = config.getStartInterceptors().iterator(); i.hasNext();) {
            String classname = i.next().getName();
            list.add(ContextInterceptorFactory.getInstance().getInterceptor(classname));
        }
        startInterceptors = (ContextInterceptor[]) list.toArray(new ContextInterceptor[] {});

        list.clear();
        for (Iterator<Class<? extends ContextInterceptor>> i = config.getEndInterceptors().iterator(); i.hasNext();) {
            String classname = i.next().getName();
            list.add(ContextInterceptorFactory.getInstance().getInterceptor(classname));
        }
        endInterceptors = (ContextInterceptor[]) list.toArray(new ContextInterceptor[] {});
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
    
    public ContextInterceptor[] getStartInterceptors() {
        return startInterceptors;
    }
    
    public ContextInterceptor[] getEndInterceptors() {
        return endInterceptors;
    }
    
    public String getName() {
        return name;
    }
    
    public String getPageMatchingVariant(String pagename, Variant variant) {
        if (variant != null & variant.getVariantFallbackArray() != null && variantmanager != null) {
            return variantmanager.getVariantMatchingPageRequestName(pagename, variant);
        } else {
            return pagename;
        }
    }
    
}
