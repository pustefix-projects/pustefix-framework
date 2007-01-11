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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.log4j.Category;

import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IHandlerFactory;
import de.schlund.pfixcore.util.PropertiesUtils;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.config.IWrapperConfig;
import de.schlund.pfixxml.config.PageRequestConfig;
import de.schlund.pfixxml.loader.*;
import de.schlund.pfixxml.perflogging.PerfEvent;
import de.schlund.pfixxml.perflogging.PerfEventType;

/**
 * This class is a default implementation of the <code>IHandlerContainer</code> interface.
 * <br/>
 *
 * Created: Thu Apr 18 13:49:36 2002
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class IHandlerSimpleContainer implements IHandlerContainer, Reloader {
    /** Store all created handlers here*/
    private HashSet    handlers;
    /** Store all handlers here which do not have a 'ihandlercontainer.ignore' property*/
    private HashSet    activeset;
    
    private String policy;
    
    public  static final String   PROP_CONTAINER = "ihandlercontainer";
    private static final String   PROP_POLICY    = PROP_CONTAINER + ".policy";
    private static       Category CAT = Category.getInstance(IHandlerSimpleContainer.class);
    
    // implementation of de.schlund.pfixcore.workflow.app.IHandlerContainer interface

    /**
     * Initialize the IHandlers. Get the handlers from {@link IHandlerFactory}
     * and store them.
     * @param props the properties containing the interface names
     * @see de.schlund.pfixcore.workflow.app.IHandlerContainer#initIHandlers(Properties)
     */
    public void initIHandlers(PageRequestConfig config) {
        handlers  = new HashSet();
        activeset = new HashSet();
        
        if (config.getIWrapperPolicy() == PageRequestConfig.Policy.ALL) {
            this.policy = "ALL";
        } else if (config.getIWrapperPolicy() == PageRequestConfig.Policy.ANY) {
            this.policy = "ANY";
        } else {
            this.policy = "NONE";
        }
        
        IWrapperConfig[] interfaces = config.getIWrappers();
        for (int i = 0; i < interfaces.length; i++) {
            IWrapperConfig iConfig = interfaces[i];
            String wrapperclass = iConfig.getWrapperClass().getName();
            IHandler handler = IHandlerFactory.getInstance().getIHandlerForWrapperClass(wrapperclass);
            handlers.add(handler);
            if (!iConfig.isActiveIgnore()) {
                activeset.add(handler);
            }
        }
        
        AppLoader appLoader = AppLoader.getInstance();
        if (appLoader.isEnabled()) {
            appLoader.addReloader(this);
        }
    }
    
    /**
     * The principal accessibility of a page is deduced as follows:
     * If ANY of all the associated IHandlers returns false on a call to
     * prerequisitesMet(context), the page is NOT accessible.
     * @param context the current context
     * @return true if page is accesible, else false
     * @exception Exception if an error occurs
     * @see de.schlund.pfixcore.workflow.app.IHandlerContainer#isPageAccessible(Context)
     */
    public boolean isPageAccessible(Context context) throws Exception {
        if (handlers.isEmpty()) return true; // border case
        
        for (Iterator iter = handlers.iterator(); iter.hasNext(); ) {
            IHandler handler = (IHandler) iter.next();
            PerfEvent pe = new PerfEvent(PerfEventType.IHANDLER_PREREQUISITES_MET.name(), handler.getClass().getName());
            pe.start();
            boolean  test = handler.prerequisitesMet(context);
            pe.save();
            
            if (!test) {
                return false;
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
     * @param context the current context
     * @return true if handlers are active, else false
     * @exception Exception if an error occurs
     * @see de.schlund.pfixcore.workflow.app.IHandlerContainer#areHandlerActive(Context)
     */
    public boolean areHandlerActive(Context context) throws Exception  {
        if (activeset.isEmpty() || policy.equals("NONE")) {
            return true; // border case
        }

        boolean retval = true;

        if (policy.equals("ALL")) {
            retval = true;
            for (Iterator iter = activeset.iterator(); iter.hasNext(); ) {
                IHandler handler = (IHandler) iter.next();
                
                boolean test = doIsActive(handler, context);
                if (!test) {
                    retval = false;
                    break;
                }
            }
        } else if (policy.equals("ANY")) {
            retval = false;
            for (Iterator iter = activeset.iterator(); iter.hasNext(); ) {
                IHandler handler = (IHandler) iter.next();
                
                boolean test = doIsActive(handler, context);
                if (test) {
                    retval = true;
                    break;
                }
            }
        } else {
            throw new RuntimeException("ERROR: property '" + PROP_POLICY + "' must be 'ALL', 'ANY'(default) or 'NONE'");
        }
        
        return (retval);
    }
    
    /**
     * Call the <see>isActive</see>-Method of the passed <see>IHandler</see>
     * with the given context.
     * 
     * @param handler
     * @param ctx
     * @return
     * @throws Exception
     */
    private boolean doIsActive(IHandler handler, Context ctx) throws Exception{
        PerfEvent pe = new PerfEvent(PerfEventType.IHANDLER_IS_ACTIVE.name(), handler.getClass().getName());
        pe.start();
        boolean  test = handler.isActive(ctx);
        pe.save();
        return test;
    }
    
    /**
     * Call the <see>needsData/see>-Method of the passed <see>IHandler</see>
     * with the given context.
     * 
     * @param handler
     * @param ctx
     * @return
     * @throws Exception
     */
    private boolean doNeedsData(IHandler handler, Context ctx) throws Exception{
        PerfEvent pe = new PerfEvent(PerfEventType.IHANDLER_NEEDS_DATA.name(), handler.getClass().getName());
        pe.start();
        boolean  test = handler.needsData(ctx);
        pe.save();
        return test;
    }
    
    /**
     * The method <code>needsData</code> tells if any of the IHandlers this instance 
     * aggregates still needs data.
     * @param context the current context
     * @return true if data is needed, else false
     * @exception Exception if an error occurs
     * @see de.schlund.pfixcore.workflow.app.IHandlerContainer#needsData(Context)
     */
    public boolean needsData(Context context) throws Exception  {
        if (handlers.isEmpty()) return true; // border case
        
        for (Iterator iter = handlers.iterator(); iter.hasNext(); ) {
            IHandler handler = (IHandler) iter.next();
            if (handler.isActive(context)) {
                
                boolean test = doNeedsData(handler, context);
                if (test) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public void reload() {
        HashSet  handlersNew = new HashSet();
        Iterator iter        = handlers.iterator();
        while (iter.hasNext()) {
            IHandler ihOld = (IHandler) iter.next();
            IHandler ihNew = (IHandler) StateTransfer.getInstance().transfer(ihOld);
            handlersNew.add(ihNew);
        }
        handlers          = handlersNew;
        HashSet activeNew = new HashSet();
        iter              = activeset.iterator();
        while (iter.hasNext()) {
            IHandler ihOld = (IHandler) iter.next();
            IHandler ihNew = (IHandler) StateTransfer.getInstance().transfer(ihOld);
            activeNew.add(ihNew);
        }
        activeset = activeNew;
    }
    
}// IHandlerSimpleContainer
