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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.generator.IWrapperParam;
import de.schlund.pfixcore.generator.RequestData;
import de.schlund.pfixcore.generator.StatusCodeInfo;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.ResultDocument;
import de.schlund.pfixxml.XMLException;
import de.schlund.pfixxml.config.IWrapperConfig;
import de.schlund.pfixxml.config.PageRequestConfig;
import de.schlund.pfixxml.perflogging.PerfEvent;
import de.schlund.pfixxml.perflogging.PerfEventType;
import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;

/**
 * Default implementation of the <code>IWrapperContainer</code> interface. 
 * <br/>
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 */

public class IWrapperContainerImpl implements IWrapperContainer {
    private TreeMap<String, IWrapper> wrappers = new TreeMap<String, IWrapper>();

    private Set<IWrapper> allwrappers          = new TreeSet<IWrapper>();
    private Set<IWrapper> allsubmit            = new TreeSet<IWrapper>();
    private Set<IWrapper> allretrieve          = new TreeSet<IWrapper>();
    
    private Context            context   = null;
    private ResultDocument     resdoc    = null;
    private RequestData        reqdata   = null;
    private boolean            is_loaded = false;

    protected Logger LOG = Logger.getLogger(this.getClass());
    
    private static final String SELECT_WRAPPER        = "SELWRP";  
    private static final String RETRIEVE_ONLY_WRAPPER = "RETONLY";  
    private static final String SUBMIT_ONLY_WRAPPER   = "SUBONLY";      
    private static final String WRAPPER_LOGDIR        = "interfacelogging";

    /**
     * This method must be called right after an instance of this class is created.
     *
     * @param context a <code>Context</code> value. Not null.
     * @param preq a <code>PfixServletRequest</code> value. Not null.
     * @param resdoc a <code>ResultDocument</code> value. Not null.
     * @exception Exception if an error occurs
     * @see de.schlund.pfixcore.workflow.app.IWrapperContainer#initIWrappers(Context, PfixServletRequest, ResultDocument) 
     */
    public synchronized void init(Context context, PfixServletRequest preq, ResultDocument resdoc) throws Exception  {
        if (context == null)
            throw new IllegalArgumentException("A 'null' value for the Context argument is not acceptable here.");
        if (preq == null)
            throw new IllegalArgumentException("A 'null' value for the PfixServletRequest argument is not acceptable here.");
        if (resdoc == null)
            throw new IllegalArgumentException("A 'null' value for the ResultDocument argument is not acceptable here.");
        
        this.context = context;
        this.resdoc  = resdoc;
        this.reqdata = new RequestDataImpl(context,preq);
        
        createIWrapperGroups(); 
    }
    
