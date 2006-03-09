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

package de.schlund.pfixcore.workflow;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;

import org.apache.log4j.Category;

import de.schlund.pfixxml.ConfigurableObject;
import de.schlund.pfixxml.Variant;
import de.schlund.pfixxml.config.ContextConfig;
import de.schlund.pfixxml.config.PageRequestConfig;

/**
 * PageRequestProperties.java
 *
 *
 * Created: Sun Oct  7 13:28:11 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 */

public class PageRequestProperties implements ConfigurableObject {
    private HashSet            preqnames        = new HashSet();
    private HashMap            preqprops        = new HashMap();
    private HashMap            variantpagecache = new HashMap();
    private Category           CAT              = Category.getInstance(this.getClass());
    public static final String PREFIX           = "pagerequest";
    
    public void init(Object confObj) throws Exception {
        ContextConfig config = (ContextConfig) confObj;
        
        PageRequestConfig[] pageConfigs = config.getPageRequests();
        
        for (int i = 0; i < pageConfigs.length; i++) {
            PageRequestConfig pageConfig = pageConfigs[i];
            String fullname = pageConfig.getPageName();
            preqnames.add(fullname);
            preqprops.put(fullname, pageConfig.getProperties());
        }
    }

    public String toString() {
        StringBuffer buff = new StringBuffer();
        for (Iterator i = preqprops.keySet().iterator(); i.hasNext();) {
            String     key   = (String) i.next();
            Properties props = (Properties) preqprops.get(key);
            buff.append(key + " ==> " + props.toString() + "\n");
        }
        return buff.toString();
    }

    public Properties getPropertiesForPageRequest(PageRequest preq) {
    	return getPropertiesForPageRequestName(preq.getName());
    }

    public Properties getPropertiesForPageRequestName(String name) {
    	return (Properties) preqprops.get(name);
    }

    public boolean pageRequestIsDefined(PageRequest preq) {
        return pageRequestNameIsDefined(preq.getName());
    }

    public boolean pageRequestNameIsDefined(String fullname) {
        return preqnames.contains(fullname);
    }

    public String[] getAllDefinedPageRequestNames() {
        return (String[]) preqnames.toArray(new String[] {});
    }

    public String getVariantMatchingPageRequestName(String name, Variant variant) {
        String variant_id = variant.getVariantId();
        String fullname   = (String) variantpagecache.get(variant_id + "@" + name);
        
        if (fullname == null) {
            synchronized(variantpagecache) {
                fullname = (String) variantpagecache.get(variant_id + "@" + name);
                if (fullname == null) {
                    // CAT.debug("------ Cache miss " + variant_id + "@" + name);
                    String[] variant_arr = variant.getVariantFallbackArray();
                    for (int i = 0; i < variant_arr.length; i++) {
                        String tmp = name + "::" + variant_arr[i];
                        if (pageRequestNameIsDefined(tmp)) {
                            CAT.debug("=== Found PR for '" + tmp + "' ===");
                            fullname = tmp;
                            break;
                        } else {
                            CAT.debug("=== PR NOT FOUND for '" + tmp + "' ===");
                        }
                    }
                    if (fullname == null) {
                        fullname = name;
                    }
                    variantpagecache.put(variant_id + "@" + name, fullname);
                }
            } // Yes, DCL doesn't work, but if we hit the race, it doesn't matter here.
        }
        return fullname;
    }
    
} // PageRequestProperties
