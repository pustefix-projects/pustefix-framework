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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.pustefixframework.config.Constants;
import org.pustefixframework.config.contextxmlservice.IWrapperConfig;
import org.pustefixframework.config.contextxmlservice.PageRequestConfigHolder;
import org.pustefixframework.config.contextxmlservice.PageRequestOutputResourceHolder;
import org.pustefixframework.config.contextxmlservice.parser.internal.IWrapperConfigMap;
import org.pustefixframework.config.contextxmlservice.parser.internal.PageRequestIWrapperConfigExtensionPointImpl;
import org.pustefixframework.config.contextxmlservice.parser.internal.PageRequestOutputResourceExtensionPointImpl;
import org.pustefixframework.config.contextxmlservice.parser.internal.PageRequestOutputResourceMap;
import org.pustefixframework.config.contextxmlservice.parser.internal.PustefixContextXMLRequestHandlerConfigImpl;
import org.pustefixframework.config.contextxmlservice.parser.internal.IWrapperConfigImpl;
import org.pustefixframework.config.contextxmlservice.parser.internal.PageRequestConfigImpl;
import org.pustefixframework.config.contextxmlservice.parser.internal.ScriptingStatePathInfo;
import org.pustefixframework.config.contextxmlservice.parser.internal.StateConfigImpl;
import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.container.spring.beans.support.ScopedProxyUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.w3c.dom.Element;

import com.marsching.flexiparse.configuration.RunOrder;
import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.generator.UseHandlerBeanRef;
import de.schlund.pfixcore.generator.UseHandlerClass;
import de.schlund.pfixcore.generator.UseHandlerScript;
import de.schlund.pfixcore.scripting.ScriptingIHandler;
import de.schlund.pfixcore.workflow.ConfigurableState;
import de.schlund.pfixcore.workflow.app.DefaultIWrapperState;
import de.schlund.pfixcore.workflow.app.IHandlerContainerImpl;


public class PageRequestParsingHandler implements ParsingHandler {

    public void handleNode(HandlerContext context) throws ParserException {
        if (context.getRunOrder() == RunOrder.START) {
            handleNodeStart(context);
        } else if (context.getRunOrder() == RunOrder.END) {
            handleNodeEnd(context);
        }
    }

    private void handleNodeStart(HandlerContext context) throws ParserException {
        Element element = (Element)context.getNode();
        
        PustefixContextXMLRequestHandlerConfigImpl config = ParsingUtils.getSingleTopObject(PustefixContextXMLRequestHandlerConfigImpl.class, context);     
       
        PageRequestConfigImpl pageConfig = null;
        String pageName;
        if (element.getLocalName().equals("pagerequest")) {
            pageName = element.getAttribute("name").trim();
        } else {
            // Variants - page name attribute is set on parent element
            pageName = ((Element) element.getParentNode()).getAttribute("name");
        }
        if (pageName.length() == 0) {
            throw new ParserException("Mandatory attribute \"name\" is missing!");
        }
        if (element.getLocalName().equals("variant")) {
            String variantName = element.getAttribute("name");
            if (variantName.length() == 0) {
                throw new ParserException("Mandatory attribute \"name\" is missing on <variant> element.");
            }
            pageName = pageName + "::" + variantName;
        }
        
        String copyfrom = element.getAttribute("copyfrom").trim();
        if (copyfrom.length() > 0) {
            if (element.getElementsByTagNameNS(Constants.NS_CONTEXT_XML_SERVICE, "default").getLength() > 0 || element.getElementsByTagNameNS(Constants.NS_CONTEXT_XML_SERVICE, "variant").getLength() > 0) {
                throw new ParserException("Variants may not be combined with \"copyfrom\" attribute.");
            }
            Collection<PageRequestConfigImpl> pageCollection = context.getObjectTreeElement().getRoot().getObjectsOfTypeFromSubTree(PageRequestConfigImpl.class);
            for (PageRequestConfigImpl sourceConfig : pageCollection) {
                if (sourceConfig.getPageName().equals(copyfrom)) {
                    try {
                        pageConfig = (PageRequestConfigImpl) sourceConfig.clone();
                    } catch (CloneNotSupportedException e) {
                        throw new RuntimeException("Unexpected CloneNotSupportedException", e);
                    } 
                    pageConfig.setPageName(pageName);
                    context.getObjectTreeElement().addObject(pageConfig);
                    
                } else if (sourceConfig.getPageName().startsWith(copyfrom + "::")) {
                    try {
                        pageConfig = (PageRequestConfigImpl) sourceConfig.clone();
                    } catch (CloneNotSupportedException e) {
                        throw new RuntimeException("Unexpected CloneNotSupportedException", e);
                    }
                    String newName = pageName + sourceConfig.getPageName().substring(sourceConfig.getPageName().indexOf("::"));
                    pageConfig.setPageName(newName);
                    context.getObjectTreeElement().addObject(pageConfig);
                }
            }
            if (pageConfig == null) {
                throw new ParserException("Page \"" + copyfrom + "\" referenced by page \"" + pageName + "\" could not be found.");
            }
        } else {
            pageConfig = new PageRequestConfigImpl();
            pageConfig.setPageName(pageName);
            StateConfigImpl stateConfig = new StateConfigImpl();
            stateConfig.setDefaultStaticState(config.getDefaultStaticState());
            stateConfig.setDefaultStaticStateParentBeanName(config.getDefaultStaticStateParentBeanName());
            stateConfig.setDefaultIHandlerState(config.getDefaultIHandlerState());
            stateConfig.setDefaultIHandlerStateParentBeanName(config.getDefaultIHandlerStateParentBeanName());
            context.getObjectTreeElement().addObject(pageConfig);
            context.getObjectTreeElement().addObject(stateConfig);
        }
    }

