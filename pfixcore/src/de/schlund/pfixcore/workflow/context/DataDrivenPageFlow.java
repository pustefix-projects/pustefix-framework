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

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.schlund.pfixcore.exception.PustefixApplicationException;
import de.schlund.pfixcore.exception.PustefixCoreException;
import de.schlund.pfixcore.workflow.FlowStep;
import de.schlund.pfixcore.workflow.PageFlowContext;
import de.schlund.pfixcore.workflow.PageRequest;
import de.schlund.pfixcore.workflow.PageRequestStatus;
import de.schlund.pfixxml.ResultDocument;
import de.schlund.pfixxml.config.PageFlowConfig;
import de.schlund.pfixxml.config.PageFlowStepConfig;

/**
 * @author: jtl
 *
 */

public class DataDrivenPageFlow implements PageFlow {
    private String    flowname;
    private String    rootname;
    private ArrayList<FlowStep> allsteps = new ArrayList<FlowStep>();
    private HashMap<String, FlowStep> stepmap = new HashMap<String, FlowStep>();
    private String    finalpage;
    
    private final static Logger LOG = Logger.getLogger(DataDrivenPageFlow.class);
    
    public DataDrivenPageFlow(PageFlowConfig config) {
        flowname = config.getFlowName();
        if (flowname.indexOf("::") > 0) {
            rootname = flowname.substring(0, flowname.indexOf("::"));
        } else {
            rootname = flowname;
        }
        
        finalpage = config.getFinalPage();
        
        for (PageFlowStepConfig stepConfig : config.getFlowSteps()) {
            FlowStep step = new FlowStep(stepConfig);
            allsteps.add(step);
            stepmap.put(step.getPageName(), step);
        }
        
        if (LOG.isDebugEnabled()) {
            for (int i = 0; i < allsteps.size(); i++) {
                LOG.debug(">>> Workflow '" + config.getFlowName() + "' Step #" + i + " " + allsteps.get(i));
            }
        }
    }

    public boolean containsPage(String page, PageFlowContext context) {
        return stepmap.keySet().contains(page);
    }


    public String getName() {
        return flowname;
    }

    public String getRootName() {
        return rootname;
    }

    public String toString() {
        String ret = "";
        for (int i = 0; i < allsteps.size(); i++) {
            if (ret.length() > 0) {
                ret += ", ";
            } else {
                ret  = flowname + " = ";
            }
            ret += "[" + i + ": " + allsteps.get(i) + "]";
        }
        if (finalpage != null) {
            ret += " FINAL: " + finalpage;
        }
        
        return ret;
    }

