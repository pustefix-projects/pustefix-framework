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

import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.pustefixframework.config.contextxmlservice.ContextConfig;

import de.schlund.pfixcore.workflow.PageRequest;
import de.schlund.pfixcore.workflow.VariantManager;
import de.schlund.pfixxml.Variant;

/**
 * @author: jtl
 *
 */

public class PageFlowManager {
    private VariantManager vmanager;
    private ContextConfig config;

    private final static Logger LOG = Logger.getLogger(PageFlowManager.class);

    public final static String PROP_PREFIX = "context.pageflow";

    public PageFlowManager(ContextConfig config, VariantManager variantmanager) {
        vmanager = variantmanager;
        this.config = config;
    }

    protected PageFlow pageFlowToPageRequest(PageFlow lastflow, PageRequest page, Variant variant) {
        String pageRootName = page.getRootName();
        if (lastflow == null || !lastflow.containsPage(pageRootName)) {
            
            LinkedList<String> pageFlowRootNames = new LinkedList<String>();
            for (PageFlow pageFlow : config.getPageFlowMap().values()) {
                if (pageFlow.containsPage(pageRootName)) {
                    pageFlowRootNames.add(pageFlow.getRootName());
                }
            }
            if (pageFlowRootNames.size() == 0) {
                LOG.debug("===> Page " + page + " isn't a member of any pageflow: returning no pageflow");
                return null;
            }
            if (config.getPageRequestConfig(page.getName()) != null) {
                String defaultFlowForRequest = this.config.getPageRequestConfig(page.getName()).getDefaultFlow();
                if (defaultFlowForRequest != null) {
                    LOG.debug("===> Page " + page + " has a default flow specified: Using flow " + defaultFlowForRequest);
                    String pageflowname = vmanager.getVariantMatchingPageFlowName(defaultFlowForRequest, variant);
                    PageFlow pf = getPageFlowByName(pageflowname);
                    if (pf.containsPage(page.getRootName())) {
                        LOG.debug("===> Switching to pageflow: " + pf.getName());
                        return pf;
                    }
                }
            }
            for (String pageFlowRootName : pageFlowRootNames) {
                String pageFlowName = vmanager.getVariantMatchingPageFlowName(pageFlowRootName, variant);
                PageFlow pf = getPageFlowByName(pageFlowName);
                if (pf.containsPage(page.getRootName())) {
                    //LOG.debug("===> Switching to pageflow: " + pf.getName());
                    return pf;
                }
            }
            LOG.debug("===> Page " + page + " isn't a member of any valid pageflow: returning no pageflow");
            return null;
        } else {
            LOG.debug("===> Page " + page + " is member of the last used pageflow: Reusing flow " + lastflow.getName());
            return lastflow;
        }
    }
    
    protected PageFlow getPageFlowByName(String rootname, Variant variant) {
        return getPageFlowByName(vmanager.getVariantMatchingPageFlowName(rootname, variant));
    }
    
    protected PageFlow getPageFlowByName(String fullname) {
        return config.getPageFlowMap().get(fullname);
    }
}
