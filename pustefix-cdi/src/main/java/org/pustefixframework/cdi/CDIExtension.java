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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessBean;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Named;

public class CDIExtension implements Extension {
    
    private static Map<BeanManager, List<Bean<Object>>> cdiBeanRegistry = new WeakHashMap<BeanManager, List<Bean<Object>>>();   
    private static Map<BeanManager, List<SpringBean>> unknownBeanRegistry = new WeakHashMap<BeanManager, List<SpringBean>>();
    private static Map<BeanManager, BeanFactoryAdapter> beanFactoryAdapters = new WeakHashMap<BeanManager, BeanFactoryAdapter>();
    
        
    public void processBean(@Observes ProcessBean<Object> procBean, BeanManager manager) {
        //Collect CDI managed beans
        Bean<Object> bean = procBean.getBean();
        if(!(bean instanceof SpringBean))  {
            getCDIBeans(manager).add(bean);
        }
    }
    
    public void beforeBeanDiscovery(@Observes BeforeBeanDiscovery event, BeanManager manager) {
        cdiBeanRegistry.put(manager, new ArrayList<Bean<Object>>());
        unknownBeanRegistry.put(manager, new ArrayList<SpringBean>());
        beanFactoryAdapters.put(manager, new BeanFactoryAdapter());
    }
    
    public void afterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager manager) {
        //Assume unknown beans will be provided by Spring and bridge beans to CDI
        List<SpringBean> beans = getUnknownBeans(manager);
        for(SpringBean bean : beans) {
            event.addBean(bean);
        }
    }
    
    public static List<Bean<Object>> getCDIBeans(BeanManager beanManager) {
        return cdiBeanRegistry.get(beanManager);
    }
    
    public static List<SpringBean> getUnknownBeans(BeanManager beanManager) {
        return unknownBeanRegistry.get(beanManager);
    }
    
    public static BeanFactoryAdapter getBeanFactoryAdapter(BeanManager beanManager) {
        return beanFactoryAdapters.get(beanManager);
    }
    
    public void processInjectionTarget(@Observes ProcessInjectionTarget<?> pit, BeanManager beanManager) {
        Set<InjectionPoint> ips = pit.getInjectionTarget().getInjectionPoints();
        for(InjectionPoint ip : ips) {
            Class<?> clazz = (Class<?>)ip.getType();
            AnnotatedType<?> annotatedType = beanManager.createAnnotatedType(clazz);
            //TODO: check if really no CDI bean by additional criteria
            if(!annotatedType.isAnnotationPresent(Named.class)) {
                Set<Type> beanTypes = annotatedType.getTypeClosure();
                BeanFactoryAdapter beanFactoryAdapter = beanFactoryAdapters.get(beanManager);
                HashSet<Annotation> qualifiers = new HashSet<Annotation>();
                qualifiers.add(new AnnotationLiteral<Any>() {});
                qualifiers.add(new AnnotationLiteral<Default>() {});
                Set<Class<? extends Annotation>> stereotypes = new HashSet<Class<? extends Annotation>>();
                getUnknownBeans(beanManager).add(new SpringBean(beanFactoryAdapter, clazz.getSimpleName(), clazz, beanTypes, qualifiers, stereotypes));
            }
        }
    }
    
}
