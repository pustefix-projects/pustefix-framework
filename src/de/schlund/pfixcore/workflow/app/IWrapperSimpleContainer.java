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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.generator.IWrapperParam;
import de.schlund.pfixcore.generator.RequestData;
import de.schlund.pfixcore.generator.StatusCodeInfo;
import de.schlund.pfixcore.util.PropertiesUtils;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.RequestParam;
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
 * Created: Fri Aug 17 14:58:49 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class IWrapperSimpleContainer implements IWrapperContainer {
    private   TreeMap<String, IWrapper> wrappers    = new TreeMap<String, IWrapper>();

    // depends on request
    private   ArrayList<IWrapperGroup> activegroups = new ArrayList<IWrapperGroup>();
    private   IWrapperGroup      currentgroup       = null;
    private   IWrapperGroup      selectedwrappers   = null;

    private   IWrapperGroup      contwrappers       = null;
    private   IWrapperGroup      always_retrieve    = null;

    private   Context            context        = null;
    private   ResultDocument     resdoc         = null;
    private   PfixServletRequest preq           = null;
    private   RequestData        reqdata        = null;
    private   boolean            is_splitted    = false;
    private   boolean            is_loaded      = false;
    protected Logger             LOG            = Logger.getLogger(this.getClass());
    
    public  static final String PROP_CONTAINER       = "iwrappercontainer";
    //private static final String PROP_INTERFACE       = "interface";
    public  static final String PROP_RESTRICED       = "restrictedcontinue";
    public  static final String PROP_ALWAYS_RETRIEVE = "alwaysretrieve";
    private static final String  GROUP_STATUS_PARAM = "__groupdisplay";
    private static final String  GROUP_STATUS       = "__GROUPDISPLAY__STATUS__";
    private static final Boolean GROUP_ON           = new Boolean(true);
    private static final Boolean GROUP_OFF          = new Boolean(false);
    private static final String  GROUP_ON_PARAM     = "on";
    private static final String  GROUP_OFF_PARAM    = "off";
    private static final String  GROUP_FORCE_PROP   = "forcegroupdisplay";
    private static final String  GROUP_PROP         = "group";
    private static final String  GROUP_ANONYMOUS    = "__AnonymousGroup__";

    private              String  GROUP_CURR;
    private static final String  GROUP_NEXT         = "NEXT";  
    private static final String  GROUP_PREV         = "PREV";  
    private static final String  SELECT_GROUP       = "SELGRP";  
    private static final String  SELECT_WRAPPER     = "SELWRP";  
    private static final String  WRAPPER_LOGDIR     = "interfacelogging";
    // private static final String  WRAPPER_LOGLIST    = "loginterfaces";

    /**
     * This method must be called right after an instance of this class is created.
     *
     * @param context a <code>Context</code> value. Not null.
     * @param preq a <code>PfixServletRequest</code> value. Not null.
     * @param resdoc a <code>ResultDocument</code> value. Not null.
     * @exception Exception if an error occurs
     * @see de.schlund.pfixcore.workflow.app.IWrapperContainer#initIWrappers(Context, PfixServletRequest, ResultDocument) 
     */
    public synchronized void initIWrappers(Context context, PfixServletRequest preq,
                                           ResultDocument resdoc) throws Exception  {
        if (context == null)
            throw new IllegalArgumentException("A 'null' value for the Context argument is not acceptable here.");
        if (preq == null)
            throw new IllegalArgumentException("A 'null' value for the PfixServletRequest argument is not acceptable here.");
        if (resdoc == null)
            throw new IllegalArgumentException("A 'null' value for the ResultDocument argument is not acceptable here.");
        
        this.context = context;
        this.preq    = preq;
        this.resdoc  = resdoc;

        GROUP_CURR  = "__currentindex[" + context.getCurrentPageRequest().getName() + "]";
 
        readIWrappersConfigFromProperties(); 
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
        if (wrappers.isEmpty()) return false; // border case

        if (!is_loaded) throw new XMLException("You must first call handleSubmittedData() here!");
        if (!is_splitted) splitIWrappers();
        IWrapper[] cwrappers = selectedwrappers.getIWrappers();
        for (int i = 0; i < cwrappers.length; i++) {
            IWrapper wrapper = cwrappers[i];
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
        // COMMENT: is it OK to NOT enforce loading here? Needed to be able to set errors in retrieveCurrentStatus, too.
        // if (!is_loaded) throw new XMLException("You must first call handleSubmittedData() here!");
        if (!is_splitted) splitIWrappers();
        IWrapper[] cwrappers = selectedwrappers.getIWrappers();
        for (int i = 0; i < cwrappers.length; i++) {
            IWrapper        wrapper = cwrappers[i];
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
        if (!is_splitted) splitIWrappers();
        IWrapper[] cwrappers = currentgroup.getIWrappers();
        for (int i = 0; i < cwrappers.length; i++) {
            IWrapper            wrapper  = cwrappers[i];
            String              prfx     = wrapper.gimmePrefix();
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
     * Use this method to query if the IWrapperContainer wants to continue with submitting data (thereby staying on the page),
     * or if it assumes this whole page to be completed (and give control to the context to continue with the pageflow).
     *
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     * @see de.schlund.pfixcore.workflow.app.IWrapperContainer#continueSubmit() 
     */
    public boolean stayAfterSubmit() throws Exception {
        if (wrappers.isEmpty()) return false; // border case

        if (!is_splitted) splitIWrappers();
        
        if (selectedwrappers != currentgroup) {
            if (contwrappers != null && contwrappers.containsAll(selectedwrappers)) {
                LOG.debug("*** Allow continue because all selected wrappers are members of the restriced_continue group!");
                return false;
            }
            return true;
        }
        
        Integer index = checkForNextIndexInRequest();
        if (index == null) {
            return false;
        } else {
            setCurrentIWrapperGroupByIndex(index.intValue());
            selectedwrappers = currentgroup;
            return true;
        }
    }

    /**
     * Returns the {@link ResultDocument ResultDocument} that's associated with this IWrapperContainer.
     * @return a <code>ResultDocument</code> value
     * @see de.schlund.pfixcore.workflow.app.IWrapperContainer#getAssociatedResultDocument()
     */
    public ResultDocument getAssociatedResultDocument() {
        return resdoc;
    }

   
    /**
     * Returns the {@link PfixServletRequest} that's associated with this IWrapperContainer.
     *
     * @return a <code>HttpServletRequest</code> value
     * @see de.schlund.pfixcore.workflow.app.IWrapperContainer#getAssociatedPfixServletRequest()
     */
    public PfixServletRequest getAssociatedPfixServletRequest() {
        return preq;
    }

    /**
     * Returns the {@link Context} that's associated with this IWrapperContainer.
     *
     * @return a <code>Context</code> value
     * @see de.schlund.pfixcore.workflow.app.IWrapperContainer#getAssociatedContext() 
     */
    public Context getAssociatedContext() {
        return context;
    }

 
    /**
     * <code>addIWrapperStatus</code> inserts the status of all IWrappers into the result tree.
     * You can only call this method if this instance was initialized with a non null ResultDocument.
     * The IWrappers will be grouped according to the group they belong to. If there's no groups defined,
     * one big anonymous group will be created that contains all the defined IWrappers
     *
     * @exception Exception if an error occurs
     * @see de.schlund.pfixcore.workflow.app.IWrapperContainer#addIWrapperStatus()
     */
    public void addIWrapperStatus() throws Exception {
        if (!is_splitted) splitIWrappers();
        Element grpstati = resdoc.createNode("iwrappergroups");
        grpstati.setAttribute("currentindex", "" + getCurrentIWrapperGroupIndex());
        grpstati.setAttribute("lastindex", "" + (activegroups.size() - 1));
        grpstati.setAttribute("groupdisplay", "" + checkGroupDisplayStatus());
        
        for (int i = 0; i < activegroups.size(); i++ ) {
            IWrapperGroup group  = (IWrapperGroup) activegroups.get(i);
            String        name   = group.getName();
            String        curr   = "false";
            if (currentgroup == group) {
                curr = "true";
            }
            Element status = resdoc.createSubNode(grpstati, "group");
            status.setAttribute("index", "" + i);
            status.setAttribute("name", name);
            status.setAttribute("current", curr);

            IWrapper[] tmpwrappers = group.getIWrappers();
            for (int j = 0; j < tmpwrappers.length; j++) {
                IWrapper wrapper = tmpwrappers[j];
                IHandler handler = wrapper.gimmeIHandler();
                Element  wrap    = resdoc.createSubNode(status, "interface");
                wrap.setAttribute("active", "" + handler.isActive(context));
                wrap.setAttribute("name", wrapper.getClass().getName());
                wrap.setAttribute("prefix", wrapper.gimmePrefix());
            }
        }
    }


    /**
     * The method <code>needsData</code> tells if any of the IWrappers this instance aggregates still needs Data.
     *
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     * @see de.schlund.pfixcore.workflow.app.IWrapperContainer#needsData()
     */
    public boolean needsData() throws Exception{
        if (wrappers.isEmpty()) return true; // border case

        // synchronized (wrappers) {
            for (Iterator<IWrapper> i = wrappers.values().iterator(); i.hasNext(); ) {
                IWrapper wrapper = i.next();
                IHandler handler = wrapper.gimmeIHandler();
                if (handler.isActive(context) && handler.needsData(context)) {
                    return true;
                }
            }
        // }
        return false;
    }
    
  
    /**
     * <code>handleSubmittedData</code> will call all or a part of the defined IWrappers
     * (depending of grouping and/or restricting the IWrappers) to get
     * the IHandler that these IWrappers are bound to. The IHandler are called in turn to handle the submitted data.
     * This works by calling ihandler.handleSubmittedData(Context context, IWrapper wrapper).
     * See {@link IHandler}.
     * You can only call this method if you initialized the instance with a non null ResultDocument.
     *
     * @exception Exception if an error occurs
     * @see de.schlund.pfixcore.workflow.app.IWrapperContainer#handleSubmittedData() 
     */
    public void handleSubmittedData() throws Exception {
        if (!is_splitted) splitIWrappers();
        IWrapper[] cwrappers   = currentgroup.getIWrappers();
        
        for (int i = 0; i < cwrappers.length; i++) {
            IWrapper wrapper = cwrappers[i];
            IHandler handler = wrapper.gimmeIHandler();
            if (handler.isActive(context)) {
                wrapper.load(reqdata);
                if (selectedwrappers.contains(wrapper)) {
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
     * <code>retrieveCurrentStatus</code> will call all or a part of the defined IWrappers
     * (depending of grouping and/or restricting the IWrappers) to get the "active" IHandlers, which
     * are called in turn via ihandler.retrieveCurrentStatus(Context context, IWrapper wrapper).
     *
     * @exception Exception if an error occurs
     * @see de.schlund.pfixcore.workflow.app.IWrapperContainer#retrieveCurrentStatus() 
     */
    public void retrieveCurrentStatus() throws Exception {
        if (!is_splitted)
            splitIWrappers();
        IWrapper[] selwrappers = selectedwrappers.getIWrappers();
        retrieveCurrentStatusForWrappers(selwrappers);

        if (always_retrieve != null && !always_retrieve.isEmpty()) {
            for (int i = 0; i < always_retrieve.getIWrappers().length; i++) {
                IWrapper wrapper = always_retrieve.getIWrappers()[i];
                if (!selectedwrappers.contains(wrapper) && currentgroup.contains(wrapper)) {
                    IHandler handler = wrapper.gimmeIHandler();
                    if (handler.isActive(context)) {
                        handler.retrieveCurrentStatus(context, wrapper);
                    }
                }
            }
        }
    }


    //
    //   PRIVATE
    // 

    private void retrieveCurrentStatusForWrappers(IWrapper[] allwrappers) throws Exception {
        for (int i = 0; i < allwrappers.length; i++) {
            IWrapper wrapper = allwrappers[i];
            IHandler handler = wrapper.gimmeIHandler();
            if (handler.isActive(context)) {
                handler.retrieveCurrentStatus(context, wrapper);
            }
        }
    }

    private void splitIWrappers() throws Exception {
        reqdata = new RequestDataImpl(context, preq);
        checkGroupDisplayStatus();
        boolean group_wanted = ((Boolean) preq.getSession(false).getAttribute(GROUP_STATUS)).booleanValue();
        boolean group_defined = hasGroupDefinition();
        if (group_wanted && group_defined) {
            readIWrapperGroupsConfigFromProperties();
            currentgroup = getCurrentGroupFromRequest();
        } else {
            IWrapperGroup group = new IWrapperGroup();
            group.setName(GROUP_ANONYMOUS);
            for (Iterator<IWrapper> i = wrappers.values().iterator(); i.hasNext(); ) {
                group.addIWrapper(i.next());
            }
            activegroups.add(group);
            currentgroup = group;
        }
        checkForRestrictedCalling();

        createRestrictedContinueGroup();
        createAlwaysRetrieveGroup();
        
        is_splitted = true;
    }


    private void checkForRestrictedCalling() throws Exception {
        IWrapperGroup selected    = new IWrapperGroup();
        String[]      selwrappers = reqdata.getCommands(SELECT_WRAPPER);
        if (selwrappers != null) {
            for (int i = 0; i < selwrappers.length; i++) {
                String   prefix  = selwrappers[i];
                LOG.debug("  >> Restricted to Wrapper: " + prefix);
                IWrapper selwrap = (IWrapper) wrappers.get(prefix);
                if (selwrap == null) {
                    LOG.warn(" *** No wrapper found for prefix " + prefix + "! ignoring");
                    continue;
                }
                selected.addIWrapper(selwrap);
            }
        }

        if (selected.isEmpty()) {
            selectedwrappers = currentgroup;
        } else {
            selectedwrappers = selected;
        }
    }
    
    private boolean checkGroupDisplayStatus() {
        Properties props = context.getPropertiesForCurrentPageRequest();
        String     force = props.getProperty(GROUP_FORCE_PROP);

        if (force != null && (force.equals("yes") || force.equals("true") || force.equals("on"))) {
            return true;
        }
        
        HttpSession  session = preq.getSession(false);
        RequestParam status  = preq.getRequestParam(GROUP_STATUS_PARAM);
        if (status != null && (status.getValue().equals(GROUP_ON_PARAM))) {
            LOG.debug("*** Request says: Groupdisplay ON");
            session.setAttribute(GROUP_STATUS, GROUP_ON);
            return true;
        } else if (status != null && status.getValue().equals(GROUP_OFF_PARAM)) {
            LOG.debug("*** Request says: Groupdisplay OFF");
            session.setAttribute(GROUP_STATUS, GROUP_OFF);
            return false;
        } else if (session.getAttribute(GROUP_STATUS) == null) {
            LOG.debug("*** Nothing in Session: init by switching Groupdisplay ON");
            session.setAttribute(GROUP_STATUS, GROUP_ON);
            return true;
        } else {
            LOG.debug("*** Session says: Groupddisplay is " + session.getAttribute(GROUP_STATUS));
            return ((Boolean) session.getAttribute(GROUP_STATUS)).booleanValue();
        }
    }

    private void readIWrappersConfigFromProperties() throws Exception  {
        //Properties props      = context.getPropertiesForCurrentPageRequest();
        //HashMap    interfaces = PropertiesUtils.selectProperties(props, PROP_INTERFACE);
        PageRequestConfig config = context.getConfigForCurrentPageRequest();
        Collection<? extends IWrapperConfig> interfaces = config.getIWrappers().values();

        if (interfaces.size() == 0) {
            LOG.debug("*** Found no interfaces for this page (page=" + context.getCurrentPageRequest().getName() + ")");
        } else {
            // Initialize all wrappers
            int i = 0;
            for (IWrapperConfig iConfig : interfaces) {
                String prefix = iConfig.getPrefix();
                int order = i++;

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
                    throw new XMLException("unable to instantiate class ["+iface + "] :" + e.getMessage());
                } catch (IllegalAccessException e) {
                    throw new XMLException("unable to acces class [" + iface +"] :" + e.getMessage());
                } catch (ClassCastException e) {
                    throw new XMLException("class [" + iface + "] does not implement the interface IWrapper :" + e.getMessage());
                }
                
                wrapper.defineOrder(order);
                
                wrappers.put(prefix, wrapper);
                wrapper.init(prefix);

                String  logdir  = context.getProperties().getProperty(WRAPPER_LOGDIR);
                boolean dolog   = iConfig.getLogging();
                if (dolog && logdir != null && !logdir.equals("")) {
                    FileResource dir = ResourceUtil.getFileResourceFromDocroot(logdir);
                    if (dir.isDirectory() && dir.canWrite()) {
                        wrapper.initLogging(dir, context.getCurrentPageRequest().getName(), context.getVisitId());
                    }
                }
            }
            
        }
    }

    private void createAlwaysRetrieveGroup() {
        always_retrieve     = new IWrapperGroup();
        
        for (IWrapperConfig iConfig : context.getConfigForCurrentPageRequest().getIWrappers().values()) {
            if (iConfig.isAlwaysRetrieve()) {
                IWrapper wrapper = (IWrapper) wrappers.get(iConfig.getPrefix());
                LOG.debug("*** Adding interface '" + iConfig.getPrefix() + "' to the always_retrieve group...");
                always_retrieve.addIWrapper(wrapper);
            }
        }
    }

    private void createRestrictedContinueGroup() {
        contwrappers = new IWrapperGroup();
        
        for (IWrapperConfig iConfig : context.getConfigForCurrentPageRequest().getIWrappers().values()) {
            if (iConfig.isContinue()) {
                IWrapper wrapper = (IWrapper) wrappers.get(iConfig.getPrefix());
                LOG.debug("*** Adding interface '" + iConfig.getPrefix() + "' to the restricted_continue group...");
                contwrappers.addIWrapper(wrapper);
            }
        }
    }
    
    private void readIWrapperGroupsConfigFromProperties() throws Exception {
        Properties props = context.getPropertiesForCurrentPageRequest();
        TreeMap<String, String> pmap = PropertiesUtils.selectPropertiesSorted(props, GROUP_PROP);
        
        for (Iterator<String> i = pmap.keySet().iterator(); i.hasNext(); ) {
            String        page  = i.next();
            String        spec  = pmap.get(page);
            IWrapperGroup group = new IWrapperGroup();
            group.setName(page);
            if (spec == null || spec.equals("")) {
                throw new XMLException("Specification for group '" + page + "' must be given.");
            }
            StringTokenizer tok = new StringTokenizer(spec);
            for ( ; tok.hasMoreTokens(); ) {
                String   prefix  = tok.nextToken();
                IWrapper wrapper = (IWrapper) wrappers.get(prefix);
                if (wrapper == null) {
                    throw new XMLException("No matching interface for specification token '" + prefix + "'");
                }
                group.addIWrapper(wrapper);
            }
            if (checkGroupActivity(group)) {
                activegroups.add(group);
            }
        }
    }

    private boolean checkGroupActivity(IWrapperGroup group) throws Exception{
        IWrapper[] gwrappers = group.getIWrappers();
        for (int i = 0; i < gwrappers.length; i++) {
            IHandler handler = gwrappers[i].gimmeIHandler(); 
            if (handler.isActive(context)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean hasGroupDefinition() {
        Properties props = context.getPropertiesForCurrentPageRequest();
        HashMap<String, String> pmap = PropertiesUtils.selectProperties(props, GROUP_PROP);
        if (pmap.isEmpty()) {
            LOG.debug("*** Properties say: Have NO group definition");
            return false;
        } else {
            LOG.debug("*** Properties say: Have group definition");
            return true;
        }
    }

    private IWrapperGroup getCurrentGroupFromRequest() throws Exception {
        LOG.debug("* looking for group index: " +  GROUP_CURR);
        RequestParam page = preq.getRequestParam(GROUP_CURR);
        // synchronized (activegroups) { 
            if (page == null || page.getValue().equals("")) {
                LOG.debug("*** Request specifies NO group index: Using index 0");
                return (IWrapperGroup) activegroups.get(0);
            } else {
                LOG.debug("*** Request specifies group index: Using index " + page);
                Integer index = new Integer(page.getValue());
                return (IWrapperGroup) activegroups.get(index.intValue());
            }
        // }
    }
    
    private Integer checkForNextIndexInRequest() {
        int current_idx = getCurrentIWrapperGroupIndex();
        int last_idx    = activegroups.size() - 1;
        LOG.debug("* Current Idx: " + current_idx + " LastIdx: " + last_idx);
        String[] grpcmdvals = reqdata.getCommands(SELECT_GROUP); 

        if (grpcmdvals != null && grpcmdvals.length > 0) {
            if (grpcmdvals.length > 1) {
                LOG.warn(" *** WARNING: SELGRP commando was submitted more than once! *** ");
            }
            String val = grpcmdvals[0];

            if (val.equals(GROUP_NEXT)) {
                LOG.debug("* CMD VAL is NEXT");
                if (current_idx < last_idx) {
                    LOG.debug("* Setting idx to: " + (current_idx + 1));
                    return new Integer(current_idx + 1);
                } else {
                    LOG.debug("* Next idx out of bounds; setting to: " + last_idx);
                    return new Integer(last_idx);
                }
            } else if (val.equals(GROUP_PREV)) {
                LOG.debug("* CMD VAL is PREV");
                if (current_idx > 0) {
                    LOG.debug("* Setting idx to: " + (current_idx - 1));
                    return new Integer(current_idx - 1);
                } else {
                    LOG.debug("* Prev idx out of bounds; setting to: 0");
                    return new Integer(0);
                }
            } else {
                LOG.debug("* CMD VAL is: " + val);
                Integer index;
                try {
                    index = new Integer(val);
                } catch (Exception e) {
                    LOG.warn("Couldn't parse index into integer: '" + val + "': " + e.toString());
                    return new Integer(current_idx);
                }
                if (index.intValue() >= 0 && index.intValue() <= last_idx) {
                    return index;
                } else {
                    LOG.debug("* Idx out of bounds; resetting to current value: " + current_idx);
                    return new Integer(current_idx);
                }
            }

        }
        LOG.debug("* Found no group index cmd; returning null");
        return null;
    }
    
    private int getCurrentIWrapperGroupIndex() {
        return activegroups.indexOf(currentgroup);
    }
    
    private void setCurrentIWrapperGroupByIndex(int index) {
        if (index < 0 || index >= activegroups.size()) {
            throw new RuntimeException("IWrapperGroup index must be between 0 and " + (activegroups.size() - 1));
        }
        currentgroup = (IWrapperGroup) activegroups.get(index);
    }

    private class IWrapperGroup {
        private TreeSet<IWrapper> group = new TreeSet<IWrapper>();
        private String  name;
        
        public void setName(String name) {
            this.name = name;
        }
        
        public void addIWrapper(IWrapper wrapper) {
            group.add(wrapper);
        }
        
        public String getName() {
            return name;
        }
        
        public boolean isEmpty() {
            return group.isEmpty();
        }

        public boolean contains(IWrapper wrapper) {
            return group.contains(wrapper);
        }

        public boolean containsAll(IWrapperGroup ingroup) {
            IWrapper[] in = ingroup.getIWrappers();
            return group.containsAll(Arrays.asList(in));
        }
            
        public IWrapper[] getIWrappers() {
            // synchronized (group) {
                return group.toArray(new IWrapper[] {});
            // }
        }
    }// IWrapperGroup
    
    
}// IWrapperSimpleContainer
