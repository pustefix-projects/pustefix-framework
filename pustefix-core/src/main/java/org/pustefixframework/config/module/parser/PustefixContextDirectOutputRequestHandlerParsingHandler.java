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

package org.pustefixframework.config.module.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.pustefixframework.config.customization.CustomizationInfo;
import org.pustefixframework.config.customization.PropertiesBasedCustomizationInfo;
import org.pustefixframework.config.customization.RuntimeProperties;
import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.config.module.ModuleFlag;
import org.pustefixframework.resource.InputStreamResource;
import org.pustefixframework.resource.ResourceLoader;
import org.pustefixframework.resource.URLResource;
import org.pustefixframework.util.io.StreamUtils;
import org.pustefixframework.util.xml.NamespaceUtils;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
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

/**
 * Starts parsing of the direct output configuration file in Pustefix modules.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class PustefixContextDirectOutputRequestHandlerParsingHandler implements ParsingHandler {

    private static String DEPRECATED_CONFIG_NS = "http://www.pustefix-framework.org/2008/namespace/context-xml-service-config";
    private static String CONFIG_NS = "http://www.pustefix-framework.org/2009/namespace/context-xml-service-config";
    
    private static Logger LOG = Logger.getLogger(PustefixContextDirectOutputRequestHandlerParsingHandler.class);
    
    public void handleNode(HandlerContext context) throws ParserException {
        ResourceLoader resourceLoader = ParsingUtils.getSingleTopObject(ResourceLoader.class, context);

        Element serviceElement = (Element) context.getNode();
        
        Element configurationFileElement = (Element) serviceElement.getElementsByTagNameNS(serviceElement.getNamespaceURI(), "config-file").item(0);
        if (configurationFileElement == null) {
            throw new ParserException("Could not find expected <config-file> element");
        }
        
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
        
        Collection<BeanDefinitionRegistry> beanRegs = context.getObjectTreeElement().getObjectsOfTypeFromTopTree(BeanDefinitionRegistry.class);
        if (beanRegs.size() == 0) {
            throw new ParserException("No BeanDefinitionRegistry object found.");
        } else if (beanRegs.size() > 1) {
            throw new ParserException("Multiple BeanDefinitionRegistry objects found.");
        }
        BeanDefinitionRegistry beanReg = beanRegs.iterator().next();
        Properties runtimeProperties = RuntimeProperties.getProperties();
        CustomizationInfo cusInfo = new PropertiesBasedCustomizationInfo(runtimeProperties);
        try {
            ConfigurableOsgiBundleApplicationContext appContext = ParsingUtils.getSingleTopObject(ConfigurableOsgiBundleApplicationContext.class, context);
            
            Parser directOutputConfigParser = new OSGiAwareParser(appContext.getBundleContext(), "META-INF/org/pustefixframework/config/direct-output-service/parser/direct-output-service-config-module.xml");
            
            InputStream in = null;
            try {
                String namespace = NamespaceUtils.getNamespace(configurationResource.getInputStream());
                if(DEPRECATED_CONFIG_NS.equals(namespace)) {
                    LOG.warn("Direct output configuration uses deprecated namespace '" + DEPRECATED_CONFIG_NS + "'. " +
                             "You should replace it by '" + CONFIG_NS +"'.");
                    LOG.warn("Trying to continue by replacing the namespace on the fly.");
                    String content = StreamUtils.load(configurationResource.getInputStream(), "utf-8");
                    content = content.replaceAll(DEPRECATED_CONFIG_NS, CONFIG_NS);
                    in = new ByteArrayInputStream(content.getBytes("utf8"));
                }
            } catch(Exception x) {
                throw new ParserException("Error checking namespace", x);
            }
            if(in == null) in = configurationResource.getInputStream();
            
            InputSource configurationSource = new InputSource(in);
            if (configurationResource instanceof URLResource) {
                URLResource urlResource = (URLResource) configurationResource;
                configurationSource.setSystemId(urlResource.getURL().toExternalForm());
            }
            
            final ObjectTreeElement directOutputConfigTree = directOutputConfigParser.parse(configurationSource, cusInfo, beanReg, appContext, resourceLoader, new ModuleFlag());
            SubObjectTree subTree = new SubObjectTree() {
              public ObjectTreeElement getRoot() {
                    return directOutputConfigTree;
                }  
            };
            context.getObjectTreeElement().addObject(subTree);
        } catch (ParserException e) {
            throw new BeanDefinitionStoreException("Error while parsing " + configurationURI.toASCIIString() + ": " + e.getMessage(), e);
        } catch (IOException e) {
            throw new BeanDefinitionStoreException("Error while parsing " + configurationURI.toASCIIString() + ": " + e.getMessage(), e);
        }
    }

}
