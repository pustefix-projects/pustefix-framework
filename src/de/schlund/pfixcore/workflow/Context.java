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

package de.schlund.pfixcore.workflow;

import de.schlund.pfixcore.workflow.*;
import de.schlund.pfixcore.workflow.Navigation.*;
import de.schlund.pfixxml.*;
import de.schlund.pfixxml.serverutil.*;
import de.schlund.pfixxml.targets.*;
import de.schlund.pfixcore.util.*;
import java.util.*;
import javax.servlet.http.*;
import org.apache.log4j.*;
import org.w3c.dom.*;


/**
 * This class is the corner piece of our workflow concept.
 * Here we decide which {@link de.schlund.pfixcore.workflow.State State} handles the
 * request. This decision is based on the notion of workflows (aka
 * {@link de.schlund.pfixcore.workflow.PageFlow PageFlow}s).
 * 
 *
 * @author jtl
 * @version 2.0
 *
 *
 */

public class Context implements AppContext {
    // from constructor
    private String                 name;
    private Properties             properties;
    
    // shared between all instances that have the same properties
    private PageFlowManager        pageflowmanager;
    private PageRequestProperties  preqprops;
    private PageMap                pagemap;
    
    // new instance for every Context
    private ContextResourceManager rmanager;
    private Navigation             navigation = null;
    
    // values read from properties
    private boolean                autoinvalidate_navi = true;
    private boolean                in_adminmode        = false;
    private PageRequest            admin_pagereq;

    // the request state
    private PageRequest            currentpagerequest;
    private PageFlow               currentpageflow;

    private HashMap                navigation_visible = null;
    private String                 visit_id           = null;
    private boolean                needs_update;
    
    private static Category LOG = Category.getInstance(Context.class.getName());

    private final static String NOSTORE           = "nostore";
    private final static String DEFPROP           = "context.defaultpageflow";
    private final static String NAVPROP           = "xmlserver.depend.xml";
    private final static String PROP_NAVI_AUTOINV = "navigation.autoinvalidate"; 
    private final static String WATCHMODE         = "context.adminmode.watch";
    private final static String ADMINPAGE         = "context.adminmode.page";
    private final static String ADMINMODE         = "context.adminmode";

    // log, if duration of state's isAccesible method is longer than these values
    private static int MAXTIME_ISACCESSIBLE_DEBUG = 10;
    private static int MAXTIME_ISACCESSIBLE_WARN = 100;

    /**
     * <code>init</code> sets up the Context for operation.
     *
     * @param properties a <code>Properties</code> value
     * @exception Exception if an error occurs
     */
    public void init(Properties properties, String name) throws Exception {
	this.properties = properties;
        this.name       = name;
        
	rmanager = new ContextResourceManager();
	rmanager.init(this);
        
        reset();
    }

    public void reset() {
        needs_update       = true;
        invalidateNavigation();
    }
    
    public void invalidateNavigation() {
        navigation_visible = new HashMap();
    }
    
    private void do_update() throws Exception {
    	// get PropertyObjects from PropertyObjectManager
    	PropertyObjectManager pom = PropertyObjectManager.getInstance();
        
        pageflowmanager = (PageFlowManager) pom.getPropertyObject(properties,"de.schlund.pfixcore.workflow.PageFlowManager");
        preqprops       = (PageRequestProperties) pom.getPropertyObject(properties,"de.schlund.pfixcore.workflow.PageRequestProperties");
        pagemap         = (PageMap) pom.getPropertyObject(properties,"de.schlund.pfixcore.workflow.PageMap");
        
        // The navigation is possibly shared across more than one context, i.e. more than one properties object.
        // So we can't let it be handled by the PropertyObjectManager.
        if (properties.getProperty(NAVPROP) != null) {
            navigation = NavigationFactory.getInstance().getNavigation(properties.getProperty(NAVPROP));
        }

        currentpageflow    = pageflowmanager.getPageFlowByName(properties.getProperty(DEFPROP));
        currentpagerequest = currentpageflow.getFirstStep();
        
        checkForAdminMode();
        checkForNavigationReuse();

        needs_update = false;
    }

    private void checkForNavigationReuse() {
        String navi_autoinv = properties.getProperty(PROP_NAVI_AUTOINV);
        if (navi_autoinv != null && navi_autoinv.equals("false")) {
            autoinvalidate_navi = false;
            LOG.warn("\n**** CAUTION **** Setting autoinvalidate of navigation to FALSE!!!! \n" +
                     "**** You need to call context.invalidateNavigation() to update the navigation.");
        } else {
            autoinvalidate_navi = true;
        }
    }
    
