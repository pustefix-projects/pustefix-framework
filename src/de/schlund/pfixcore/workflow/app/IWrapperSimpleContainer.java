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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Category;
import org.w3c.dom.Element;

import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.generator.IWrapperParamInfo;
import de.schlund.pfixcore.generator.RequestData;
import de.schlund.pfixcore.util.PropertiesUtils;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.RequestParam;
import de.schlund.pfixxml.ResultDocument;
import de.schlund.pfixxml.ResultForm;
import de.schlund.pfixxml.XMLException;
import de.schlund.util.statuscodes.StatusCode;
import de.schlund.pfixxml.loader.*;

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

public class IWrapperSimpleContainer implements IWrapperContainer, Reloader {
    private   HashMap            wrappers           = new HashMap();
    private   HashMap            prefixmap          = new HashMap();

    // depends on request
    private   ArrayList          activegroups       = new ArrayList();
    private   IWrapperGroup      currentgroup       = null;
    private   IWrapperGroup      selectedwrappers   = null;

    private   IWrapperGroup      contwrappers       = null;
    private   IWrapperGroup      always_retrieve    = null;

    private   Context            context            = null;
    private   ResultDocument     resdoc             = null;
    private   PfixServletRequest preq               = null;
    private   RequestData        reqdata            = null;
    private   boolean            is_splitted        = false;
    private   boolean            is_loaded          = false;
    protected Category           CAT                = Category.getInstance(this.getClass().getName());
    
    public  static final String  PROP_CONTAINER     = "iwrappercontainer";
    private static final String  PROP_INTERFACE     = "interface";
    public  static final String  PROP_RESTRICED     = "restrictedcontinue";
    public  static final String  PROP_ALWAYS_RETRIEVE = "alwaysretrieve";
    
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
     * Use this methods at any time to check if any error has happened in
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

