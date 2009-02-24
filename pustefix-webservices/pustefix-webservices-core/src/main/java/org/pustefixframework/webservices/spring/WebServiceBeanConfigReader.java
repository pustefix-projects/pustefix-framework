package org.pustefixframework.webservices.spring;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.pustefixframework.webservices.Constants;
import org.pustefixframework.webservices.config.ServiceConfig;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.FileSystemResource;

import de.schlund.pfixxml.resources.FileResource;

public class WebServiceBeanConfigReader {
    
    public static List<ServiceConfig> read(FileResource res) throws Exception {
        
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(WebServiceBeanConfigReader.class.getClassLoader());
        
        try {
        
            SimpleBeanDefinitionRegistry beanReg = new SimpleBeanDefinitionRegistry();
            XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(beanReg);
            File file = new File(res.toURL().toURI());
            FileSystemResource fileRes = new FileSystemResource(file);
            xmlReader.loadBeanDefinitions(fileRes);
            
            List<ServiceConfig> serviceList = new ArrayList<ServiceConfig>();
            String[] names = beanReg.getBeanDefinitionNames();
            for(String name:names) {
                
                BeanDefinition beanDef = beanReg.getBeanDefinition(name);
                if(beanDef.getBeanClassName().equals(WebServiceRegistration.class.getName())) {
                    MutablePropertyValues props = beanDef.getPropertyValues();
               
                    String serviceName = getStringValue(props,"serviceName",true);
                    String protocol = getStringValue(props,"protocol",false);
                    String interfaceName = getStringValue(props,"interface",false);
                    String targetBeanName = getStringValue(props,"targetBeanName",false);
                    String implName = null;
                    if(targetBeanName == null) {
                        PropertyValue prop = props.getPropertyValue("target");
                        if(prop==null) throw new IllegalArgumentException("Either 'target' or 'targetBeanName' property must be set.");
                        Object target = prop.getValue();
                        if(target instanceof BeanDefinitionHolder) {
                            BeanDefinitionHolder holder = (BeanDefinitionHolder)target;
                            BeanDefinition holderDef = holder.getBeanDefinition();
                            implName = getImplementation(holderDef);
                        } else throw new IllegalArgumentException("Object of type '"+BeanDefinitionHolder.class.getName()+"' "+
                                "expected as value of property 'target'!");
                    } else {
                        BeanDefinition targetBeanDef = beanReg.getBeanDefinition(targetBeanName);
                        implName = getImplementation(targetBeanDef);
                    }
                    String authConstraintRef = getStringValue(props,"authConstraint",false);
                    
                    ServiceConfig serviceConfig = new ServiceConfig(null);
                    serviceConfig.setName(serviceName);
                    serviceConfig.setScopeType(Constants.SERVICE_SCOPE_APPLICATION);
                    serviceConfig.setInterfaceName(interfaceName);
                    serviceConfig.setImplementationName(implName);
                    serviceConfig.setProtocolType(protocol);
                    serviceConfig.setAuthConstraintRef(authConstraintRef);
                    serviceList.add(serviceConfig);
                }
                
            }
            return serviceList;
        
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        } 
    }
    
    private static String getImplementation(BeanDefinition beanDef) {
        if(beanDef.getOriginatingBeanDefinition()!=null) {
            return getImplementation(beanDef.getOriginatingBeanDefinition());
        }
        return beanDef.getBeanClassName();
    }
    
    private static String getStringValue(MutablePropertyValues props, String propName, boolean mandatory) {
        String value = null;
        PropertyValue prop = props.getPropertyValue(propName);
        if(prop!=null) value = (String)prop.getValue();
        if(value!=null) {
            value = value.trim();
            if(value.equals("")) value = null;
        }
        if(value == null && mandatory) throw new IllegalArgumentException("BeanDefinition property '"+propName+"' is mandatory.");
        return value;
    }
    
}
