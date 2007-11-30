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

import org.apache.log4j.Logger;

/**
 * The <code>DirectOutputStateFactory</code> class is a singleton that manages and creates
 * one instance of each requested DirectOutputState.
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 */
public class DirectOutputStateFactory {
    private static HashMap      knownstates = new HashMap();
    private static Logger       LOG         = Logger.getLogger(StateFactory.class);
    private static DirectOutputStateFactory instance    = new DirectOutputStateFactory();
    
    private DirectOutputStateFactory() {
    }

    /**
     * The singleton's <code>getInstance</code> method.
     *
     * @return The <code>DirectOutputStateFactory</code> instance.
     */
    public static DirectOutputStateFactory getInstance() {
        return instance;
    }
    
    /**
     *<code>getDirectOutputState</code> returns the matching DirectOutputState for <code>classname</code>.
     *
     * @param classname a <code>String</code> value
     * @return a <code>DirectOutputState</code> value
     */
    public DirectOutputState getDirectOutputState(String classname) {
        synchronized (knownstates) {
            DirectOutputState retval = (DirectOutputState) knownstates.get(classname); 
            if (retval == null) {
                try {
                    Class stateclass = Class.forName(classname);
                    retval = (DirectOutputState) stateclass.newInstance();
                } catch (InstantiationException e) {
                    LOG.error("unable to instantiate class [" + classname + "]", e);
                } catch (IllegalAccessException e) {
                    LOG.error("unable access class [" + classname + "]", e);
                } catch (ClassNotFoundException e) {
                    LOG.error("unable to find class [" + classname + "]", e);
                } catch (ClassCastException e) {
                    LOG.error("class [" + classname + "] does not implement the interface DirectOutputState", e);
                }
                knownstates.put(classname, retval);
            }
            return retval;
        }
    }
}
