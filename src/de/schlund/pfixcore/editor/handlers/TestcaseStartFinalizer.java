package de.schlund.pfixcore.editor.handlers;

import org.w3c.dom.Element;

import de.schlund.pfixcore.editor.resources.CRTestcase;
import de.schlund.pfixcore.editor.resources.EditorRes;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextResourceManager;
import de.schlund.pfixcore.workflow.app.IWrapperContainer;
import de.schlund.pfixcore.workflow.app.ResdocSimpleFinalizer;
import de.schlund.pfixxml.ResultDocument;

/**
 * @author jh
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class TestcaseStartFinalizer extends ResdocSimpleFinalizer {

    /**
     * @see de.schlund.pfixcore.workflow.app.ResdocSimpleFinalizer#renderDefault(IWrapperContainer)
     */
    protected void renderDefault(IWrapperContainer container)
        throws Exception {
        Context context = container.getAssociatedContext();
        ContextResourceManager crm = context.getContextResourceManager();
        CRTestcase crtc = EditorRes.getCRTestcase(crm);
        ResultDocument resdoc = container.getAssociatedResultDocument();
        
        Element ele = resdoc.createNode("selected_testcases");
        String[] selected = crtc.getTestcasesForProcessing();
        for(int i = 0; i < selected.length; i++) {
            Element e = resdoc.addTextChild(ele, "selected_testcase", selected[i]);
            ele.appendChild(e);
        }
    }

}
