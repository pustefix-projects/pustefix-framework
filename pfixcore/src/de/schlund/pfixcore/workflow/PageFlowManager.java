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
import de.schlund.pfixcore.util.*;
import java.util.*;
import javax.servlet.http.*;
import org.apache.log4j.*;

/**
 * @author: jtl
 *
 *
 */

public class PageFlowManager implements PropertyObject {
    private              HashMap  flowmap      = new HashMap();
    private       static Category LOG          = Category.getInstance(PageFlowManager.class.getName());
    public  final static String   PROP_PREFIX  = "context.pageflow";
    public  final static String   PARAM_FLOW   = "__pageflow";

    public void init(Properties props) throws Exception {
        HashSet names = new HashSet();
        HashMap tmp   = PropertiesUtils.selectProperties(props, PROP_PREFIX);
        for (Iterator i = tmp.keySet().iterator(); i.hasNext(); ) {
            String full = (String) i.next();
            String name = full.substring(0, full.indexOf("."));
            names.add(name);
        }

        for (Iterator i = names.iterator(); i.hasNext(); ) {
            String    name = (String) i.next();
            LOG.debug("===> Found flowname: " + name);
            PageFlow  pf   = new PageFlow(props, name);
            flowmap.put(name, pf);
        }
    }

    public PageFlow pageFlowToPageRequest(PageFlow currentflow, PageRequest page, PfixServletRequest req) {
        RequestParam flowname = req.getRequestParam(PARAM_FLOW);
        if (flowname != null && !flowname.getValue().equals("")) {
            LOG.debug("===> User requesting to switch to flow '" + flowname.getValue() + "'");
            PageFlow flow = getPageFlowByName(flowname.getValue());
            if (flow != null) {
                LOG.debug("===> Flow '" + flowname.getValue() + "' exists...");
                if  (flow.containsPageRequest(page)) {
                    LOG.debug("===> and it contains page '" + page.getName() + "'");
                    LOG.debug("===> Switching to workflow: " + flow.getName());
                    return flow;
                } else {
                    LOG.debug("===> but it doesn't contain page '" + page.getName() + "': Ignoring request.");
                }
            }
        }
        return pageFlowToPageRequest(currentflow, page);
    }

    protected PageFlow pageFlowToPageRequest(PageFlow currentflow, PageRequest page) {
        LOG.debug("===> Current pageflow: " + currentflow.getName() + " / current page: " + page);
        if (!currentflow.containsPageRequest(page)) {
            
                for (Iterator i = flowmap.keySet().iterator(); i.hasNext(); ) {
                    PageFlow pf = (PageFlow) flowmap.get(i.next());
                    if (pf.containsPageRequest(page)) {
                        LOG.debug("===> Switching to workflow: " + pf.getName());
                        return pf;
                    }
                }
            
            LOG.debug("===> Found no other workflow containing page " +
                      page + ". Reusing flow " + currentflow.getName());
        } else {
            LOG.debug("===> Page " + page + " is member of current workflow: Reusing flow " + currentflow.getName());
        }
        return currentflow;
    }

    public PageFlow getPageFlowByName(String name) {
            return (PageFlow) flowmap.get(name);
    }
}
