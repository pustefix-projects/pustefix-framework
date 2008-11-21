/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.pustefixframework.config.contextxmlservice.parser;

import java.util.Collection;
import java.util.Map;

import org.pustefixframework.config.Constants;
import org.pustefixframework.config.contextxmlservice.IWrapperConfig;
import org.pustefixframework.config.contextxmlservice.parser.internal.ContextConfigImpl;
import org.pustefixframework.config.contextxmlservice.parser.internal.ContextXMLServletConfigImpl;
import org.pustefixframework.config.contextxmlservice.parser.internal.IWrapperConfigImpl;
import org.pustefixframework.config.contextxmlservice.parser.internal.PageRequestConfigImpl;
import org.pustefixframework.config.contextxmlservice.parser.internal.ScriptingStatePathInfo;
import org.pustefixframework.config.contextxmlservice.parser.internal.StateConfigImpl;
import org.pustefixframework.config.generic.ParsingUtils;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
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


public class PageRequestParsingHandler implements ParsingHandler {

    public void handleNode(HandlerContext context) throws ParserException {
       if (context.getRunOrder() == RunOrder.START) {
            Element element = (Element)context.getNode();
            
            ContextConfigImpl ctxConfig = ParsingUtils.getSingleSubObjectFromRoot(ContextConfigImpl.class, context);
            ContextXMLServletConfigImpl config = ParsingUtils.getSingleTopObject(ContextXMLServletConfigImpl.class, context);     
           
            PageRequestConfigImpl pageConfig = new PageRequestConfigImpl();
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
            pageConfig.setPageName(pageName);
            
            boolean isCopyPage = false;
            String copyfrom = element.getAttribute("copyfrom").trim();
            if (copyfrom.length() > 0) {
                isCopyPage = true;
                if (element.getElementsByTagNameNS(Constants.NS_CONTEXT_XML_SERVICE, "default").getLength() > 0 || element.getElementsByTagNameNS(Constants.NS_CONTEXT_XML_SERVICE, "variant").getLength() > 0) {
                    throw new ParserException("Variants may not be combined with \"copyfrom\" attribute.");
                }
                Collection<PageRequestConfigImpl> pageCollection = context.getObjectTreeElement().getRoot().getObjectsOfTypeFromSubTree(PageRequestConfigImpl.class);
                for (PageRequestConfigImpl sourceConfig : pageCollection) {
                    if (sourceConfig.getPageName().equals(pageName)) {
                        PageRequestConfigImpl newConfig;
                        try {
                            newConfig = (PageRequestConfigImpl) sourceConfig.clone();
                        } catch (CloneNotSupportedException e) {
                            throw new RuntimeException("Unexpected CloneNotSupportedException", e);
                        } 
                        newConfig.setPageName(pageName);
                        ctxConfig.addPageRequest(newConfig);
                        context.getObjectTreeElement().addObject(newConfig);
                        
                    } else if (sourceConfig.getPageName().startsWith(pageName + "::")) {
                        PageRequestConfigImpl newConfig;
                        try {
                            newConfig = (PageRequestConfigImpl) sourceConfig.clone();
                        } catch (CloneNotSupportedException e) {
                            throw new RuntimeException("Unexpected CloneNotSupportedException", e);
                        }
                        String newName = pageName + sourceConfig.getPageName().substring(sourceConfig.getPageName().indexOf("::"));
                        newConfig.setPageName(newName);
                        ctxConfig.addPageRequest(newConfig);
                        context.getObjectTreeElement().addObject(newConfig);
                    }
                }
            }
            
            if (!isCopyPage) {
                StateConfigImpl stateConfig = new StateConfigImpl();
                stateConfig.setDefaultStaticState(config.getDefaultStaticState());
                stateConfig.setDefaultStaticStateParentBeanName(config.getDefaultStaticStateParentBeanName());
                stateConfig.setDefaultIHandlerState(config.getDefaultIHandlerState());
                stateConfig.setDefaultIHandlerStateParentBeanName(config.getDefaultIHandlerStateParentBeanName());
                ctxConfig.addPageRequest(pageConfig);
                
                context.getObjectTreeElement().addObject(pageConfig);
                context.getObjectTreeElement().addObject(stateConfig);
            }
            
        } else if (context.getRunOrder() == RunOrder.END) {
            Element pagerequestElement = (Element) context.getNode();
            if (pagerequestElement.getLocalName().equals("pagerequest") && pagerequestElement.getAttribute("copyfrom").length() > 0) {
                // Pagerequest is just copied, so no postprocessing is required
                return;
            }
            
            PageRequestConfigImpl pageConfig = ParsingUtils.getSingleObject(PageRequestConfigImpl.class, context);
            StateConfigImpl stateConfig = ParsingUtils.getSingleObject(StateConfigImpl.class, context);
            
            if (!stateConfig.isExternalBean()) {
                BeanDefinitionRegistry beanRegistry = ParsingUtils.getSingleTopObject(BeanDefinitionRegistry.class, context);
                DefaultBeanNameGenerator nameGenerator = new DefaultBeanNameGenerator();
                
                BeanDefinitionBuilder beanBuilder;
                BeanDefinition beanDefinition;
                String configBeanName;
                String stateBeanName;
                
                @SuppressWarnings("unchecked")
                Map<String, Object> wrapperMap = new ManagedMap(stateConfig.getIWrappers().size());
                for (String prefix : stateConfig.getIWrappers().keySet()) {
                    IWrapperConfig wrapperConfig = stateConfig.getIWrappers().get(prefix);
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
                        BeanDefinitionHolder beanHolder = new BeanDefinitionHolder(beanDefinition, handlerBeanName);
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
                    wrapperMap.put(prefix, new RuntimeBeanReference(wrapperConfigBeanName));
                }
                
                beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(StateConfigImpl.class);
                beanBuilder.setScope("singleton");
                @SuppressWarnings("unchecked")
                Map<String, Object> contextResources = new ManagedMap(stateConfig.getContextResources().size());
                contextResources.putAll(stateConfig.getContextResources());
                beanBuilder.addPropertyValue("contextResources", contextResources);
                beanBuilder.addPropertyValue("finalizer", stateConfig.getFinalizer());
                beanBuilder.addPropertyValue("IWrapperPolicy", stateConfig.getIWrapperPolicy());
                beanBuilder.addPropertyValue("IWrappers", wrapperMap);
                beanBuilder.addPropertyValue("processActions", stateConfig.getProcessActions());
                beanBuilder.addPropertyValue("properties", stateConfig.getProperties());
                beanBuilder.addPropertyValue("state", stateConfig.getState());
                beanDefinition = beanBuilder.getBeanDefinition();
                configBeanName = nameGenerator.generateBeanName(beanDefinition, beanRegistry);
                beanRegistry.registerBeanDefinition(configBeanName, beanDefinition);
                
                Collection<ScriptingStatePathInfo> scriptPathInfoCollection = context.getObjectTreeElement().getObjectsOfTypeFromSubTree(ScriptingStatePathInfo.class);
                beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(stateConfig.getState());
                if(stateConfig.getParentBeanName()!=null) {
                    beanBuilder.setParentName(stateConfig.getParentBeanName());
                }
                beanBuilder.setScope(stateConfig.getScope());
                beanBuilder.addPropertyReference("config", configBeanName);
                if (scriptPathInfoCollection.iterator().hasNext()) {
                    beanBuilder.addPropertyValue("scriptPath", scriptPathInfoCollection.iterator().next().getScriptPath());
                }
                beanDefinition = beanBuilder.getBeanDefinition();
                if (pageConfig.getBeanName() != null && pageConfig.getBeanName().length() > 0) {
                    stateBeanName = pageConfig.getBeanName();
                } else {
                    stateBeanName = nameGenerator.generateBeanName(beanDefinition, beanRegistry);
                    pageConfig.setBeanName(stateBeanName);
                }
                if (stateConfig.getScope().equals("singleton") || stateConfig.getScope().equals("prototype")) {
                    beanRegistry.registerBeanDefinition(stateBeanName, beanDefinition);
                } else {
                    BeanDefinitionHolder beanHolder = new BeanDefinitionHolder(beanDefinition, stateBeanName);
                    beanHolder = ScopedProxyUtils.createScopedProxy(beanHolder, beanRegistry, true);
                    beanRegistry.registerBeanDefinition(beanHolder.getBeanName(), beanHolder.getBeanDefinition());
                    if (beanHolder.getAliases() != null) {
                        for (String alias : beanHolder.getAliases()) {
                            beanRegistry.registerAlias(beanHolder.getBeanName(), alias);
                        }
                    }
                }
            }
        }
    }

}
