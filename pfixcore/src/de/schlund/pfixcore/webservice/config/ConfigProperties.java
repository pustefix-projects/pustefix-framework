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
    
    public Iterator getPropertyKeys(String regex) {
        Pattern pat=Pattern.compile(regex);
        ArrayList al=new ArrayList();
        Enumeration enum=properties.propertyNames();
        while(enum.hasMoreElements()) {
            String key=(String)enum.nextElement();
            Matcher mat=pat.matcher(key);
            if(mat.matches()) al.add(key);
        }
        return al.iterator();
    }
    
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    public String toString() {
        StringBuffer sb=new StringBuffer();
        ArrayList al=new ArrayList(properties.keySet());
        Collections.sort(al);
        Iterator it=al.iterator();
        while(it.hasNext()) {
            String key=(String)it.next();
            String val=properties.getProperty(key);
            sb.append(key+"="+val+"\n");
        }
        return sb.toString();
    }

}
