package org.pustefixframework.web.mvc.filter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class FilterResolver implements WebArgumentResolver, HandlerMethodArgumentResolver {
    
    @Override
    public Object resolveArgument(MethodParameter methodParameter,
            NativeWebRequest webRequest) throws Exception {
        
        if(methodParameter.getParameterType() == Filter.class) {   
            List<Filter> propFilters = new ArrayList<Filter>();
            Iterator<String> paramNames = webRequest.getParameterNames();
            while(paramNames.hasNext()) {
                String paramName = paramNames.next();
                if(paramName.startsWith("filter.")) {
                    String property = paramName.substring(7).trim();
                    if(!property.isEmpty()) {
                        String[] values = webRequest.getParameterValues(paramName);
                        if(values != null && values.length > 0) {
                            if(values.length == 1) {
                                Property filter =  new Property(property, values[0]);
                                propFilters.add(filter);
                            } else {
                                Property[] propSpecs = new Property[values.length];
                                for(int i=0; i<values.length; i++) {
                                    propSpecs[i] = new Property(property, values[i]);
                                }
                                propFilters.add(new Or(propSpecs));
                            }
                        }
                    }
                }
            }
            if(propFilters.size() == 1) {
                return propFilters.get(0);
            } else {
                return new And(propFilters.toArray(new Filter[propFilters.size()])); 
            }
        }
        return WebArgumentResolver.UNRESOLVED;
    }
    
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        try {
            return resolveArgument(parameter, webRequest);
        } catch(Exception x) {
            throw new RuntimeException(x);
        }
    }

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return methodParameter.getParameterType() == Filter.class;
    }

}
