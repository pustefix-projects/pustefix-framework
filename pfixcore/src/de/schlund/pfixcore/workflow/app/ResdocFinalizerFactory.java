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

package de.schlund.pfixcore.workflow.app;
import java.util.HashMap;

import org.apache.log4j.Category;

/**
 * ResdocFinalizerFactory.java
 *
 *
 * Created: Fri Oct 12 22:02:19 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class ResdocFinalizerFactory {
    private static Category               LOG      = Category.getInstance(ResdocFinalizerFactory.class.getName());
    private static HashMap                known    = new HashMap();
    private static ResdocFinalizerFactory instance = new ResdocFinalizerFactory();

    public static ResdocFinalizerFactory getInstance() {
        return instance;
    }

    public ResdocFinalizer getResdocFinalizer(String classname) {
        synchronized (known) {
            ResdocFinalizer retval = (ResdocFinalizer) known.get(classname); 
            if (retval == null) {
                try {
                    Class theclass = Class.forName(classname);
                    retval = (ResdocFinalizer) theclass.newInstance();
                } catch (InstantiationException e) {
                    LOG.error("unable to instantiate class [" + classname + "]", e);
                } catch (IllegalAccessException e) {
                    LOG.error("unable access class [" + classname + "]", e);
                } catch (ClassNotFoundException e) {
                    LOG.error("unable to find class [" + classname + "]", e);
                } catch (ClassCastException e) {
                    LOG.error("class [" + classname + "] does not implement the interface ResdocFinalizer", e);
                }
                known.put(classname, retval);
            }
            return retval;
        }
    }

}// ResdocFinalizerFactory
