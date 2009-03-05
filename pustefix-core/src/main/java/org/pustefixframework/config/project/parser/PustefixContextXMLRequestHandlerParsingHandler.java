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

package org.pustefixframework.config.project.parser;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.pustefixframework.admin.mbeans.WebappAdmin;
import org.pustefixframework.config.Constants;
import org.pustefixframework.config.contextxmlservice.ContextXMLServletConfig;
import org.pustefixframework.config.customization.CustomizationAwareParsingHandler;
import org.pustefixframework.config.customization.CustomizationInfo;
import org.pustefixframework.config.customization.PropertiesBasedCustomizationInfo;
import org.pustefixframework.config.global.GlobalConfigurationHolder;
import org.pustefixframework.config.global.parser.GlobalConfigurationReader;
import org.pustefixframework.config.project.XMLGeneratorInfo;
import org.pustefixframework.http.PustefixContextXMLRequestHandler;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
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
import de.schlund.pfixxml.config.BuildTimeProperties;
import de.schlund.pfixxml.config.includes.IncludesResolver;
import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.serverutil.SessionAdmin;
import de.schlund.pfixxml.testrecording.TestRecording;

public class PustefixContextXMLRequestHandlerParsingHandler extends CustomizationAwareParsingHandler {

    public void handleNodeIfActive(HandlerContext context) throws ParserException {
        Element serviceElement = (Element) context.getNode();
        Element pathElement = (Element) serviceElement.getElementsByTagNameNS(Constants.NS_PROJECT, "path").item(0);
        if (pathElement == null) {
            throw new ParserException("Could not find expected <path> element");
        }
        Element configurationFileElement = (Element) serviceElement.getElementsByTagNameNS(Constants.NS_PROJECT, "config-file").item(0);
        if (configurationFileElement == null) {
            throw new ParserException("Could not find expected <config-file> element");
        }
        String path = pathElement.getTextContent().trim();
        String configurationFile = configurationFileElement.getTextContent().trim();
        
        Collection<XMLGeneratorInfo> infoCollection = context.getObjectTreeElement().getRoot().getObjectsOfTypeFromSubTree(XMLGeneratorInfo.class);
        if (infoCollection.size() != 1) {
            throw new ParserException("Found " + infoCollection.size() + " instances of XMLGeneratorInfo but expected exactly one");
        }
        XMLGeneratorInfo info = infoCollection.iterator().next();
        String dependFile = info.getConfigurationFile();
       
        FileResource fileRes = ResourceUtil.getFileResource(configurationFile);
        Resource res = null;
        try {
            res = new UrlResource(fileRes.toURL());
        } catch(MalformedURLException x) {
            throw new ParserException("Illegal resource URL",x);
        }
        Iterator<Object> it = context.getObjectTreeElement().getObjects().iterator();
        while(it.hasNext()) {
            System.out.println("OBJ: "+it.next().getClass().getName());
        }
        Collection<BeanDefinitionRegistry> beanRegs = context.getObjectTreeElement().getObjectsOfTypeFromTopTree(BeanDefinitionRegistry.class);
        if(beanRegs.size()==0) throw new ParserException("No BeanDefinitionRegistry object found.");
        else if(beanRegs.size()>1) throw new ParserException("Multiple BeanDefinitionRegistry objects found.");
        BeanDefinitionRegistry beanReg = beanRegs.iterator().next();
        Properties buildTimeProperties = BuildTimeProperties.getProperties();
        CustomizationInfo cusInfo = new PropertiesBasedCustomizationInfo(buildTimeProperties);
        try {
            GlobalConfigurationHolder globalConfig = (new GlobalConfigurationReader()).readGlobalConfiguration();
            Parser contextXmlConfigParser = new ClasspathConfiguredParser("META-INF/org/pustefixframework/config/context-xml-service/parser/context-xml-service-config.xml");
            
            //Resolve config-includes
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setXIncludeAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(ResourceUtil.getFileResource(configurationFile).getInputStream()); 
            IncludesResolver resolver = new IncludesResolver("http://www.pustefix-framework.org/2008/namespace/context-xml-service-config", "config-include");
            resolver.resolveIncludes(doc);
            
            final ObjectTreeElement contextXmlConfigTree = contextXmlConfigParser.parse(doc, cusInfo, globalConfig, beanReg);
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
        
        BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(PustefixContextXMLRequestHandler.class);
        beanBuilder.setScope("singleton");
        beanBuilder.setInitMethodName("init");
        beanBuilder.addPropertyValue("dependFile", dependFile);
        beanBuilder.addPropertyValue("handlerURI", path + "/**");
        beanBuilder.addPropertyValue("context", new RuntimeBeanReference(ContextImpl.class.getName()));
        beanBuilder.addPropertyValue("configuration", config);
        beanBuilder.addPropertyValue("sessionAdmin", new RuntimeBeanReference(SessionAdmin.class.getName()));
        if(beanReg.isBeanNameInUse(TestRecording.class.getName())) {
            beanBuilder.addPropertyValue("testRecording", new RuntimeBeanReference(TestRecording.class.getName()));
        }
        beanBuilder.addPropertyValue("webappAdmin", new RuntimeBeanReference(WebappAdmin.class.getName()));
        BeanDefinition beanDefinition = beanBuilder.getBeanDefinition();
        BeanDefinitionHolder beanHolder = new BeanDefinitionHolder(beanDefinition, PustefixContextXMLRequestHandler.class.getName() + "#" + path);
        context.getObjectTreeElement().addObject(beanHolder);
        
    }

}
