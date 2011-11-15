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

import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.pustefixframework.config.Constants;
import org.pustefixframework.config.contextxmlservice.ContextXMLServletConfig;
import org.pustefixframework.config.customization.CustomizationAwareParsingHandler;
import org.pustefixframework.config.customization.CustomizationInfo;
import org.pustefixframework.config.customization.PropertiesBasedCustomizationInfo;
import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.config.project.EditorInfo;
import org.pustefixframework.config.project.ProjectInfo;
import org.pustefixframework.config.project.SessionTimeoutInfo;
import org.pustefixframework.config.project.SessionTrackingStrategyInfo;
import org.pustefixframework.http.AdditionalTrailInfo;
import org.pustefixframework.http.DefaultAdditionalTrailInfoImpl;
import org.pustefixframework.http.PustefixContextXMLRequestHandler;
import org.pustefixframework.http.PustefixInternalsRequestHandler;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.marsching.flexiparse.objecttree.ObjectTreeElement;
import com.marsching.flexiparse.objecttree.SubObjectTree;
import com.marsching.flexiparse.parser.ClasspathConfiguredParser;
import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.Parser;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixcore.workflow.ContextImpl;
import de.schlund.pfixcore.workflow.SiteMap;
import de.schlund.pfixxml.SessionCleaner;
import de.schlund.pfixxml.TenantInfo;
import de.schlund.pfixxml.config.EnvironmentProperties;
import de.schlund.pfixxml.config.includes.IncludesResolver;
import de.schlund.pfixxml.exceptionprocessor.ExceptionProcessingConfiguration;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.serverutil.SessionAdmin;
import de.schlund.pfixxml.targets.TargetGenerator;
import de.schlund.pfixxml.targets.cachestat.CacheStatistic;

public class PustefixContextXMLRequestHandlerParsingHandler extends CustomizationAwareParsingHandler {

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
        ProjectInfo projectInfo = ParsingUtils.getSingleTopObject(ProjectInfo.class, context);
        if(projectInfo.getDefiningModule() != null && !configurationFile.matches("^\\w+:.*")) {
            if(configurationFile.startsWith("/")) configurationFile = configurationFile.substring(1);
            configurationFile = "module://" + projectInfo.getDefiningModule() + "/" + configurationFile;
        }
        
        boolean renderExternal = false;
        Element renderExtElement = (Element) serviceElement.getElementsByTagNameNS(Constants.NS_PROJECT, "render-external").item(0);
        if (renderExtElement != null) {
            renderExternal = Boolean.parseBoolean(renderExtElement.getTextContent().trim());
        }
        
        Class<?> trailInfoClass = DefaultAdditionalTrailInfoImpl.class;
        Element infoElement = (Element) serviceElement.getElementsByTagNameNS(Constants.NS_PROJECT, "additional-trail-info").item(0);
        if (infoElement != null) {
            String className = infoElement.getTextContent().trim();
            if(className.equals("")) {
                trailInfoClass = null;
            } else {
                try {
                    trailInfoClass = Class.forName(className);
                } catch(ClassNotFoundException x) {
                    throw new ParserException("Can't get additional-trail-info class: " + className, x);
                }
                if(!AdditionalTrailInfo.class.isAssignableFrom(trailInfoClass)) {
                    throw new ParserException("Class '" + trailInfoClass.getName() + "' doesn't implement '" + AdditionalTrailInfo.class.getName() + "'.");
                }
            }
        }
        String additionalTrailInfoRef = null;
        if(trailInfoClass != null) {
            BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(trailInfoClass);
            beanBuilder.setScope("singleton");
            BeanDefinitionHolder beanHolder = new BeanDefinitionHolder(beanBuilder.getBeanDefinition(), trailInfoClass.getName());
            context.getObjectTreeElement().addObject(beanHolder);
            additionalTrailInfoRef = trailInfoClass.getName();
        }
        
        int maxStoredDoms = 5;
        Element maxElement = (Element) serviceElement.getElementsByTagNameNS(Constants.NS_PROJECT, "max-stored-doms").item(0);
        if (maxElement != null) {
            maxStoredDoms = Integer.parseInt(maxElement.getTextContent().trim());
        }

        de.schlund.pfixxml.resources.Resource res = ResourceUtil.getResource(configurationFile);
        if(!res.exists()) {
            throw new ParserException("Context configuration file can't be found: " + res);
        }

