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

package org.pustefixframework.config.project.parser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.pustefixframework.config.Constants;
import org.pustefixframework.config.customization.CustomizationAwareParsingHandler;
import org.pustefixframework.config.customization.CustomizationInfo;
import org.pustefixframework.config.directoutputservice.DirectOutputPageRequestConfig;
import org.pustefixframework.config.directoutputservice.DirectOutputServiceConfig;
import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.config.project.SessionTrackingStrategyInfo;
import org.pustefixframework.http.PustefixContextDirectOutputRequestHandler;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.MapFactoryBean;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.springframework.beans.factory.support.ManagedMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.marsching.flexiparse.objecttree.ObjectTreeElement;
import com.marsching.flexiparse.objecttree.SubObjectTree;
import com.marsching.flexiparse.parser.ClasspathConfiguredParser;
import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixcore.workflow.ContextImpl;
import de.schlund.pfixxml.config.includes.IncludesResolver;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.serverutil.SessionAdmin;

public class PustefixContextDirectOutputRequestHandlerParsingHandler extends CustomizationAwareParsingHandler {

    @Override
    public void handleNodeIfActive(HandlerContext context) throws ParserException {
        Element serviceElement = (Element) context.getNode();
        
        String path = null; 
        Element pathElement = (Element) serviceElement.getElementsByTagNameNS(Constants.NS_PROJECT, "path").item(0);
        if(pathElement != null) path = pathElement.getTextContent().trim();
        
        Element configurationFileElement = (Element) serviceElement.getElementsByTagNameNS(Constants.NS_PROJECT, "config-file").item(0);
        if (configurationFileElement == null) {
            throw new ParserException("Could not find expected <config-file> element");
        }
        String configurationFile = configurationFileElement.getTextContent().trim();

        Collection<CustomizationInfo> infoCollection = context.getObjectTreeElement().getObjectsOfTypeFromTopTree(CustomizationInfo.class);
        if (infoCollection.isEmpty()) {
            throw new ParserException("Could not find instance of CustomizationInfo");
        }
        CustomizationInfo info = infoCollection.iterator().next();
        
        BeanDefinitionRegistry registry = ParsingUtils.getSingleTopObject(BeanDefinitionRegistry.class, context);
        
        ClasspathConfiguredParser configParser = new ClasspathConfiguredParser("META-INF/org/pustefixframework/config/direct-output-service/parser/direct-output-service-config.xml");
        final ObjectTreeElement root;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setXIncludeAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(ResourceUtil.getFileResource(configurationFile).getInputStream()); 
            IncludesResolver resolver = new IncludesResolver("http://www.pustefix-framework.org/2008/namespace/direct-output-service-config", "config-include");
            resolver.resolveIncludes(doc);
            root = configParser.parse(doc, info, registry);
        } catch (FileNotFoundException e) {
            throw new ParserException("Could not find referenced configuration file: " + configurationFile, e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Could not initialize XML parser: " + e.getMessage(), e);
        } catch (SAXException e) {
            throw new ParserException("Error while parsing referenced file: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new ParserException("Error while reading file: " + e.getMessage(), e);
        }
        SubObjectTree subtree = new SubObjectTree() {

            public ObjectTreeElement getRoot() {
                return root;
            }
            
        };
        context.getObjectTreeElement().addObject(subtree);
        
        DirectOutputServiceConfig config = root.getObjectsOfTypeFromSubTree(DirectOutputServiceConfig.class).iterator().next();
        Map<String, Object> stateMap = new HashMap<String, Object>();
        for (DirectOutputPageRequestConfig pConfig : config.getPageRequests()) {
            stateMap.put(pConfig.getPageName(), new RuntimeBeanReference(pConfig.getBeanName()));
        }
        SessionTrackingStrategyInfo strategyInfo = ParsingUtils.getSingleTopObject(SessionTrackingStrategyInfo.class, context);
        
        BeanNameGenerator nameGenerator = new DefaultBeanNameGenerator();
        BeanDefinitionBuilder beanBuilder;
        BeanDefinition beanDefinition;
        
        String mapBeanName;
        beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(MapFactoryBean.class);
        @SuppressWarnings({"unchecked","rawtypes"})
        Map<String, Object> managedMap = new ManagedMap(stateMap.size());
        managedMap.putAll(stateMap);
        beanBuilder.addPropertyValue("sourceMap", managedMap);
        beanDefinition = beanBuilder.getBeanDefinition();
        mapBeanName = nameGenerator.generateBeanName(beanDefinition, registry);
        registry.registerBeanDefinition(mapBeanName, beanDefinition);
        
        beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(PustefixContextDirectOutputRequestHandler.class);
        beanBuilder.setScope("singleton");
        beanBuilder.setInitMethodName("init");
        if(path != null && !path.equals("") && !path.equals("/")) beanBuilder.addPropertyValue("handlerURI", path + "/**");
        beanBuilder.addPropertyValue("context", new RuntimeBeanReference(ContextImpl.class.getName()));
        beanBuilder.addPropertyValue("stateMap", new RuntimeBeanReference(mapBeanName));
        beanBuilder.addPropertyValue("configuration", config);
        beanBuilder.addPropertyValue("sessionAdmin", new RuntimeBeanReference(SessionAdmin.class.getName()));
        beanBuilder.addPropertyValue("sessionTrackingStrategy", strategyInfo.getSessionTrackingStrategyInstance());
        beanDefinition = beanBuilder.getBeanDefinition();
        registry.registerBeanDefinition(nameGenerator.generateBeanName(beanDefinition, registry), beanDefinition);
    }

}