    private void checkForAdminMode() {
        admin_pagereq = null;
        in_adminmode  = false;

        String watchprop = properties.getProperty(WATCHMODE);
        if (watchprop != null && !watchprop.equals("")) {
            String adminprop = properties.getProperty(ADMINMODE + "." + watchprop + ".status");
            String adminpage = properties.getProperty(ADMINPAGE);
            if (adminpage != null && !adminpage.equals("") && adminprop != null && adminprop.equals("on")) {
                LOG.debug("*** setting Wartungsmodus for : " + watchprop + " ***");
                admin_pagereq = new PageRequest(adminpage);
                in_adminmode  = true;
            }
        }
    }
    
    /**
     * <code>handleRequest</code> is the entry point where the Context is called
     * from outside to handle the supplied request.
     *
     * @param req a <code>HttpServletRequest</code> value
     * @return a <code>SPDocument</code> value
     * @exception Exception if an error occurs
     */
    public synchronized SPDocument handleRequest(PfixServletRequest preq) throws Exception {
        if (needs_update) {
            do_update();
        }
        
        SPDocument  spdoc;
        PageRequest prevpage = getCurrentPageRequest();
        PageFlow    prevflow = getCurrentPageFlow();
        
        if (visit_id == null) 
            visit_id = (String) preq.getSession(false).getValue(ServletManager.VISIT_ID);

        if (in_adminmode) {
            setCurrentPageRequest(admin_pagereq);
            spdoc = documentFromCurrentStep(preq, false);
            setCurrentPageRequest(prevpage);
            spdoc.setPagename(admin_pagereq.getName());
            return spdoc;
        }
        
    	trySettingPageRequestAndFlow(preq);
        spdoc = documentFromFlow(preq);

        if (spdoc.getResponseError() != 0) {
            setCurrentPageRequest(prevpage);
            setCurrentPageFlow(prevflow);
            return spdoc;
        }
        
        if (navigation != null && spdoc != null) {
            addNavigation(navigation, spdoc, preq);
        }
        
        if (spdoc != null && spdoc.getPagename() == null) {
            spdoc.setPagename(getCurrentPageRequest().getName());
        }

        if (pageIsSidestepPage(getCurrentPageRequest())) {
            LOG.warn("*** Sidestep page: Restoring to page " +  prevpage + " and " + prevflow);
            setCurrentPageRequest(prevpage);
            setCurrentPageFlow(prevflow);
            LOG.warn("*** Sidestep page: Inhibit storing of spdoc");
            spdoc.setNostore(true);
        }
        
        return spdoc;
    }


    private boolean pageIsSidestepPage(PageRequest page) {
        Properties props  = preqprops.getPropertiesForPageRequest(page);
        if (props != null) {
            String nostore = props.getProperty(NOSTORE);
            if (nostore != null && nostore.toLowerCase().equals("true")) {
                LOG.warn("*** Found sidestep page: " + page);
                return true;
            }
        } else {
            LOG.error("*** Got NULL properties for page " + page);
        }
        return false;
    }
    
    /**
     * <code>getContextResourceManager</code> returns the ContextResourceManager defined in init(Properties properties).
     *
     * @return a <code>ContextResourceManager</code> value
     */
    public ContextResourceManager getContextResourceManager() {
        return rmanager;
    }

    /**
     * <code>getProperties</code> returns the Properties supplied in init(Properties properties).
     *
     * @return a <code>Properties</code> value
     */
    public Properties getProperties() {
	return properties;
    }

    /**
     * Describe <code>getPropertiesForCurrentPageRequest</code> method here.
     * This returnes the all Properties which match the current PageRequest <b>without</b> the common prefix.
     * E.g. the page is named "foo", then all properties starting with "pagerequest.foo.*" will be returned after
     * stripping the "pagerequest.foo." prefix.
     *
     * @return a <code>Properties</code> value
     */
    public Properties getPropertiesForCurrentPageRequest() {
        return preqprops.getPropertiesForPageRequest(getCurrentPageRequest());
    }

