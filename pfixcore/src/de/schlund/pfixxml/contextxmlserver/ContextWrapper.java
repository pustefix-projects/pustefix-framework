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

package de.schlund.pfixxml.contextxmlserver;

import java.util.Properties;

import javax.servlet.http.Cookie;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextResource;
import de.schlund.pfixcore.workflow.ContextResourceManager;
import de.schlund.pfixcore.workflow.PageFlow;
import de.schlund.pfixcore.workflow.PageRequest;
import de.schlund.pfixxml.Variant;
import de.schlund.pfixxml.config.ContextConfig;
import de.schlund.pfixxml.config.PageRequestConfig;
import de.schlund.util.statuscodes.StatusCode;

/**
 * Wraps the splitted context implementations in a way that is compatible with
 * the old pattern.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ContextWrapper implements Context {
    private ServerContextImpl context;
    private SessionContextImpl scontext;
    private RequestContextImpl rcontext;
    
    public ContextWrapper(ServerContextImpl context, SessionContextImpl scontext, RequestContextImpl rcontext) {
        this.context = context;
        this.scontext = scontext;
        this.rcontext = rcontext;
    }

    public ContextResourceManager getContextResourceManager() {
        if (scontext == null) {
            throw new IllegalStateException("ContextResourceManager is only availabe within a session");
        }
        return scontext.getContextResourceManager();
    }

    public Properties getProperties() {
        return context.getProperties();
    }

    public Properties getPropertiesForCurrentPageRequest() {
        PageRequestConfig pconf = getConfigForCurrentPageRequest();
        if (pconf == null) {
            return null;
        }
        return pconf.getProperties();
    }

    public PageRequestConfig getConfigForCurrentPageRequest() {
        if (rcontext == null) {
            throw new IllegalStateException("PageRequest is only available witihin request handling");
        }
        return context.getContextConfig().getPageRequestConfig(rcontext.getPageRequest().getName());
    }

    public PageRequest getCurrentPageRequest() {
        if (rcontext == null) {
            throw new IllegalStateException("PageRequest is only available witihin request handling");
        }
        return rcontext.getPageRequest();
    }

    public PageFlow getCurrentPageFlow() {
        if (rcontext == null) {
            throw new IllegalStateException("PageFlow is only available witihin request handling");
        }
        return rcontext.getPageFlow();
    }

    public void setPageFlow(String pageflow) {
        if (rcontext == null) {
            throw new IllegalStateException("PageFlow is only available witihin request handling");
        }
        rcontext.setPageFlow(pageflow);
    }

    public void setJumpToPageRequest(String pagename) {
        if (rcontext == null) {
            throw new IllegalStateException("JumpToPage is only available witihin request handling");
        }
        rcontext.setJumpToPage(pagename);
    }

    public void setJumpToPageFlow(String pageflow) {
        if (rcontext == null) {
            throw new IllegalStateException("JumpToPageFlow is only available witihin request handling");
        }
        rcontext.setJumpToPageFlow(pageflow);
    }

    public void prohibitContinue() {
        if (rcontext == null) {
            throw new IllegalStateException("PageFlow handling is only available witihin request handling");
        }
        rcontext.prohibitContinue();
    }

    public void invalidateNavigation() {
        if (rcontext == null) {
            throw new IllegalStateException("Request handling is only available witihin request handling");
        }
        rcontext.invalidateNavigation();
    }

    public Cookie[] getRequestCookies() {
        if (rcontext == null) {
            throw new IllegalStateException("Cookies are only available witihin request handling");
        }
        return rcontext.getCookies();
    }

    public void setLanguage(String lang) {
        if (rcontext != null) {
            rcontext.setLanguage(lang);
        } else if (scontext != null) {
            scontext.setLanguage(lang);
        } else {
            throw new IllegalStateException("A request or at least a session has to be present for language handling");
        }
    }

    public void addCookie(Cookie cookie) {
        if (rcontext == null) {
            throw new IllegalStateException("Cookies are only available witihin request handling");
        }
        rcontext.addCookie(cookie);
    }

    public Variant getVariant() {
        if (rcontext != null) {
            return rcontext.getVariant();
        } else if (scontext != null) {
            return scontext.getVariant();
        } else {
            throw new IllegalStateException("A request or at least a session has to be present for variant handling");
        }
    }

    public void setVariant(Variant variant) {
        if (rcontext != null) {
            rcontext.setVariant(variant);
        } else if (scontext != null) {
            scontext.setVariant(variant);
        } else {
            throw new IllegalStateException("A request or at least a session has to be present for variant handling");
        }
    }

    public void setVariantForThisRequestOnly(Variant variant) {
        if (rcontext == null) {
            throw new IllegalStateException("This feature is only available during request handling");
        }
    }

    public String getVisitId() {
        if (scontext == null) {
            throw new IllegalStateException("Session is needed for visit id");
        }
        return scontext.getVisitId();
    }

    public boolean flowBeforeNeedsData() throws Exception {
        if (rcontext == null) {
            throw new IllegalStateException("PageFlow information is only availabe during request handling");
        }
        return rcontext.flowStepsBeforeCurrentStepNeedData();
    }

    public boolean finalPageIsRunning() {
        if (rcontext == null) {
            throw new IllegalStateException("PageFlow information is only availabe during request handling");
        }
        return rcontext.finalPageIsRunning();
    }

    public boolean jumpToPageIsRunning() {
        if (rcontext == null) {
            throw new IllegalStateException("PageFlow information is only availabe during request handling");
        }
        return rcontext.jumpToPageIsRunning();
    }

    public boolean flowIsRunning() {
        if (rcontext == null) {
            throw new IllegalStateException("PageFlow information is only availabe during request handling");
        }
        return rcontext.flowIsRunning();
    }

    public boolean isCurrentPageRequestInCurrentFlow() {
        if (rcontext == null) {
            throw new IllegalStateException("PageFlow information is only availabe during request handling");
        }
        return rcontext.isCurrentPageRequestInCurrentFlow();
    }

    public boolean isCurrentPageFlowRequestedByUser() {
        if (rcontext == null) {
            throw new IllegalStateException("PageFlow information is only availabe during request handling");
        }
        return rcontext.isCurrentPageFlowRequestedByUser();
    }

    public boolean isJumptToPageSet() {
        if (rcontext == null) {
            throw new IllegalStateException("PageFlow information is only availabe during request handling");
        }
        return (rcontext.getJumpToPage() != null);
    }

    public boolean isJumptToPageFlowSet() {
        if (rcontext == null) {
            throw new IllegalStateException("PageFlow information is only availabe during request handling");
        }
        return (rcontext.getJumpToPageFlow() != null);
    }

    public boolean isProhibitContinueSet() {
        if (rcontext == null) {
            throw new IllegalStateException("PageFlow information is only availabe during request handling");
        }
        return rcontext.isProhibitContinue();
    }

    public void setAutoinvalidateNavigationForThisRequestOnly(boolean invalidate) {
        if (rcontext == null) {
            throw new IllegalStateException("This method is only available during request handling");
        }
        if (invalidate == true) {
            throw new IllegalArgumentException("This method is only intended to be called with the argument \"false\"");
        }
        rcontext.reuseNavigation();
    }

    public boolean stateMustSupplyFullDocument() {
        if (rcontext == null) {
            throw new IllegalStateException("PageFlow information is only availabe during request handling");
        }
        return rcontext.stateMustSupplyFullDocument();
    }

    public String getName() {
        return context.getName();
    }

    public Throwable getLastException() {
        if (rcontext == null) {
            throw new IllegalStateException("This method is only available during request processing");
        }
        return rcontext.getLastException();
    }

    public void addPageMessage(StatusCode scode) {
        if (rcontext == null) {
            throw new IllegalStateException("PageMessages are only availabe during request handling");
        }
        rcontext.addPageMessage(scode);
    }

    public void addPageMessage(StatusCode scode, String level) {
        if (rcontext == null) {
            throw new IllegalStateException("PageMessages are only availabe during request handling");
        }
        rcontext.addPageMessage(scode, level);
    }

    public void addPageMessage(StatusCode scode, String[] args) {
        if (rcontext == null) {
            throw new IllegalStateException("PageMessages are only availabe during request handling");
        }
        rcontext.addPageMessage(scode, args);
    }

    public void addPageMessage(StatusCode scode, String[] args, String level) {
        if (rcontext == null) {
            throw new IllegalStateException("PageMessages are only availabe during request handling");
        }
        rcontext.addPageMessage(scode, args, level);
    }

    public Properties getPropertiesForContextResource(ContextResource res) {
        return context.getPropertiesForContextResource(res);
    }

    public ContextConfig getContextConfig() {
        return context.getContextConfig();
    }
    
    public String toString() {
        StringBuffer contextbuf = new StringBuffer("\n");

        if (rcontext != null) {
            contextbuf.append(rcontext.toString());
        }
        if (scontext != null) {
            contextbuf.append(scontext.toString());
        }

        return contextbuf.toString();
    }

}
