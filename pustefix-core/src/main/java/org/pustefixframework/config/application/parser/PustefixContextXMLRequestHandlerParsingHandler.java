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

package org.pustefixframework.config.application.parser;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Properties;

import org.pustefixframework.admin.mbeans.WebappAdmin;
import org.pustefixframework.config.Constants;
import org.pustefixframework.config.application.ApplicationFlag;
import org.pustefixframework.config.application.EditorInfo;
import org.pustefixframework.config.application.EditorLocation;
import org.pustefixframework.config.application.XMLGeneratorInfo;
import org.pustefixframework.config.contextxmlservice.ContextConfigHolder;
import org.pustefixframework.config.contextxmlservice.PustefixContextXMLRequestHandlerConfig;
import org.pustefixframework.config.contextxmlservice.parser.internal.SimplePustefixContextXMLRequestHandlerConfig;
import org.pustefixframework.config.customization.CustomizationInfo;
import org.pustefixframework.config.customization.PropertiesBasedCustomizationInfo;
import org.pustefixframework.config.customization.RuntimeProperties;
import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.http.PustefixContextXMLRequestHandler;
import org.pustefixframework.resource.InputStreamResource;
import org.pustefixframework.resource.ResourceLoader;
import org.pustefixframework.resource.URLResource;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.springframework.osgi.context.ConfigurableOsgiBundleApplicationContext;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.marsching.flexiparse.objecttree.ObjectTreeElement;
import com.marsching.flexiparse.objecttree.SubObjectTree;
import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.OSGiAwareParser;
import com.marsching.flexiparse.parser.Parser;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixcore.workflow.ContextImpl;
import de.schlund.pfixxml.SessionCleaner;
import de.schlund.pfixxml.exceptionprocessor.ExceptionProcessingConfiguration;
import de.schlund.pfixxml.serverutil.SessionAdmin;
import de.schlund.pfixxml.testrecording.TestRecording;

public class PustefixContextXMLRequestHandlerParsingHandler implements ParsingHandler {