    /**
     * <code>flowIsRunning</code> can be called from inside a {@link de.schlund.pfixcore.workflow.State State}
     * It returned true if the Context is currently running one of the defined workflows.
     *
     * @return a <code>boolean</code> value
     */
    public boolean flowIsRunning() {
        if (getCurrentPageRequest().getStatus() == PageRequestStatus.WORKFLOW) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * <code>finalPageIsRunning</code> can be called from inside a {@link de.schlund.pfixcore.workflow.State State}
     * It returned true if the Context is currently running a FINAL page of a defined workflow.
     *
     * @return a <code>boolean</code> value
     */
    public boolean finalPageIsRunning() {
        if (getCurrentPageRequest().getStatus() == PageRequestStatus.FINAL) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * <code>getPageFlowManager</code> returns the PageFlowManager defined in init(Properties properties)
     *
     * @return a <code>PageFlowManager</code> value
     */
    protected PageFlowManager getPageFlowManager() {
        return pageflowmanager;
    }

    /**
     * <code>getPageMap</code> returns the PageMap defined in init(Properties properties)
     *
     * @return a <code>PageMap</code> value
     */
    protected PageMap getPageMap() {
        return pagemap;
    }
    
    /**
     * <code>getCurrentPageFlow</code> returnes the currently active PageFlow.
     *
     * @return a <code>PageFlow</code> value
     */
    protected PageFlow getCurrentPageFlow() {
        return currentpageflow;
    }

    /**
     * <code>setCurrentPageFlow</code> sets the currently active PageFlow.
     *
     * @param flow a <code>PageFlow</code> value
     */
    protected void setCurrentPageFlow(PageFlow flow) {
        currentpageflow = flow;
    }

    /**
     * <code>getCurrentPageRequest</code> gets the currently active PageRequest.
     *
     * @return a <code>PageRequest</code> value
     */
    public PageRequest getCurrentPageRequest() {
        return currentpagerequest;
    }

    /**
     * <code>setCurrentPageRequest</code> sets the currently active PageRequest.
     *
     * @param page a <code>PageRequest</code> value
     */
    protected void setCurrentPageRequest(PageRequest page) {
        currentpagerequest = page;
    }

    private void addNavigation(Navigation navi, SPDocument spdoc, PfixServletRequest preq) throws Exception {
        LOG.info(" **** MAKE NAVIGATION !!! ****");
        long     start   = System.currentTimeMillis();
        Document doc     = spdoc.getDocument();
        Element  element = doc.createElement("navigation");
        doc.getDocumentElement().appendChild(element);
        
        StringBuffer debug_buffer = new StringBuffer();
        StringBuffer warn_buffer = new StringBuffer();
        
        if (autoinvalidate_navi) {
            recursePages(navi.getNavigationElements(), element, doc, preq, null, warn_buffer, debug_buffer);
            LOG.info(" **** MADE NEW NAVIGATION !!! **** (" + (System.currentTimeMillis() - start) + "ms)");
        } else {
            recursePages(navi.getNavigationElements(), element, doc, preq, navigation_visible, warn_buffer, debug_buffer);
            LOG.info(" **** REUSING NAVIGATION !!! **** (" + (System.currentTimeMillis() - start) + "ms)");
        }
        
        // print the timing information on all isAccessibles
        if(LOG.isDebugEnabled()) {
            if(debug_buffer.length() > 0) {
                LOG.debug("All isAccessibles which took longer than "+MAXTIME_ISACCESSIBLE_DEBUG+" ms:\n" + debug_buffer.toString());
            }
        }
        
        if(warn_buffer.length() > 0) { 
            LOG.warn("All isAccessibles which took longer than "+MAXTIME_ISACCESSIBLE_WARN+" ms:\n" + warn_buffer.toString());
        }
        
    }

    private void recursePages(NavigationElement[] pages, Element parent,
                              Document doc, PfixServletRequest pfixreq, HashMap vis_map, StringBuffer warn_buffer, StringBuffer debug_buffer) throws Exception {
        for (int i = 0; i < pages.length; i++) {
            NavigationElement page = pages[i];
            String            name = page.getName();
            // LOG.info("====> looking at page " + name);
            PageRequest preq     = new PageRequest(name);
            Element     pageelem = doc.createElement("page");
            parent.appendChild(pageelem);
            pageelem.setAttribute("name", name);
            pageelem.setAttribute("handler", page.getHandler());

            Integer page_vis = null;
            if (vis_map != null) {
                page_vis = (Integer) vis_map.get(page);
            }

            if (page_vis != null) {
                pageelem.setAttribute("visible", "" + page_vis.intValue());
            } else {
                if (preqprops.pageRequestIsDefined(preq)) {
                    // LOG.info("    * found props for page " + name);
                    State       state   = pagemap.getState(preq);
                    // LOG.info("    * found state " + state.getClass() + " for page " + name);
                    PageRequest saved   = getCurrentPageRequest();
                    setCurrentPageRequest(preq);
                    
                    // Get some timing infos about the state's isAccesible method
                    long isaccessible_start =  System.currentTimeMillis();
                    boolean     visible = state.isAccessible(this, pfixreq);
                    long duration = System.currentTimeMillis() - isaccessible_start;  
                    
                    if(duration > MAXTIME_ISACCESSIBLE_WARN) {
                        warn_buffer.append("IsAccessible for state at page '"+preq.getName()+"' took longer than "+MAXTIME_ISACCESSIBLE_WARN+" ms! Duration="+duration+"\n");
                    }
                    
                    if(LOG.isDebugEnabled()) {
                        if(duration > MAXTIME_ISACCESSIBLE_DEBUG) {
                            debug_buffer.append("IsAccessible for state at page '"+preq.getName()+"' took longer than "+MAXTIME_ISACCESSIBLE_DEBUG+" ms! Duration="+duration+"\n");
                        }
                    }
                    
                    // LOG.info("    * state accessible? " + visible);
                    setCurrentPageRequest(saved);
                    if (visible) {
                        pageelem.setAttribute("visible", "1");
                    } else {
                        pageelem.setAttribute("visible", "0");
                    }
                    if (vis_map != null) {
                        vis_map.put(page, new Integer(visible ? 1 : 0));
                    }
                } else {
                    // LOG.info("    * found NO PROPS for page " + name);
                    pageelem.setAttribute("visible", "-1");
                    if (vis_map != null) {
                        vis_map.put(page, new Integer(-1));
                    }
                }
                
                if (page.hasChildren()) {
                    recursePages(page.getChildren(), pageelem, doc, pfixreq, vis_map, warn_buffer, debug_buffer);
                }
            }
        }
    }
    
    /**
     * <code>getCurrentSessionId</code> returns the visit_id.
     *
     * @return <code>String</code> visit_id
     */
    public String getVisitId() throws RuntimeException {
	if ( visit_id != null) {
	    return visit_id;
	} else {
	    throw new RuntimeException("visit_id not set, but asked for!!!!");
	}
    }


    public boolean flowBeforeNeedsData(PfixServletRequest preq) throws Exception {
        PageRequest[] workflow = getCurrentPageFlow().getAllSteps();
        boolean       retval   = false;
        
        PageRequest   saved    = getCurrentPageRequest();
        for (int i = 0; i < workflow.length; i++) {
            PageRequest page = workflow[i];
            if (page.equals(saved)) {
                break;
            } else {
                page.setStatus(PageRequestStatus.WORKFLOW);
                setCurrentPageRequest(page);
                SPDocument document = documentFromCurrentStep(preq, true);
                if (document != null) {
                    retval = true;
                    break;
                }
            }
        }
        setCurrentPageRequest(saved);
        
        return retval;
    }
    
    private SPDocument documentFromFlow(PfixServletRequest preq) throws Exception {
        SPDocument    document = null;
        PageRequest[] workflow;
        
        document = documentFromCurrentStep(preq, false);
        
        if (getCurrentPageFlow() != null) {
            workflow = getCurrentPageFlow().getAllSteps();

            PageRequest saved = getCurrentPageRequest();

            if (document != null) {
                LOG.debug("* [" + getCurrentPageRequest() + "] returned document, skipping workflow.");
            } else {
                LOG.debug("* [" + getCurrentPageRequest() + "] returned 'null', starting workflow process");

                for (int i = 0; i < workflow.length; i++) {
                    PageRequest page = workflow[i];
                    page.setStatus(PageRequestStatus.WORKFLOW);
                    if (page.equals(saved)) {
                        LOG.debug("* Skipping step [" + page + "] in workflow (been there already...)");
                    } else {
                        LOG.debug("* Workflow is at step [" + i + " - " + page + "]");
                        setCurrentPageRequest(page);
                        document = documentFromCurrentStep(preq, true);
                        if (document != null) {
                            LOG.debug("* [" + page + "] returned document, leaving workflow");
                            break;
                        } else {
                            LOG.debug("* [" + page + "] returned 'null', going to next step in workflow");
                        }
                    }
                }

                if (document == null) {
                    PageRequest finalpage = getCurrentPageFlow().getFinalPage();
                    if (finalpage == null) {
                        throw new XMLException("Reached end of Workflow '" + getCurrentPageFlow().getName() +
                                               "' with neither getting a non-null SPDocument or having a FINAL page defined");
                    } else {
                        setCurrentPageRequest(finalpage);
                        setCurrentPageFlow(pageflowmanager.pageFlowToPageRequest(getCurrentPageFlow(), finalpage));
                        finalpage.setStatus(PageRequestStatus.FINAL);
                        document = documentFromCurrentStep(preq, true);
                        if (document != null) {
                            LOG.debug("* [" + finalpage + "] returned document (as FINAL page)");
                        } else {
                            throw new XMLException("[" + finalpage + "] returned 'null' as FINAL - maybe inaccessible?");
                        }
                    }
                }
            }
        } else {
            throw new XMLException("*** ERROR! *** current Stateflow == null!");
        }

        return document;
    }

    /**
     * <code>documentFromCurrentStep</code> handles how to get a SPDocument from the state
     * that is associated (via the properties) to the current PageRequest.
     *
     * @param req a <code>HttpServletRequest</code> value
     * @param skip_on_inaccessible a <code>boolean</code> value
     * @return a <code>SPDocument</code> value
     * @exception Exception if an error occurs
     */
    protected SPDocument documentFromCurrentStep(PfixServletRequest preq, boolean skip_on_inaccessible) throws Exception {
        State state = getPageMap().getState(getCurrentPageRequest());
        if (state == null) {
            LOG.warn("* Can't get a handling state for page " + getCurrentPageRequest().getName());
            SPDocument spdoc = new SPDocument();
            spdoc.setResponseError(HttpServletResponse.SC_NOT_FOUND);
            return spdoc;
        }
        
        LOG.debug("* Classname of handling state: " + state.getClass().getName());
        if (!state.isAccessible(this, preq)) {
            if (skip_on_inaccessible) {
                return null;
            } else {
                LOG.warn("State for page " + getCurrentPageRequest().getName() +
                         " is not accessible! Trying first page of default flow.");
                setCurrentPageFlow(pageflowmanager.getPageFlowByName(properties.getProperty(DEFPROP)));
                setCurrentPageRequest(currentpageflow.getFirstStep());
                state = getPageMap().getState(getCurrentPageRequest());
                if (state == null || !state.isAccessible(this, preq)) {
                    throw new XMLException("Even first state " + state + " of default flow was not accessible! Bailing out.");
                }
            }
        }
        return (state.getDocument(this, preq).getSPDocument());
    }

    public boolean isCurrentPageRequestInCurrentFlow() {
        if (getCurrentPageFlow().containsPageRequest(getCurrentPageRequest())) {
            return true;
        } else {
            return false;
        }
    }
    
    protected void trySettingPageRequestAndFlow(PfixServletRequest preq) {
        PageRequest page = new PageRequest(preq); 
        if (!page.isEmpty()) {
            page.setStatus(PageRequestStatus.DIRECT);
            setCurrentPageRequest(page);
            setCurrentPageFlow(pageflowmanager.pageFlowToPageRequest(getCurrentPageFlow(), page, preq));
            LOG.debug("* Setting currentpagerequest to [" + page.getName() + "]");
            LOG.debug("* Setting currentpageflow to [" + getCurrentPageFlow().getName() + "]");
        } else {
            page = getCurrentPageRequest();
            if (page != null) {
                page.setStatus(PageRequestStatus.REUSE);
                LOG.debug("* Reusing page [" + page.getName() + "]");
                LOG.debug("* Reusing flow [" + getCurrentPageFlow().getName() + "]");
            } else {
                throw new RuntimeException("Don't have a current page to use as output target");
            }
        }
    }

    /**
     * <code>toString</code> tries to give a detailed printed representation of the Context.
     * WARNING: this may be very long!
     *
     * @return a <code>String</code> value
     */
    public String toString() {
        StringBuffer contextbuf = new StringBuffer("\n");
	
	contextbuf.append("     workflow:      " + getCurrentPageFlow()  + "\n");
        contextbuf.append("     PageRequest:   " + getCurrentPageRequest() + "\n"); 
        if (getCurrentPageRequest() != null) { 
            contextbuf.append("       -> State: " + getPageMap().getState(getCurrentPageRequest()) + "\n");
            contextbuf.append("       -> Status: " + getCurrentPageRequest().getStatus() + "\n");
        }
        contextbuf.append("     >>>> ContextResourcen <<<<\n");
        for (Iterator i = rmanager.getResourceIterator(); i.hasNext(); ) {
	    ContextResource res = (ContextResource) i.next();
            contextbuf.append("         " + res.getClass().getName() + ": ");
            contextbuf.append(res.toString() + "\n");
	}
	
	return contextbuf.toString();
    }

    public String getName() {
        return name;
    }
}
