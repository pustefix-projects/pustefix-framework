/*
 * Place license here
 */

package org.pustefixframework.config.contextxml.parser;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import org.pustefixframework.config.contextxml.ContextConfig;
import org.pustefixframework.config.contextxml.PageRequestConfig;
import org.pustefixframework.config.contextxml.parser.internal.ContextXMLServletConfigImpl;
import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.config.generic.PropertyFileReader;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.springframework.beans.factory.support.ManagedMap;

import com.marsching.flexiparse.configuration.RunOrder;
import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixcore.workflow.ContextImpl;
import de.schlund.pfixcore.workflow.ContextResourceManagerImpl;
import de.schlund.pfixcore.workflow.PageMap;
import de.schlund.pfixcore.workflow.context.ServerContextImpl;
import de.schlund.pfixxml.resources.ResourceUtil;

/**
 * 
 * @author mleidig
 *
 */
public class ContextXMLParsingHandler implements ParsingHandler {

    public void handleNode(HandlerContext context) throws ParserException {
        
        if (context.getRunOrder() == RunOrder.START) {
            
            ContextXMLServletConfigImpl ctxConfig = new ContextXMLServletConfigImpl();
            context.getObjectTreeElement().addObject(ctxConfig);
            
            Properties properties = new Properties(System.getProperties());
            try {
                PropertyFileReader.read(ResourceUtil.getFileResourceFromDocroot("common/conf/pustefix.xml"), properties);
            } catch (ParserException e) {
                throw new ParserException("Error while reading common/conf/pustefix.xml", e);
            } catch (IOException e) {
                throw new ParserException("Error while reading common/conf/pustefix.xml", e);
            }
            ctxConfig.setProperties(properties);
            
        } else {
            ContextConfig contextConfig = ParsingUtils.getSingleSubObjectFromRoot(ContextConfig.class, context);
            
            BeanDefinitionBuilder beanBuilder;
            BeanDefinition beanDefinition;
            BeanDefinitionHolder beanHolder;
            DefaultBeanNameGenerator beanNameGenerator = new DefaultBeanNameGenerator();
            BeanDefinitionRegistry beanRegistry = ParsingUtils.getSingleTopObject(BeanDefinitionRegistry.class, context);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> pageMap = new ManagedMap();
            Collection<PageRequestConfig> pageCollection = context.getObjectTreeElement().getObjectsOfTypeFromSubTree(PageRequestConfig.class);
            for (PageRequestConfig pageConfig : pageCollection) {
                pageMap.put(pageConfig.getPageName(), new RuntimeBeanReference(pageConfig.getBeanName()));
            }
            
            beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(PageMap.class);
            beanBuilder.setScope("singleton");
            beanBuilder.addPropertyValue("map", pageMap);
            beanDefinition = beanBuilder.getBeanDefinition();
            String pageMapBeanName = beanNameGenerator.generateBeanName(beanDefinition, beanRegistry);
            beanRegistry.registerBeanDefinition(pageMapBeanName, beanDefinition);
            
            beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(ServerContextImpl.class);
            beanBuilder.setScope("singleton");
            beanBuilder.setInitMethodName("init");
            beanBuilder.addPropertyValue("config", contextConfig);
            beanBuilder.addPropertyReference("pageMap", pageMapBeanName);
            beanDefinition = beanBuilder.getBeanDefinition();
            beanHolder = new BeanDefinitionHolder(beanDefinition, ServerContextImpl.class.getName() );
            context.getObjectTreeElement().addObject(beanHolder);
            
            BeanDefinitionRegistry beanReg = ParsingUtils.getSingleTopObject(BeanDefinitionRegistry.class, context);
            
            beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(ContextResourceManagerImpl.class);
            beanBuilder.setScope("session");
            beanDefinition = beanBuilder.getBeanDefinition();
            beanHolder = new BeanDefinitionHolder(beanDefinition, ContextResourceManagerImpl.class.getName());
            beanHolder = ScopedProxyUtils.createScopedProxy(beanHolder, beanReg, true);
            context.getObjectTreeElement().addObject(beanHolder); 
            
            beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(ContextImpl.class);
            beanBuilder.setScope("session");
            beanBuilder.addPropertyReference("serverContext", ServerContextImpl.class.getName());
            beanBuilder.addPropertyReference("contextResourceManager", ContextResourceManagerImpl.class.getName());
            beanDefinition = beanBuilder.getBeanDefinition();
            beanHolder = new BeanDefinitionHolder(beanDefinition, ContextImpl.class.getName());
            beanHolder = ScopedProxyUtils.createScopedProxy(beanHolder, beanReg, true);
            context.getObjectTreeElement().addObject(beanHolder); 
            
        }
    }

}
