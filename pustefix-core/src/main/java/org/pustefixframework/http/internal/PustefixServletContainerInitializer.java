/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.pustefixframework.http.internal;

import java.util.EnumSet;
import java.util.Set;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.pustefixframework.http.ErrorFilter;
import org.pustefixframework.http.PustefixInitFilter;
import org.springframework.web.filter.DelegatingFilterProxy;

public class PustefixServletContainerInitializer implements ServletContainerInitializer {

    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {

        PustefixInit.initEnvironmentProperties(ctx);

        DelegatingFilterProxy filterProxy = new DelegatingFilterProxy(PustefixInitFilter.class.getName());
        FilterRegistration filterReg = ctx.addFilter("PustefixInitFilter", filterProxy);
        filterReg.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/*");

        filterProxy = new DelegatingFilterProxy(ErrorFilter.class.getName());
        filterReg = ctx.addFilter("ErrorFilter", filterProxy);
        filterReg.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/*");
    }
    
}
