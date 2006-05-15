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

package de.schlund.pfixcore.workflow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import de.schlund.pfixxml.ConfigurableObject;
import de.schlund.pfixxml.PropertyObjectManager;
import de.schlund.pfixxml.Variant;
import de.schlund.pfixxml.config.ContextConfig;
import de.schlund.pfixxml.config.PageFlowConfig;
import de.schlund.pfixxml.config.PageFlowStepConfig;

/**
 * @author: jtl
 *
 */

public class PageFlowManager implements ConfigurableObject {
    private Map<String, Set<String>> pagetoflowmap = new HashMap<String, Set<String>>();
    private Map<String, PageFlow> flowmap = new HashMap<String, PageFlow>();
    
    private VariantManager vmanager;

    private static Logger LOG = Logger.getLogger(PageFlowManager.class);

    public final static String PROP_PREFIX = "context.pageflow";

    public void init(Object confObj) throws Exception {
        ContextConfig config = (ContextConfig) confObj;
        vmanager = (VariantManager) PropertyObjectManager.getInstance().getConfigurableObject(config, VariantManager.class);
        PageFlowConfig[] pageflows = config.getPageFlowConfigs();

        // Initialize map mapping each page name to a list of
        // flows which contain this page in at least one variant
        // and create PageFlow object for each flow
        for (int i = 0; i < pageflows.length; i++) {
            PageFlow flow = new PageFlow(pageflows[i]);
            flowmap.put(flow.getName(), flow);
            
            String rootname = flow.getRootName();
            PageFlowStepConfig[] steps = pageflows[i].getFlowSteps();
            for (int j = 0; j < steps.length; j++) {
                String pagename = steps[j].getPage();
                Set<String> names = pagetoflowmap.get(pagename);
                if (names == null) {
                    names = new HashSet<String>();
                    pagetoflowmap.put(pagename, names);
                }
                if (!names.contains(rootname)) {
                    names.add(rootname);
                }
            }
        }
    }

    protected PageFlow pageFlowToPageRequest(PageFlow currentflow, PageRequest page, Variant variant) {
        LOG.debug("===> Testing pageflow: " + currentflow.getName() + " / page: " + page);
        if (!currentflow.containsPage(page.getRootName())) {
            Set<String> rootflownames = pagetoflowmap.get(page.getRootName());
            if (rootflownames == null) {
                LOG.debug("===> Page " + page + " isn't a member of any pageflow: Reusing flow " + currentflow.getName());
                return currentflow;
            }
            for (Iterator i = rootflownames.iterator(); i.hasNext();) {
                String pageflowname = vmanager.getVariantMatchingPageFlowName((String) i.next(), variant);
                PageFlow pf = getPageFlowByName(pageflowname);
                if (pf.containsPage(page.getRootName())) {
                    LOG.debug("===> Switching to pageflow: " + pf.getName());
                    return pf;
                }
            }
            LOG.debug("===> Page " + page + " isn't a member of any pageflow: Reusing flow " + currentflow.getName());
        } else {
            LOG.debug("===> Page " + page + " is member of current pageflow: Reusing flow " + currentflow.getName());
        }
        return currentflow;
    }
    
    protected PageFlow getPageFlowByName(String fullname) {
        return flowmap.get(fullname);
    }
}
