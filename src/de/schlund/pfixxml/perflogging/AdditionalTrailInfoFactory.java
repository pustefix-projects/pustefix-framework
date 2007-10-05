package de.schlund.pfixxml.perflogging;


import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class AdditionalTrailInfoFactory {
    private static AdditionalTrailInfoFactory instance = new AdditionalTrailInfoFactory();
    private static Logger LOG = Logger.getLogger(AdditionalTrailInfoFactory.class);

    private AdditionalTrailInfoFactory() {} 
    
    private Map<String, AdditionalTrailInfo> implementors = new HashMap<String, AdditionalTrailInfo>();
        
    public static AdditionalTrailInfoFactory getInstance() {
        return instance;
    }
    
    public synchronized AdditionalTrailInfo getAdditionalTrailInfo(String implemenation_class) {
        
        if (implementors.get(implemenation_class) == null) {
            LOG.info("Creating object from class "+implemenation_class);
            try {
                Class<?>             clazz = Class.forName(implemenation_class);
                AdditionalTrailInfo info  = (AdditionalTrailInfo) clazz.newInstance();
                implementors.put(implemenation_class, info);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Didn't find class " + implemenation_class, e);
            } catch (InstantiationException e) {
                throw new IllegalStateException("Couldn't instantiate " + implemenation_class, e);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        return implementors.get(implemenation_class);
    }

}
