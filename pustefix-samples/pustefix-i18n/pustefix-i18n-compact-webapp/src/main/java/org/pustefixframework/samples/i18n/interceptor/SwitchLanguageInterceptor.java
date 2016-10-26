package org.pustefixframework.samples.i18n.interceptor;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextInterceptor;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.RequestParam;

public class SwitchLanguageInterceptor implements ContextInterceptor {
    
    @Override
    public void process(Context context, PfixServletRequest preq) {
        RequestParam param = preq.getRequestParam("lang");
        if(param != null) {
            String lang = param.getValue();
            context.setLanguage(lang);
            System.out.println("!!! Switch language to '" + lang + "' !!!");
        }
        
    }
    
}