    private void handleNodeEnd(HandlerContext context) throws ParserException {
        Element pagerequestElement = (Element) context.getNode();
        if (!pagerequestElement.getLocalName().equals("pagerequest") || pagerequestElement.getAttribute("copyfrom").trim().length() == 0) {
            // Post processing is only required if configuration
            // has not been copied.
            createStateBeanDefinition(context);
        }
        
        // Create bean definitions for page configurations and
        // register them using a holder object
        BeanDefinitionRegistry beanRegistry = ParsingUtils.getSingleTopObject(BeanDefinitionRegistry.class, context);
        DefaultBeanNameGenerator beanNameGenerator = new DefaultBeanNameGenerator();
        
        for (PageRequestConfigImpl pageConfig : context.getObjectTreeElement().getObjectsOfType(PageRequestConfigImpl.class)) {
            BeanDefinition beanDefinition = pageConfig.generateBeanDefinition();
            String beanName = beanNameGenerator.generateBeanName(beanDefinition, beanRegistry);
            beanRegistry.registerBeanDefinition(beanName, beanDefinition);
            final RuntimeBeanReference beanReference = new RuntimeBeanReference(beanName);
            final String pageName = pageConfig.getPageName();
            PageRequestConfigHolder holder = new PageRequestConfigHolder() {

                public Object getPageRequestConfigObject() {
                    return beanReference; 
                }

                public String getName() {
                    return pageName;
                }
                
            };
            context.getObjectTreeElement().addObject(holder);
        }
    }

