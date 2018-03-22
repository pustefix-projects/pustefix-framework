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
package org.pustefixframework.http;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.pustefixframework.container.spring.beans.TenantScope;
import org.pustefixframework.util.URLUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import de.schlund.pfixxml.LanguageInfo;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.Tenant;
import de.schlund.pfixxml.TenantInfo;
import de.schlund.pfixxml.config.EnvironmentProperties;

public class PustefixInitInterceptor implements HandlerInterceptor {

    private final static String DEFAULT_ENCODING = "UTF-8";

    private TenantInfo tenantInfo;
    private LanguageInfo langInfo;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        //Set character encoding of request to default if not already set.
        if(request.getCharacterEncoding() == null) {
            request.setCharacterEncoding(DEFAULT_ENCODING);
        }
        //Set character encoding of response to default if not already set, i.e. if it's set to null or to
        //the Servlet API default value 'ISO-8859-1' (we can't detect if it's intentionally set or the default
        //value applies, so we just override the default, when the request encoding doesn't equal to it).
        if(response.getCharacterEncoding() == null || (response.getCharacterEncoding().equals("ISO-8859-1")
                && !"ISO-8859-1".equals(request.getCharacterEncoding()))) {
            response.setCharacterEncoding(DEFAULT_ENCODING);
        }

        //Simple means of switching to other tenant using URL parameter (during development only).
        String tenantParam = request.getParameter("__tenant");
        if(tenantParam != null && !"prod".equals(EnvironmentProperties.getProperties().getProperty("mode"))) {
            Tenant tenant = tenantInfo.getTenant(tenantParam);
            if(tenant != null) {
                response.addCookie(new Cookie(TenantScope.REQUEST_ATTRIBUTE_TENANT, tenant.getName()));
            }
            HttpSession session = request.getSession(false);
            if(session != null) {
                session.invalidate();
            }
            response.sendRedirect(request.getRequestURL().toString());
            return false;
        }

        if(tenantInfo != null && !tenantInfo.getTenants().isEmpty()) {
            
            Tenant tenant = tenantInfo.getMatchingTenant(request);
            if(tenant == null) {
                //check if tenant was provided as cookie (only allowed at development time)
                if(!"prod".equals(EnvironmentProperties.getProperties().getProperty("mode"))) {
                    Cookie[] cookies = request.getCookies();
                    if(cookies != null) {
                        for(Cookie cookie: cookies) {
                            if(cookie.getName().equals(TenantScope.REQUEST_ATTRIBUTE_TENANT)) {
                                String tenantName = cookie.getValue();
                                if(tenantName != null) {
                                    tenant = tenantInfo.getTenant(tenantName);
                                    break;
                                }
                            }
                        }
                    }
                }
                if(tenant == null) {
                    tenant = tenantInfo.getTenants().get(0);
                }
            }
            request.setAttribute(TenantScope.REQUEST_ATTRIBUTE_TENANT, tenant);
            
            if(tenant.useLangPrefix()) {
                String matchingLanguage = tenant.getDefaultLanguage();
                String pathPrefix = URLUtils.getFirstPathComponent(request.getPathInfo());
                if(pathPrefix != null) {
                    if(tenantInfo.isLanguagePrefix(pathPrefix)) {
                        String language = tenant.getSupportedLanguageByCode(pathPrefix);
                        if(language != null) {
                            matchingLanguage = language;
                        } else {
                            matchingLanguage = tenant.getDefaultLanguage();
                        }
                    }
                }
                request.setAttribute(AbstractPustefixRequestHandler.REQUEST_ATTR_LANGUAGE, matchingLanguage);
            } else if(tenant.getSupportedLanguages().size() == 1) {
                request.setAttribute(AbstractPustefixRequestHandler.REQUEST_ATTR_LANGUAGE, tenant.getDefaultLanguage());
            } else if(tenant.getSupportedLanguages().size() > 1) {
                String lang = null;
                HttpSession session = request.getSession(false);
                if(session != null) {
                    lang = (String)session.getAttribute(AbstractPustefixRequestHandler.REQUEST_ATTR_LANGUAGE);
                }
                if(lang == null) {
                    lang = tenant.getDefaultLanguage();
                }
                request.setAttribute(AbstractPustefixRequestHandler.REQUEST_ATTR_LANGUAGE, lang);
            }
        } else if(langInfo != null && !langInfo.getSupportedLanguages().isEmpty()) {
            String matchingLanguage = langInfo.getDefaultLanguage();
            String pathPrefix = URLUtils.getFirstPathComponent(request.getPathInfo());
            if(pathPrefix != null) {
                String language = langInfo.getSupportedLanguageByCode(pathPrefix);
                if(language != null && !language.equals(langInfo.getDefaultLanguage())) {
                    matchingLanguage = language;
                }
            }
            request.setAttribute(AbstractPustefixRequestHandler.REQUEST_ATTR_LANGUAGE, matchingLanguage);
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {

        request.removeAttribute(AbstractPustefixRequestHandler.REQUEST_ATTR_LANGUAGE);
        request.removeAttribute(TenantScope.REQUEST_ATTRIBUTE_TENANT);

        PfixServletRequest preq = (PfixServletRequest)request.getAttribute(PfixServletRequest.class.getName());
        if(preq != null) {
            preq.resetRequest();
        }
    }

    public void setTenantInfo(TenantInfo tenantInfo) {
        this.tenantInfo = tenantInfo;
    }

    public void setLanguageInfo(LanguageInfo langInfo) {
        this.langInfo = langInfo;
    }

}
