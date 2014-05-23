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
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessBean;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
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
            Set<Bean<?>> found = null;
            if(bean.getName() != null) {
                found = manager.getBeans(bean.getName());
            } 
            if(found == null || found.size() == 0) {
                Annotation[] qualifiers = bean.getQualifiers().toArray(new Annotation[bean.getQualifiers().size()]);
                found = manager.getBeans(bean.getBeanClass(), qualifiers);
            }
            if(found.size() == 0) {
                event.addBean(bean);
            }
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
            
            BeanFactoryAdapter beanFactoryAdapter = beanFactoryAdapters.get(beanManager);
            Class<?> clazz = (Class<?>)ip.getType();
            Annotated annotated = ip.getAnnotated();
            
            String name = null;
            Named named = annotated.getAnnotation(Named.class);
            if(named != null) {
                name = named.value();
                //if(name.equals("")) {
                //    name = clazz.getSimpleName();
                //    name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
                //}
            }
            
            AnnotatedType<?> annotatedType = beanManager.createAnnotatedType(clazz);
            Set<Type> beanTypes = annotatedType.getTypeClosure();
            
            Set<Annotation> qualifiers = new HashSet<Annotation>();
            Set<Class<? extends Annotation>> stereoTypes = new HashSet<Class<? extends Annotation>>();
            Set<Annotation> annotations = ip.getAnnotated().getAnnotations();
            for(Annotation annotation: annotations) {
                if(beanManager.isQualifier(annotation.annotationType())) {
                    qualifiers.add(annotation);
                } else if(beanManager.isStereotype(annotation.annotationType())) {
                    stereoTypes.add(annotation.annotationType());
                }
            }
            
            SpringBean bean = new SpringBean(beanFactoryAdapter, name, clazz, beanTypes, qualifiers, stereoTypes);
            
            getUnknownBeans(beanManager).add(bean);
            
        }
    }

}
