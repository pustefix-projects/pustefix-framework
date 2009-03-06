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
 *
 */

package de.schlund.pfixcore.workflow.context;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.pustefixframework.config.contextxmlservice.ContextConfig;
import org.pustefixframework.config.contextxmlservice.PageRequestConfig;

import de.schlund.pfixcore.workflow.PageRequest;
import de.schlund.pfixcore.workflow.VariantManager;
import de.schlund.pfixxml.Variant;

/**
 * @author: jtl
 *
 */

public class PageFlowManager {
    private Map<String, Set<String>> pagetoflowmap = new HashMap<String, Set<String>>();
    private Map<String, PageFlow> flowmap = new HashMap<String, PageFlow>();
    
    private VariantManager vmanager;
    private ContextConfig config;

    private final static Logger LOG = Logger.getLogger(PageFlowManager.class);

    public final static String PROP_PREFIX = "context.pageflow";

    public PageFlowManager(ContextConfig config, VariantManager variantmanager) {
        vmanager = variantmanager;
        this.config = config;

        // Initialize map mapping each page name to a list of
        // flows which contain this page in at least one variant
        // and create PageFlow object for each flow
        for (PageFlow flow : config.getPageFlows()) {
            flowmap.put(flow.getName(), flow);
            
            String rootname = flow.getRootName();
            for (PageRequestConfig pageConfig : config.getPageRequestConfigs()) {
                String pageName = getRootName(pageConfig.getPageName());
                if (flow.containsPage(pageName)) {
                    Set<String> names = pagetoflowmap.get(pageName);
                    if (names == null) {
                        names = new HashSet<String>();
                        pagetoflowmap.put(pageName, names);
                    }
                    if (!names.contains(rootname)) {
                        names.add(rootname);
                    }
                }
            }
        }
    }
    
    private String getRootName(String genericName) {
        if (!genericName.contains("::")) {
            return genericName;
        } else {
            return genericName.substring(0, genericName.indexOf("::"));
        }
    }

    protected PageFlow pageFlowToPageRequest(PageFlow lastflow, PageRequest page, Variant variant) {
        //LOG.debug("===> Testing pageflow: " + currentflow.getName() + " / page: " + page);
        if (lastflow == null || !lastflow.containsPage(page.getRootName())) {
            Set<String> rootflownames = pagetoflowmap.get(page.getRootName());
            if (rootflownames == null) {
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
            for (Iterator<String> i = rootflownames.iterator(); i.hasNext();) {
                String pageflowname = vmanager.getVariantMatchingPageFlowName(i.next(), variant);
                PageFlow pf = getPageFlowByName(pageflowname);
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
        return flowmap.get(fullname);
    }
}
