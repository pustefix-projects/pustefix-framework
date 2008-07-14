/*
 * Place license here
 */

package org.pustefixframework.config.project.parser.spring;

import java.util.Collection;

import org.pustefixframework.config.Constants;
import org.pustefixframework.config.customization.CustomizationAwareParsingHandler;
import org.pustefixframework.config.project.XMLGeneratorInfo;
import org.pustefixframework.http.PustefixContextXMLRequestHandler;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

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
        
        BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(PustefixContextXMLRequestHandler.class);
        beanBuilder.setScope("singleton");
        beanBuilder.addPropertyValue("propFile", configurationFile);
        beanBuilder.addPropertyValue("dependFile", dependFile);
        beanBuilder.addPropertyValue("handlerURI", path + "/**");
        beanBuilder.addPropertyValue("commonPropFile", "pfixroot:/common/conf/pustefix.xml");
        BeanDefinition beanDefinition = beanBuilder.getBeanDefinition();
        BeanDefinitionHolder beanHolder = new BeanDefinitionHolder(beanDefinition, PustefixContextXMLRequestHandler.class.getName() + "#" + path);
        context.getObjectTreeElement().addObject(beanHolder);
    }

}
