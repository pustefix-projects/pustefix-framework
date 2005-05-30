/*
 * de.schlund.pfixcore.webservice.config.ConfigProperties
 */
package de.schlund.pfixcore.webservice.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.*;

import org.apache.log4j.Category;

/**
 * ConfigProperties.java 
 * 
 * Created: 22.07.2004
 * 
 * @author mleidig
 */
public class ConfigProperties {

    private Category CAT=Category.getInstance(getClass().getName());
    private boolean DEBUG=CAT.isDebugEnabled();
    
    private File[] propFiles;
    private Properties properties;
    private HashMap modTimes;
    
    public ConfigProperties(File[] propFiles) throws Exception {
        this.propFiles=propFiles;
        properties=new Properties(System.getProperties());
        modTimes=new HashMap();
        loadProperties();
    }
    
    public void reload() throws Exception {
        loadProperties();
    }

    private void loadProperties() throws Exception {
        for(int i=0;i<propFiles.length;i++) {
            if(propFiles[i]!=null && propFiles[i].exists()) {
                Long modTime=(Long)modTimes.get(propFiles[i]);
                long time=propFiles[i].lastModified();
                if(modTime==null || time>modTime.longValue()) {
                    properties.load(new FileInputStream(propFiles[i]));
                    modTimes.put(propFiles[i],new Long(time));
                }
            } else {
                throw new FileNotFoundException("Property file '"+propFiles[i].getAbsolutePath()+"' doesn't exist");
            }
        }
    }
    
    public Iterator getPropertyKeys() {
        return getPropertyKeys(".*");
    }
    
    public Iterator getPropertyKeys(String regex) {
        Pattern pat=Pattern.compile(regex);
        ArrayList al=new ArrayList();
        Enumeration eeek_java_5_enum=properties.propertyNames();
        while(eeek_java_5_enum.hasMoreElements()) {
            String key=(String)eeek_java_5_enum.nextElement();
            Matcher mat=pat.matcher(key);
            if(mat.matches()) al.add(key);
        }
        return al.iterator();
    }
    
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    public boolean getBooleanProperty(String propName,boolean mandatory,boolean defaultVal) throws ConfigException {
        String val=properties.getProperty(propName);
        if(val==null) {
            if(mandatory) throw new ConfigException(ConfigException.MISSING_PROPERTY,propName);
            else return defaultVal;
        }
        if(val.equalsIgnoreCase("true")) return true;
        if(val.equalsIgnoreCase("false")) return false;
        throw new ConfigException(ConfigException.ILLEGAL_PROPERTY_VALUE,propName,val);
    }

    public int getIntegerProperty(String propName,boolean mandatory,int defaultVal) throws ConfigException {
        String val=properties.getProperty(propName);
        if(val==null) {
            if(mandatory) throw new ConfigException(ConfigException.MISSING_PROPERTY,propName);
            else return defaultVal;
        }
        try {
        	int intVal=Integer.parseInt(val);
            return intVal;
        } catch(NumberFormatException x) {
        	throw new ConfigException(ConfigException.ILLEGAL_PROPERTY_VALUE,propName,val);
        }
    }
    
    public String getStringProperty(String propName,boolean mandatory) throws ConfigException {
        String val=properties.getProperty(propName);
        if(val==null && mandatory) throw new ConfigException(ConfigException.MISSING_PROPERTY,propName);
        return val;
    }

    public String getStringProperty(String propName,String[] allowedValues,boolean mandatory) throws ConfigException {
        String val=properties.getProperty(propName);
        if(val==null) {
            if(mandatory) throw new ConfigException(ConfigException.MISSING_PROPERTY,propName);
            else return null;
        }
        for(int i=0;i<allowedValues.length;i++) {
            if(val.equals(allowedValues[i])) return val;
        }
        throw new ConfigException(ConfigException.ILLEGAL_PROPERTY_VALUE,propName,val);
    }
    
    public String getStringProperty(String propName,String[] allowedValues,String defaultValue) throws ConfigException {
        String val=properties.getProperty(propName);
        if(val==null) return defaultValue;
        else {
        	for(int i=0;i<allowedValues.length;i++) {
        		if(val.equals(allowedValues[i])) return val;
        	}
        	throw new ConfigException(ConfigException.ILLEGAL_PROPERTY_VALUE,propName,val);
        }
    }
    
    public String getStringProperty(String propName,String regex,boolean mandatory) throws ConfigException {
        String val=properties.getProperty(propName);
        if(val==null) {
            if(mandatory) throw new ConfigException(ConfigException.MISSING_PROPERTY,propName);
            else return null;
        }
        Pattern pat=Pattern.compile(regex);
        Matcher mat=pat.matcher(val);
        if(mat.matches()) return val; 
        throw new ConfigException(ConfigException.ILLEGAL_PROPERTY_VALUE,propName,val);
    }
    
    public Properties getProperties() {
        return getProperties(".*");
    }
    
    public Properties getProperties(String regex) {
        Properties props=new Properties();
        Iterator it=getPropertyKeys(regex);
        while(it.hasNext()) {
            String key=(String)it.next();
            String val=properties.getProperty(key);
            props.setProperty(key,val);
        }
        return props;
    }
    
    public boolean diffProperties(ConfigProperties diffProps) {
        return diffProperties(diffProps,".*");
    }
    
    public boolean diffProperties(ConfigProperties diffProps,String regex) {
        HashSet keys=new HashSet();
        Iterator it=getPropertyKeys(regex);
        while(it.hasNext()) {
            String key=(String)it.next();
            keys.add(key);
            String diffVal=diffProps.getProperty(key);
            if(diffVal==null) return true;
            String val=getProperty(key);
            if(!val.equals(diffVal)) return true;
        }
        it=diffProps.getPropertyKeys(regex);
        while(it.hasNext()) {
            String key=(String)it.next();
            if(!keys.contains(key)) return true;
        }
        return false;
    }
    
    public String toString() {
        return toString(".*");
    }
    
    public String toString(String regex) {
        StringBuffer sb=new StringBuffer();
        ArrayList al=new ArrayList();
        Iterator it=getPropertyKeys(regex);
        while(it.hasNext()) al.add(it.next());
        Collections.sort(al);
        it=al.iterator();
        while(it.hasNext()) {
            String key=(String)it.next();
            String val=properties.getProperty(key);
            sb.append(key+"="+val+"\n");
        }
        return sb.toString();
    }

}
