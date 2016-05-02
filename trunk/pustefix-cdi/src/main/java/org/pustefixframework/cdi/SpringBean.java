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
package org.pustefixframework.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;

/**
 * CDI Bean interface implementation for Spring beans.
 * Delegates creation/destroying of concrete bean instances
 * to a Spring BeanFactory.
 * 
 * @author mleidig@schlund.de
 *
 */
public class SpringBean implements Bean<Object> {
    
    private String beanName;
    private Class<?> beanClass;
    private Set<Type> beanTypes;
    private Set<Annotation> qualifiers;
    private Set<Class<? extends Annotation>> stereotypes;
    private BeanFactoryAdapter beanFactory;
       
    public SpringBean(BeanFactoryAdapter beanFactory, String beanName, Class<?> beanClass, 
            Set<Type> beanTypes, Set<Annotation> qualifiers, Set<Class<? extends Annotation>> stereotypes) {
        this.beanFactory = beanFactory;
        this.beanName = beanName;
        this.beanClass = beanClass;
        this.beanTypes = beanTypes;
        this.qualifiers = qualifiers;
        this.stereotypes = stereotypes;
    }
        
    public String getName() {
        return beanName;
    }
   
    public Class<?> getBeanClass() {
        return beanClass;
    }
       
    public Set<Type> getTypes() {
        return beanTypes;
    }
   
    public Set<Annotation> getQualifiers() {
        return qualifiers;
    }
       
    public Set<Class<? extends Annotation>> getStereotypes() {
        return stereotypes;
    }
   
    public Class<? extends Annotation> getScope() {
        return Dependent.class;
    }
    
    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.emptySet();
    }
        
    public boolean isAlternative() {
        return false;
    }
   
    public boolean isNullable() {
        return true;
    }
   
    public Object create(CreationalContext<Object> creationalContext) {
        return beanFactory.getBean(beanName);
    }
   
    public void destroy(Object beanInstance, CreationalContext<Object> creationalContext) {
        beanFactory.destroyBean(beanName, beanInstance);
    }

}