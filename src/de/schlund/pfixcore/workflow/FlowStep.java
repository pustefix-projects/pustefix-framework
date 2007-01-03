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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Category;
import org.w3c.dom.Document;

import de.schlund.pfixcore.exception.PustefixApplicationException;
import de.schlund.pfixcore.exception.PustefixCoreException;
import de.schlund.pfixcore.workflow.context.PageFlow;
import de.schlund.pfixxml.ResultDocument;
import de.schlund.pfixxml.config.PageFlowStepActionConditionConfig;
import de.schlund.pfixxml.config.PageFlowStepActionConfig;
import de.schlund.pfixxml.config.PageFlowStepConfig;
import de.schlund.pfixxml.util.XPath;

/**
 * @author: jtl@schlund.de
 *
 */

public class FlowStep {

    private ArrayList actions_oncontinue  = new ArrayList();
    private ArrayList tests_oncontinue    = new ArrayList();
    private PageFlowStepConfig config;

    private static Category LOG = Category.getInstance(PageFlow.class.getName());
    public FlowStep(PageFlowStepConfig config) {
        this.config = config;
        
        PageFlowStepActionConditionConfig[] conditions = config.getActionConditions();
        
        for (int i = 0; i < conditions.length; i++) {
            PageFlowStepActionConfig[] actions = conditions[i].getActions();
            ArrayList actionList = new ArrayList();
            for (int j = 0; j < actions.length; j++) {
                FlowStepAction action = FlowStepActionFactory.getInstance().createAction(actions[j].getActionType().getName());
                HashMap datamap = new HashMap();
                Properties params = actions[j].getParams();
                for (Iterator k = params.keySet().iterator(); k.hasNext();) {
                    String key = (String) k.next();
                    String value = params.getProperty(key);
                    datamap.put(key, value);
                }
                action.setData(datamap);
                actionList.add(action);
            }
            actions_oncontinue.add(actionList);
            tests_oncontinue.add(conditions[i].getXPathExpression());
        }

    }

    public void applyActionsOnContinue(Context context, ResultDocument resdoc) throws PustefixApplicationException, PustefixCoreException {
        if (!actions_oncontinue.isEmpty()) {
            for (int i = 0; i < actions_oncontinue.size(); i++) {
                ArrayList actionList = (ArrayList) actions_oncontinue.get(i);
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
                    for (Iterator j = actionList.iterator(); j.hasNext();) {
                        FlowStepAction action = (FlowStepAction) j.next();
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

    public String toString() {
        return("[page=" + this.config.getPage() + " stophere=" + this.config.isStopHere() + " #cont_actions=" + actions_oncontinue.size()+ "]");
    }
}
