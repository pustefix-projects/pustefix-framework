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
import de.schlund.util.*;
import java.util.*;
import org.apache.log4j.*;

/**
 * IHandlerContainerFactory.java
 *
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version
 *
 *
 */

public class IHandlerContainerFactory {
    private static Category                 LOG      = Category.getInstance(IHandlerContainerFactory.class.getName());
    private static HashMap                  known    = new HashMap();
    private static IHandlerContainerFactory instance = new IHandlerContainerFactory();
    
    public static IHandlerContainerFactory getInstance() {
        return instance;
    }

    public IHandlerContainer getIHandlerContainer(String classname, Context context) {
        synchronized (known) {
            String            pagename    = context.getCurrentPageRequest().getName();
            String            contextname = context.getName();
            String            key         = pagename + "@" + contextname;
            IHandlerContainer retval      = (IHandlerContainer) known.get(key); 
            if (retval == null) {
                try {
                    Class theclass = Class.forName(classname);
                    retval = (IHandlerContainer) theclass.newInstance();
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
                known.put(key, retval);
            }
            return retval;
        }
    }

}// IHandlerContainerFactory
