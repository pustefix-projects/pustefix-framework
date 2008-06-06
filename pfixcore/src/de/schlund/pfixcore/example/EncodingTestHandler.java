/*
 * Created on 19.02.2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.schlund.pfixcore.example;

import de.schlund.pfixcore.example.iwrapper.EncodingTest;
import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;

/**
 * @author ml
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class EncodingTestHandler implements IHandler {

    public void handleSubmittedData(Context context,IWrapper wrapper) throws Exception {
        EncodingTest test     = (EncodingTest)wrapper;
        String       encoding = test.getEncoding();
        if (encoding == null || encoding.trim().equals("") || encoding.equals("none")) {
            ContextEncodingTest ctx = context.getContextResourceManager().getResource(ContextEncodingTest.class);
            ctx.setText(test.getText());
        }
    }
    
    public void retrieveCurrentStatus(Context context,IWrapper wrapper) throws Exception {
        EncodingTest        test = (EncodingTest)wrapper;
        ContextEncodingTest ctx  = context.getContextResourceManager().getResource(ContextEncodingTest.class);
        if (ctx.getText() != null) test.setText(ctx.getText());
    }
    
    public boolean needsData(Context context) throws Exception {
        return false;
    }
    
    public boolean prerequisitesMet(Context context) throws Exception {
        return true;
    }

    public boolean isActive(Context context) throws Exception {
        return true;
    }
    
}
