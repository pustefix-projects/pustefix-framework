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

import de.schlund.pfixcore.util.FlyWeightChecker;
import java.util.*;
import org.apache.log4j.*;

/**
 * StateFactory.java
 *
 *
 * Created: Sat Oct 13 00:07:21 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 */

public class StateFactory {
    private static HashMap      knownstates = new HashMap();
    private static Category     LOG         = Category.getInstance(StateFactory.class.getName());
    private static StateFactory instance    = new StateFactory();
    
    public static StateFactory getInstance() {
        return instance;
    }
    
    private StateFactory() {
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
                    Class stateclass = Class.forName(classname);
                    retval = (State) stateclass.newInstance();
                    if (!FlyWeightChecker.check(retval)) {
                        throw new IllegalStateException("You MUST NOT use non-static/non-final fields in flyweight class " + classname);
                    }
                    knownstates.put(classname, retval);
                } catch (InstantiationException e) {
                    throw new IllegalStateException("unable to instantiate class [" + classname + "] :" + e.getMessage());
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("unable access class [" + classname + "] :" + e.getMessage());
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException("unable to find class [" + classname + "] :" + e.getMessage());
                } catch (ClassCastException e) {
                    throw new IllegalStateException("class [" + classname + "] does not implement the interface IHandler. :" + e.getMessage());
                }
            }
            return retval;
        }
    }
    
}
