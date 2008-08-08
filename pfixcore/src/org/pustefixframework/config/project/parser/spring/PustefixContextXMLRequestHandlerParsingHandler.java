/*
 * Place license here
 */

package org.pustefixframework.config.project.parser.spring;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import org.pustefixframework.config.Constants;
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
import org.w3c.dom.Element;

import com.marsching.flexiparse.objectree.ObjectTreeElement;
import com.marsching.flexiparse.objectree.SubObjectTree;
import com.marsching.flexiparse.parser.ClasspathConfiguredParser;
import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.Parser;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixcore.workflow.ContextImpl;
import de.schlund.pfixcore.workflow.context.ServerContextImpl;
import de.schlund.pfixxml.config.BuildTimeProperties;
import de.schlund.pfixxml.config.ContextXMLServletConfig;
import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;

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
       
        //TODO: remove file renaming
        String configFilePath = configurationFile.substring(0,configurationFile.indexOf(".conf.xml"))+".xml";
        FileResource fileRes = ResourceUtil.getFileResource(configFilePath);
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
            Parser contextXmlConfigParser = new ClasspathConfiguredParser("META-INF/org/pustefixframework/config/contextxml/parser/context-xml-config.xml");
            final ObjectTreeElement contextXmlConfigTree = contextXmlConfigParser.parse(res.getInputStream(), cusInfo, globalConfig, beanReg);
            SubObjectTree subTree = new SubObjectTree() {
              public ObjectTreeElement getRoot() {
                    return contextXmlConfigTree;
                }  
            };
            context.getObjectTreeElement().addObject(subTree);
        } catch (ParserException e) {
            throw new BeanDefinitionStoreException("Error while parsing " + res + ": " + e.getMessage(), e);
        } catch (IOException e) {
            throw new BeanDefinitionStoreException("Error while parsing " + res + ": " + e.getMessage(), e);
        }
        
        ContextXMLServletConfig config = context.getObjectTreeElement().getObjectsOfTypeFromSubTree(ContextXMLServletConfig.class).iterator().next();
        
        BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(PustefixContextXMLRequestHandler.class);
        beanBuilder.setScope("singleton");
        beanBuilder.addPropertyValue("propFile", configurationFile);
        beanBuilder.addPropertyValue("dependFile", dependFile);
        beanBuilder.addPropertyValue("handlerURI", path + "/**");
        beanBuilder.addPropertyValue("commonPropFile", "pfixroot:/common/conf/pustefix.xml");
        beanBuilder.addPropertyValue("serverContext", new RuntimeBeanReference(ServerContextImpl.class.getName()));
        beanBuilder.addPropertyValue("context", new RuntimeBeanReference(ContextImpl.class.getName()));
        beanBuilder.addPropertyValue("configuration", config);
        BeanDefinition beanDefinition = beanBuilder.getBeanDefinition();
        BeanDefinitionHolder beanHolder = new BeanDefinitionHolder(beanDefinition, PustefixContextXMLRequestHandler.class.getName() + "#" + path);
        context.getObjectTreeElement().addObject(beanHolder);
        
    }

}
