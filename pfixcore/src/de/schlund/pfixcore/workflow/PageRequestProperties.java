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


import de.schlund.pfixcore.util.*;
import de.schlund.pfixxml.*;
import java.util.*;
import org.apache.log4j.*;

/**
 * PageRequestProperties.java
 *
 *
 * Created: Sun Oct  7 13:28:11 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version
 *
 *
 */

public class PageRequestProperties {
    private static final String PREFIX = "pagerequest";

    private Properties properties;
    private HashSet    preqs     = new HashSet();
    private HashMap    preqprops = new HashMap();
    private Category   CAT       = Category.getInstance(this.getClass());
    
    public PageRequestProperties(Properties properties) throws Exception {
        this.properties = properties;

        Map map = PropertiesUtils.selectProperties(properties, PREFIX);
        HashSet set = new HashSet();

        for (Iterator i = map.keySet().iterator(); i.hasNext();) {
            String key = (String) i.next();
            key = key.substring(0, key.indexOf("."));

            if (properties.getProperty(PREFIX + "." + key + "." + PageMap.CLASSNAMEPROP)
                == null) {
                throw new XMLException(
                    "No 'classname' property found for " + "PageRequest '" + key + "'");
            }

            set.add(key);
        }

        for (Iterator i = set.iterator(); i.hasNext();) {
            PageRequest preq = new PageRequest((String) i.next());
            preqs.add(preq);
        }
    }

    public Properties getPropertiesForPageRequest(PageRequest preq) {
        Properties props = null;
        if (pageRequestIsDefined(preq)) {
            synchronized(preqprops) {
                props = (Properties) preqprops.get(preq.getName());
                if (props == null) {
                    HashMap map =
                        PropertiesUtils.selectProperties(properties, PREFIX + "." + preq.getName());
                    if (map != null) {
                        props = new Properties();
                        for (Iterator i = map.keySet().iterator(); i.hasNext();) {
                            String key = (String) i.next();
                            props.setProperty(key, (String) map.get(key));
                        }
                        preqprops.put(preq.getName(), props);
                    }
                }
            }
        }
        return props;
    }

    public boolean pageRequestIsDefined(PageRequest preq) {
        return (preqs.contains(preq));
    }

    public PageRequest[] getAllDefinedPageRequests() {
        return (PageRequest[]) preqs.toArray(new PageRequest[] {});
    }
} // PageRequestProperties
