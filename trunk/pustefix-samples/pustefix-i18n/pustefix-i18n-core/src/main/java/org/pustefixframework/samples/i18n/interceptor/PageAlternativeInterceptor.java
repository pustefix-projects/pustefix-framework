package org.pustefixframework.samples.i18n.interceptor;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextInterceptor;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.RequestParam;

public class PageAlternativeInterceptor implements ContextInterceptor {

    public void process(Context context, PfixServletRequest preq) {
        if("Info".equals(preq.getPageName())) {
            RequestParam param = preq.getRequestParam("category");
            if(param != null) {
                String category = param.getValue().trim();
                if(category.length() > 0) {
                    context.setCurrentPageAlternative(category);
                }
            }
        }
        
    }
    
}
