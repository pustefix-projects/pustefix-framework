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

package de.skelexamples;

import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextResourceManager;

/**
 * A class handling the request sent by the action of the form. This class is
 * similar to a Struts Action (inherited from org.apache.struts.action.Action).
 * The Struts method perform is similar to handleSubmittedData.
 * 
 * @author <a href="mailto:rapude@schlund.de">Ralf Rapude </a>
 * @version $Id$
 */
public class DemoTextHandler implements IHandler {

    /**
     * @see de.schlund.pfixcore.generator.IHandler#handleSubmittedData(de.schlund.pfixcore.workflow.Context,
     *      de.schlund.pfixcore.generator.IWrapper)
     */
    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
        ContextResourceManager crm = context.getContextResourceManager();
        ContextDemoText cdemotxt = (ContextDemoText) crm.getResource("de.skelexamples.ContextDemoText");
        DemoText txtwrp = (DemoText) wrapper;
        String txt = txtwrp.getDemoText();

        cdemotxt.setDemoText(txt);
    }

    /**
     * @see de.schlund.pfixcore.generator.IHandler#retrieveCurrentStatus(de.schlund.pfixcore.workflow.Context,
     *      de.schlund.pfixcore.generator.IWrapper)
     */
    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
        ContextResourceManager crm = context.getContextResourceManager();
        ContextDemoText cdemotxt = (ContextDemoText) crm.getResource("de.skelexamples.ContextDemoText");
        DemoText txtwrp = (DemoText) wrapper;
        String txt = cdemotxt.getDemoText();

        if (txt != null) {
            txtwrp.setDemoText(txt);
        }
    }

    public boolean prerequisitesMet(Context context) throws Exception {
        return true;
    }

    public boolean isActive(Context context) throws Exception {
        return true;
    }

    public boolean needsData(Context context) throws Exception {
        ContextResourceManager crm = context.getContextResourceManager();
        ContextDemoText cdemotxt = (ContextDemoText) crm.getResource("de.skelexamples.ContextDemoText");
        if (cdemotxt.getDemoText() != null) {
            return false;
        } else {
            return true;
        }
    }
}
