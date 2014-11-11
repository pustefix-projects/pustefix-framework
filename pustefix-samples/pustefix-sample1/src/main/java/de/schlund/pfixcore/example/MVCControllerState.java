package de.schlund.pfixcore.example;

import org.springframework.data.web.PageableArgumentResolver;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.app.DefaultIWrapperState;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.ResultDocument;

public class MVCControllerState extends DefaultIWrapperState {

    @Override
    public ResultDocument getDocument(Context context, PfixServletRequest preq) throws Exception {
        
        AnnotationMethodHandlerAdapter adapter = new AnnotationMethodHandlerAdapter();
        //adapter.setCustomArgumentResolver(new FilterResolver());
        adapter.setCustomArgumentResolver(new PageableArgumentResolver());
        ModelAndView modelAndView = adapter.handle(preq.getRequest(), null, this);
        ResultDocument resDoc = super.getDocument(context, preq);
        ModelMap modelMap = modelAndView.getModelMap();
        for(String key: modelMap.keySet()) {
            Object value = modelMap.get(key);
            if(value instanceof BindingResult) {
                //TODO: add serializer
            } else {
                ResultDocument.addObject(resDoc.getRootElement(), key, modelMap.get(key));
            }
        }
        return resDoc;
    }
    
   
    
}
