/*
 * Place license here
 */

package org.pustefixframework.config.contextxml.parser;

import java.util.HashSet;
import java.util.Set;

import org.pustefixframework.config.generic.ParsingUtils;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixxml.config.impl.ContextConfigImpl;
import de.schlund.pfixxml.config.impl.ContextResourceConfigImpl;

/**
 * 
 * @author mleidig
 *
 */
public class ContextResourceParsingHandler implements ParsingHandler {
    
    private final Set<String> supportedScopes;
    private final String defaultScope = "session";
    
    public ContextResourceParsingHandler() {
        supportedScopes = new HashSet<String>();
        supportedScopes.add("prototype");
        supportedScopes.add("singleton");
        supportedScopes.add("request");
        supportedScopes.add("session");
    }
    
    public void handleNode(HandlerContext context) throws ParserException {
        
        Element element = (Element)context.getNode();
      
        ContextConfigImpl config = ParsingUtils.getSingleTopObject(ContextConfigImpl.class, context);
        BeanDefinitionRegistry beanReg = ParsingUtils.getSingleTopObject(BeanDefinitionRegistry.class, context);
        
        Set<String> registeredInterfaces = new HashSet<String>();
        
        String className = element.getAttribute("class").trim();
        if(className.length()>0) {
            
            Class<?> implClass = null;
            try {
                implClass = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new ParserException("Could not load class \"" + className + "\"!", e);
            }
            ContextResourceConfigImpl crConfig = new ContextResourceConfigImpl(implClass);
            
            // We add ourself as an "interface", so even if there's no <implements> we still have one
            // name to reference this implementation...
            crConfig.addInterface(implClass);
        
            NodeList itfNodes = element.getElementsByTagName("implements");
            for(int i=0;i<itfNodes.getLength();i++) {
                Element itfElem = (Element)itfNodes.item(i);
                String itfClassName = itfElem.getAttribute("class");
                if(itfClassName==null || itfClassName.trim().equals("")) {
                    throw new ParserException("Element 'interface' requires 'class' attribute for "+
                            "resource class '"+className+"'.");
                }
                if(registeredInterfaces.contains(itfClassName)) {
                    throw new ParserException("ContextResource interface '"+itfClassName+"' is already registered.");
                }
                Class<?> itfClass = null;
                try {
                    itfClass = Class.forName(itfClassName);
                } catch (ClassNotFoundException e) {
                    throw new ParserException("Could not load class \"" + itfClassName + "\"!", e);
                }
                if (!itfClass.isAssignableFrom(crConfig.getContextResourceClass())) {
                    throw new ParserException("ContextResource class " + crConfig.getContextResourceClass() + " does not implement specified interface " + itfClass);
                }
                crConfig.addInterface(itfClass);
            }
            
            String scope = element.getAttribute("scope");
            if(scope == null || scope.trim().equals("")) scope = defaultScope;
            else if(!supportedScopes.contains(scope)) {
                throw new ParserException("Bean scope isn't supported: "+scope);
            }
            
            BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(implClass);
            beanBuilder.setScope(scope);
            BeanDefinition beanDefinition = beanBuilder.getBeanDefinition();
            
            String beanName = element.getAttribute("bean-name").trim();
            if(beanName.length()==0) {
                BeanNameGenerator nameGenerator = new DefaultBeanNameGenerator();
                beanName = nameGenerator.generateBeanName(beanDefinition, beanReg);
            }
            crConfig.setBeanName(beanName);
          
            config.addContextResource(crConfig);
            
            BeanDefinitionHolder beanHolder = new BeanDefinitionHolder(beanDefinition, beanName );
            System.out.println("ADD BEAN: "+beanName);
            beanHolder = ScopedProxyUtils.createScopedProxy(beanHolder, beanReg, true);
            context.getObjectTreeElement().addObject(beanHolder);
        
        }
            
        
              
       
        
    }
    
}
