package de.schlund.pfixcore.example;

import java.io.File;

import de.schlund.pfixcore.example.iwrapper.EncodingTestUpload;
import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;

/**
 * @author mleidig@schlund.de
 */
public class EncodingTestUploadHandler implements IHandler {

	public void handleSubmittedData(Context context,IWrapper wrapper) throws Exception {
        EncodingTestUpload upload=(EncodingTestUpload)wrapper;
        File file=upload.getFile();
        String text=upload.getText();
        ContextEncodingTest ctx=context.getContextResourceManager().getResource(ContextEncodingTest.class);
        if(text!=null) ctx.setText(text);
        if(file!=null) ctx.setFile(file);
    }
    
    public void retrieveCurrentStatus(Context context,IWrapper wrapper) throws Exception {
        EncodingTestUpload upload=(EncodingTestUpload)wrapper;
        ContextEncodingTest ctx=context.getContextResourceManager().getResource(ContextEncodingTest.class);
        if(ctx.getText()!=null) upload.setText(ctx.getText());
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
