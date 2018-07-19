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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.regex.Pattern;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.SessionCookieConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.pustefixframework.config.contextxmlservice.ServletManagerConfig;
import org.pustefixframework.container.spring.beans.TenantScope;
import org.pustefixframework.container.spring.http.UriProvidingHttpRequestHandler;
import org.pustefixframework.util.NetUtils;
import org.pustefixframework.util.net.IPRangeMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ServletContextAware;

import de.schlund.pfixcore.workflow.PageMap;
import de.schlund.pfixcore.workflow.PageProvider;
import de.schlund.pfixcore.workflow.SiteMap;
import de.schlund.pfixcore.workflow.SiteMap.PageLookupResult;
import de.schlund.pfixxml.LanguageInfo;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.PfixServletRequestImpl;
import de.schlund.pfixxml.Tenant;
import de.schlund.pfixxml.TenantInfo;
import de.schlund.pfixxml.serverutil.SessionAdmin;

/**
 * ServletManager.java
 *
 *
 * Created: Wed May  8 16:39:06 2002
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 */

public abstract class AbstractPustefixRequestHandler implements PageProvider, SessionTrackingStrategyContext, UriProvidingHttpRequestHandler, ServletContextAware {

    protected Logger LOGGER_SESSION = LoggerFactory.getLogger("LOGGER_SESSION");
    
    public static final String DEFAULT_SESSION_COOKIE_NAME = "JSESSIONID";
    
    public static final String           VISIT_ID                      = "__VISIT_ID__";
    public static final String           PROP_LOADINDEX                = "__PROPERTIES_LOAD_INDEX";
    
    public static final String           PROP_COOKIE_SEC_NOT_ENFORCED  = "servletmanager.cookie_security_not_enforced";
    public static final String           PROP_SSL_REDIRECT_PORT        = "pfixcore.ssl_redirect_port.for.";
    public static final String           PROP_NONSSL_REDIRECT_PORT     = "pfixcore.nonssl_redirect_port.for.";
    protected static final String        DEF_CONTENT_TYPE              = "text/html";
    
    public static final String SESSION_ATTR_COOKIE_SESSION = "__PFX_SESSION_FROM_COOKIE__";
    
    public static final String REQUEST_ATTR_LANGUAGE = "__PFX_LANGUAGE__";
    public static final String REQUEST_ATTR_PAGE_ALTERNATIVE = "__PFX_PAGE_ALTERNATIVE__";
    public static final String REQUEST_ATTR_PAGE_ADDITIONAL_PATH = "__PFX_PAGE_ADDITIONAL_PATH__";
    public static final String REQUEST_ATTR_PAGEFLOW = "__PFX_PAGEFLOW__";
    public static final String REQUEST_ATTR_PAGEGROUP = "__PFX_PAGEGROUP__";
    public static final String REQUEST_ATTR_INVALIDATE_SESSION_AFTER_COMPLETION = "__PFX_INVALIDATE_SESSION_AFTER_COMPLETION__";
    public static final String REQUEST_ATTR_CLIENT_ABORTED = "__PFX_CLIENT_ABORTED__";
    public static final String REQUEST_ATTR_REQUEST_TYPE = "__PFX_REQUEST_TYPE__";
    
    public static enum RequestType { PAGE, RENDER, DIRECT };
    
    private static final IPRangeMatcher privateIPRange = new IPRangeMatcher("10.0.0.0/8", "169.254.0.0/16", 
            "172.16.0.0/12", "192.168.0.0/16", "fc00::/7");

    private static final Logger LOG = LoggerFactory.getLogger(AbstractPustefixRequestHandler.class);
    private ServletContext servletContext;
    protected String handlerURI;
    private SessionAdmin sessionAdmin;
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

    public static String getSessionCookieName(HttpServletRequest req) {
        SessionCookieConfig cookieConfig = req.getServletContext().getSessionCookieConfig();
        if(cookieConfig != null) {
            if(cookieConfig.getName() != null) {
                return cookieConfig.getName();
            }
        }
        return DEFAULT_SESSION_COOKIE_NAME;
    }
    
    public static String getServerName(HttpServletRequest req) {
        String forward = req.getHeader("X-Forwarded-Server");
        if (forward != null && !forward.equals("")) {
            return forward;
        } else {
            return req.getServerName();
        }
    }
    
    public static String getRemoteAddr(HttpServletRequest req) {
        String remoteIp = req.getRemoteAddr();
        String forward = req.getHeader("X-Forwarded-For");
        if (forward != null && !forward.equals("")) {
            if(privateIPRange.matches(remoteIp)) {
                String[] ips = forward.split(",");
                for(int i=ips.length - 1; i >= 0; i--) {
                    String ip = ips[i].trim();
                    if(ip.length() > 0) {
                        if(NetUtils.checkIP(ip) && !privateIPRange.matches(ip)) {
                            remoteIp = ip;
                            break;
                        }
                    }
                }
            }
        }
        return remoteIp;
    }
    
