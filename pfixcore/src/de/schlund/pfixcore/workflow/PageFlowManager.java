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

import de.schlund.pfixxml.*;
import de.schlund.pfixxml.config.ContextConfig;
import de.schlund.pfixxml.config.PageFlowConfig;
import de.schlund.pfixcore.util.PropertiesUtils;
import java.util.*;

import org.apache.log4j.*;

/**
 * @author: jtl
 *
 */

public class PageFlowManager implements ConfigurableObject {
    private              HashMap  flowmap       = new HashMap();
    private              HashSet  rootflownames = new HashSet();
    private       static Logger LOG           = Logger.getLogger(PageFlowManager.class);
    public  final static String   PROP_PREFIX   = "context.pageflow";

    public void init(Object confObj) throws Exception {
        ContextConfig config = (ContextConfig) confObj;
        PageFlowConfig[] pageflows = config.getPageFlows();
        
        for (int i = 0; i < pageflows.length; i++) {
            String name = pageflows[i].getFlowName();
            LOG.debug("===> Found flowname: " + name);
            PageFlow  pf = new PageFlow(pageflows[i]);
            flowmap.put(name, pf);
            if (!name.contains("::")) {
                rootflownames.add(name);
            }
        }
    }

    protected PageFlow pageFlowToPageRequest(PageFlow currentflow, PageRequest page, Variant variant) {
        LOG.debug("===> Testing pageflow: " + currentflow.getName() + " / page: " + page);
        if (!currentflow.containsPage(page.getRootName())) {
            for (Iterator i = rootflownames.iterator(); i.hasNext(); ) {
                PageFlow pf = getPageFlowByName((String) i.next(), variant);
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

    public PageFlow getPageFlowByName(String name, Variant variant) {
        LOG.debug("=== Requesting FLOW " + name);

        if (variant != null && variant.getVariantFallbackArray() != null) {
            String[] variant_arr = variant.getVariantFallbackArray();
            for (int i = 0; i < variant_arr.length; i++) {
                String   fullname = name + "::" + variant_arr[i];
                PageFlow flow     = (PageFlow) flowmap.get(fullname);
                if (flow != null) {
                    LOG.debug("=== Found FLOW for '" + fullname + "' ===");
                    return flow;
                }
                LOG.debug("=== FLOW NOT FOUND for '" + fullname + "' ===");
            }
        }
        return (PageFlow) flowmap.get(name);
    }
}
