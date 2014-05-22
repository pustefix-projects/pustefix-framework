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

import java.util.Map;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

/**
 * Custom Spring scope for CDI managed beans.
 * Delegates creation/destroying of concrete bean instances
 * to the CDI BeanManager.
 * 
 * @author mleidig@schlund.de
 *
 */
public class CDIScope implements Scope {
    
    private BeanManager beanManager;
    private Map<String, Bean<Object>> cdiBeans;
      
    public CDIScope(BeanManager beanManager, Map<String, Bean<Object>> cdiBeans) {
        this.beanManager = beanManager;
        this.cdiBeans = cdiBeans;
    }
      
    public Object get(String name, ObjectFactory<?> objectFactory) {
        Bean<Object> bean = cdiBeans.get(name);
        CreationalContext<Object> context = beanManager.createCreationalContext(bean);
        return beanManager.getReference(bean, bean.getBeanClass(), context);
    }
      
    public Object remove(String name) {
        Object beanInstance = get(name, null);
        Bean<Object> bean = cdiBeans.get(name);
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