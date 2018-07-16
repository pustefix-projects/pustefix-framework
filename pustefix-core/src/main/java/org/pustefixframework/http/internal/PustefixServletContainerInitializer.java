package org.pustefixframework.http.internal;

import java.util.EnumSet;
import java.util.Set;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.pustefixframework.http.PustefixInitFilter;
import org.springframework.web.filter.DelegatingFilterProxy;

public class PustefixServletContainerInitializer implements ServletContainerInitializer {

    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {

        PustefixInit.initEnvironmentProperties(ctx);
        ctx.addListener(new SessionStatusListenerAdapter());

        DelegatingFilterProxy filterProxy = new DelegatingFilterProxy(PustefixInitFilter.class.getName());
        FilterRegistration filterReg = ctx.addFilter("PustefixInitFilter", filterProxy);
        filterReg.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/*");
    }
    
}