    private void createStateBeanDefinition(HandlerContext context) throws ParserException {
        PageRequestConfigImpl pageConfig = ParsingUtils.getSingleObject(PageRequestConfigImpl.class, context);
        StateConfigImpl stateConfig = ParsingUtils.getSingleObject(StateConfigImpl.class, context);
        
        if (stateConfig.isExternalBean()) {
            // No post processing of state config is required
            // as state config will not be used. We just have 
            // to set the reference to the state.
            pageConfig.setStateReference(new RuntimeBeanReference(stateConfig.getBeanName()));
            return;
        }
        
        BeanDefinitionRegistry beanRegistry = ParsingUtils.getSingleTopObject(BeanDefinitionRegistry.class, context);
        DefaultBeanNameGenerator nameGenerator = new DefaultBeanNameGenerator();
        
        BeanDefinitionBuilder beanBuilder;
        BeanDefinition beanDefinition;
        String configBeanName;
        
        @SuppressWarnings("unchecked")
        List<Object> wrapperObjects = new ManagedList();
        for (Object o : context.getObjectTreeElement().getObjectsOfTypeFromSubTree(Object.class)) {
            if (o instanceof IWrapperConfig) {
                wrapperObjects.add(createBeansForIWrapperConfig((IWrapperConfig) o, pageConfig.getPageName(), beanRegistry));
            } else if (o instanceof PageRequestIWrapperConfigExtensionPointImpl) {
                wrapperObjects.add(o);
            }
        }
        beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(IWrapperConfigMap.class);
        beanBuilder.setScope("singleton");
        beanBuilder.addPropertyValue("IWrapperConfigObjects", wrapperObjects);
        beanDefinition = beanBuilder.getBeanDefinition();
        String wrapperMapBeanName = nameGenerator.generateBeanName(beanDefinition, beanRegistry);
        beanRegistry.registerBeanDefinition(wrapperMapBeanName, beanDefinition);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> contextResources = new ManagedMap();
        for (PageRequestOutputResourceHolder holder : context.getObjectTreeElement().getObjectsOfTypeFromSubTree(PageRequestOutputResourceHolder.class)) {
            contextResources.put(holder.getName(), holder.getOutputResource());
        }
        List<PageRequestOutputResourceExtensionPointImpl> contextResourceExtensionPoints = new LinkedList<PageRequestOutputResourceExtensionPointImpl>();
        contextResourceExtensionPoints.addAll(context.getObjectTreeElement().getObjectsOfTypeFromSubTree(PageRequestOutputResourceExtensionPointImpl.class));
        beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(PageRequestOutputResourceMap.class);
        beanBuilder.setScope("singleton");
        beanBuilder.addPropertyValue("outputResources", contextResources);
        beanBuilder.addPropertyValue("outputResourceExtensionPoints", contextResourceExtensionPoints);
        beanDefinition = beanBuilder.getBeanDefinition();
        String outputResourceMapBeanName = nameGenerator.generateBeanName(beanDefinition, beanRegistry);
        beanRegistry.registerBeanDefinition(outputResourceMapBeanName, beanDefinition);
        
        Class<? extends ConfigurableState> stateClass = stateConfig.getState();
        if (stateClass == null) {
            if (wrapperObjects.size() > 0) {
                stateClass = stateConfig.getDefaultIHandlerState();
            } else {
                stateClass = stateConfig.getDefaultStaticState();
            }
        }
        
        beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(StateConfigImpl.class);
        beanBuilder.setScope("singleton");
        contextResources.putAll(stateConfig.getContextResources());
        beanBuilder.addPropertyReference("contextResources", outputResourceMapBeanName);
        beanBuilder.addPropertyValue("requiresToken", stateConfig.requiresToken());
        beanBuilder.addPropertyValue("IWrapperPolicy", stateConfig.getIWrapperPolicy());
        beanBuilder.addPropertyReference("IWrappers", wrapperMapBeanName);
        beanBuilder.addPropertyValue("processActions", stateConfig.getProcessActions());
        beanBuilder.addPropertyValue("properties", stateConfig.getProperties());
        beanBuilder.addPropertyValue("state", stateClass);
        beanDefinition = beanBuilder.getBeanDefinition();
        configBeanName = nameGenerator.generateBeanName(beanDefinition, beanRegistry);
        beanRegistry.registerBeanDefinition(configBeanName, beanDefinition);
        
        String handlerContainerBeanName = null;
        if (DefaultIWrapperState.class.isAssignableFrom(stateClass)) {
            beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(IHandlerContainerImpl.class);
            beanBuilder.setScope("request");
            beanBuilder.addPropertyReference("stateConfig", configBeanName);
            beanBuilder.setInitMethodName("init");
            beanDefinition = beanBuilder.getBeanDefinition();
            handlerContainerBeanName = nameGenerator.generateBeanName(beanDefinition, beanRegistry);
            BeanDefinitionHolder beanHolder = new BeanDefinitionHolder(beanDefinition, handlerContainerBeanName);
            beanHolder = ScopedProxyUtils.createScopedProxy(beanHolder, beanRegistry, true);
            beanRegistry.registerBeanDefinition(beanHolder.getBeanName(), beanHolder.getBeanDefinition());
            if (beanHolder.getAliases() != null) {
                for (String alias : beanHolder.getAliases()) {
                    beanRegistry.registerAlias(beanHolder.getBeanName(), alias);
                }
            }
        }
        
        Collection<ScriptingStatePathInfo> scriptPathInfoCollection = context.getObjectTreeElement().getObjectsOfTypeFromSubTree(ScriptingStatePathInfo.class);
        beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(stateClass);
        String parentBeanName = stateConfig.getParentBeanName();
        if (parentBeanName == null) {
            if (stateConfig.getState() == null) {
                // State has not been set explicitly
                if (wrapperObjects.size() > 0) {
                    parentBeanName = stateConfig.getDefaultIHandlerStateParentBeanName();
                } else {
                    parentBeanName = stateConfig.getDefaultStaticStateParentBeanName();
                }
            }
        }
        if (parentBeanName != null) {
            beanBuilder.setParentName(stateConfig.getParentBeanName());
        }
        beanBuilder.setScope(stateConfig.getScope());
        beanBuilder.addPropertyReference("config", configBeanName);
        if (scriptPathInfoCollection.iterator().hasNext()) {
            beanBuilder.addPropertyValue("scriptPath", scriptPathInfoCollection.iterator().next().getScriptPath());
        }
        if (handlerContainerBeanName != null) {
            beanBuilder.addPropertyReference("IHandlerContainer", handlerContainerBeanName);
        }
        beanDefinition = beanBuilder.getBeanDefinition();
        if (stateConfig.getBeanName() == null) {
            String stateBeanName = nameGenerator.generateBeanName(beanDefinition, beanRegistry);
            stateConfig.setBeanName(stateBeanName);
        }
        pageConfig.setStateReference(new RuntimeBeanReference(stateConfig.getBeanName()));
        if (stateConfig.getScope().equals("singleton") || stateConfig.getScope().equals("prototype")) {
            beanRegistry.registerBeanDefinition(stateConfig.getBeanName(), beanDefinition);
        } else {
            BeanDefinitionHolder beanHolder = new BeanDefinitionHolder(beanDefinition, stateConfig.getBeanName());
            beanHolder = ScopedProxyUtils.createScopedProxy(beanHolder, beanRegistry, true);
            beanRegistry.registerBeanDefinition(beanHolder.getBeanName(), beanHolder.getBeanDefinition());
            if (beanHolder.getAliases() != null) {
                for (String alias : beanHolder.getAliases()) {
                    beanRegistry.registerAlias(beanHolder.getBeanName(), alias);
                }
            }
        }
    }
    
