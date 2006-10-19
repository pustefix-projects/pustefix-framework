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
 */

package de.schlund.pfixcore.workflow.context;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.http.HttpSession;

import de.schlund.pfixcore.workflow.ContextImpl;
import de.schlund.pfixcore.workflow.ContextResource;
import de.schlund.pfixcore.workflow.ContextResourceManager;
import de.schlund.pfixcore.workflow.RequestContextImpl;
import de.schlund.pfixxml.AbstractXMLServer;
import de.schlund.pfixxml.ServletManager;
import de.schlund.pfixxml.Variant;

/**
 * Implementation of the session part of the context used by ContextXMLServer,
 * DirectOutputServer and WebServiceServlet. This class should never be directly
 * used by application developers.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class SessionContextImpl implements SessionContext {
    private HttpSession session;
    
    private String lastPageName = null;
    private String lastPageFlowName = null;
    
    private Variant variant = null;
    private String visitId = null;
    private ContextResourceManager crm;
    
    // private Map<NavigationElement, Integer> navigationMap = new HashMap<NavigationElement, Integer>();
    private Set<String> visitedPages = Collections.synchronizedSet(new HashSet<String>());
    
    public SessionContextImpl(ServerContextImpl context, ContextImpl scontext, HttpSession session) throws Exception {
        this.session = session;
        this.crm = new ContextResourceManager();
        // We need a dummy request context during initialization
        scontext.setRequestContextForCurrentThread(new RequestContextImpl(context, this, scontext));
        try {
            crm.init(scontext, scontext.getContextConfig());
        } finally {
            scontext.setRequestContextForCurrentThread(null);
        }
    }
    
    public SessionContextImpl(ServerContextImpl context, HttpSession session) throws Exception {
        this.session = session;
        this.crm = new ContextResourceManager();
        crm.init(new RequestContextImpl(context, this), context.getContextConfig());
    }



    public ContextResourceManager getContextResourceManager() {
        return crm;
    }

    public void setLanguage(String langcode) {
        session.setAttribute(AbstractXMLServer.SESS_LANG, langcode);
    }

    public String getLanguage() {
        try {
            return (String) session.getAttribute(AbstractXMLServer.SESS_LANG);
        } catch (IllegalStateException e) {
            // May be thrown if session has been invalidated
            return null;
        }
    }

    public Variant getVariant() {
        return variant;
    }

    public void setVariant(Variant variant) {
        this.variant = variant;
    }

    public String getVisitId() {
        if (visitId == null) {
            visitId = (String) session.getAttribute(ServletManager.VISIT_ID);
            if (visitId == null) {
                throw new RuntimeException("visit_id not set, but asked for!!!!");
            }
        }
        return visitId;
    }
    
    public String getLastPageFlowName() {
        return lastPageFlowName;
    }

    public void setLastPageFlowName(String lastPageFlowName) {
        this.lastPageFlowName = lastPageFlowName;
    }

    public String getLastPageName() {
        return lastPageName;
    }

    public void setLastPageName(String lastPageName) {
        this.lastPageName = lastPageName;
    }
    
    // public Map<NavigationElement, Integer> getNavigation() {
    //     synchronized (navigationMap) {
    //         return new HashMap<NavigationElement, Integer>(navigationMap);
    //     }
    // }
    
    // public boolean navigationNeedsRefresh() {
    //     synchronized (navigationMap) {
    //         if (navigationMap.size() == 0) {
    //             return true;
    //         } else {
    //             return false;
    //         }
    //     }
    // }
    
    // public void refreshNavigation(Navigation navi, AccessibilityChecker checker) {
    //     synchronized (navigationMap) {
    //         navigationMap.clear();
    //         recurseNavigation(navi.getNavigationElements(), checker);
    //     }
    // }
    // 
    // private void recurseNavigation(NavigationElement[] pages, AccessibilityChecker checker) {
    //     for (int i = 0; i < pages.length; i++) {
    //         NavigationElement page = pages[i];
    //         try {
    //             if (checker.isPageAccessible(page.getName())) {
    //                 navigationMap.put(page, 1);
    //             } else {
    //                 navigationMap.put(page, 0);
    //             }
    //         } catch (Exception e) {
    //             // This is not totally clean as there might be other causes
    //             // for exceptions than a non-existing page, but it should
    //             // work for most cases
    //             navigationMap.put(page, -1);
    //         }
    //         if (page.hasChildren()) {
    //             recurseNavigation(page.getChildren(), checker);
    //         }
    //     }
    // }
    
    public void addVisitedPage(String pagename) {
        visitedPages.add(pagename);
    }
    
    public boolean isVisitedPage(String pagename) {
        return visitedPages.contains(pagename);
    }
    
    public String toString() {
        StringBuffer contextbuf = new StringBuffer("\n");

        contextbuf.append("     >>>> ContextResourcen <<<<\n");
        for (Iterator i = crm.getResourceIterator(); i.hasNext(); ) {
            ContextResource res = (ContextResource) i.next();
            contextbuf.append("         " + res.getClass().getName() + ": ");
            contextbuf.append(res.toString() + "\n");
        }

        return contextbuf.toString();
    }
}
