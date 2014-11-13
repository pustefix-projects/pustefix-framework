package de.schlund.pfixcore.example;

import org.pustefixframework.web.mvc.internal.ControllerStateAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.app.DefaultIWrapperState;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.ResultDocument;

public class MVCControllerState extends DefaultIWrapperState {

    @Autowired
    private ControllerStateAdapter adapter;
    
    @Override
    public ResultDocument getDocument(Context context, PfixServletRequest preq) throws Exception {
        
        ModelAndView modelAndView = null;
        try {
            modelAndView = adapter.getAdapter().handle(preq.getRequest(), null, this);
        } catch(NoSuchRequestHandlingMethodException x) {
            //let implementing a handler method be optional and ignore this exception
        }
        ResultDocument resDoc = super.getDocument(context, preq);
        if(modelAndView != null) {
            ModelMap modelMap = modelAndView.getModelMap();
            for(String key: modelMap.keySet()) {
                Object value = modelMap.get(key);
                if(value instanceof BindingResult) {
                    //TODO: add serializer
                } else {
                    ResultDocument.addObject(resDoc.getRootElement(), key, modelMap.get(key));
                }
            }
        }
        return resDoc;
    }

}
