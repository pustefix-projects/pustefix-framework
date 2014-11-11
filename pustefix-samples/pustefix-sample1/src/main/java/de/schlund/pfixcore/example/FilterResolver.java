package de.schlund.pfixcore.example;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.context.request.NativeWebRequest;

public class FilterResolver implements WebArgumentResolver {
    
    @Override
    public Object resolveArgument(MethodParameter methodParameter,
            NativeWebRequest webRequest) throws Exception {
        
        if(methodParameter.getParameterType() == Filter.class) {
            /**
            Filter pageable = new Pageable();
           
            String value = webRequest.getParameter("page.size");
            if(value != null) {
                int pageSize = Integer.parseInt(value);
                pageable.setSize(pageSize);
            }
            
            value = webRequest.getParameter("page.no");
            if(value != null) {
                int pageNo = Integer.parseInt(value);
                pageable.setNo(pageNo);
            }
            return pageable;
            */
        }
        
        return WebArgumentResolver.UNRESOLVED;
    }
    
}