    public void handleNode(HandlerContext context) throws ParserException {
        ResourceLoader resourceLoader = ParsingUtils.getSingleTopObject(ResourceLoader.class, context);

        Element serviceElement = (Element) context.getNode();
        
        Element pathElement = (Element) serviceElement.getElementsByTagNameNS(Constants.NS_APPLICATION, "path").item(0);
        if (pathElement == null) {
            throw new ParserException("Could not find expected <path> element");
        }
        
        Element configurationFileElement = (Element) serviceElement.getElementsByTagNameNS(Constants.NS_APPLICATION, "config-file").item(0);
        if (configurationFileElement == null) {
            throw new ParserException("Could not find expected <config-file> element");
        }
        
        String path = pathElement.getTextContent().trim();
        URI configurationURI;
        try {
            configurationURI = new URI(configurationFileElement.getTextContent().trim());
        } catch (URISyntaxException e) {
            throw new ParserException("Not a valid URI: " + configurationFileElement.getTextContent().trim());
        }
        InputStreamResource configurationResource = resourceLoader.getResource(configurationURI, InputStreamResource.class);
        if (configurationResource == null) {
            throw new ParserException("Resource at URI \"" + configurationURI.toASCIIString() + "\" could not be found");
        }
        
        boolean renderExternal = false;
        Element renderExtElement = (Element) serviceElement.getElementsByTagNameNS(Constants.NS_APPLICATION, "render-external").item(0);
        if (renderExtElement != null) {
            renderExternal = Boolean.parseBoolean(renderExtElement.getTextContent().trim());
        }
        
        String additionalTrailInfoRef = null;
        Element infoElement = (Element) serviceElement.getElementsByTagNameNS(Constants.NS_APPLICATION, "additional-trail-info").item(0);
        if (infoElement != null) {
            String className = infoElement.getTextContent().trim();
            Class<?> clazz;
            try {
                clazz = Class.forName(className);
            } catch(ClassNotFoundException x) {
                throw new ParserException("Can't get additional-trail-info class: " + className, x);
            }
            BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
            beanBuilder.setScope("singleton");
            BeanDefinitionHolder beanHolder = new BeanDefinitionHolder(beanBuilder.getBeanDefinition(), className);
            context.getObjectTreeElement().addObject(beanHolder);
            additionalTrailInfoRef = className;
        }
        
        int maxStoredDoms = 5;
        Element maxElement = (Element) serviceElement.getElementsByTagNameNS(Constants.NS_APPLICATION, "max-stored-doms").item(0);
        if (maxElement != null) {
            maxStoredDoms = Integer.parseInt(maxElement.getTextContent().trim());
        }
        
        Collection<XMLGeneratorInfo> infoCollection = context.getObjectTreeElement().getRoot().getObjectsOfTypeFromSubTree(XMLGeneratorInfo.class);
        if (infoCollection.size() != 1) {
            throw new ParserException("Found " + infoCollection.size() + " instances of XMLGeneratorInfo but expected exactly one");
        }
        XMLGeneratorInfo info = infoCollection.iterator().next();
        
        Collection<EditorLocation> editorLocationCollection = context.getObjectTreeElement().getRoot().getObjectsOfTypeFromSubTree(EditorLocation.class);
        EditorLocation editorLocation = null;
        if (editorLocationCollection.size() > 0) {
            editorLocation = editorLocationCollection.iterator().next();
        }
        Collection<BeanDefinitionRegistry> beanRegs = context.getObjectTreeElement().getObjectsOfTypeFromTopTree(BeanDefinitionRegistry.class);
        if (beanRegs.size() == 0) {
            throw new ParserException("No BeanDefinitionRegistry object found.");
        } else if (beanRegs.size() > 1) {
            throw new ParserException("Multiple BeanDefinitionRegistry objects found.");
        }
        BeanDefinitionRegistry beanReg = beanRegs.iterator().next();
        Properties buildTimeProperties = RuntimeProperties.getProperties();
        CustomizationInfo cusInfo = new PropertiesBasedCustomizationInfo(buildTimeProperties);
        try {
            ConfigurableOsgiBundleApplicationContext appContext = ParsingUtils.getSingleTopObject(ConfigurableOsgiBundleApplicationContext.class, context);
            
            Parser contextXmlConfigParser = new OSGiAwareParser(appContext.getBundleContext(), "META-INF/org/pustefixframework/config/context-xml-service/parser/context-xml-service-config-application.xml");
            
            InputSource configurationSource = new InputSource(configurationResource.getInputStream());
            if (configurationResource instanceof URLResource) {
                URLResource urlResource = (URLResource) configurationResource;
                configurationSource.setSystemId(urlResource.getURL().toExternalForm());
            }
            
            final ObjectTreeElement contextXmlConfigTree = contextXmlConfigParser.parse(configurationSource, cusInfo, beanReg, info, appContext, resourceLoader, new ApplicationFlag());
            SubObjectTree subTree = new SubObjectTree() {
              public ObjectTreeElement getRoot() {
                    return contextXmlConfigTree;
                }  
            };
            context.getObjectTreeElement().addObject(subTree);
        } catch (ParserException e) {
            throw new BeanDefinitionStoreException("Error while parsing " + configurationURI.toASCIIString() + ": " + e.getMessage(), e);
        } catch (IOException e) {
            throw new BeanDefinitionStoreException("Error while parsing " + configurationURI.toASCIIString() + ": " + e.getMessage(), e);
        }
        
        PustefixContextXMLRequestHandlerConfig config = ParsingUtils.getSingleSubObject(PustefixContextXMLRequestHandlerConfig.class, context);
        ContextConfigHolder contextConfigHolder = ParsingUtils.getSingleSubObject(ContextConfigHolder.class, context);
        
        BeanDefinitionBuilder beanBuilder;
        BeanDefinition beanDefinition;
        BeanNameGenerator beanNameGenerator = new DefaultBeanNameGenerator();
        
        beanDefinition = SimplePustefixContextXMLRequestHandlerConfig.generateBeanDefinition(config, contextConfigHolder);
        String configBeanName = beanNameGenerator.generateBeanName(beanDefinition, beanReg);
        beanReg.registerBeanDefinition(configBeanName, beanDefinition);
        
        beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(PustefixContextXMLRequestHandler.class);
        beanBuilder.setScope("singleton");
        beanBuilder.setInitMethodName("init");
        beanBuilder.addPropertyValue("targetGenerator", new RuntimeBeanReference(info.getTargetGeneratorBeanName()));
        beanBuilder.addPropertyValue("handlerURI", path + "/**");
        beanBuilder.addPropertyValue("context", new RuntimeBeanReference(ContextImpl.class.getName()));
        beanBuilder.addPropertyReference("configuration", configBeanName);
        beanBuilder.addPropertyValue("sessionAdmin", new RuntimeBeanReference(SessionAdmin.class.getName()));
        if(beanReg.isBeanNameInUse(TestRecording.class.getName())) {
            beanBuilder.addPropertyValue("testRecording", new RuntimeBeanReference(TestRecording.class.getName()));
        }
        beanBuilder.addPropertyValue("webappAdmin", new RuntimeBeanReference(WebappAdmin.class.getName()));
        if(editorLocation != null) {
        	beanBuilder.addPropertyValue("editorLocation", editorLocation.getLocation());
        }
        beanBuilder.addPropertyValue("checkModtime", info.getCheckModtime());
        beanBuilder.addPropertyValue("sessionCleaner", new RuntimeBeanReference(SessionCleaner.class.getName()));
        beanBuilder.addPropertyValue("renderExternal", renderExternal);
        if(additionalTrailInfoRef!=null) 
            beanBuilder.addPropertyValue("additionalTrailInfo", new RuntimeBeanReference(additionalTrailInfoRef));
        beanBuilder.addPropertyValue("maxStoredDoms", maxStoredDoms);
        beanBuilder.addPropertyValue("exceptionProcessingConfiguration", new RuntimeBeanReference(ExceptionProcessingConfiguration.class.getName()));
        Collection<EditorInfo> editorInfos = context.getObjectTreeElement().getRoot().getObjectsOfTypeFromSubTree(EditorInfo.class);
        if(editorInfos.size()>0) {
            beanBuilder.addPropertyValue("editModeAllowed", editorInfos.iterator().next().getEnabled());
        }
        if(!RuntimeProperties.getProperties().getProperty("mode").equals("prod")) {
        	beanBuilder.addPropertyValue("xmlOnlyAllowed", true);
        }
        beanDefinition = beanBuilder.getBeanDefinition();
        BeanDefinitionHolder beanHolder = new BeanDefinitionHolder(beanDefinition, PustefixContextXMLRequestHandler.class.getName() + "#" + path);
        context.getObjectTreeElement().addObject(beanHolder);
        
    }

}
