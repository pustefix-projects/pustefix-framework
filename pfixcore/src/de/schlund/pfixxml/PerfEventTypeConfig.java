/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package de.schlund.pfixxml;

import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Logger;

import de.schlund.pfixcore.util.PropertiesUtils;

/**
 * @author jh
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class PerfEventTypeConfig {
    private static PerfEventTypeConfig instance = new PerfEventTypeConfig();
    private static Logger LOG = Logger.getLogger(PerfEventTypeConfig.class);
    private boolean initialised = false;
    private HashMap properties;
    
    private PerfEventTypeConfig() {}
    
    public static PerfEventTypeConfig getInstance() { 
        return instance;
    }
    
    
    
    public void init(Properties props) throws Exception {
        properties = PropertiesUtils.selectProperties(props, "perfstat");
        initialised = true;
    }

    public long getPerfDelayProperty(String name) {
        checkInit();
        if(properties.get(name) == null) {
            LOG.error("Property named '"+name+"' not found.");
            throw new IllegalArgumentException("Property named '"+name+"' not found");
        }
        return Long.parseLong(properties.get(name).toString());
    }
    
    private void checkInit() {
        if(!initialised) {
            LOG.error("Factory not configured yet!");
            throw new IllegalStateException("Factory not configured yet!");
        }
    }
}
