/*
 * de.schlund.pfixcore.webservice.config.Configuration
 */
package de.schlund.pfixcore.webservice.config;

import java.util.*;

/**
 * Configuration.java 
 * 
 * Created: 22.07.2004
 * 
 * @author mleidig
 */
public class Configuration {

    private ConfigProperties props;
    private GlobalServiceConfig globConf;
    private HashMap srvsConf;
    
    public Configuration(ConfigProperties props) throws ConfigException {
        this.props=props;
        init();
    }
    
    private void init() throws ConfigException {
        globConf=new GlobalServiceConfig(props);
        srvsConf=new HashMap();
        Iterator it=props.getPropertyKeys("webservice\\.[^\\.]*\\.name");
        while(it.hasNext()) {
            String key=(String)it.next();
            String name=props.getProperty(key);
            ServiceConfig sc=new ServiceConfig(props,name);
            srvsConf.put(name,sc);
        }
    }
    
    public void reload() throws ConfigException {
        globConf.reload();
        HashSet names=new HashSet();
        Iterator it=props.getPropertyKeys("webservice\\.[^\\.]*\\.name");
        while(it.hasNext()) {
            String key=(String)it.next();
            String name=props.getProperty(key);
            ServiceConfig sc=(ServiceConfig)srvsConf.get(name);
            if(sc!=null) sc.reload();
            else {
                sc=new ServiceConfig(props,name);
                srvsConf.put(name,sc);
            }
            names.add(name);
        }
        it=srvsConf.keySet().iterator();
        while(it.hasNext()) {
            String name=(String)it.next();
            if(!names.contains(name)) srvsConf.remove(name);
        }
    }
    
    public GlobalServiceConfig getGlobalServiceConfig() {
        return globConf;
    }
    
    public ServiceConfig getServiceConfig(String name) {
        return (ServiceConfig)srvsConf.get(name);
    }
    
    public Iterator getServiceConfig() {
        return srvsConf.values().iterator();
    }
    
    public boolean doesDiff(Configuration sc) {
        if(sc.getGlobalServiceConfig().doesDiff(getGlobalServiceConfig())) return true;
        Iterator it=getServiceConfig();
        int cnt=0;
        while(it.hasNext()) {
            cnt++;
            ServiceConfig conf=(ServiceConfig)it.next();
            ServiceConfig scConf=sc.getServiceConfig(conf.getName());
            if(scConf==null || scConf.doesDiff(conf)) return true;
        }
        it=sc.getServiceConfig();
        int scCnt=0;
        while(it.hasNext()) {
            scCnt++;
            it.next();
        }
        if(cnt!=scCnt) return true;
        return false;
    }
    
}
