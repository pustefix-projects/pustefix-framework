/*
 * de.schlund.pfixcore.webservice.config.AbstractConfiguration
 */
package de.schlund.pfixcore.webservice.config;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * AbstractConfiguration.java 
 * 
 * Created: 13.08.2004
 * 
 * @author mleidig
 */
public abstract class AbstractConfiguration {

    protected ConfigProperties props;
    
    protected boolean getBooleanProperty(String propName,boolean mandatory) throws ServiceConfigurationException {
        String val=props.getProperty(propName);
        if(val==null) {
            if(mandatory) throw new ServiceConfigurationException(ServiceConfigurationException.MISSING_PROPERTY,propName);
            else return false;
        }
        if(val.equalsIgnoreCase("true")) return true;
        if(val.equalsIgnoreCase("false")) return false;
        throw new ServiceConfigurationException(ServiceConfigurationException.ILLEGAL_PROPERTY_VALUE,propName,val);
    }
    
    protected String getStringProperty(String propName,String[] allowedValues,boolean mandatory) throws ServiceConfigurationException {
        String val=props.getProperty(propName);
        if(val==null) {
            if(mandatory) throw new ServiceConfigurationException(ServiceConfigurationException.MISSING_PROPERTY,propName);
            else return null;
        }
        for(int i=0;i<allowedValues.length;i++) {
            if(val.equals(allowedValues[i])) return val;
        }
        throw new ServiceConfigurationException(ServiceConfigurationException.ILLEGAL_PROPERTY_VALUE,propName,val);
    }
    
    public boolean changed(AbstractConfiguration config) {
        System.out.println("CHECK");
        Method[] methods=config.getClass().getMethods();
        for(int i=0;i<methods.length;i++) {
            String name=methods[i].getName();
            Class retType=methods[i].getReturnType();
            if(name.startsWith("get") && methods[i].getParameterTypes().length==0 && (retType.isPrimitive() || retType==String.class)) {
                try {
                    Object res1=methods[i].invoke(this,new Object[] {});
                    Object res2=methods[i].invoke(config,new Object[] {});
                    System.out.println("check "+methods[i].getName()+" "+res1.equals(res2));
                    if(!res1.equals(res2)) return true;
                } catch (Exception x) {
                    x.printStackTrace();
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean equals(String s1,String s2) {
        if(s1==null && s2==null) return true;
        if(s1==null || s2==null) return false;
        return s1.equals(s2);
    }
    
}
