/*
 * de.schlund.pfixcore.webservice.config.AbstractConfig
 */
package de.schlund.pfixcore.webservice.config;

import java.io.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;

/**
 * AbstractConfig.java 
 * 
 * Created: 13.08.2004
 * 
 * @author mleidig
 */
public abstract class AbstractConfig {

    protected ConfigProperties props;
    
    public ConfigProperties getConfigProperties() {
        return props;
    }
    
    protected abstract String getPropertiesDescriptor();
    
    protected abstract Properties getProperties();
    
    protected void serializeProperties(OutputStream out) throws IOException {
        Properties p=getProperties();
        p.store(out,getPropertiesDescriptor());
    }
    
    public void saveProperties(File file) throws IOException {
        serializeProperties(new FileOutputStream(file));
    }
    
    public boolean doesDiff(AbstractConfig conf) {
        Properties props1=getProperties();
        Properties props2=conf.getProperties();
        HashSet keys=new HashSet();
        Iterator it=props1.keySet().iterator();
        while(it.hasNext()) {
            String key=(String)it.next();
            keys.add(key);
            String diffVal=props2.getProperty(key);
            if(diffVal==null) return true;
            String val=props1.getProperty(key);
            if(!val.equals(diffVal)) return true;
        }
        it=props2.keySet().iterator();
        while(it.hasNext()) {
            String key=(String)it.next();
            if(!keys.contains(key)) return true;
        }
        return false;
    }
    
    public String toString() {
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        try {
            serializeProperties(out);
        } catch(IOException x) {
            return getPropertiesDescriptor();
        }
        return new String(out.toByteArray());
    }
    
}
