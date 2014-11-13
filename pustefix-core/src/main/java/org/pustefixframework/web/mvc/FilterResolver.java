package org.pustefixframework.web.mvc;

import java.util.Iterator;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.context.request.NativeWebRequest;

public class FilterResolver implements WebArgumentResolver {
    
    @Override
    public Object resolveArgument(MethodParameter methodParameter,
            NativeWebRequest webRequest) throws Exception {
        
        if(methodParameter.getParameterType() == Filter.class) {
            
            Filter filter = new Filter();
            Iterator<String> paramNames = webRequest.getParameterNames();
            while(paramNames.hasNext()) {
                String paramName = paramNames.next();
                if(paramName.startsWith("filter.")) {
                    String property = paramName.substring(7);
                    String value = webRequest.getParameter(paramName);
                    filter.setProperty(property);
                    filter.setValue(value);
                }
            }
            //TODO: support multiple/complex filters, validation
            return filter;
        }
        
        return WebArgumentResolver.UNRESOLVED;
    }
    
}
