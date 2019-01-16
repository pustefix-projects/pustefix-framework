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
package de.schlund.pfixcore.workflow.context;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.pustefixframework.container.spring.beans.TenantScope;
import org.pustefixframework.http.AbstractPustefixRequestHandler;
import org.pustefixframework.http.AbstractPustefixXMLRequestHandler;
import org.pustefixframework.web.ServletUtils;

import de.schlund.pfixcore.auth.Authentication;
import de.schlund.pfixcore.auth.AuthenticationImpl;
import de.schlund.pfixcore.auth.Role;
import de.schlund.pfixcore.exception.PustefixApplicationException;
import de.schlund.pfixcore.exception.PustefixCoreException;
import de.schlund.pfixcore.util.TokenUtils;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextResourceManager;
import de.schlund.pfixcore.workflow.ContextResourceManagerImpl;
import de.schlund.pfixxml.Tenant;
import de.schlund.pfixxml.Variant;

/**
 * Implementation of the session part of the context used by
 * ContextXMLServlet, DirectOutputServlet and WebServiceServlet. This class
 * should never be directly used by application developers.
 */
public class SessionContextImpl {

    private HttpSession                session;
    private Variant                    variant      = null;
    private Tenant                     tenant;
    private String                     language;
    private String                     visitId      = null;
    private ContextResourceManager     crm;
    private Authentication             authentication;
    private Set<String>                visitedPages = Collections.synchronizedSet(new HashSet<String>());
    private Map<String, String>        tokens;
    private String                     csrfToken;
    private String                     lastFlow;

    public void init(Context context) throws PustefixApplicationException, PustefixCoreException {
        
        synchronized(this) {
            if(authentication == null) {
       
                this.authentication = new AuthenticationImpl(context.getContextConfig().getRoleProvider());
                List<Role> roles = context.getContextConfig().getRoleProvider().getRoles();
                if (roles != null) {
                    for (Role role : roles) {
                        if (role.isInitial())
                            authentication.addRole(role.getName());
                    }
                }
                
                ((ContextResourceManagerImpl)crm).init(context, context.getContextConfig());
            }
        }
    }
    
    public HttpSession getSession() {
        return session;
    }
    
    public void setSession(HttpSession session) {
        this.session = session;
    }

    public ContextResourceManager getContextResourceManager() {
        return crm;
    }

    public void setContextResourceManager(ContextResourceManager crm) {
        this.crm = crm;
    }
    
    public void setLanguage(String langcode) {
        this.language = langcode;
        session.setAttribute(AbstractPustefixRequestHandler.REQUEST_ATTR_LANGUAGE, langcode);
    }

    public String getLanguage() {
        return language;
    }

    public Variant getVariant() {
        return variant;
    }

    public void setVariant(Variant variant) {
        this.variant = variant;
    }
    
    public Tenant getTenant() {
        return tenant;
    }
    
    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
        session.setAttribute(TenantScope.REQUEST_ATTRIBUTE_TENANT, tenant.getName());
    }

    public String getVisitId() {
        if (visitId == null) {
            visitId = (String) session.getAttribute(ServletUtils.SESSION_ATTR_VISIT_ID);
            if (visitId == null) {
                throw new RuntimeException("visit_id not set, but asked for!!!!");
            }
        }
        return visitId;
    }

    public void addVisitedPage(String pagename) {
        visitedPages.add(pagename);
    }

    public boolean isVisitedPage(String pagename) {
        return visitedPages.contains(pagename);
    }

    public void invalidateToken(String tokenName) {
        synchronized (this) {
            if (tokens != null)
                tokens.remove(tokenName);
        }
    }

    public boolean isValidToken(String tokenName, String token) {
        synchronized (this) {
            if (tokens == null)
                return false;
            String storedToken = tokens.get(tokenName);
            return storedToken != null && storedToken.equals(token);
        }
    }

    public String getToken(String tokenName) {
        synchronized (this) {
            if (tokens == null)
                tokens = new LinkedHashMap<String, String>();
            String token = TokenUtils.createRandomToken();
            if (tokens.size() > 25) {
                Iterator<String> it = tokens.keySet().iterator();
                it.next();
                it.remove();
            }
            tokens.put(tokenName, token);
            return token;
        }
    }

    public String getCSRFToken() {
        synchronized (this) {
            if(csrfToken == null) {
                csrfToken = TokenUtils.createSecureRandomToken(32);
            }
            return csrfToken;
        }
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public String getLastFlow() {
        return lastFlow;
    }
    
    public void setLastFlow(String lastFlow) {
        this.lastFlow = lastFlow;
    }
    
    @Override
    public String toString() {
        StringBuffer contextbuf = new StringBuffer("\n");

        contextbuf.append("     >>>> Resources <<<<\n");
        for (Iterator<Object> i = crm.getResourceIterator(); i.hasNext();) {
            Object res = i.next();
            contextbuf.append("         " + res.getClass().getName() + ": ");
            contextbuf.append(res.toString() + "\n");
        }

        return contextbuf.toString();
    }

    public void markSessionForCleanup() {
        this.session.setAttribute(AbstractPustefixXMLRequestHandler.SESS_CLEANUP_FLAG_STAGE1, true);
    }

    public void invalidateSessionAfterCompletion() {
        session.setAttribute(AbstractPustefixRequestHandler.REQUEST_ATTR_INVALIDATE_SESSION_AFTER_COMPLETION, true);
    }

}
