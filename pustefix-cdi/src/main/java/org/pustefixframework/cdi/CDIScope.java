package org.pustefixframework.cdi;

import java.util.Map;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

public class CDIScope implements Scope {
    
    private BeanManager beanManager;
    private Map<String, Bean<Object>> beans;
      
    public CDIScope(BeanManager beanManager, Map<String, Bean<Object>> beans) {
        this.beanManager = beanManager;
        this.beans = beans;
    }
      
    public Object get(String name, ObjectFactory<?> objectFactory) {
        Bean<Object> bean = beans.get(name);
        CreationalContext<Object> context = beanManager.createCreationalContext(bean);
        return beanManager.getReference(bean, bean.getBeanClass(), context);
    }
      
    public Object remove(String name) {
        Object beanInstance = get(name, null);
        Bean<Object> bean = beans.get(name);
        CreationalContext<Object> context = beanManager.createCreationalContext(bean);
        bean.destroy(beanInstance, context);
        return beanInstance;
    }
    
    public String getConversationId() {
        return null;
    }
    
    public Object resolveContextualObject(String key) {
        return null;
    }
    
    public void registerDestructionCallback(String name, Runnable callback) {   
    }
     
}