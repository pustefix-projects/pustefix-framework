package org.pustefixframework.http;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.pustefixframework.util.URLUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

import de.schlund.pfixxml.LanguageInfo;
import de.schlund.pfixxml.Tenant;
import de.schlund.pfixxml.TenantInfo;

/**
 * Detect language/tenant based on requested host and URL path 
 * and store them as request attributes, thus making them available
 * to other interceptors or request handlers.
 */
public class InitializingWebRequestInterceptor implements WebRequestInterceptor {
	
	private Logger LOG = Logger.getLogger(InitializingWebRequestInterceptor.class);
	
	private TenantInfo tenantInfo;
	private LanguageInfo languageInfo;
	
	public void setTenantInfo(TenantInfo tenantInfo) {
		this.tenantInfo = tenantInfo;
	}
	
	public void setLanguageInfo(LanguageInfo languageInfo) {
		this.languageInfo = languageInfo;
	}
	
	@Override
	public void preHandle(WebRequest request) throws Exception {
		HttpServletRequest req = ((ServletWebRequest)request).getRequest();
        if(tenantInfo != null && !tenantInfo.getTenants().isEmpty()) {
            Tenant matchingTenant = tenantInfo.getMatchingTenant(req);
            if(matchingTenant == null) {
                matchingTenant = tenantInfo.getTenants().get(0);
            }
            req.setAttribute(AbstractPustefixRequestHandler.REQUEST_ATTR_TENANT, matchingTenant);
            if(LOG.isDebugEnabled()) {
            	LOG.debug("Set tenant " + matchingTenant.getName());
            }
            String matchingLanguage = matchingTenant.getDefaultLanguage();
            String pathPrefix = URLUtils.getFirstPathComponent(req.getPathInfo());
            if(pathPrefix != null) {
                if(tenantInfo.isLanguagePrefix(pathPrefix)) {
                    String language = matchingTenant.getSupportedLanguageByCode(pathPrefix);
                    if(language != null) {
                        matchingLanguage = language;
                    } else {
                        matchingLanguage = matchingTenant.getDefaultLanguage();
                    }
                }
            }
            req.setAttribute(AbstractPustefixRequestHandler.REQUEST_ATTR_LANGUAGE, matchingLanguage);
            if(LOG.isDebugEnabled()) {
            	LOG.debug("Set language " + matchingLanguage);
            }
        } else if(languageInfo != null && !languageInfo.getSupportedLanguages().isEmpty()) {
            String matchingLanguage = languageInfo.getDefaultLanguage();
            String pathPrefix = URLUtils.getFirstPathComponent(req.getPathInfo());
            if(pathPrefix != null) {
                String language = languageInfo.getSupportedLanguageByCode(pathPrefix);
                if(language != null && !language.equals(languageInfo.getDefaultLanguage())) {
                    matchingLanguage = language;
                }
            }
            req.setAttribute(AbstractPustefixRequestHandler.REQUEST_ATTR_LANGUAGE, matchingLanguage);
        }
	}
	
	@Override
	public void postHandle(WebRequest request, ModelMap model) throws Exception {
		//do nothing
	}
	
	@Override
	public void afterCompletion(WebRequest request, Exception ex) throws Exception {
		//do nothing
	}
	
}