        if (!is_loaded) throw new XMLException("You first need to have called handleSubmittedData() here!");
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
        if (!is_loaded) throw new XMLException("You first need to have called handleSubmittedData() here!");
        if (!is_splitted) splitIWrappers();
        ResultForm resform   = resdoc.createResultForm();
        IWrapper[] cwrappers = selectedwrappers.getIWrappers();
        for (int i = 0; i < cwrappers.length; i++) {
            IWrapper            wrapper = cwrappers[i];
            String              prefix  = wrapper.gimmePrefix();
            IWrapperParamInfo[] errors = wrapper.gimmeAllParamInfosWithErrors();
            if (errors != null) {
                for (int j = 0; j < errors.length; j++) {
                    IWrapperParamInfo param  = errors[j];
                    StatusCode[]      scodes = param.getStatusCodes();
                    String            name   = prefix + "." + param.getName(); 
                    if (scodes != null) {
                        for (int k = 0; k < scodes.length; k++) {
                            StatusCode code = scodes[k];
                            String[]   args = param.getArgsForStatusCode(code);
                            resform.addStatusCode(context.getProperties(), code, args, name);
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
        ResultForm resform   = resdoc.createResultForm();
        IWrapper[] cwrappers = currentgroup.getIWrappers();
        for (int i = 0; i < cwrappers.length; i++) {
            IWrapper            wrapper  = cwrappers[i];
            String              prfx     = wrapper.gimmePrefix();
            IWrapperParamInfo[] pinfoall = wrapper.gimmeAllParamInfos();
            for (int j = 0; j < pinfoall.length; j++) {
                IWrapperParamInfo pinfo  = pinfoall[j];
                String            name   = pinfo.getName();
                String[]          strval = pinfo.getStringValue();
                if (strval != null) {
                    for (int k = 0; k < strval.length; k++) {
                        resform.addValue(prfx + "." + name, strval[k]);
                    }
                }
            }
        }
    }

  
    /**
     * Use this method to query if the IWrapperContainer wants to continue with submitting data,
     * or if it assumes this whole page to be completed.
     *
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     * @see de.schlund.pfixcore.workflow.app.IWrapperContainer#continueSubmit() 
     */
    public boolean continueSubmit() throws Exception {
        if (wrappers.isEmpty()) return false; // border case

        if (!is_splitted) splitIWrappers();
        
        if (selectedwrappers != currentgroup) {
            if (contwrappers != null && contwrappers.containsAll(selectedwrappers)) {
                CAT.debug("*** No more submit because all selected wrappers are members of the restriced_continue group!");
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
     * Do not call this method if you initialized the instance with a null ResultDocument!
     * The method will fail with a RuntimeException if you try to do nevertheless.
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

            IWrapper[] wrappers = group.getIWrappers();
            for (int j = 0; j < wrappers.length; j++) {
                IWrapper wrapper = wrappers[j];
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

        synchronized (wrappers) {
            for (Iterator i = wrappers.values().iterator(); i.hasNext(); ) {
                IWrapper wrapper = (IWrapper) i.next();
                IHandler handler = wrapper.gimmeIHandler();
                if (handler.isActive(context) && handler.needsData(context)) {
                    return true;
                }
            }
        }
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
                if (!wrapper.errorHappened() && selectedwrappers.contains(wrapper)) {
                    handler.handleSubmittedData(context, wrapper);
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
        reqdata = (RequestData) (new RequestDataImpl(context, preq));
        checkGroupDisplayStatus();
        boolean group_wanted = ((Boolean) preq.getSession(false).getValue(GROUP_STATUS)).booleanValue();
        boolean group_defined = hasGroupDefinition();
        if (group_wanted && group_defined) {
            readIWrapperGroupsConfigFromProperties();
            currentgroup = getCurrentGroupFromRequest();
        } else {
            IWrapperGroup group = new IWrapperGroup();
            group.setName(GROUP_ANONYMOUS);
            for (Iterator i = wrappers.values().iterator(); i.hasNext(); ) {
                group.addIWrapper((IWrapper) i.next());
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
                String prefix = selwrappers[i];
                CAT.debug("  >> Restricted to Wrapper: " + prefix);
                String iface  = (String) prefixmap.get(prefix);
                if (iface == null) {
                    CAT.warn(" *** No interface found for prefix " + prefix + "! ignoring");
                    continue;
                }
                IWrapper selwrap = (IWrapper) wrappers.get(iface); 
                if (selwrap == null) {
                    CAT.warn(" *** No wrapper found for prefix " + prefix + "! ignoring");
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
            CAT.debug("*** Request says: Groupdisplay ON");
            session.putValue(GROUP_STATUS, GROUP_ON);
            return true;
        } else if (status != null && status.getValue().equals(GROUP_OFF_PARAM)) {
            CAT.debug("*** Request says: Groupdisplay OFF");
            session.putValue(GROUP_STATUS, GROUP_OFF);
            return false;
        } else if (session.getValue(GROUP_STATUS) == null) {
            CAT.debug("*** Nothing in Session: init by switching Groupdisplay ON");
            session.putValue(GROUP_STATUS, GROUP_ON);
            return true;
        } else {
            CAT.debug("*** Session says: Groupddisplay is " + session.getValue(GROUP_STATUS));
            return ((Boolean) session.getValue(GROUP_STATUS)).booleanValue();
        }
    }

    private void readIWrappersConfigFromProperties() throws Exception  {
        Properties props      = context.getPropertiesForCurrentPageRequest();
        HashMap    interfaces = PropertiesUtils.selectProperties(props, PROP_INTERFACE);

        if (interfaces.isEmpty()) {
            CAT.debug("*** Found no interfaces for this page (page=" + context.getCurrentPageRequest().getName() + ")");
        } else {
            // Initialize all wrappers
            for (Iterator i = interfaces.keySet().iterator(); i.hasNext(); ) {
                String prefix = (String) i.next();
                String realprefix = prefix;
                int    order      = -1;
                int    index;
                if ((index = prefix.indexOf(".")) > 0) {
                    order      = Integer.parseInt(prefix.substring(0, index)); 
                    realprefix = prefix.substring(index + 1);
                } else {
                    throw new XMLException("You need to give a order for the interfaces to be called!");
                }
                
                String iface  = (String) interfaces.get(prefix);
                if (iface == null || iface.equals("")) {
                    throw new XMLException("No interface for prefix " + realprefix);
                }
                
                Class thewrapper = null;
                IWrapper wrapper = null;
                try {
                    AppLoader appLoader = AppLoader.getInstance();
                    if (appLoader.isEnabled()) {
                        wrapper = (IWrapper) appLoader.loadClass(iface).newInstance();
                    } else {
                        thewrapper = Class.forName(iface);
                        wrapper    = (IWrapper) thewrapper.newInstance();
                    }
                } catch (ClassNotFoundException e) {
                    throw new XMLException("unable to find class [" + iface + "] :" + e.getMessage());
                } catch (InstantiationException e) {
                    throw new XMLException("unable to instantiate class ["+iface + "] :" + e.getMessage());
                } catch (IllegalAccessException e) {
                    throw new XMLException("unable to acces class [" + iface +"] :" + e.getMessage());
                } catch (ClassCastException e) {
                    throw new XMLException("class [" + iface + "] does not implement the interface IWrapper :" + e.getMessage());
                }
                
                if (order > -1) {
                    wrapper.defineOrder(order);
                }
                prefixmap.put(realprefix, iface);
                wrappers.put(iface, wrapper);
                wrapper.init(realprefix);
                
                AppLoader appLoader = AppLoader.getInstance();
                if (appLoader.isEnabled()) appLoader.addReloader(this);
            }
        }
    }

    private void createAlwaysRetrieveGroup() {
        Properties props = context.getPropertiesForCurrentPageRequest();
        always_retrieve = new IWrapperGroup();
        String retrieve = props.getProperty(PROP_ALWAYS_RETRIEVE);
        if (retrieve != null && !retrieve.equals("")) {
            StringTokenizer tok = new StringTokenizer(retrieve);
            for (; tok.hasMoreTokens(); ) {
                String prefix = tok.nextToken();
                String iface  = (String) prefixmap.get(prefix);
                if (iface == null) {
                    CAT.warn("*** Prefix '" + prefix + "' is not mapped to a defined interface. Ignoring...");
                } else {
                    if (CAT.isDebugEnabled()) {
                        CAT.debug("*** Adding interface '" + iface + "' to the always_retrieve group...");
                    }
                    IWrapper wrapper = (IWrapper) wrappers.get(iface);
                    always_retrieve.addIWrapper(wrapper);
                }
            }
        }
    }

    private void createRestrictedContinueGroup() {
        Properties props = context.getPropertiesForCurrentPageRequest();
        contwrappers    = new IWrapperGroup();
        String restricted = props.getProperty(PROP_RESTRICED);
        if (restricted != null && !restricted.equals("")) {
            StringTokenizer tok = new StringTokenizer(restricted);
            for (; tok.hasMoreTokens(); ) {
                String prefix = tok.nextToken();
                String iface  = (String) prefixmap.get(prefix);
                if (iface == null) {
                    CAT.warn("*** Prefix '" + prefix + "' is not mapped to a defined interface. Ignoring...");
                } else {
                    CAT.debug("*** Adding interface '" + iface + "' to the restricted_continue group...");
                    IWrapper wrapper = (IWrapper) wrappers.get(iface);
                    contwrappers.addIWrapper(wrapper);
                }
            }
        }
    }
    
    private void readIWrapperGroupsConfigFromProperties() throws Exception {
        Properties props = context.getPropertiesForCurrentPageRequest();
        TreeMap    pmap  = PropertiesUtils.selectPropertiesSorted(props, GROUP_PROP);
        for (Iterator i = pmap.keySet().iterator(); i.hasNext(); ) {
            String        page  = (String) i.next();
            String        spec  = (String) pmap.get(page);
            IWrapperGroup group = new IWrapperGroup();

            group.setName(page);
            
            if (spec == null || spec.equals("")) {
                throw new XMLException("Specification for group '" + page + "' must be given.");
            }
            StringTokenizer tok = new StringTokenizer(spec);
            for ( ; tok.hasMoreTokens(); ) {
                String token = tok.nextToken();
                String iface = (String) prefixmap.get(token);
                if (iface == null || iface.equals("")) {
                    throw new XMLException("No matching interface for specification token '" + token + "'");
                }
                IWrapper wrapper = (IWrapper) wrappers.get(iface);
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
        HashMap    pmap  = PropertiesUtils.selectProperties(props, GROUP_PROP);
        if (pmap.isEmpty()) {
            CAT.debug("*** Properties say: Have NO group definition");
            return false;
        } else {
            CAT.debug("*** Properties say: Have group definition");
            return true;
        }
    }

    private IWrapperGroup getCurrentGroupFromRequest() throws Exception {
        CAT.debug("* looking for group index: " +  GROUP_CURR);
        RequestParam page = preq.getRequestParam(GROUP_CURR);
        synchronized (activegroups) { 
            if (page == null || page.getValue().equals("")) {
                CAT.debug("*** Request specifies NO group index: Using index 0");
                return (IWrapperGroup) activegroups.get(0);
            } else {
                CAT.debug("*** Request specifies group index: Using index " + page);
                Integer index = new Integer(page.getValue());
                return (IWrapperGroup) activegroups.get(index.intValue());
            }
        }
    }
    
    private Integer checkForNextIndexInRequest() {
        int current_idx = getCurrentIWrapperGroupIndex();
        int last_idx    = activegroups.size() - 1;
        CAT.debug("* Current Idx: " + current_idx + " LastIdx: " + last_idx);
        String[] grpcmdvals = reqdata.getCommands(SELECT_GROUP); 

        if (grpcmdvals != null && grpcmdvals.length > 0) {
            if (grpcmdvals.length > 1) {
                CAT.warn(" *** WARNING: SELGRP commando was submitted more than once! *** ");
            }
            String val = grpcmdvals[0];

            if (val.equals(GROUP_NEXT)) {
                CAT.debug("* CMD VAL is NEXT");
                if (current_idx < last_idx) {
                    CAT.debug("* Setting idx to: " + (current_idx + 1));
                    return new Integer(current_idx + 1);
                } else {
                    CAT.debug("* Next idx out of bounds; setting to: " + last_idx);
                    return new Integer(last_idx);
                }
            } else if (val.equals(GROUP_PREV)) {
                CAT.debug("* CMD VAL is PREV");
                if (current_idx > 0) {
                    CAT.debug("* Setting idx to: " + (current_idx - 1));
                    return new Integer(current_idx - 1);
                } else {
                    CAT.debug("* Prev idx out of bounds; setting to: 0");
                    return new Integer(0);
                }
            } else {
                CAT.debug("* CMD VAL is: " + val);
                Integer index;
                try {
                    index = new Integer(val);
                } catch (Exception e) {
                    CAT.warn("Couldn't parse index into integer: '" + val + "': " + e.toString());
                    return new Integer(current_idx);
                }
                if (index.intValue() >= 0 && index.intValue() <= last_idx) {
                    return index;
                } else {
                    CAT.debug("* Idx out of bounds; resetting to current value: " + current_idx);
                    return new Integer(current_idx);
                }
            }

        }
        CAT.debug("* Found no group index cmd; returning null");
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
        private TreeSet group = new TreeSet();
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
            synchronized (group) {
                return (IWrapper[]) group.toArray(new IWrapper[] {});
            }
        }
    }// IWrapperGroup

    public void reload() {
          HashMap  wrappersNew = new HashMap();
          Iterator it = wrappers.keySet().iterator();
          while(it.hasNext()) {
              String   str       = (String)it.next();
              IWrapper iwOld     = (IWrapper) wrappers.get(str);
              IWrapper iwNew     = (IWrapper)  StateTransfer.getInstance().transfer(iwOld);
              String   className = iwOld.getClass().getName();
              wrappersNew.put(str,iwNew);
          }
          wrappers = wrappersNew;
          
    }

    
}// IWrapperSimpleContainer
