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
import de.schlund.pfixxml.loader.*;

/**
 * StateFactory.java
 *
 *
 * Created: Sat Oct 13 00:07:21 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 */

public class StateFactory implements Reloader {
    private static HashMap      knownstates = new HashMap();
    private static Category     LOG         = Category.getInstance(StateFactory.class.getName());
    private static StateFactory instance    = new StateFactory();
    
    public static StateFactory getInstance() {
        return instance;
    }
    
    private StateFactory() {
        AppLoader appLoader = AppLoader.getInstance();
        if (appLoader.isEnabled()) appLoader.addReloader(this);
    }
    /**
     * <code>getState</code> returns the matching State for classname.
     *
     * @param classname a <code>String</code> value
     * @return a <code>State</code> value
     */
    public State getState(String classname) {
        synchronized (knownstates) {
            State retval = (State) knownstates.get(classname); 
            if (retval == null) {
                try {
                    AppLoader appLoader=AppLoader.getInstance();
                    if (appLoader.isEnabled()) {
                        retval = (State) appLoader.loadClass(classname).newInstance();
                    } else {
                        Class stateclass = Class.forName(classname);
                        retval = (State) stateclass.newInstance();
                    }
                } catch (InstantiationException e) {
                    LOG.error("unable to instantiate class [" + classname + "]", e);
                } catch (IllegalAccessException e) {
                    LOG.error("unable access class [" + classname + "]", e);
                } catch (ClassNotFoundException e) {
                    LOG.error("unable to find class [" + classname + "]", e);
                } catch (ClassCastException e) {
                    LOG.error("class [" + classname + "] does not implement the interface State", e);
                }
                knownstates.put(classname, retval);
            }
            return retval;
        }
    }
    
    public void reload() {
        HashMap  knownNew = new HashMap();
        Iterator it       = knownstates.keySet().iterator();
        while (it.hasNext()) {
            String str  = (String) it.next();
            State  sOld = (State) knownstates.get(str);
            State  sNew = (State) StateTransfer.getInstance().transfer(sOld);
            knownNew.put(str,sNew);
        }
        knownstates = knownNew;
    }
}
