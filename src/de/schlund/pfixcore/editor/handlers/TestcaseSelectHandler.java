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
package de.schlund.pfixcore.editor.handlers;

import java.util.Iterator;

import org.apache.log4j.Category;

import de.schlund.pfixcore.editor.interfaces.TestcaseSelect;
import de.schlund.pfixcore.editor.resources.CRTestcase;
import de.schlund.pfixcore.editor.resources.EditorRes;
import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextResourceManager;

/**
 * 
 * Handler for selecting testcases.
 * <br/>
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */
public class TestcaseSelectHandler implements IHandler {
    private static Category CAT = Category.getInstance(TestcaseSelectHandler.class.getName());
    /**
     * @see de.schlund.pfixcore.generator.IHandler#handleSubmittedData(Context, IWrapper)
     */
    public void handleSubmittedData(Context context, IWrapper wrapper)
        throws Exception {
            TestcaseSelect testcase = (TestcaseSelect)wrapper;
            boolean delete = testcase.getDoDelete().booleanValue();
            boolean select = testcase.getDoSelect().booleanValue();
            String[] foo = testcase.getSelect();
                        
            ContextResourceManager crm = context.getContextResourceManager();
            CRTestcase crtc = EditorRes.getCRTestcase(crm);
            if(select) {
                crtc.setSelectedTestcases(foo);
            } else if(delete) {
                crtc.removeTestcase(foo);
            } else {
                // ERROR?
            }
    }

    /**
     * @see de.schlund.pfixcore.generator.IHandler#retrieveCurrentStatus(Context, IWrapper)
     */
    public void retrieveCurrentStatus(Context context, IWrapper wrapper)
        throws Exception {
        ContextResourceManager crm = context.getContextResourceManager();
        CRTestcase crtc = EditorRes.getCRTestcase(crm);

        if(crtc.hasSelectedTestcases()) {
            String[] foo =  new String[crtc.getSelectedTestcases().size()];
            Iterator iter = crtc.getSelectedTestcases().iterator();
            int i=0;
            while(iter.hasNext()) {
                foo[i++] = (String)iter.next();
            }
        }
    }

    /**
     * @see de.schlund.pfixcore.generator.IHandler#prerequisitesMet(Context)
     */
    public boolean prerequisitesMet(Context context) throws Exception {
        boolean ret = true;
        return ret;
    }

    /**
     * @see de.schlund.pfixcore.generator.IHandler#isActive(Context)
     */
    public boolean isActive(Context context) throws Exception {
        ContextResourceManager crm = context.getContextResourceManager();
        CRTestcase crtc = EditorRes.getCRTestcase(crm);
        boolean ret = crtc.getAvailableTestcases() != null && crtc.getAvailableTestcases().length > 0;
        return ret;
    }

    /**
     * @see de.schlund.pfixcore.generator.IHandler#needsData(Context)
     */
    public boolean needsData(Context context) throws Exception {
        ContextResourceManager crm = context.getContextResourceManager();
        CRTestcase crtc = EditorRes.getCRTestcase(crm);
        boolean ret = !crtc.hasSelectedTestcases();
        return ret;
    }
}