    /**
     * Use this method at any time to check if any error has happened in
     * any of the "currently active" IWrappers of this Container.
     * Depending on the use of grouping, or restricting to certain
     * IWrappers the term "currently active" means something between
     * the two extremes of "all handled IWrappers" to
     * "just the one I explicitely talk to".
     *
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     * @see de.schlund.pfixcore.workflow.app.IWrapperContainer#errorHappened() 
     */
    public boolean errorHappened() throws Exception {
        if (allwrappers.isEmpty()) return false; // border case
        if (!is_loaded) throw new XMLException("You must first call handleSubmittedData() here!");

        for (Iterator<IWrapper> iter = allsubmit.iterator(); iter.hasNext();) {
            IWrapper wrapper = iter.next();
            if (wrapper.errorHappened()) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method takes care of putting all error messages into the ResultDocument.
     * This method needs the instance to be initialized with a non-null resdoc param (see
     * {@link initIWrappers initIWrappers}).
     * @exception Exception if an error occurs
     * @see de.schlund.pfixcore.workflow.app.IWrapperContainer#addErrorCodes()
     */
    public void addErrorCodes() throws Exception {
        for (Iterator<IWrapper> iter = allsubmit.iterator(); iter.hasNext();) {
            IWrapper        wrapper = iter.next();
            String          prefix  = wrapper.gimmePrefix();
            IWrapperParam[] errors  = wrapper.gimmeAllParamsWithErrors();
            if (errors != null) {
                wrapper.tryErrorLogging();
                for (int j = 0; j < errors.length; j++) {
                    IWrapperParam    param      = errors[j];
                    StatusCodeInfo[] scodeinfos = param.getStatusCodeInfos();
                    String           name       = prefix + "." + param.getName();
                    if (scodeinfos != null) {
                        for (int k = 0; k < scodeinfos.length; k++) {
                            StatusCodeInfo sci  = scodeinfos[k];
                            resdoc.addStatusCode(context.getProperties(), sci.getStatusCode(), sci.getArgs(), sci.getLevel(), name);
                        }
                    }
                }
            }
        }
    }
    
    
    /**
     * This method puts all the string representation of all submitted
     * form values back into the result tree.
     * This method needs the instance to be initialized with a non-null resdoc param (see
     * {@link initIWrappers initIWrappers}).
     *
     * @exception Exception if an error occurs
     * @see de.schlund.pfixcore.workflow.app.IWrapperContainer#addStringValues()
     */
    public void addStringValues() throws Exception {
        for (Iterator<IWrapper> iter = allwrappers.iterator(); iter.hasNext();) {
            IWrapper        wrapper  = iter.next();
            String          prfx     = wrapper.gimmePrefix();
            IWrapperParam[] pinfoall = wrapper.gimmeAllParams();
            for (int j = 0; j < pinfoall.length; j++) {
                IWrapperParam pinfo  = pinfoall[j];
                String            name   = pinfo.getName();
                String[]          strval = pinfo.getStringValue();
                if (strval != null) {
                    for (int k = 0; k < strval.length; k++) {
                        resdoc.addValue(prfx + "." + name, strval[k]);
                    }
                }
            }
        }
    }

    /**
     * <code>addIWrapperStatus</code> inserts the status of all IWrappers into the result tree.
     * @exception Exception if an error occurs
     * @see de.schlund.pfixcore.workflow.app.IWrapperContainer#addIWrapperStatus()
     */
    public void addIWrapperStatus() throws Exception {
        Element status = resdoc.createNode("wrapperstatus");
        for (Iterator<IWrapper> iter = allwrappers.iterator(); iter.hasNext();) {
            IWrapper wrapper = iter.next();
            IHandler handler = wrapper.gimmeIHandler();
            Element  wrap    = resdoc.createSubNode(status, "wrapper");
            wrap.setAttribute("active", "" + handler.isActive(context));
            wrap.setAttribute("name", wrapper.getClass().getName());
            wrap.setAttribute("prefix", wrapper.gimmePrefix());
        }
    }


    /**
     * <code>handleSubmittedData</code> will call all or a part of the defined IWrappers
     * (depending on restricting the IWrappers) to get
     * the IHandler that these IWrappers are bound to. The IHandler are called in turn to handle the submitted data.
     * This works by calling ihandler.handleSubmittedData(Context context, IWrapper wrapper).
     * See {@link IHandler}.
     *
     * @exception Exception if an error occurs
     * @see de.schlund.pfixcore.workflow.app.IWrapperContainer#handleSubmittedData() 
     */
    public void handleSubmittedData() throws Exception {
        for (Iterator<IWrapper> iter = allwrappers.iterator(); iter.hasNext();) {
            IWrapper wrapper = iter.next();
            IHandler handler = wrapper.gimmeIHandler();
            if (handler.isActive(context)) {
                wrapper.load(reqdata);
                if (allsubmit.contains(wrapper)) {
                    wrapper.tryParamLogging();
                    if (!wrapper.errorHappened()) {
                        PerfEvent pe = new PerfEvent(PerfEventType.IHANDLER_HANDLESUBMITTEDDATA, handler.getClass().getName());
                        pe.start();
                        handler.handleSubmittedData(context, wrapper);
                        pe.save();
                    }
                }
            }
        }
        is_loaded = true;
    }

    /**
     * <code>retrieveCurrentStatus</code> will call all or only a subset of the defined IWrappers
     * (depending on restricting the IWrappers and the RETRONLY and SUBMTONLY commands  to get the 
     * "active" IHandlers, which are called in turn via ihandler.retrieveCurrentStatus(Context context, IWrapper wrapper).
     *
     * @exception Exception if an error occurs
     * @see de.schlund.pfixcore.workflow.app.IWrapperContainer#retrieveCurrentStatus() 
     */
    public void retrieveCurrentStatus() throws Exception {
        for (Iterator<IWrapper> iter = allretrieve.iterator(); iter.hasNext();) {
            IWrapper wrapper = iter.next();
            IHandler handler = wrapper.gimmeIHandler();
            if (handler.isActive(context)) {
                handler.retrieveCurrentStatus(context, wrapper);
            }
        }
    }

    //
    //   PRIVATE
    // 

    private void createIWrapperGroups() throws Exception  {
        PageRequestConfig config = context.getConfigForCurrentPageRequest();
        Collection<? extends IWrapperConfig> confwrappers = config.getIWrappers().values();

        if (confwrappers.size() == 0) {
            LOG.debug("*** Found no wrappers for this page (page=" + context.getCurrentPageRequest().getName() + ")");
        } else {
            // Initialize all wrappers
            int order = 0;
            for (IWrapperConfig iConfig : confwrappers) {
                String prefix = iConfig.getPrefix();
                if (wrappers.get(prefix) != null) {
                    throw new XMLException("FATAL: you have already defined a wrapper with prefix " +
                                           prefix + " on page " + context.getCurrentPageRequest().getName());
                }
                
                String iface  = iConfig.getWrapperClass().getName();
                
                Class<?> thewrapper = null;
                IWrapper wrapper = null;
                try {
                    thewrapper = Class.forName(iface);
                    wrapper    = (IWrapper) thewrapper.newInstance();
                } catch (ClassNotFoundException e) {
                    throw new XMLException("unable to find class [" + iface + "] :" + e.getMessage());
                } catch (InstantiationException e) {
                    throw new XMLException("unable to instantiate class [" + iface + "] :" + e.getMessage());
                } catch (IllegalAccessException e) {
                    throw new XMLException("unable to acces class [" + iface +"] :" + e.getMessage());
                } catch (ClassCastException e) {
                    throw new XMLException("class [" + iface + "] does not implement the interface IWrapper :" + e.getMessage());
                }
                
                wrapper.defineOrder(order++);
                
                wrappers.put(prefix, wrapper);
                wrapper.init(prefix);
                
                allwrappers.add(wrapper);
                                
                String  logdir  = context.getProperties().getProperty(WRAPPER_LOGDIR);
                boolean dolog   = iConfig.getLogging();
                if (dolog && logdir != null && !logdir.equals("")) {
                    FileResource dir = ResourceUtil.getFileResourceFromDocroot(logdir);
                    if (dir.isDirectory() && dir.canWrite()) {
                        wrapper.initLogging(dir, context.getCurrentPageRequest().getName(), context.getVisitId());
                    }
                }
            }

            // TODO: This mechanism MUST be at least augmented with a "action based" selection mechanism.
            // In the future we will only allow to select from a  predefined set of wrapper sets.
            // The configuration will group wrappers to actions, and the request is only allowed to give one of 
            // the defined action names, but it will be no longer possible to create any desired grouping from the 
            // outside.
            
            List<IWrapper> selectedwrappers = new ArrayList<IWrapper>();
            List<IWrapper> only_retrieve    = new ArrayList<IWrapper>();
            List<IWrapper> only_submit      = new ArrayList<IWrapper>();

            String[] selwrappers = reqdata.getCommands(SELECT_WRAPPER);
            if (selwrappers != null) {
                for (int i = 0; i < selwrappers.length; i++) {
                    String   prefix  = selwrappers[i];
                    LOG.debug("  >> Restricted to Wrapper: " + prefix);
                    IWrapper selwrap = (IWrapper) wrappers.get(prefix);
                    if (selwrap == null) {
                        LOG.warn(" *** No wrapper found for prefix " + prefix + "! ignoring");
                        continue;
                    }
                    selectedwrappers.add(selwrap);
                }
            }

            String[] no_retrwrappers = reqdata.getCommands(SUBMIT_ONLY_WRAPPER);
            if (no_retrwrappers != null) {
                for (int i = 0; i < no_retrwrappers.length; i++) {
                    String   prefix  = no_retrwrappers[i];
                    IWrapper no_retrwrap = (IWrapper) wrappers.get(prefix);
                    LOG.debug("  >> Don't call retrieveCurrentStatus() for Wrapper: " + prefix);
                    if (no_retrwrap == null) {
                        LOG.warn(" *** No wrapper found for prefix " + prefix + "! ignoring");
                        continue;
                    }
                    only_submit.add(no_retrwrap);
                }
            }
            
            String[] retrwrappers = reqdata.getCommands(RETRIEVE_ONLY_WRAPPER);
            if (retrwrappers != null) {
                for (int i = 0; i < retrwrappers.length; i++) {
                    String   prefix  = retrwrappers[i];
                    IWrapper retrwrap = (IWrapper) wrappers.get(prefix);
                    LOG.debug("  >> Always call retrieveCurrentStatus() for Wrapper: " + prefix);
                    if (retrwrap == null) {
                        LOG.warn(" *** No wrapper found for prefix " + prefix + "! ignoring");
                        continue;
                    }
                    only_retrieve.add(retrwrap);
                }
            }
            
            allsubmit.addAll(selectedwrappers);
            allsubmit.addAll(only_submit);
            // If we don't have any "submit&retrieve" or "submit only" wrappers, use all wrappers
            if (allsubmit.isEmpty()) {
                allsubmit.addAll(allwrappers);
            }
            
            allretrieve.addAll(selectedwrappers);
            allretrieve.addAll(only_retrieve);
            if (allretrieve.isEmpty()) {
                allretrieve.addAll(allwrappers);
            }

        }
    }

    
}// IWrapperSimpleContainer
