package de.schlund.pfixcore.editor.handlers;

import de.schlund.pfixcore.editor.interfaces.TestcaseSelect;
import de.schlund.pfixcore.editor.interfaces.TestcaseSelect;
import de.schlund.pfixcore.editor.resources.CRTestcase;
import de.schlund.pfixcore.editor.resources.EditorRes;
import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextResourceManager;

/**
 * @author jh
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class TestcaseSelectHandler implements IHandler {
    
    /**
     * @see de.schlund.pfixcore.generator.IHandler#handleSubmittedData(Context, IWrapper)
     */
    public void handleSubmittedData(Context context, IWrapper wrapper)
        throws Exception {
            TestcaseSelect testcase = (TestcaseSelect)wrapper;
            String[] foo = testcase.getcase();
            for(int i=0; i<foo.length; i++) {
                System.out.println("handle: +++"+foo[i]);
            }
            
            ContextResourceManager crm = context.getContextResourceManager();
            CRTestcase crtc = (CRTestcase) EditorRes.getCRTestcase(crm);
            crtc.setTestcasesForProcessing(foo);
    }

    /**
     * @see de.schlund.pfixcore.generator.IHandler#retrieveCurrentStatus(Context, IWrapper)
     */
    public void retrieveCurrentStatus(Context context, IWrapper wrapper)
        throws Exception {
        ContextResourceManager crm = context.getContextResourceManager();
        CRTestcase crtc = (CRTestcase) EditorRes.getCRTestcase(crm);

        if(crtc.hasTestcasesForProcessing()) {
            TestcaseSelect testcase = (TestcaseSelect)wrapper;
            String[] foo =  crtc.getTestcasesForProcessing();
            for(int i=0; i<foo.length; i++) {
                System.out.println("foo: "+foo[i]);
            }
            testcase.setStringValcase(foo);
        }
    }

    /**
     * @see de.schlund.pfixcore.generator.IHandler#prerequisitesMet(Context)
     */
    public boolean prerequisitesMet(Context context) throws Exception {
        return true;
    }

    /**
     * @see de.schlund.pfixcore.generator.IHandler#isActive(Context)
     */
    public boolean isActive(Context context) throws Exception {
        return true;
    }

    /**
     * @see de.schlund.pfixcore.generator.IHandler#needsData(Context)
     */
    public boolean needsData(Context context) throws Exception {
   
        return false;
    }

   

}
