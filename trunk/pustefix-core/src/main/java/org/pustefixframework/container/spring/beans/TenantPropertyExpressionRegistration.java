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
package org.pustefixframework.container.spring.beans;

import java.lang.reflect.Method;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.expression.StandardBeanExpressionResolver;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.MethodExecutor;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.support.ReflectiveMethodResolver;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import de.schlund.pfixxml.Tenant;

/**
 * Makes tenantProperty() function available in Spring expression language.
 * Using the expression "#{tenantProperty('myproperty')}" you can set tenant-specific
 * bean property values. Tenant-specific values are configured by prefixing or suffixing 
 * the actual property name with the according tenant name, e.g. 'mytenant.myproperty'
 */
public class TenantPropertyExpressionRegistration implements BeanFactoryPostProcessor {

    private ConfigurableListableBeanFactory beanFactory;
    private boolean prefixProperties = true;
    private char nameSeparator = '.';
    
    /**
     * Set if the tenant name should prefix the property name or vice versa.
     * By default it's prefixed, e.g. 'mytenant.myproperty'. Otherwise it's
     * suffixed, e.g. 'myproperty.mytenant'.
     */
    public void setPrefixProperties(boolean prefixProperties) {
        this.prefixProperties = prefixProperties;
    }
    
    /**
     * Set how tenant name and property name should be separated.
     * The default separator is a dot, e.g. "mytenant.myproperty".
     */
    public void setNameSeparator(char nameSeparator) {
        this.nameSeparator = nameSeparator; 
    }
    
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

        this.beanFactory = beanFactory;
        beanFactory.setBeanExpressionResolver(new StandardBeanExpressionResolver() {
            @Override
            protected void customizeEvaluationContext(StandardEvaluationContext evalContext) {
                evalContext.addMethodResolver(new TenantMethodResolver());
            }
        });
    }

    public String getTenantProperty(final String param) {
    
        RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
        Tenant tenant = (Tenant)attributes.getAttribute(TenantScope.REQUEST_ATTRIBUTE_TENANT, RequestAttributes.SCOPE_REQUEST);
    
        String propertyName;
        if(tenant == null) {
            propertyName = param;
        } else {
            if(prefixProperties) {
                propertyName = tenant.getName() + nameSeparator + param;
            } else {
                propertyName = param + nameSeparator + tenant.getName();
            }
        }
        return beanFactory.resolveEmbeddedValue("${" + propertyName + "}");
    }

    class TenantMethodResolver extends ReflectiveMethodResolver {

        @Override
        public MethodExecutor resolve(EvaluationContext context, Object targetObject, String name,
                List<TypeDescriptor> argumentTypes) throws AccessException {

            if ("tenantProperty".equals(name)) {
                return new TenantMethodExecutor();
            }
            return super.resolve(context, targetObject, name, argumentTypes);
        }
    }

    class TenantMethodExecutor implements MethodExecutor {

        @Override
        public TypedValue execute(EvaluationContext context, Object target,    Object... arguments) throws AccessException {
    
            try {
                String property = getTenantProperty((String)arguments[0]);
                Method method = TenantPropertyExpressionRegistration.class.getDeclaredMethod("getTenantProperty", new Class[] {String.class});
                TypeDescriptor descriptor = new TypeDescriptor(new MethodParameter(method, -1));
                return new TypedValue(property,    descriptor);
    
            } catch (NoSuchMethodException e) {
                throw new AccessException("Error getting tenant property", e);
            }
        }
    
    }

}