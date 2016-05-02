package org.pustefixframework.container.spring.http;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;


public class MVCStateHandlerMapping {
    
    public static String[] determineUrlsForHandlerMethods(Class<?> handlerType) {
        
        final Set<String> urls = new LinkedHashSet<String>();
        Set<Class<?>> handlerTypes = new LinkedHashSet<Class<?>>();
        handlerTypes.add(handlerType);
        handlerTypes.addAll(Arrays.asList(handlerType.getInterfaces()));
        for (Class<?> currentHandlerType : handlerTypes) {
            ReflectionUtils.doWithMethods(currentHandlerType, new ReflectionUtils.MethodCallback() {
                public void doWith(Method method) {
                    RequestMapping mapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);
                    if (mapping != null) {
                        String[] mappedPatterns = mapping.value();
                        if (mappedPatterns.length > 0) {
                            for (String mappedPattern : mappedPatterns) {
                                
                                addUrlsForPath(urls, mappedPattern);
                            }
                        }
                    }
                }
            }, ReflectionUtils.USER_DECLARED_METHODS);
        }
        return StringUtils.toStringArray(urls);
    }
    
    private static void addUrlsForPath(Set<String> urls, String path) {
        urls.add(path);
        if (path.indexOf('.') == -1 && !path.endsWith("/")) {
            urls.add(path + ".*");
            urls.add(path + "/");
        }
    }
}
