package de.schlund.pfixcore.editor.handlers;

import org.apache.log4j.Category;

import de.schlund.pfixcore.editor.interfaces.TestcaseStart;
import de.schlund.pfixcore.editor.resources.CRTestcase;
import de.schlund.pfixcore.editor.resources.EditorRes;
import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextResourceManager;
import de.schlund.pfixxml.testenv.TestClientException;


/**
 * Handler for starting selected testcases.
 * 
 * <br/>
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */
public class TestcaseStartHandler implements IHandler {
    private static Category CAT = Category.getInstance(TestcaseStartHandler.class.getName());

    /**
     * @see de.schlund.pfixcore.generator.IHandler#handleSubmittedData(Context, IWrapper)
     */
    public void handleSubmittedData(Context context, IWrapper wrapper)  {
        TestcaseStart tcs = (TestcaseStart) wrapper;
        boolean doit = tcs.getDoStart().booleanValue();
        if(doit) {
            ContextResourceManager crm = context.getContextResourceManager();
            CRTestcase crtc = (CRTestcase) EditorRes.getCRTestcase(crm);
            try {
                crtc.executeTest();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                CAT.error(e);
            } 
        }
    }

    /**
     * @see de.schlund.pfixcore.generator.IHandler#retrieveCurrentStatus(Context, IWrapper)
     */
    public void retrieveCurrentStatus(Context context, IWrapper wrapper)
        throws Exception {
    }

    /**
     * @see de.schlund.pfixcore.generator.IHandler#prerequisitesMet(Context)
     */
    public boolean prerequisitesMet(Context context) throws Exception {
        ContextResourceManager crm = context.getContextResourceManager();
        CRTestcase crtc = (CRTestcase) EditorRes.getCRTestcase(crm);
        boolean ret = crtc.hasSelectedTestcases();
        return ret;
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
        ContextResourceManager crm = context.getContextResourceManager();
        CRTestcase crtc = EditorRes.getCRTestcase(crm);
        boolean ret = !crtc.hasStartedTestcases(); 
        return ret;
    }
}
