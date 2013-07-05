package org.pustefixframework.web.mvc.internal;

import org.pustefixframework.web.mvc.InputHandler;

import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;

/**
 * Adapter making InputHandlers compatible to the IHandler interface.
 * 
 */
public class InputHandlerAdapter implements IHandler {

    private InputHandler<IWrapper> delegate;
    
    @Override
    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
        delegate.handleSubmittedData(wrapper);
    }
    
    @Override
    public boolean isActive(Context context) throws Exception {
        return delegate.isActive();
    }
    
    @Override
    public boolean needsData(Context context) throws Exception {
        return delegate.needsData();
    }
    
    @Override
    public boolean prerequisitesMet(Context context) throws Exception {
        return delegate.prerequisitesMet();
    }
    
    @Override
    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
        delegate.retrieveCurrentStatus(wrapper);
    }

    public void setDelegate(InputHandler<IWrapper> delegate) {
        this.delegate = delegate;
    }

}
