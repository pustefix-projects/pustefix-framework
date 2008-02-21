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

import org.apache.log4j.Logger;

import de.schlund.pfixxml.config.ContextConfig;
import de.schlund.pfixxml.config.PageRequestConfig;

public class PageMap {

    protected            HashMap<String, State> pagemap = new HashMap<String, State>();
    private final static Logger                 LOG = Logger.getLogger(PageMap.class);
    
    public PageMap(ContextConfig config) {

        for (PageRequestConfig pageConfig : config.getPageRequestConfigs()) {
            String page       = pageConfig.getPageName();
            Class<? extends State> stateClass = pageConfig.getState();
            State  state      = StateFactory.getInstance().getState(stateClass.getName());

            if (state == null) {
                LOG.error("***** Skipping page '" + page + "' as it's corresponding class " + stateClass.getName() +
                          "couldn't be initialized by the StateFactory");
            } else {
                pagemap.put(page, state);
            }
        }

    }

    public State getState(PageRequest page) {
        return getState(page.getName());
    }

    public State getState(String pagename) {
        return (State) pagemap.get(pagename);
    }

    @Override
    public String toString() {
        String ret = "";
        for (Iterator<String> i = pagemap.keySet().iterator(); i.hasNext(); ) {
            if (ret.length() > 0) {
                ret += ", ";
            }
            String key = i.next();
            ret += key + " -> " + pagemap.get(key).getClass().getName();
        }
        return ret;
    }
    
}
