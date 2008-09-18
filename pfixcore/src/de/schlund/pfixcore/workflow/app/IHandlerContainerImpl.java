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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;

import org.pustefixframework.config.contextxmlservice.IWrapperConfig;
import org.pustefixframework.config.contextxmlservice.StateConfig;

import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.ResultDocument;
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

public class IHandlerContainerImpl implements IHandlerContainer {
    /** Store all created handlers here*/
    private HashSet<IHandler> handlers;
    /** Store all handlers here which do not have a 'checkactive' attribute set to 'false' */
    private HashSet<IHandler> activeset;
    
    private String policy;
    
    private StateConfig stateConfig;
    
    /**
     * Initialize the IHandlers. Get the handlers from {@link IHandlerFactory}
     * and store them.
     * @param props the properties containing the interface names
     * @see de.schlund.pfixcore.workflow.app.IHandlerContainer#initIHandlers(Properties)
     */
    public void initIHandlers(StateConfig config) {
        handlers  = new HashSet<IHandler>();
        activeset = new HashSet<IHandler>();
        stateConfig = config;
        
        if (config.getIWrapperPolicy() == StateConfig.Policy.ALL) {
            this.policy = "ALL";
        } else if (config.getIWrapperPolicy() == StateConfig.Policy.ANY) {
            this.policy = "ANY";
        } else {
            this.policy = "NONE";
        }
        
        for (IWrapperConfig iConfig : config.getIWrappers().values()) {
            IHandler handler = iConfig.getHandler();
            handlers.add(handler);
            if (iConfig.doCheckActive()) {
                activeset.add(handler);
            }
        }
        
    }
    
    public boolean isAccessible(Context context) throws Exception {
        return (isPageAccessible(context) && areHandlerActive(context));
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
    private boolean isPageAccessible(Context context) throws Exception {
        if (handlers.isEmpty()) return true; // border case
        
        for (Iterator<IHandler> iter = handlers.iterator(); iter.hasNext(); ) {
            IHandler handler = iter.next();
            PerfEvent pe = new PerfEvent(PerfEventType.IHANDLER_PREREQUISITESMET, handler.getClass().getName());
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
    private boolean areHandlerActive(Context context) throws Exception  {
        
        if (activeset.isEmpty() || policy.equals("NONE")) {
            return true; // border case
        }
        
        boolean retval = true;

        if (policy.equals("ALL")) {
            retval = true;
            for (Iterator<IHandler> iter = activeset.iterator(); iter.hasNext(); ) {
                IHandler handler = iter.next();
                
                boolean test = doIsActive(handler, context);
                if (!test) {
                    retval = false;
                    break;
                }
            }
        } else if (policy.equals("ANY")) {
            retval = false;
            for (Iterator<IHandler> iter = activeset.iterator(); iter.hasNext(); ) {
                IHandler handler = iter.next();
                
                boolean test = doIsActive(handler, context);
                if (test) {
                    retval = true;
                    break;
                }
               
            }
        } else {
            throw new RuntimeException("ERROR: property policy must be 'ALL', 'ANY'(default) or 'NONE'");
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
        PerfEvent pe = new PerfEvent(PerfEventType.IHANDLER_ISACTIVE, handler.getClass().getName());
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
        PerfEvent pe = new PerfEvent(PerfEventType.IHANDLER_NEEDSDATA, handler.getClass().getName());
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
        if (handlers.isEmpty()) return false; // border case
        
        for (Iterator<IHandler> iter = handlers.iterator(); iter.hasNext(); ) {
            IHandler handler = iter.next();
            if (handler.isActive(context) && doNeedsData(handler, context)) {
                return true;
            }
        }
        return false;
    }
    
    
    public IWrapperContainer createIWrapperContainerInstance(Context context, PfixServletRequest preq, ResultDocument resdoc) throws Exception {
        IWrapperContainer container = new IWrapperContainerImpl();
        // TODO: This initialization could be made better. We should retrieve all static information
        // from the config file here ONCE, and give it to the IWrapperContainer instance already aggregated/sorted instead of letting 
        // the IWrapperContainer do the job all over again each time an instance is created. The only thing that should have to 
        // be calculated from start in the IWrapperContainer instance are all things depending on the actual request data.
        container.init(context, preq, resdoc, stateConfig);
        return container;
    }

    
}// IHandlerContainerImpl
