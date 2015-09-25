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
package org.pustefixframework.eventbus;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

/**
 * BeanPostProcessor checking Spring managed beans for Subscribe annotation
 * and registering them as EventSubscriber objects at the EventBus.
 */
public class EventSubscriberBeanPostProcessor implements BeanPostProcessor, Ordered {

    private final Set<Class<?>> nonAnnotatedClasses = Collections.newSetFromMap(new ConcurrentHashMap<Class<?>, Boolean>());

    @Autowired
    EventBus eventBus;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, String beanName) throws BeansException {
        if (!this.nonAnnotatedClasses.contains(bean.getClass()) && !ScopedProxyUtils.isScopedTarget(beanName)) {

            final Set<Method> annotatedMethods = new LinkedHashSet<Method>(1);
            final Class<?> targetClass = AopUtils.getTargetClass(bean);
            ReflectionUtils.doWithMethods(targetClass, new ReflectionUtils.MethodCallback() {
                @Override
                public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                    Subscribe anno = AnnotationUtils.getAnnotation(method, Subscribe.class);
                    if (anno != null) {
                        annotatedMethods.add(method);
                        Class<?>[] parameters = method.getParameterTypes();
                        if(parameters.length == 1) {
                            EventSubscriber subscriber = new EventSubscriber();
                            subscriber.setBean(bean);
                            subscriber.setMethod(method);
                            subscriber.setEventType(parameters[0]);
                            eventBus.addSubscriber(subscriber);
                        } else {
                            throw new FatalBeanException("Method " + targetClass.getName() + "." + method.getName() +
                                    "(...) has wrong number of parameters. Methods annotated with @Subscribe require " +
                                    "exactly one parameter for the event object.");
                        }
                    }
                }
            });
            if (annotatedMethods.isEmpty()) {
                this.nonAnnotatedClasses.add(bean.getClass());
            }
        }
        return bean;
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }

}
