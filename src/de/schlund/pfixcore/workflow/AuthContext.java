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

import de.schlund.pfixxml.*;
import de.schlund.pfixxml.serverutil.*;
import java.util.*;
import javax.servlet.http.*;
import org.apache.log4j.*;

/**
 * @author jtl
 */
public class AuthContext extends Context {
    private static final String AUTH_PROP = "authcontext.authpage";
    protected PageRequest authpage;
    
    private static Category CAT = Category.getInstance(AuthContext.class.getName());

    public void init(Properties properties, ContainerUtil conutil, String name) throws Exception {
        super.init(properties, conutil, name);
	String authpagename = properties.getProperty(AUTH_PROP);
        if (authpagename == null) {
            throw new XMLException("Need Authpage property '" + AUTH_PROP + "' for an AuthContext");
        }
        authpage = new PageRequest(authpagename);
    }

    public SPDocument checkAuthorization(PfixServletRequest preq) throws Exception {
        SPDocument  spdoc = null;
        // Push the authpage to be the current PageRequest
        PageRequest saved = getCurrentPageRequest();
        if (saved.getStatus() == PageRequestStatus.WORKFLOW) {
            authpage.setStatus(PageRequestStatus.AUTH_WORKFLOW);
        } else {
            authpage.setStatus(PageRequestStatus.AUTH);
        }
        setCurrentPageRequest(authpage);
        CAT.debug("-----------------> AUTHPAGE CALLED");
        spdoc = super.documentFromCurrentStep(preq, false);
        CAT.debug("-----------------> BACK FROM AUTHPAGE");
        // Pop back to the nominally current PageRequest
        setCurrentPageRequest(saved);
        return spdoc;
    }
    
    protected SPDocument documentFromCurrentStep(PfixServletRequest preq, boolean skip_on_error) throws Exception {

        SPDocument spdoc = checkAuthorization(preq);
        
        // But only call it if the authentication state returned a null document 
        if (spdoc == null) {
            spdoc = super.documentFromCurrentStep(preq, skip_on_error);
        } else {
            // we need to make sure to associate the right page with the document
            // as the current PageRequest is different from the authpage.
            spdoc.setPagename(authpage.getName());
        }
        return spdoc;
    }

    protected void trySettingPageRequestAndFlow(PfixServletRequest preq) {
        PageRequest page = new PageRequest(preq);
        if (!page.isEmpty() && !page.equals(authpage)) { // never directly set the authpage to be current!
            page.setStatus(PageRequestStatus.DIRECT);
            setCurrentPageRequest(page);
            setCurrentPageFlow(getPageFlowManager().pageFlowToPageRequest(getCurrentPageFlow(), page, preq));
            CAT.debug("* Setting currentpagerequest to [" + page.getName() + "]");
            CAT.debug("* Setting currentpageflow to [" + getCurrentPageFlow().getName() + "]");
        } else {
            page = getCurrentPageRequest();
            if (page != null) {
                page.setStatus(PageRequestStatus.REUSE);
                CAT.debug("* Reusing page [" + page.getName() + "]");
                CAT.debug("* Reusing flow [" + getCurrentPageFlow().getName() + "]");
            } else {
                throw new RuntimeException("Don't have a current page to use as output target");
            }
        }
    }

    public boolean flowIsRunning() {
        if (getCurrentPageRequest().getStatus() == PageRequestStatus.WORKFLOW ||
            getCurrentPageRequest().getStatus() == PageRequestStatus.AUTH_WORKFLOW) {
            return true;
        } else {
            return false;
        }
    }
    
}
