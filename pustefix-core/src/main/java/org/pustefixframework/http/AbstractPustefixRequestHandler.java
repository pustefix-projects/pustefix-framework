/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.pustefixframework.http;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.pustefixframework.config.contextxmlservice.ServletManagerConfig;
import org.pustefixframework.container.spring.beans.TenantScope;
import org.pustefixframework.container.spring.http.UriProvidingHttpRequestHandler;
import org.pustefixframework.web.ServletUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ServletContextAware;

import de.schlund.pfixcore.workflow.PageMap;
import de.schlund.pfixcore.workflow.PageProvider;
import de.schlund.pfixcore.workflow.SiteMap;
import de.schlund.pfixcore.workflow.SiteMap.PageLookupResult;
import de.schlund.pfixxml.LanguageInfo;
import de.schlund.pfixxml.PageAliasResolver;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.PfixServletRequestImpl;
import de.schlund.pfixxml.Tenant;
import de.schlund.pfixxml.TenantInfo;


public abstract class AbstractPustefixRequestHandler implements PageAliasResolver, PageProvider, UriProvidingHttpRequestHandler, ServletContextAware {

    protected Logger LOGGER_SESSION = LoggerFactory.getLogger("LOGGER_SESSION");

    protected static final String DEF_CONTENT_TYPE = "text/html";

    public static final String REQUEST_ATTR_LANGUAGE = "__PFX_LANGUAGE__";
    public static final String REQUEST_ATTR_PAGE_ALTERNATIVE = "__PFX_PAGE_ALTERNATIVE__";
    public static final String REQUEST_ATTR_PAGE_ADDITIONAL_PATH = "__PFX_PAGE_ADDITIONAL_PATH__";
    public static final String REQUEST_ATTR_PAGEFLOW = "__PFX_PAGEFLOW__";
    public static final String REQUEST_ATTR_PAGEGROUP = "__PFX_PAGEGROUP__";
    public static final String REQUEST_ATTR_INVALIDATE_SESSION_AFTER_COMPLETION = "__PFX_INVALIDATE_SESSION_AFTER_COMPLETION__";
    public static final String REQUEST_ATTR_CLIENT_ABORTED = "__PFX_CLIENT_ABORTED__";
    public static final String REQUEST_ATTR_REQUEST_TYPE = "__PFX_REQUEST_TYPE__";
    
    public static enum RequestType { PAGE, RENDER, DIRECT };

    private ServletContext servletContext;
    protected String handlerURI;
    protected TenantInfo tenantInfo;
    protected LanguageInfo languageInfo;
    protected SiteMap siteMap;
    protected PageMap pageMap;
    
    public abstract ServletManagerConfig getServletManagerConfig();

    public boolean needsSSL(PfixServletRequest preq) throws ServletException {
        return this.getServletManagerConfig().isSSL();
    }

    public abstract boolean needsSession();

    public abstract boolean allowSessionCreate();

    public void setHandlerURI(String uri) {
        this.handlerURI = uri;
    }
    
    public String[] getRegisteredURIs() {
        if(handlerURI != null) return new String[] { handlerURI };
        return new String[0];
    }

    public void handleRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        PfixServletRequest preq = new PfixServletRequestImpl(req, getServletManagerConfig().getProperties());

        if(!req.isSecure() && needsSSL(preq)) {
            ServletUtils.redirectToSSL(req, res, HttpServletResponse.SC_TEMPORARY_REDIRECT);
            return;
        }

        if(needsSession()) {
            HttpSession session = req.getSession(false);
            if(session == null) {
                session = req.getSession(true);
                preq.updateRequest(req);
            }
        }

