package de.schlund.pfixcore.editor.handlers;

import org.apache.log4j.Category;

import de.schlund.pfixcore.editor.interfaces.TestcaseReset;
import de.schlund.pfixcore.editor.resources.CRTestcase;
import de.schlund.pfixcore.editor.resources.EditorRes;
import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextResourceManager;

/**
 * Handler for resetting the ContextResource.
 * <br/>
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */
public class TestcaseResetHandler implements IHandler {

    private static Category CAT = Category.getInstance(TestcaseResetHandler.class.getName());

    /**
     * @see de.schlund.pfixcore.generator.IHandler#handleSubmittedData(Context, IWrapper)
     */
    public void handleSubmittedData(Context context, IWrapper wrapper)
        throws Exception {
        TestcaseReset reset = (TestcaseReset) wrapper;
        Boolean do_reset = reset.getReset();
        if(do_reset != null && do_reset.booleanValue() == Boolean.TRUE.booleanValue()) {
            if(CAT.isDebugEnabled()) {
                CAT.debug("TestcaseResetHandler: resetting ContextResource ");
            }
            ContextResourceManager crm = context.getContextResourceManager();
            CRTestcase crtc = EditorRes.getCRTestcase(crm);
            crtc.doReset();
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
