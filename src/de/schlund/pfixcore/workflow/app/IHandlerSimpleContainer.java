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

import de.schlund.pfixcore.generator.*;
import de.schlund.pfixcore.util.*;
import de.schlund.pfixcore.workflow.*;
import de.schlund.pfixcore.workflow.app.*;
import de.schlund.pfixxml.*;
import java.util.*;
import org.apache.log4j.*;


/**
 * IHandlerSimpleContainer.java
 *
 *
 * Created: Thu Apr 18 13:49:36 2002
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version
 *
 *
 */

public class IHandlerSimpleContainer implements IHandlerContainer {
    private HashSet handlers;
    private HashSet activeset;
    private Context context;
    private long    loadindex = -1;
    
    public  static final String   PROP_CONTAINER = "ihandlercontainer";
    private static final String   PROP_POLICY    = PROP_CONTAINER + ".policy";
    private static final String   PROP_IGNORE    = PROP_CONTAINER + ".ignoreforactive";
    private static final String   PROP_INTERFACE = "interface";
    private static       Category CAT = Category.getInstance(IHandlerSimpleContainer.class);
    
    // implementation of de.schlund.pfixcore.workflow.app.IHandlerContainer interface

    /**
     *
     * @param param1 <description>
     * @exception java.lang.Exception <description>
     */
    public void initIHandlers(Context context) {
        this.context = context;
        updateIHandlers();
    }

    private void updateIHandlers() {
        long newload = context.getPropertyLoadIndex();
        if (newload > loadindex) {
            loadindex = newload;
            handlers  = new HashSet();
            activeset = new HashSet();
            Properties props      = context.getPropertiesForCurrentPageRequest();
            HashMap    interfaces = PropertiesUtils.selectProperties(props, PROP_INTERFACE);
            String     ignore     = props.getProperty(PROP_IGNORE);
            HashSet    skipprefix = new HashSet(); 
            
            if (ignore != null && !ignore.equals("")) {
                StringTokenizer tok = new StringTokenizer(ignore);
                while (tok.hasMoreElements()) {
                    skipprefix.add(tok.nextToken());
                }
            }
            
            if (!interfaces.isEmpty()) {
                for (Iterator i = interfaces.keySet().iterator(); i.hasNext(); ) {
                    String   numprefix = (String) i.next();
                    String   prefix    = numprefix; 
                    if (numprefix.indexOf(".") > 0) {
                        prefix = numprefix.substring(numprefix.indexOf(".") + 1); 
                    }
                    String   wrapperclass = (String) interfaces.get(numprefix);
                    IHandler handler      = IHandlerFactory.getInstance().getIHandlerForWrapperClass(wrapperclass);
                    handlers.add(handler);
                    if (!skipprefix.contains(prefix)) {
                        // CAT.debug("~~~~~~~~~~~~~~~~~ Adding " + prefix + " to activeset ~~~~~~~~~~~~~~~");
                        activeset.add(handler);
                    }
                }
            }
        }
    }
    
    /**
     * The principal accessibility of a page is deduced as follows:
     * If ANY of all the associated IHandlers returns false on a call to
     * prerequisitesMet(context), the page is NOT accessible.
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    
    public boolean isPageAccessible() throws Exception {
        updateIHandlers();
        if (handlers.isEmpty()) return true; // border case
        
        synchronized (handlers) {
            for (Iterator i = handlers.iterator(); i.hasNext(); ) {
                IHandler handler = (IHandler) i.next();
                if (!handler.prerequisitesMet(context)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * <code>areHandlerActive</code> asks all IHandlers that are contained on
     * the page and which are not listed in the property <code>ihandlercontainer.ignore</code>
     * (as a space separated list) in turn if they are active (<code>IHandler.isActive(Context context)</code>).
     * Depending on the policy (set by the (sub-) property <code>ihandlercontainer.policy</code>) which can be
     * <code>ALL</code>, <code>ANY</code> (default) or <code>NONE</code>,
     * this method requires either all (ALL), at least one (ANY) or none (NONE) of the
     * handlers to be active to return a value of <code>true</code>
     * If no wrapper/handler is defined, it returns true, too.
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public boolean areHandlerActive() throws Exception {
        updateIHandlers();
        if (activeset.isEmpty()) return true; // border case
        Properties props  = context.getPropertiesForCurrentPageRequest();
        String     policy = props.getProperty(PROP_POLICY);
        boolean    retval = true;
        
        if (policy == null) {
            policy = "ANY";
        }
        
        if (policy.equals("NONE")) {
            return true;
        }
        
        if (policy.equals("ALL")) {
            retval = true;
            synchronized (activeset) {
                for (Iterator i = activeset.iterator(); i.hasNext(); ) {
                    IHandler handler = (IHandler) i.next();
                    if (!handler.isActive(context)) {
                        retval = false;
                        break;
                    }
                }
            }
        } else if (policy.equals("ANY")) {
            retval = false;
            synchronized (activeset) {
                for (Iterator i = activeset.iterator(); i.hasNext(); ) {
                    IHandler handler = (IHandler) i.next();
                    if (handler.isActive(context)) {
                        retval = true;
                        break;
                    }
                }
            }
        } else {
            throw new RuntimeException("ERROR: property '" + PROP_POLICY + "' must be 'ALL', 'ANY'(default) or 'NONE'");
        }
        
        return (retval);
    }

    /**
     * The method <code>needsData</code> tells if any of the IHandlers this instance aggregates still needs Data.
     *
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public boolean needsData() throws Exception {
        updateIHandlers();
        if (handlers.isEmpty()) return true; // border case
        
        synchronized (handlers) {
            for (Iterator i = handlers.iterator(); i.hasNext(); ) {
                IHandler handler = (IHandler) i.next();
                if (handler.isActive(context) && handler.needsData(context)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    
    
}// IHandlerSimpleContainer
