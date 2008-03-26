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

    /**
     * Return position of page in the PageFlow, starting with 0. Return -1 if
     * page isn't a member of the PageFlow.
     *
     * @param page a <code>String</code> value
     * @return an <code>int</code> value
     */
    public int getIndexOfPage(String page) {
        FlowStep step = (FlowStep) stepmap.get(page);
        if (step != null) {
            return allsteps.indexOf(step);
        } else {
            return -1;
        }
    }

    public String getName() {
        return flowname;
    }

    public String getRootName() {
        return rootname;
    }

    public FlowStep[] getAllSteps() {
        return (FlowStep[]) allsteps.toArray(new FlowStep[] {});
    }
    
    public FlowStep getFlowStepForPage(String page) {
        return (FlowStep) stepmap.get(page);
    }

    public FlowStep getFirstStep() {
        return (FlowStep) allsteps.get(0);
    }
    
    public String getFinalPage() {
        return finalpage;
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

    public String findNextPage(PageFlowContext context) throws PustefixApplicationException {
        FlowStep[] workflow = getAllSteps();
        PageRequest saved = context.getCurrentPageRequest();
        boolean after_current = false;

        for (int i = 0; i < workflow.length; i++) {
            FlowStep step = workflow[i];
            PageRequest page = context.createPageRequest(step.getPageName());

            if (!context.checkIsAccessible(page, PageRequestStatus.WORKFLOW)) {
                LOG.debug("* Skipping step [" + page + "] in page flow (state is not accessible...)");
                // break;
            } else {
                LOG.debug("* Page flow is at step " + i + ": [" + page + "]");
                if (after_current && (step.wantsToStopHere() || context.isForceStopAtNextStep())) {
                    if (context.isForceStopAtNextStep())
                        LOG.debug("=> Request specifies to act like stophere='true'");
                    LOG.debug("=> [" + page + "]: Page flow wants to stop, getting document now.");
                    return page.getRootName();
                } else if (context.checkNeedsData(page, PageRequestStatus.WORKFLOW)) {
                    LOG.debug("=> [" + page + "]: needsData() returned TRUE, leaving page flow and getting document now.");
                    return page.getRootName();
                } else {
                    LOG.debug("=> [" + page + "]: Page flow doesn't want to stop and needsData() returned FALSE");
                    LOG.debug("=> [" + page + "]: going to next step in page flow.");
                }
            }
            if (saved != null && page.equals(saved)) {
                after_current = true;
            }
        }
        return null;
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
            if (context.checkIsAccessible(page, current.getStatus()) && context.checkNeedsData(page, current.getStatus())) {
                return true;
            }
        }
        return false;
    }

    public void addPageFlowInfo(Element root, PageFlowContext context) {
        Document doc = root.getOwnerDocument();
        FlowStep[] steps = getAllSteps();
        for (int i = 0; i < steps.length; i++) {
            String step = steps[i].getPageName();
            Element stepelem = doc.createElement("step");
            root.appendChild(stepelem);
            stepelem.setAttribute("name", step);
        }
    }

    public void hookAfterRequest(ResultDocument resdoc, PageFlowContext context) throws PustefixApplicationException, PustefixCoreException {
        FlowStep current = getFlowStepForPage(context.getCurrentPageRequest().getRootName());
        current.applyActionsOnContinue(context, resdoc);
    }

    public boolean hasHookAfterRequest(PageFlowContext context) {
        FlowStep current = getFlowStepForPage(context.getCurrentPageRequest().getRootName());
        return current.hasOnContinueAction();
    }

}
