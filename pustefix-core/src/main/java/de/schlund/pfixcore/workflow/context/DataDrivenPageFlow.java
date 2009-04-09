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

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.pustefixframework.config.contextxmlservice.PageFlowConfig;
import org.pustefixframework.config.contextxmlservice.PageFlowStepConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.schlund.pfixcore.exception.PustefixApplicationException;
import de.schlund.pfixcore.exception.PustefixCoreException;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.FlowStep;
import de.schlund.pfixcore.workflow.PageFlowContext;
import de.schlund.pfixxml.ResultDocument;

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

    public boolean containsPage(String pagename) {
        return stepmap.keySet().contains(pagename);
    }


    public String getName() {
        return flowname;
    }

    public String getRootName() {
        return rootname;
    }

    @Override
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

    public String findNextPage(PageFlowContext context, String currentpagename, boolean stopatcurrentpage, boolean stopatnextaftercurrentpage) throws PustefixApplicationException {
        FlowStep[] workflow = getAllSteps();
        boolean after_current = false;

        for (int i = 0; i < workflow.length; i++) {
            FlowStep step = workflow[i];
            String   stepname = step.getPageName();

            if (!context.checkIsAccessible(stepname)) {
                LOG.debug("* Skipping step [" + stepname + "] in page flow (state is not accessible...)");
            } else {
                LOG.debug("* Page flow is at step " + i + ": [" + stepname + "]");
                if (stopatcurrentpage && stepname.equals(currentpagename)) {
                    LOG.debug("=> [" + stepname + "]: Request specified to not advance futher than the original target page.");
                    return stepname;
                }
                if (after_current && (step.wantsToStopHere() || stopatnextaftercurrentpage)) {
                    if (stopatnextaftercurrentpage) {
                        LOG.debug("=> [" + stepname + "]: Request specified to stop right after the current page");
                    } else {
                        LOG.debug("=> [" + stepname + "]: Page flow wants to stop here.");
                    }
                    return stepname;
                } else if (context.checkNeedsData(stepname)) {
                    LOG.debug("=> [" + stepname + "]: needsData() returned TRUE, leaving page flow.");
                    return stepname;
                } else {
                    LOG.debug("=> [" + stepname + "]: Page flow or request don't specify to stop and needsData() returned FALSE");
                    LOG.debug("=> [" + stepname + "]: going to next step in page flow.");
                }
            }
            if (stepname.equals(currentpagename)) {
                after_current = true;
            }
        }
        // If we come here, we need to check for the final page, and use this instead.
        if (finalpage != null) {
            LOG.debug("=> Pageflow [" + getName() + "] defines page [" + finalpage + "] as final page");
            if (!context.checkIsAccessible(finalpage)) {
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
            LOG.debug("=> Request wants us to use original target page [" + currentpagename + "] as final page");
            if (!context.checkIsAccessible(currentpagename)) {
                LOG.debug("   ...but it is not accessible");
            } else {
                return currentpagename;
            }
        } 
        
        throw new PustefixApplicationException("*** Reached end of page flow '" + getName() + "' " + "without having found a valid, accessible page ***");
        
    }

    public boolean precedingFlowNeedsData(PageFlowContext context, String currentpagename) throws PustefixApplicationException {
        FlowStep[] workflow = getAllSteps();

        for (int i = 0; i < workflow.length; i++) {
            FlowStep step = workflow[i];
            String pagename = step.getPageName();
            if (pagename.equals(currentpagename)) {
                return false;
            }
            if (context.checkIsAccessible(pagename) && context.checkNeedsData(pagename)) {
                return true;
            }
        }
        return false;
    }

    public void addPageFlowInfo(String currentpagename, Element root) {
        Document doc = root.getOwnerDocument();
        FlowStep[] steps = getAllSteps();
        for (int i = 0; i < steps.length; i++) {
            String step = steps[i].getPageName();
            Element stepelem = doc.createElement("step");
            root.appendChild(stepelem);
            stepelem.setAttribute("name", step);
            if (step.equals(currentpagename)) {
                stepelem.setAttribute("current", "true");
            }
        }
    }

    public void hookAfterRequest(Context context, ResultDocument resdoc) throws PustefixApplicationException, PustefixCoreException {
    	String currentpagename = context.getCurrentPageRequest().getRootName();
    	if (containsPage(currentpagename)) {
            FlowStep current = stepmap.get(currentpagename);
            current.applyActionsOnContinue(context, resdoc);
        }
    }

    public boolean hasHookAfterRequest(String currentpagename) {
    	if (containsPage(currentpagename)) {
            FlowStep current = stepmap.get(currentpagename);
            return current.hasOnContinueAction();
        }
        return false;
    }


    private FlowStep[] getAllSteps() {
        return (FlowStep[]) allsteps.toArray(new FlowStep[] {});
    }

}
