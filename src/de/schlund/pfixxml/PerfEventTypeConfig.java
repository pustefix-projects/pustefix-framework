/*
 * Created on 30.06.2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.schlund.pfixxml;

import java.util.HashMap;
import java.util.Properties;

import de.schlund.pfixcore.util.PropertiesUtils;
import de.schlund.util.FactoryInit;

/**
 * @author jh
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class PerfEventTypeConfig implements FactoryInit {
    private static PerfEventTypeConfig instance = new PerfEventTypeConfig();
    private boolean initialised = false;
    private HashMap properties;
    
    private PerfEventTypeConfig() {}
    
    public static PerfEventTypeConfig getInstance() { 
        return instance;
    }
    
    
    
    public void init(Properties props) throws Exception {
        properties = PropertiesUtils.selectProperties(props, "perfstat");
        System.out.println("properties: "+properties);
    }

    public long getPerfDelayProperty(String name) {
        if(properties.get(name) == null) {
            throw new IllegalArgumentException("Property named '"+name+"'   ");
        }
        return Long.parseLong(properties.get(name).toString());
    }
    
    private void checkInit() {
        if(!initialised) {
            throw new IllegalStateException("Factory not configured yet!");
        }
    }
}
