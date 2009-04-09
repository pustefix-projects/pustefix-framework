/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package de.schlund.pfixcore.workflow.app;

import java.util.HashMap;

import de.schlund.pfixxml.XMLException;

/**
 * This factory is responsible for creating objects of type ResdocFinalizer.
 * It implements the singleton pattern.
 * <br/>
 *
 * Created: Fri Oct 12 22:02:19 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class ResdocFinalizerFactory {
    /** Store the already created ResdocFinalizer here, use classname as key*/
    private static HashMap<String, ResdocFinalizer> known = new HashMap<String, ResdocFinalizer>();
    private static ResdocFinalizerFactory instance = new ResdocFinalizerFactory();

    private ResdocFinalizerFactory() {
    }

    /**
     * Return the only instance of this singleton.
     * @return the instance
     */
    public static ResdocFinalizerFactory getInstance() {
        return instance;
    }

    /**
     * Get the ResdocFinalizer according to the passed classname. If the ResdocFinalizer
     * is already known it will be returned, else it will be created.
     * @param classname the classname of the ResdocFinalizer
     * @throws XMLException on errors when creating the ResdocFinalizer.
     */
    public ResdocFinalizer getResdocFinalizer(String classname) throws XMLException {
        synchronized (known) {
            ResdocFinalizer retval = (ResdocFinalizer) known.get(classname);
            if (retval == null) {
                try {
                    Class<?> theclass = Class.forName(classname);
                    retval = (ResdocFinalizer) theclass.newInstance();
                } catch (InstantiationException e) {
                    throw new XMLException("unable to instantiate class [" + classname + "]" + e.getMessage());
                } catch (IllegalAccessException e) {
                    throw new XMLException("unable access class [" + classname + "]" + e.getMessage());
                } catch (ClassNotFoundException e) {
                    throw new XMLException("unable to find class [" + classname + "]" + e.getMessage());
                } catch (ClassCastException e) {
                    throw new XMLException("class [" + classname + "] does not implement the interface ResdocFinalizer :" + e.getMessage());
                }
                known.put(classname, retval);
            }
            return retval;
        }
    }

} // ResdocFinalizerFactory
