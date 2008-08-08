/*
 * Place license here
 */

package org.pustefixframework.config.contextxml.parser;

import org.pustefixframework.config.generic.ParsingUtils;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixcore.workflow.ContextImpl;
import de.schlund.pfixcore.workflow.ContextResourceManagerImpl;
import de.schlund.pfixcore.workflow.context.ServerContextImpl;
import de.schlund.pfixxml.config.impl.ContextConfigImpl;
import de.schlund.pfixxml.config.impl.ContextXMLServletConfigImpl;

/**
 * 
 * @author mleidig
 *
 */
public class ContextParsingHandler implements ParsingHandler {

    public void handleNode(HandlerContext context) throws ParserException {
        
        Element element = (Element)context.getNode();
        ParsingUtils.checkAttributes(element, new String[] {"defaultpage"}, new String[] {"synchronized"});
        
        ContextXMLServletConfigImpl config = ParsingUtils.getSingleTopObject(ContextXMLServletConfigImpl.class, context);
        
        ContextConfigImpl ctxConfig = new ContextConfigImpl();
        // Navigation is stored in depend.xml
        ctxConfig.setNavigationFile(config.getDependFile());
        ctxConfig.setDefaultState(config.getDefaultStaticState());
        ctxConfig.setDefaultPage(element.getAttribute("defaultpage"));
        String syncStr = element.getAttribute("synchronized");
        if (syncStr != null) {
            ctxConfig.setSynchronized(Boolean.parseBoolean(syncStr));
        } else {
            ctxConfig.setSynchronized(true);
        }
        config.setContextConfig(ctxConfig);
        context.getObjectTreeElement().addObject(ctxConfig);
        

        BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(ServerContextImpl.class);
        beanBuilder.setScope("singleton");
        BeanDefinition beanDefinition = beanBuilder.getBeanDefinition();
        BeanDefinitionHolder beanHolder = new BeanDefinitionHolder(beanDefinition, ServerContextImpl.class.getName() );
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
        beanBuilder.addPropertyValue("serverContext", new RuntimeBeanReference(ServerContextImpl.class.getName()));
        beanBuilder.addPropertyValue("contextResourceManager", new RuntimeBeanReference(ContextResourceManagerImpl.class.getName()));
        beanDefinition = beanBuilder.getBeanDefinition();
        beanHolder = new BeanDefinitionHolder(beanDefinition, ContextImpl.class.getName());
        beanHolder = ScopedProxyUtils.createScopedProxy(beanHolder, beanReg, true);
        context.getObjectTreeElement().addObject(beanHolder); 
        
    }

}