        Collection<BeanDefinitionRegistry> beanRegs = context.getObjectTreeElement().getObjectsOfTypeFromTopTree(BeanDefinitionRegistry.class);
        if(beanRegs.size()==0) throw new ParserException("No BeanDefinitionRegistry object found.");
        else if(beanRegs.size()>1) throw new ParserException("Multiple BeanDefinitionRegistry objects found.");
        BeanDefinitionRegistry beanReg = beanRegs.iterator().next();
        Properties envProperties = EnvironmentProperties.getProperties();
        CustomizationInfo cusInfo = new PropertiesBasedCustomizationInfo(envProperties);
        try {
            Parser contextXmlConfigParser = new ClasspathConfiguredParser("META-INF/org/pustefixframework/config/context-xml-service/parser/context-xml-service-config.xml");
            
            //Resolve config-includes
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setXIncludeAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(res.getInputStream()); 
            IncludesResolver resolver = new IncludesResolver("http://www.pustefix-framework.org/2008/namespace/context-xml-service-config", "config-include");
            resolver.resolveIncludes(doc);
            
            final ObjectTreeElement contextXmlConfigTree = contextXmlConfigParser.parse(doc, cusInfo, beanReg, projectInfo);
            SubObjectTree subTree = new SubObjectTree() {
              public ObjectTreeElement getRoot() {
                    return contextXmlConfigTree;
                }  
            };
            context.getObjectTreeElement().addObject(subTree);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Could not initialize XML parser: " + e.getMessage(), e);
        } catch (SAXException e) {
            throw new ParserException("Error while parsing referenced file: " + e.getMessage(), e);
        } catch (ParserException e) {
            throw new BeanDefinitionStoreException("Error while parsing " + res + ": " + e.getMessage(), e);
        } catch (IOException e) {
            throw new BeanDefinitionStoreException("Error while parsing " + res + ": " + e.getMessage(), e);
        }
        
        ContextXMLServletConfig config = context.getObjectTreeElement().getObjectsOfTypeFromSubTree(ContextXMLServletConfig.class).iterator().next();
        SessionTrackingStrategyInfo strategyInfo = ParsingUtils.getSingleTopObject(SessionTrackingStrategyInfo.class, context);
        SessionTimeoutInfo timeoutInfo = ParsingUtils.getFirstTopObject(SessionTimeoutInfo.class, context, false);
        EditorInfo editorInfo = ParsingUtils.getSingleSubObjectFromRoot(EditorInfo.class, context, false);
        
        BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(PustefixContextXMLRequestHandler.class);
        beanBuilder.setScope("singleton");
        beanBuilder.setInitMethodName("init");
        beanBuilder.addPropertyValue("targetGenerator", new RuntimeBeanReference(TargetGenerator.class.getName()));
        if(path != null && !path.equals("") && !path.equals("/")) beanBuilder.addPropertyValue("handlerURI", path + "/**");
        beanBuilder.addPropertyValue("context", new RuntimeBeanReference(ContextImpl.class.getName()));
        beanBuilder.addPropertyValue("configuration", config);
        beanBuilder.addPropertyValue("sessionAdmin", new RuntimeBeanReference(SessionAdmin.class.getName()));
        if(editorInfo != null && editorInfo.getLocation() != null) {
            beanBuilder.addPropertyValue("editorLocation", editorInfo.getLocation());
            beanBuilder.addPropertyValue("includePartsEditableByDefault", editorInfo.isIncludePartsEditableByDefault());
        }
        beanBuilder.addPropertyValue("sessionCleaner", new RuntimeBeanReference(SessionCleaner.class.getName()));
        beanBuilder.addPropertyValue("renderExternal", renderExternal);
        if(additionalTrailInfoRef!=null) 
            beanBuilder.addPropertyValue("additionalTrailInfo", new RuntimeBeanReference(additionalTrailInfoRef));
        beanBuilder.addPropertyValue("maxStoredDoms", maxStoredDoms);
        beanBuilder.addPropertyValue("exceptionProcessingConfiguration", new RuntimeBeanReference(ExceptionProcessingConfiguration.class.getName()));
        beanBuilder.addPropertyValue("sessionTrackingStrategy", strategyInfo.getSessionTrackingStrategyInstance());
        if(timeoutInfo != null) {
            beanBuilder.addPropertyValue("sessionTimeoutInfo", timeoutInfo);
        }
        beanBuilder.addPropertyValue("tenantInfo", new RuntimeBeanReference(TenantInfo.class.getName()));
        beanBuilder.addPropertyValue("projectInfo", projectInfo);
        beanBuilder.addPropertyValue("siteMap", new RuntimeBeanReference(SiteMap.class.getName()));
        BeanDefinition beanDefinition = beanBuilder.getBeanDefinition();
        BeanDefinitionHolder beanHolder = new BeanDefinitionHolder(beanDefinition, PustefixContextXMLRequestHandler.class.getName() + (path != null ? "#" + path : ""));
        context.getObjectTreeElement().addObject(beanHolder);
        
        beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(PustefixInternalsRequestHandler.class);
        beanBuilder.setScope("singleton");
        beanBuilder.addPropertyValue("sessionAdmin", new RuntimeBeanReference(SessionAdmin.class.getName()));
        beanBuilder.addPropertyValue("cacheStatistic", new RuntimeBeanReference(CacheStatistic.class.getName()));
        beanDefinition = beanBuilder.getBeanDefinition();
        beanHolder = new BeanDefinitionHolder(beanDefinition, PustefixInternalsRequestHandler.class.getName());
        context.getObjectTreeElement().addObject(beanHolder);
        
    }

}
