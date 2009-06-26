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

package org.pustefixframework.config.contextxmlservice.parser;

import java.util.HashSet;
import java.util.Set;

import org.pustefixframework.config.contextxmlservice.parser.internal.ContextConfigImpl;
import org.pustefixframework.config.contextxmlservice.parser.internal.ContextResourceConfigImpl;
import org.pustefixframework.config.generic.ParsingUtils;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.springframework.osgi.context.ConfigurableOsgiBundleApplicationContext;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;


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
        ParsingUtils.checkAttributes(element, new String[] {"class"}, new String[] {"bean-name", "scope"});
        
        ContextConfigImpl config = ParsingUtils.getSingleTopObject(ContextConfigImpl.class, context);
        BeanDefinitionRegistry beanReg = ParsingUtils.getSingleTopObject(BeanDefinitionRegistry.class, context);
        ConfigurableOsgiBundleApplicationContext appContext = ParsingUtils.getSingleTopObject(ConfigurableOsgiBundleApplicationContext.class, context);
        
        Set<String> registeredInterfaces = new HashSet<String>();
        
        String className = element.getAttribute("class").trim();
       
        Class<?> implClass = null;
        try {
            implClass = Class.forName(className, true, appContext.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new ParserException("Could not load class \"" + className + "\"!", e);
        }
        ContextResourceConfigImpl crConfig = new ContextResourceConfigImpl(implClass);
            
        // We add ourself as an "interface", so even if there's no <implements> we still have one
        // name to reference this implementation...
        crConfig.addInterface(implClass);
        
        NodeList itfNodes = element.getElementsByTagNameNS("http://www.pustefix-framework.org/2008/namespace/context-xml-service-config", "implements");
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
                itfClass = Class.forName(itfClassName, true, appContext.getClassLoader());
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
        context.getObjectTreeElement().addObject(crConfig);
            
        BeanDefinitionHolder beanHolder = new BeanDefinitionHolder(beanDefinition, beanName );
        if (!scope.equals("singleton") && !scope.equals("prototype")) {
            beanHolder = ScopedProxyUtils.createScopedProxy(beanHolder, beanReg, true);
        }
        context.getObjectTreeElement().addObject(beanHolder);
       
    }
    
}
