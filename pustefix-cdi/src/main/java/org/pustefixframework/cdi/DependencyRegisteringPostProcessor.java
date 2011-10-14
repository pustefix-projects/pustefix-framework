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

public class DependencyRegisteringPostProcessor implements BeanFactoryPostProcessor {
 
    private BeanManager beanManager;   
    private List<Bean<Object>> cdiBeans;
     
    public DependencyRegisteringPostProcessor() {
        try {
            InitialContext jndiCtx = new InitialContext();
            //Object obj = jndiCtx.lookup("java:comp/BeanManager");
            
            beanManager = (BeanManager)jndiCtx.lookup("java:comp/env/BeanManager");
            System.out.println("BEANMAN: "+ (beanManager != null));
        } catch(NamingException x) {
            //TODO
            x.printStackTrace();
        }
    }
    
    public DependencyRegisteringPostProcessor(BeanManager beanManager, List<Bean<Object>> cdiBeans) {
        this.beanManager = beanManager;
        this.cdiBeans = cdiBeans;
    }
    
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry)beanFactory;
        Map<String, Bean<Object>> beans = new HashMap<String, Bean<Object>>();
 
        for(Bean<Object> bean: cdiBeans) {
            BeanDefinition beanDefinition = createBeanDefinition(bean);
            String beanName = createBeanName(bean, beanDefinition, registry);         
            registry.registerBeanDefinition(beanName, beanDefinition);
            beans.put(beanName, bean);
        }
 
        CDIScope cdiScope = new CDIScope(beanManager, beans);
        beanFactory.registerScope(CDIScope.class.getName(), cdiScope);
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