    public String findNextPage(PageFlowContext context, boolean stopatcurrentpage, boolean stopatnextaftercurrentpage) throws PustefixApplicationException {
        PageRequest currentpagerequest = context.getCurrentPageRequest();
        FlowStep[] workflow = getAllSteps();
        boolean after_current = false;

        for (int i = 0; i < workflow.length; i++) {
            FlowStep step = workflow[i];
            PageRequest page = context.createPageRequest(step.getPageName());

            if (!context.checkIsAccessible(page, PageRequestStatus.WORKFLOW)) {
                LOG.debug("* Skipping step [" + page + "] in page flow (state is not accessible...)");
            } else {
                LOG.debug("* Page flow is at step " + i + ": [" + page + "]");
                if (stopatcurrentpage && page.equals(currentpagerequest)) {
                    LOG.debug("=> [" + page + "]: Request specified to not advance futher than the original target page.");
                    return page.getRootName();
                }
                if (after_current && (step.wantsToStopHere() || stopatnextaftercurrentpage)) {
                    if (stopatnextaftercurrentpage) {
                        LOG.debug("=> [" + page + "]: Request specified to stop right after the current page");
                    } else {
                        LOG.debug("=> [" + page + "]: Page flow wants to stop here.");
                    }
                    return page.getRootName();
                } else if (context.checkNeedsData(page)) {
                    LOG.debug("=> [" + page + "]: needsData() returned TRUE, leaving page flow.");
                    return page.getRootName();
                } else {
                    LOG.debug("=> [" + page + "]: Page flow or request don't specify to stop and needsData() returned FALSE");
                    LOG.debug("=> [" + page + "]: going to next step in page flow.");
                }
            }
            if (page.equals(currentpagerequest)) {
                after_current = true;
            }
        }
        // If we come here, we need to check for the final page, and use this instead.
        if (finalpage != null) {
            LOG.debug("=> Pageflow [" + getName() + "] defines page [" + finalpage + "] as final page");
            if (!context.checkIsAccessible(context.createPageRequest(finalpage), PageRequestStatus.WORKFLOW)) {
                LOG.debug("   ...but it is not accessible");
            } else {
                return finalpage;
            }
        }
        // if we come here, we have no final page, but if we have been called stopatcurrentpage, as a last hope we will use 
        // the that page as a page to go to. We already know that this page can't be part of the current pageflow (or at least
        // it is not accessible), because in that case we would have stopped earlier while looking through the pageflow. But it 
        // could be that that page is a page external to the current flow, so we just try.
        if (stopatcurrentpage) {
            LOG.debug("=> Request wants us to use original target page [" + currentpagerequest + "] as final page");
            if (!context.checkIsAccessible(context.createPageRequest(currentpagerequest.getRootName()), PageRequestStatus.WORKFLOW)) {
                LOG.debug("   ...but it is not accessible");
            } else {
                return currentpagerequest.getRootName();
            }
        } 
        
        throw new PustefixApplicationException("*** Reached end of page flow '" + getName() + "' " + "without having found a valid, accessible page ***");
        
    }

    public boolean precedingFlowNeedsData(PageFlowContext context) throws PustefixApplicationException {
        PageRequest current = context.getCurrentPageRequest();
        FlowStep[] workflow = getAllSteps();

        for (int i = 0; i < workflow.length; i++) {
            FlowStep step = workflow[i];
            String pagename = step.getPageName();
            PageRequest page = context.createPageRequest(pagename);
            if (pagename.equals(current.getRootName())) {
                return false;
            }
            if (context.checkIsAccessible(page, current.getStatus()) && context.checkNeedsData(page)) {
                return true;
            }
        }
        return false;
    }

    public void addPageFlowInfo(PageFlowContext context, Element root) {
        Document doc = root.getOwnerDocument();
        FlowStep[] steps = getAllSteps();
        String pagename = context.getCurrentPageRequest().getRootName();
        for (int i = 0; i < steps.length; i++) {
            String step = steps[i].getPageName();
            Element stepelem = doc.createElement("step");
            root.appendChild(stepelem);
            stepelem.setAttribute("name", step);
            if (step.equals(pagename)) {
                stepelem.setAttribute("current", "true");
            }
        }
    }

    public void hookAfterRequest(PageFlowContext context, ResultDocument resdoc) throws PustefixApplicationException, PustefixCoreException {
        if (containsPage(context.getCurrentPageRequest().getRootName(), context)) {
            FlowStep current = getFlowStepForPage(context.getCurrentPageRequest().getRootName());
            current.applyActionsOnContinue(context, resdoc);
        }
    }

    public boolean hasHookAfterRequest(PageFlowContext context) {
        if (containsPage(context.getCurrentPageRequest().getRootName(), context)) {
            FlowStep current = getFlowStepForPage(context.getCurrentPageRequest().getRootName());
            return current.hasOnContinueAction();
        }
        return false;
    }

    
    
    private FlowStep[] getAllSteps() {
        return (FlowStep[]) allsteps.toArray(new FlowStep[] {});
    }
    
    private FlowStep getFlowStepForPage(String page) {
        return (FlowStep) stepmap.get(page);
    }


}
