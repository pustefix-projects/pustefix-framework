package org.pustefixframework.sample.basic.handler;

import org.pustefixframework.sample.basic.context.ContextData;
import org.pustefixframework.sample.basic.wrapper.DataWrapper;
import org.springframework.beans.factory.annotation.Autowired;

import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;

public class DataHandler implements IHandler {

    private ContextData contextData;
    
    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
        
        DataWrapper dataWrapper = (DataWrapper)wrapper;
        contextData.setName(dataWrapper.getName());
    }

    public boolean isActive(Context context) throws Exception {
        return true;
    }

    public boolean needsData(Context context) throws Exception {
        return contextData.getName() == null;
    }

    public boolean prerequisitesMet(Context context) throws Exception {
        return true;
    }

    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
        if(contextData.getName() != null) {
            DataWrapper dataWrapper = (DataWrapper)wrapper;
            dataWrapper.setName(contextData.getName());
        }
    }
    
    @Autowired
    public void setContextData(ContextData contextData) {
        this.contextData = contextData;
    }
}
