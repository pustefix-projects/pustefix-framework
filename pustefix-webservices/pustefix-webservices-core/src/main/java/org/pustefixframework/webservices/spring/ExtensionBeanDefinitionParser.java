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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.pustefixframework.extension.ExtensionPoint;
import org.pustefixframework.extension.support.ExtensionTargetInfo;
import org.pustefixframework.webservices.osgi.WebserviceExtensionImpl;
import org.pustefixframework.webservices.osgi.WebserviceExtensionPoint;
import org.pustefixframework.webservices.osgi.WebserviceExtensionPointImpl;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.osgi.service.exporter.support.OsgiServiceFactoryBean;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author mleidig
 *
 */
public class ExtensionBeanDefinitionParser extends AbstractBeanDefinitionParser { 

	private final static String XMLNS = "http://pustefixframework.org/schema/webservices";
	
   
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {

	   BeanDefinitionBuilder beanDefBuilder = BeanDefinitionBuilder.rootBeanDefinition(WebserviceExtensionImpl.class);

	   beanDefBuilder.addPropertyValue("type", "webservice");
	   beanDefBuilder.addPropertyValue("extensionPointType", WebserviceExtensionPoint.class);
	   
	   List<ExtensionTargetInfo> infos = new ArrayList<ExtensionTargetInfo>();
	   NodeList nodes = element.getElementsByTagNameNS(XMLNS, "extends");
	   for(int i=0;i<nodes.getLength();i++) {
		   Element extElem = (Element)nodes.item(i);
		   String extPoint = extElem.getAttribute("extension-point").trim();
		   if(extPoint.length() == 0) throw new IllegalArgumentException("Mandatory attribute 'extension-point' is missing");
		   String version = extElem.getAttribute("version").trim();
		   if(version.length() == 0) version = "*";
		   ExtensionTargetInfo info = new ExtensionTargetInfo();
		   info.setExtensionPoint(extPoint);
		   info.setVersion(version);
		   infos.add(info);
	   }
	   beanDefBuilder.addPropertyValue("extensionTargetInfos", infos);
	   
	   
	   List<WebserviceExtensionPoint> extList = new ArrayList<WebserviceExtensionPoint>();
	   nodes = element.getElementsByTagNameNS(XMLNS, "extension-point");
	   for(int i=0;i<nodes.getLength();i++) {
		   Element extElem = (Element)nodes.item(i);
		   WebserviceExtensionPoint ext = parseExtensionPoint(extElem, parserContext);
		   extList.add(ext);
		   //list.add(beanDef);
       }
	   beanDefBuilder.addPropertyValue("extensionPoints", extList);
	   
	   ManagedList list = new ManagedList();
	   nodes = element.getElementsByTagNameNS(XMLNS, "webservice");
	   for(int i=0;i<nodes.getLength();i++) {
		   Element srvElem = (Element)nodes.item(i);
		   BeanDefinition beanDef = parseWebservice(srvElem, parserContext);
		   list.add(beanDef);
		  
	   }
	   beanDefBuilder.addPropertyValue("webserviceRegistrations", list);
	   
	   return beanDefBuilder.getBeanDefinition();
	   
   }
   
	
	private WebserviceExtensionPoint parseExtensionPoint(Element element, ParserContext context) {
		
		//BeanDefinitionBuilder beanDefBuilder = BeanDefinitionBuilder.rootBeanDefinition(WebserviceExtensionPointImpl.class);
		WebserviceExtensionPointImpl ext = new WebserviceExtensionPointImpl();
		
		//beanDefBuilder.addPropertyValue("type", "webservice");
		ext.setType("webservice");
		
		String id = element.getAttribute("id").trim();
		if(id.length() == 0) throw new IllegalArgumentException("Id is required");
		//beanDefBuilder.addPropertyValue("id", id);
		ext.setId(id);
		
		String version = element.getAttribute("version").trim();
		if(version.length() == 0) version = "0.0.0";
		//beanDefBuilder.addPropertyValue("version", version);
		ext.setVersion(version);
		
		String cardinality = element.getAttribute("cardinality").trim();
		if(cardinality.length() == 0) cardinality = "0..n";
		//beanDefBuilder.addPropertyValue("cardinality", cardinality);
		ext.setCardinality(cardinality);
		
		Map<String, String> serviceProperties = new HashMap<String, String>();
		serviceProperties.put("extension-point", id);
		serviceProperties.put("type", "webservice");
		serviceProperties.put("version", version);
	        
		LinkedList<Class<?>> exportedInterfaces = new LinkedList<Class<?>>();
		exportedInterfaces.add(ExtensionPoint.class);
		exportedInterfaces.add(WebserviceExtensionPoint.class);
		
		BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.rootBeanDefinition(OsgiServiceFactoryBean.class);
		beanBuilder.setScope("singleton");
		beanBuilder.addPropertyValue("interfaces", exportedInterfaces.toArray(new Class<?>[exportedInterfaces.size()]));
		beanBuilder.addPropertyValue("serviceProperties", serviceProperties);
		beanBuilder.addPropertyValue("target", ext);
		
		DefaultBeanNameGenerator beanNameGenerator = new DefaultBeanNameGenerator();
		BeanDefinition beanDef = beanBuilder.getBeanDefinition();
		BeanDefinitionRegistry beanReg = context.getRegistry();
		String beanName = beanNameGenerator.generateBeanName(beanDef, beanReg);
		beanReg.registerBeanDefinition(beanName, beanDef);
		
		return ext;
		
		//return beanDefBuilder.getBeanDefinition();
	}
	
   
   private BeanDefinition parseWebservice(Element element, ParserContext parserContext) {
	   
	   BeanDefinitionBuilder beanDefBuilder = BeanDefinitionBuilder.rootBeanDefinition(WebserviceRegistration.class);
	   
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
      
       beanDefBuilder.addPropertyValue("target", target);
       
       return beanDefBuilder.getBeanDefinition();
   }
   
}