    public static int getSSLRedirectPort(int port, Properties props) {
        String redirectPort = (String)props.get(PROP_SSL_REDIRECT_PORT + String.valueOf(port));
        if(redirectPort == null) {
            //try to get SSL redirect port from Tomcat MBean
            try {
                MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
                ObjectName objNamePattern = new ObjectName("*:type=Connector,port=" + port + ",*");
                Set<ObjectName> objNames= mbeanServer.queryNames(objNamePattern, null);
                if(objNames.size() == 1) {
                    ObjectName objName = objNames.iterator().next();
                    Integer targetPort = (Integer)mbeanServer.getAttribute(objName, "redirectPort");
                    redirectPort = String.valueOf(targetPort);
                    props.setProperty(PROP_SSL_REDIRECT_PORT + String.valueOf(port), redirectPort);
                }
            } catch(JMException x) {
                LOG.warn("Error getting redirect port from Tomcat connector", x);
            }
            //if not found use default port
            if(redirectPort == null) {
                redirectPort = "443";
                props.put(PROP_SSL_REDIRECT_PORT + String.valueOf(port), redirectPort);
            }
        }
        return Integer.valueOf(redirectPort);
    }
    
    public static int getNonSSLRedirectPort(int port, Properties props) {
        String redirectPort = props.getProperty(AbstractPustefixRequestHandler.PROP_NONSSL_REDIRECT_PORT + String.valueOf(port));
        if(redirectPort == null) {
            Enumeration<?> propNames = props.propertyNames();
            String mappedPort = null;
            while(propNames.hasMoreElements() && mappedPort == null) {
                String propName = (String)propNames.nextElement();
                if(propName.startsWith(AbstractPustefixRequestHandler.PROP_SSL_REDIRECT_PORT)) {
                    int ind = propName.lastIndexOf('.');
                    if(ind > -1) {
                        String portKey = propName.substring(ind + 1);
                        String portVal = props.getProperty(propName);
                        if(portVal.equals(String.valueOf(port))) {
                            redirectPort = portKey;
                            props.put(PROP_NONSSL_REDIRECT_PORT + String.valueOf(port), redirectPort);
                        }
                    }
                }
            }
            //if not found use default port
            if(redirectPort == null) {
                redirectPort = "80";
                props.put(PROP_NONSSL_REDIRECT_PORT + String.valueOf(port), redirectPort);
            }
        }
        return Integer.valueOf(redirectPort);
    }
    
    public void setHandlerURI(String uri) {
        this.handlerURI = uri;
    }
    
    public String[] getRegisteredURIs() {
        if(handlerURI != null) return new String[] { handlerURI };
        return new String[0];
    }

    public void handleRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        PfixServletRequest preq = (PfixServletRequest)req.getAttribute(PfixServletRequest.class.getName());
        if(preq == null) {
            preq = new PfixServletRequestImpl(req, getServletManagerConfig().getProperties());
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
				ReadWriteLock lock = (ReadWriteLock)session.getAttribute(SessionUtils.SESSION_ATTR_LOCK);
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

    protected static List<Pattern> getBotPatterns() {
        List<Pattern> patterns = new ArrayList<Pattern>();
        try {
            Enumeration<URL> urls = AbstractPustefixRequestHandler.class.getClassLoader().getResources("META-INF/org/pustefixframework/http/bot-user-agents.txt");
            while(urls.hasMoreElements()) {
                URL url = urls.nextElement();
                InputStream in = url.openStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, "utf8"));
                String line;
                while((line = reader.readLine()) != null) {
                    line = line.trim();
                    if(!line.startsWith("#")) {
                        Pattern pattern = Pattern.compile(line);
                        patterns.add(pattern);
                    }
                }
                in.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading bot user-agent configuration", e);
        }
        return patterns;
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

    public static void relocate(HttpServletResponse res, String reloc_url) {
        relocate(res, HttpServletResponse.SC_MOVED_TEMPORARILY, reloc_url);
    }
    
    public static void relocate(HttpServletResponse res, int type, String reloc_url) {
        LOG.debug("\n\n        ======> relocating to " + reloc_url + "\n");
        res.setHeader("Expires", "Mon, 26 Jul 1997 05:00:00 GMT");
        res.setHeader("Pragma", "no-cache");
        res.setHeader("Cache-Control", "no-cache, no-store, private, must-revalidate");
        res.setStatus(type);
        res.setHeader("Location", reloc_url);
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
    
    public void setSessionAdmin(SessionAdmin sessionAdmin) {
        this.sessionAdmin = sessionAdmin;
    }
    
    public SessionAdmin getSessionAdmin() {
        return sessionAdmin;
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
