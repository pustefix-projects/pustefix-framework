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
package org.pustefixframework.webservices.spring;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author mleidig
 *
 */
public class WebServiceBeanDefinitionParser extends AbstractSingleBeanDefinitionParser { 

   @Override
   protected Class<?> getBeanClass(Element element) {
       return WebServiceRegistration.class;
   }

   @Override
   protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder beanDefBuilder) {
    
       String serviceName = element.getAttribute("servicename");
       beanDefBuilder.addPropertyValue("serviceName", serviceName);
      
       String interfaceName = element.getAttribute("interface").trim();
       if(interfaceName.length()>0) beanDefBuilder.addPropertyValue("interface", interfaceName);
       
       String protocol = element.getAttribute("protocol");
       if(protocol.length()>0) beanDefBuilder.addPropertyValue("protocol", protocol);
       
       String sessionType = element.getAttribute("session");
       if(sessionType.length()>0) beanDefBuilder.addPropertyValue("sessionType", sessionType);

       String authConstraint = element.getAttribute("authconstraint");
       if(authConstraint.length()>0) beanDefBuilder.addPropertyValue("authConstraint", authConstraint);
       
       String synchronize = element.getAttribute("synchronize");
       if(synchronize.length()>0) beanDefBuilder.addPropertyValue("synchronize", Boolean.valueOf(synchronize));
       
       String whitelist = element.getAttribute("whitelist");
       if(!whitelist.isEmpty()) {
           String[] values = whitelist.split("[,\\s]+");
           List<Pattern> patterns = new ArrayList<>();
           for(String value: values) {
               patterns.add(Pattern.compile(value));
           }
           beanDefBuilder.addPropertyValue("whitelist", patterns);
       }
       
       Object target = null;
       if (element.hasAttribute("ref")) { 
           target = new RuntimeBeanReference(element.getAttribute("ref"));
       }
           
       //Handle nested bean reference/definition
       NodeList nodes = element.getChildNodes();
       for (int i = 0; i < nodes.getLength(); i++) {
           Node node = nodes.item(i);
           if (node instanceof Element) {
               Element subElement = (Element)node;
               if (element.hasAttribute("ref")) {
                   parserContext.getReaderContext().error("Nested bean reference/definition isn't allowed because "
                           +"the webservice 'ref' attribute is already set to '"+element.getAttribute("ref")+"'.", element);
               }
               target = parserContext.getDelegate().parsePropertySubElement(subElement, beanDefBuilder.getBeanDefinition());    
           }
       }
      
       if (target instanceof RuntimeBeanReference) {
           beanDefBuilder.addPropertyValue("targetBeanName", ((RuntimeBeanReference) target).getBeanName());
       } else {
           beanDefBuilder.addPropertyValue("target", target);
       }
      
   }
   
}