    static RuntimeBeanReference createBeansForIWrapperConfig(IWrapperConfig wrapperConfig, String pageName, BeanDefinitionRegistry beanRegistry) throws ParserException {
        BeanDefinitionBuilder beanBuilder;
        BeanDefinition beanDefinition;
        DefaultBeanNameGenerator nameGenerator = new DefaultBeanNameGenerator();
        
        Class<? extends IWrapper> wrapperClass = wrapperConfig.getWrapperClass();
        
        Class<? extends IHandler> handlerClass = null;
        String handlerScriptPath = null;
        UseHandlerClass handlerClassAnnotation = wrapperClass.getAnnotation(UseHandlerClass.class);
        UseHandlerScript handlerScriptAnnotation = wrapperClass.getAnnotation(UseHandlerScript.class);
        UseHandlerBeanRef handlerBeanRefAnnotation = wrapperClass.getAnnotation(UseHandlerBeanRef.class);
        
        String handlerBeanName = null;
        if(handlerBeanRefAnnotation != null) {
            handlerBeanName = handlerBeanRefAnnotation.value();
        } else {
            
            if (handlerClassAnnotation != null) {
                handlerClass = handlerClassAnnotation.value();
            } else if (handlerScriptAnnotation != null) {
                handlerClass = ScriptingIHandler.class;
                handlerScriptPath = handlerScriptAnnotation.value();
            } else {
                throw new ParserException("Wrapper class " + wrapperClass.getName() + " has no handler annotation.");
            }
            
            beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(handlerClass);
            beanBuilder.setScope(wrapperConfig.getScope());
            if (handlerScriptPath != null) {
                beanBuilder.addPropertyValue("scriptPath", handlerScriptPath);
            }
            beanDefinition = beanBuilder.getBeanDefinition();
            handlerBeanName = nameGenerator.generateBeanName(beanDefinition, beanRegistry);
            String handlerBeanAlias = null;
            if (pageName != null) {
                handlerBeanAlias = handlerClass.getName() + "#" + pageName + "#" + wrapperConfig.getPrefix();
            }
            BeanDefinitionHolder beanHolder = new BeanDefinitionHolder(beanDefinition, handlerBeanName, (handlerBeanAlias == null) ? null : new String[] {handlerBeanAlias});
            if (!beanDefinition.getScope().equals("singleton") && !beanDefinition.getScope().equals("prototype")) {
                beanHolder = ScopedProxyUtils.createScopedProxy(beanHolder, beanRegistry, true);
            }
            beanRegistry.registerBeanDefinition(beanHolder.getBeanName(), beanHolder.getBeanDefinition());
            if (beanHolder.getAliases() != null) {
                for (String alias : beanHolder.getAliases()) {
                    beanRegistry.registerAlias(beanHolder.getBeanName(), alias);
                }
            }
            
        }
        
        beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(IWrapperConfigImpl.class);
        beanBuilder.setScope(wrapperConfig.getScope());
        beanBuilder.addPropertyValue("prefix", wrapperConfig.getPrefix());
        beanBuilder.addPropertyValue("wrapperClass", wrapperConfig.getWrapperClass());
        beanBuilder.addPropertyValue("logging", wrapperConfig.getLogging());
        beanBuilder.addPropertyReference("handler", handlerBeanName);
        beanBuilder.addPropertyValue("scope", wrapperConfig.getScope());
        beanBuilder.addPropertyValue("checkActive", wrapperConfig.doCheckActive());
        beanDefinition = beanBuilder.getBeanDefinition();
        String wrapperConfigBeanName = nameGenerator.generateBeanName(beanDefinition, beanRegistry);
        beanRegistry.registerBeanDefinition(wrapperConfigBeanName, beanDefinition);
        return new RuntimeBeanReference(wrapperConfigBeanName);
    }
}
