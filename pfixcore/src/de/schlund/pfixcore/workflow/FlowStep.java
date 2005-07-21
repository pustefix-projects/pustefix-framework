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


import de.schlund.pfixcore.util.PropertiesUtils;
import de.schlund.pfixxml.ResultDocument;
import de.schlund.pfixxml.util.XPath;
import java.util.*;
import org.apache.log4j.Category;

import org.w3c.dom.Document;
import javax.xml.transform.TransformerException;

/**
 * @author: jtl@schlund.de
 *
 */

public class FlowStep {
    private String  page;
    private boolean isstophere = false;

    private boolean   applyall_oncontinue = false;
    private ArrayList actions_oncontinue  = new ArrayList();
    private ArrayList tests_oncontinue    = new ArrayList();

    private static Category     LOG               = Category.getInstance(PageFlow.class.getName());
    private final static String PROPERTY_PAGEFLOW = "context.pageflowproperty";
    private final static String ACTION_PAGEFLOW   = "context.pageflowaction";
    
    public FlowStep(String page, Properties props, String flowname) {
        this.page      = page;
        String prefix  = PROPERTY_PAGEFLOW + "." + flowname + "." + page;
        Map    propmap = PropertiesUtils.selectProperties(props, prefix);
        String stop    = (String) propmap.get("stophere");
        if (stop != null && stop.equals("true")) {
            isstophere = true;
        }
        String all_cont = (String) propmap.get("oncontinue.applyall");
        if (all_cont != null && all_cont.equals("true")) {
            applyall_oncontinue = true;
        }
        
        prefix  = ACTION_PAGEFLOW + "." + flowname + "." + page + ".oncontinue";
        propmap = PropertiesUtils.selectProperties(props, prefix);

        if (propmap != null && !propmap.isEmpty()) {
            int count = 1;
            while (true) {
                String testkey = count + ".test";
                String test    = (String) propmap.get(testkey);
                if (test == null) {
                    break;
                }
                String actionkey = count + ".action";
                String action    = (String) propmap.get(actionkey);
                
                String     datakey = count + ".data";
                Properties tmp     = new Properties();
                tmp.putAll(propmap);
                HashMap datamap = PropertiesUtils.selectProperties(tmp, datakey);
                
                FlowStepAction continueact = FlowStepActionFactory.getInstance().createAction(action);
                continueact.setData(datamap);
                actions_oncontinue.add(continueact);
                tests_oncontinue.add(test);
                
                count++;
            }
        }
    }

    public void applyActionsOnContinue(Context context, ResultDocument resdoc) throws Exception {
        if (!actions_oncontinue.isEmpty()) {
            for (int i = 0; i < actions_oncontinue.size(); i++) {
                FlowStepAction action = (FlowStepAction) actions_oncontinue.get(i);
                String         test   = (String) tests_oncontinue.get(i);
                LOG.debug("*** [" + page + "] Trying on-continue-action #" + i);
                if (checkAction(test, resdoc.getSPDocument().getDocument())) {
                    LOG.debug("    ===> Action applies, calling doAction now...");
                    action.doAction(context, resdoc);
                    if (!applyall_oncontinue) {
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
        return page;
    }
    
    public String getName() {
        return page;
    }

    public boolean wantsToStopHere() {
        return isstophere;
    }

    public String toString() {
        return("[page=" + page + " stophere=" + isstophere + " #cont_actions=" + actions_oncontinue.size()+ "]");
    }
}
