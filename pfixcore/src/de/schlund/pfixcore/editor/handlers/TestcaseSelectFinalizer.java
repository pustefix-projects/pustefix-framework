package de.schlund.pfixcore.editor.handlers;

        
import org.w3c.dom.Element;

import de.schlund.pfixcore.editor.EditorProduct;
import de.schlund.pfixcore.editor.resources.CRTestcase;
import de.schlund.pfixcore.editor.resources.EditorRes;
import de.schlund.pfixcore.editor.resources.EditorSessionStatus;
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
public class TestcaseSelectFinalizer extends ResdocSimpleFinalizer {

    /**
     * @see de.schlund.pfixcore.workflow.app.ResdocSimpleFinalizer#renderDefault(IWrapperContainer)
     */
    protected void renderDefault(IWrapperContainer container)
        throws Exception {
        Context context = container.getAssociatedContext();
        ContextResourceManager crm = context.getContextResourceManager();
        ResultDocument resdoc = container.getAssociatedResultDocument();
        CRTestcase crtc = EditorRes.getCRTestcase(crm);        

        EditorSessionStatus esess = EditorRes.getEditorSessionStatus(crm);
        EditorProduct product = esess.getProduct();
        String depend = product.getDepend();
        
        String dir = crtc.getAvailableTestcasesDirectoryForProduct();
        
        Element ele = resdoc.createNode("testcases");
        ele.setAttribute("directory", dir);
        
        String[] cases = crtc.getAvailableTestcases();
        
        for(int i=0; i<cases.length; i++) {
            Element e = resdoc.addTextChild(ele, "testcase", cases[i]);
            ele.appendChild(e);
        }
    }

}
