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

import java.util.*;
import org.apache.log4j.*;
import de.schlund.pfixcore.util.*;

/**
 * @author: jtl
 *
 *
 */

public class PageFlow {
    private              ArrayList   allsteps = new ArrayList(); 
    private              String      flowname;
    private final static String      PROPERTY_PREFIX    = PageFlowManager.PROP_PREFIX;
    private final static String      FLAG_FINAL         = "FINAL";
    private final static String      PROPERTY_PAGEFLOW  = "context.pageflowproperty";
    private static       Category    LOG = Category.getInstance(PageFlow.class.getName());
    private              PageRequest finalpage = null;
    private              boolean     flow_stop_after_current = false;

    public PageFlow(Properties props, String name) {
        flowname = name;
        Map     map    = PropertiesUtils.selectProperties(props, PROPERTY_PREFIX + "." + name);
        TreeMap sorted = new TreeMap();
        
        for (Iterator i = map.keySet().iterator(); i.hasNext(); ) {
            Integer index;
            String  key      = (String) i.next();
            String  pagename = (String) map.get(key);
            if (key.equals(FLAG_FINAL)) {
                finalpage = new PageRequest(pagename);
            } else {
                try {
                    index = new Integer(key);
                    sorted.put(index, pagename);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("**** The Pageflow [" + name +
                                               "] didn't specifiy a numerical index for page [" +
                                               pagename + "] ****\n" + e.getMessage());
                }
            }
        }

        Map     propmap          = PropertiesUtils.selectProperties(props, PROPERTY_PAGEFLOW + "." + name);
        String  stopaftercurrent = (String) propmap.get("stopatfirstaftercurrent");
        if (stopaftercurrent != null && stopaftercurrent.equals("true")) {
            flow_stop_after_current = true;
        } else {
            flow_stop_after_current = false;
        }
        
        for (Iterator i = sorted.values().iterator(); i.hasNext(); ) {
            String      pagename = (String) i.next();
            PageRequest page     = new PageRequest(pagename);
            allsteps.add(page);
        }
        
        if (LOG.isDebugEnabled()) {
            for (int i = 0; i < allsteps.size(); i++) {
                LOG.debug(">>> Workflow '" + name + "' Step #" + i +
                          " name [" + ((PageRequest) allsteps.get(i)).getName() + "]");
            }
        }
    }

    public boolean getStopAtFirstAfterCurrent() {
        return flow_stop_after_current;
    }
    
    public boolean containsPageRequest(PageRequest page) {
        return allsteps.contains(page);
    }

    public String getName() {
        return flowname;
    }

    public PageRequest[] getAllSteps() {
        return (PageRequest[]) allsteps.toArray(new PageRequest[] {});
    }
    
    public PageRequest getFirstStep() {
	return (PageRequest) allsteps.get(0);
    }
    
    public PageRequest getFinalPage() {
        return finalpage;
    }

    public String toString() {
	String ret = "";
	for (int i = 0; i < allsteps.size(); i++) {
	    if (ret.length() > 0) {
		ret += ", ";
	    } else {
                ret = flowname + " = ";
            }
	    ret += "[" + i + ": " + ((PageRequest) allsteps.get(i)).getName() + "]";
	}
	return ret;
    }
}
