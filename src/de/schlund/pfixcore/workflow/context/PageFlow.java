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

import de.schlund.pfixcore.workflow.FlowStep;
import de.schlund.pfixxml.config.PageFlowConfig;
import de.schlund.pfixxml.config.PageFlowStepConfig;

/**
 * @author: jtl
 *
 */

public class PageFlow {
    private String    flowname;
    private String    rootname;
    private ArrayList allsteps = new ArrayList();
    private HashMap   stepmap  = new HashMap();
    private String    finalpage;
    
    private final static String PROPERTY_PREFIX   = PageFlowManager.PROP_PREFIX;
    private final static String FLAG_FINAL        = "FINAL";
    private static Logger       LOG               = Logger.getLogger(PageFlow.class);
    
    public PageFlow(PageFlowConfig config) {
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

    public boolean containsPage(String page) {
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

}
