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
import de.schlund.pfixcore.workflow.*;
import de.schlund.pfixxml.*;
import de.schlund.util.*;
import java.util.*;
import org.apache.log4j.*;

/**
 * IHandlerContainerManager.java
 *
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version
 *
 *
 */

public class IHandlerContainerManager implements PropertyObject {
    private static Category LOG               = Category.getInstance(IHandlerContainerManager.class.getName());
    private static String   DEF_HDL_CONTAINER = "de.schlund.pfixcore.workflow.app.IHandlerSimpleContainer";
    private        HashMap  known             = new HashMap();

    public void init(Properties properties) {
        // nothing :-)
    }
    
    public IHandlerContainer getIHandlerContainer(Context context) {
        String     classname = null;
        Properties props     = context.getPropertiesForCurrentPageRequest();

        synchronized (known) {
            PageRequest       page   = context.getCurrentPageRequest();
            IHandlerContainer retval = (IHandlerContainer) known.get(page); 
            if (retval == null) {
                LOG.debug("----- cachemiss for IHandlerContainer on page " + page.getName());
                try {
                    
                    classname = props.getProperty(IHandlerSimpleContainer.PROP_CONTAINER);
                    if (classname == null) {
                        classname = DEF_HDL_CONTAINER;
                    }
                    retval = (IHandlerContainer) Class.forName(classname).newInstance();
                    retval.initIHandlers(context);
                } catch (InstantiationException e) {
                    LOG.error("unable to instantiate class [" + classname + "]", e);
                } catch (IllegalAccessException e) {
                    LOG.error("unable access class [" + classname + "]", e);
                } catch (ClassNotFoundException e) {
                    LOG.error("unable to find class [" + classname + "]", e);
                } catch (ClassCastException e) {
                    LOG.error("class [" + classname + "] does not implement the interface IHandlerContainer", e);
                }
                known.put(page, retval);
            } else {
                LOG.debug("+++++ cachehit for IHandlerContainer on page " + page.getName());
            }
            return retval;
        }
    }

}// IHandlerContainerFactory