        callProcess(preq, req, res);
    }

    public void callProcess(PfixServletRequest preq, HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        
    	//trigger initialization of page alternative name if not already done
    	preq.getPageName();
        
        HttpSession session = req.getSession(false);
        try {
            res.setContentType(DEF_CONTENT_TYPE);
            if(needsSession() && session != null) {
				ReadWriteLock lock = ServletUtils.getSessionLock(session);
				if(lock != null) {
					Lock readLock = lock.readLock();
					readLock.lock();
					try {
						process(preq, res);
						return;
					} finally {
						readLock.unlock();
					}
				}
            }
            process(preq, res);
        } catch(Exception x) {
            throw new ServletException(x);
        } finally {
            try {
                if (session != null && (session.getAttribute(REQUEST_ATTR_INVALIDATE_SESSION_AFTER_COMPLETION) != null)) {
                    LOGGER_SESSION.info("Invalidate session VII: " + session.getId());
                    session.invalidate();
                }
            } catch(IllegalStateException x) {
                //can be ignored, because session has been already invalidated meanwhile
            }
        }
    }

    protected abstract void process(PfixServletRequest preq, HttpServletResponse res) throws Exception;

    /**
     * Can be overridden by a subclass in order to disable the check
     * whether a session id provided by a request is valid.
     * 
     * @return <code>true</code> if and only if the request handler should
     * check whether the session id is valid for every request
     */
    public boolean wantsCheckSessionIdValid() {
        return true;
    }
    
    @Override
    public String[] getRegisteredPages() {
        return new String[0];
    }
    
    public String getPageName(final String pageAlias, final HttpServletRequest request) {
        
        String pageName = pageAlias;
        
        String prefix;
        int ind = pageName.indexOf('/');
        if(ind > -1) {
            prefix = pageName.substring(0, ind);
        } else {
            prefix = pageName;
        }
        
        //check if pageAlias has language prefix
        Tenant tenant = (Tenant)request.getAttribute(TenantScope.REQUEST_ATTRIBUTE_TENANT);
        if((tenant != null && tenant.useLangPrefix() && tenant.getSupportedLanguageByCode(prefix) != null) ||
            (tenant == null && languageInfo.getSupportedLanguageByCode(prefix) != null)) {
            if(ind > -1) {
                //remove language prefix
                pageName = pageName.substring(ind + 1);
            } else {
                //default page
                return null;
            }
        }    
        
        pageName = resolvePrefix(pageName, request);
        if(pageName == null) {
            return null;
        }
        
        //check page alias
        PageLookupResult res = null;
        String lang = (String)request.getAttribute(REQUEST_ATTR_LANGUAGE);
        if(tenant != null && !tenant.useLangPrefix() && tenant.getSupportedLanguages().size() > 1) {
            res = siteMap.getPageName(pageName, lang, tenant.getSupportedLanguages());
        } else {
            res = siteMap.getPageName(pageName, lang);
        }
        
        if(pageName.startsWith(res.getAliasPageName()) && pageName.length() > res.getAliasPageName().length()) {
            String additionalPath = pageName.substring(pageName.indexOf(res.getAliasPageName()) + res.getAliasPageName().length());
            request.setAttribute(REQUEST_ATTR_PAGE_ADDITIONAL_PATH, additionalPath);
        }
        if(res.getPageAlternativeKey() != null) {
            request.setAttribute(REQUEST_ATTR_PAGE_ALTERNATIVE, res.getPageAlternativeKey());
        }
        if(res.getPageGroup() != null) {
            request.setAttribute(REQUEST_ATTR_PAGEGROUP, res.getPageGroup());
        }
        ind = res.getPageName().indexOf('/');
        if(ind > -1) {
            return res.getPageName().substring(0, ind);
        } else {
            return res.getPageName();
        }
    }

    protected String resolvePrefix(final String pageAlias, final HttpServletRequest request) {
        return pageAlias;
    }
    
    public void setServletContext(ServletContext context) {
        this.servletContext = context;
    }
    
    public ServletContext getServletContext() {
        return this.servletContext;
    }

    public void setTenantInfo(TenantInfo tenantInfo) {
        this.tenantInfo = tenantInfo;
    }

    public void setLanguageInfo(LanguageInfo languageInfo) {
        this.languageInfo = languageInfo;
    }
    
    public void setSiteMap(SiteMap siteMap) {
        this.siteMap = siteMap;
    }
    
    public void setPageMap(PageMap pageMap) {
        this.pageMap = pageMap;
    }
}
