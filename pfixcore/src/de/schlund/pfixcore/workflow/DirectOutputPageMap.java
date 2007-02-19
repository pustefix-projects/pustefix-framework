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
import java.util.Iterator;

import org.apache.log4j.Category;

import de.schlund.pfixxml.ConfigurableObject;
import de.schlund.pfixxml.config.DirectOutputPageRequestConfig;
import de.schlund.pfixxml.config.DirectOutputServletConfig;

/**
 * <code>DirectOutputPageMap</code> holds a mapping of PageRequests to DirectOutputStates. It
 * implements the PropertyObject interface and is created and initialized by a PropertyObjectManager. This
 * ensures that the the DirectOutputPageMap will be recreated every time the properties change.
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 */
public class DirectOutputPageMap implements ConfigurableObject {
    protected            HashMap  pagemap       = new HashMap();
    private final static Category CAT           = Category.getInstance(DirectOutputPageMap.class.getName());
    
    /**
     * The <code>init</code> method initializes a mapping from {@link PageRequest}s to {@link DirectOutputState}s.
     *
     * @param confObj a {@link de.schlund.pfixxml.config.DirectOutputServletConfig} object
     * @exception Exception if an error occurs
     */
    public void init(Object confObj) throws Exception {
        DirectOutputServletConfig config = (DirectOutputServletConfig) confObj;
        
        for (DirectOutputPageRequestConfig pConfig : config.getPageRequests()) {
            Class clazz = pConfig.getState();
            DirectOutputState state = DirectOutputStateFactory.getInstance().getDirectOutputState(clazz.getName());
            if (state == null) {
                CAT.error("***** Skipping page '" + pConfig.getPageName() + "' as it's corresponding class " + clazz.getName() +
                " couldn't be initialized by the DirectOutputStateFactory");
            } else {
                pagemap.put(pConfig.getPageName(), state);
            }
        }
        
    }

    /**
     * The <code>getDirectOutputState</code> method returns the DirectOutputState that is
     * associated with the <code>page</code> parameter
     *
     * @param page a <code>PageRequest</code> value
     * @return a <code>DirectOutputState</code> value
     */
    public DirectOutputState getDirectOutputState(PageRequest page) {
	return getDirectOutputState(page.getName());
    }

    public DirectOutputState getDirectOutputState(String  pagename) {
	return (DirectOutputState) pagemap.get(pagename);
    }

    public String toString() {
	String ret = "";
	for (Iterator i = pagemap.keySet().iterator(); i.hasNext(); ) {
	    if (ret.length() > 0) {
		ret += ", ";
	    }
	    String key = (String) i.next();
	    ret += key + " -> " + ((DirectOutputState) pagemap.get(key)).getClass().getName();
	}
	return ret;
    }
    
}
