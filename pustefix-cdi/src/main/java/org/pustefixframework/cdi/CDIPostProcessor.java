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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;

public class CDIPostProcessor implements BeanFactoryPostProcessor {
 
    private BeanManager beanManager;   
    private List<Bean<Object>> cdiBeans;
     
    public CDIPostProcessor() {
        try {
            InitialContext jndiCtx = new InitialContext();
            //Object obj = jndiCtx.lookup("java:comp/BeanManager");
            
            beanManager = (BeanManager)jndiCtx.lookup("java:comp/env/BeanManager");
          
            cdiBeans = CDIExtension.getCDIBeans(beanManager);
            
          
        } catch(NamingException x) {
            //TODO
            x.printStackTrace();
        }
    }
    
    public CDIPostProcessor(BeanManager beanManager, List<Bean<Object>> cdiBeans) {
        this.beanManager = beanManager;
        this.cdiBeans = cdiBeans;
    }
    
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry)beanFactory;
        Map<String, Bean<Object>> beans = new HashMap<String, Bean<Object>>();
 
        if(!cdiBeans.isEmpty()) {
            
            //Set BeanFactory for object instance lookup from CDI
            BeanFactoryAdapter beanFactoryAdapter = CDIExtension.getBeanFactoryAdapter(beanManager);
            beanFactoryAdapter.setBeanFactory(beanFactory);
            
            //Register CDI managed beans in Spring
            for(Bean<Object> bean: cdiBeans) {
                BeanDefinition beanDefinition = createBeanDefinition(bean);
                String beanName = createBeanName(bean, beanDefinition, registry);         
                registry.registerBeanDefinition(beanName, beanDefinition);
                beans.put(beanName, bean);
            }
            //Add custom CDI scope to Spring
            CDIScope cdiScope = new CDIScope(beanManager, beans);
            beanFactory.registerScope(CDIScope.class.getName(), cdiScope);
        }
         
        
    }

    private BeanDefinition createBeanDefinition(Bean<Object> bean) {
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(bean.getBeanClass());
        beanDefinition.setScope(CDIScope.class.getName());
        beanDefinition.setLazyInit(true);
        return beanDefinition;
    }
    
    private String createBeanName(Bean<Object> bean, BeanDefinition beanDefinition, BeanDefinitionRegistry registry) {
        String beanName = bean.getName();
        if (beanName == null) {
            beanName = BeanDefinitionReaderUtils.generateBeanName(beanDefinition, registry);
        }
        return beanName;
    }
    
}
