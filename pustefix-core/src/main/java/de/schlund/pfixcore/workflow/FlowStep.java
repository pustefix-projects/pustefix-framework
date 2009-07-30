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

package de.schlund.pfixcore.workflow;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.pustefixframework.config.contextxmlservice.PageFlowStepActionConditionConfig;
import org.pustefixframework.config.contextxmlservice.PageFlowStepActionConfig;
import org.pustefixframework.config.contextxmlservice.PageFlowStepConfig;
import org.w3c.dom.Document;

import de.schlund.pfixcore.exception.PustefixApplicationException;
import de.schlund.pfixcore.exception.PustefixCoreException;
import de.schlund.pfixcore.workflow.context.DataDrivenPageFlow;
import de.schlund.pfixxml.ResultDocument;
import de.schlund.pfixxml.util.XPath;

/**
 * @author: jtl@schlund.de
 *
 */

public class FlowStep {

    private ArrayList<List<FlowStepAction>> actions_oncontinue  = new ArrayList<List<FlowStepAction>>();
    private ArrayList<String> tests_oncontinue = new ArrayList<String>();
    private PageFlowStepConfig config;

    private final static Logger LOG = Logger.getLogger(DataDrivenPageFlow.class);
    public FlowStep(PageFlowStepConfig config) {
        this.config = config;
        
        for (PageFlowStepActionConditionConfig condition : config.getActionConditions()) {
            ArrayList<FlowStepAction> actionList = new ArrayList<FlowStepAction>();
            for (PageFlowStepActionConfig actionConfig : condition.getActions()) {
                FlowStepAction action;
                try {
                    action = actionConfig.getActionType().newInstance();
                } catch (InstantiationException e) {
                    throw new RuntimeException("Could not create instance of class " + actionConfig.getActionType().getName(), e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Could not create instance of class " + actionConfig.getActionType().getName(), e);
                }
                HashMap<String, String> datamap = new HashMap<String, String>();
                Properties params = actionConfig.getParams();
                for (Iterator<?> k = params.keySet().iterator(); k.hasNext();) {
                    String key = (String) k.next();
                    String value = params.getProperty(key);
                    datamap.put(key, value);
                }
                action.setData(datamap);
                actionList.add(action);
            }
            actions_oncontinue.add(actionList);
            tests_oncontinue.add(condition.getXPathExpression());
        }

    }

    public void applyActionsOnContinue(Context context, ResultDocument resdoc) throws PustefixApplicationException, PustefixCoreException {
        if (!actions_oncontinue.isEmpty()) {
            for (int i = 0; i < actions_oncontinue.size(); i++) {
                List<FlowStepAction> actionList = actions_oncontinue.get(i);
                String         test   = (String) tests_oncontinue.get(i);
                LOG.debug("*** [" + this.config.getPage() + "] Trying on-continue-action #" + i);
                boolean check;
                try {
                    check = checkAction(test, resdoc.getSPDocument().getDocument());
                } catch (TransformerException e) {
                    throw new PustefixCoreException("Error while testing XPath expression \"" + test + "\" for page " + this.getPageName(), e);
                }
                if (check) {
                    LOG.debug("    ===> Action applies, calling doAction now...");
                    for (Iterator<FlowStepAction> j = actionList.iterator(); j.hasNext();) {
                        FlowStepAction action = j.next();
                        try {
                            action.doAction(context, resdoc);
                        } catch (Exception e) {
                            throw new PustefixApplicationException("Exception while running doAction() on action " + action.getClass() + " for page " + this.getPageName(), e);
                        }
                    }
                    
                    if (!this.config.isApplyAllConditions()) {
                        break;
                    }
                    
                } else {
                    LOG.debug("    ===> Action didn't apply, continue");
                }
            }
        }
    }

    private boolean checkAction(String test, Document doc) throws TransformerException {
        if (test.equals("")) {
            return true;
        }
        return XPath.test(doc.getDocumentElement(), test);
    }

    public boolean hasOnContinueAction() {
        if (actions_oncontinue.size() > 0) {
            return true;
        } else {
            return false;
        }
    }
    
    public String getPageName() {
        return this.config.getPage();
    }
    
    public String getName() {
        return this.config.getPage();
    }

    public boolean wantsToStopHere() {
        return this.config.isStopHere();
    }

    @Override
    public String toString() {
        return("[page=" + this.config.getPage() + " stophere=" + this.config.isStopHere() + " #cont_actions=" + actions_oncontinue.size()+ "]");
    }
}
