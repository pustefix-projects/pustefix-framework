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

import de.schlund.pfixxml.PropertyObject;
import de.schlund.pfixxml.PropertyObjectManager;
import de.schlund.pfixxml.loader.AppLoader;
import de.schlund.pfixxml.loader.Reloader;
import de.schlund.pfixxml.loader.StateTransfer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import org.apache.log4j.Category;

public class PageMap implements   PropertyObject, Reloader {
    protected            HashMap  pagemap       = new HashMap();
    public  final static String   CLASSNAMEPROP = "classname";
    private final static Category CAT           = Category.getInstance(PageMap.class.getName());
    
    public void init(Properties properties) throws Exception {

        //Get PageRequestProperties object from PropertyObjectManager 
        PageRequestProperties preqprops = (PageRequestProperties) PropertyObjectManager.getInstance().
            getPropertyObject(properties,"de.schlund.pfixcore.workflow.PageRequestProperties");
        
        String[] pages = preqprops.getAllDefinedPageRequestNames();
        
        for (int i = 0; i < pages.length; i++) {
            String      page      = pages[i];
            Properties  props     = preqprops.getPropertiesForPageRequestName(page);
            String      classname = props.getProperty(CLASSNAMEPROP);
            State       state     = StateFactory.getInstance().getState(classname);

            if (state == null) {
                CAT.error("***** Skipping page '" + page + "' as it's corresponding class " + classname +
                          "couldn't be initialized by the StateFactory");
            } else {
                pagemap.put(page, state);
            }
        }

        AppLoader appLoader = AppLoader.getInstance();
        if (appLoader.isEnabled()) {
            appLoader.addReloader(this);
        }
    }

    public State getState(PageRequest page) {
        return getState(page.getName());
    }

    public State getState(String pagename) {
        return (State) pagemap.get(pagename);
    }

    public String toString() {
        String ret = "";
        for (Iterator i = pagemap.keySet().iterator(); i.hasNext(); ) {
            if (ret.length() > 0) {
                ret += ", ";
            }
            String key = (String) i.next();
            ret += key + " -> " + ((State) pagemap.get(key)).getClass().getName();
        }
        return ret;
    }
    
    public void reload() {
        HashMap  pageNew = new HashMap();
        Iterator i       = pagemap.keySet().iterator();
        while (i.hasNext()) {
            String page  = (String) i.next();
            State  stOld = (State) pagemap.get(page);
            State  stNew = (State) StateTransfer.getInstance().transfer(stOld);
            pageNew.put(page,stNew);
        }
        pagemap = pageNew;
    }
}
