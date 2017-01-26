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
package org.pustefixframework.web.servlet.i18n;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pustefixframework.http.AbstractPustefixRequestHandler;
import org.pustefixframework.util.LocaleUtils;
import org.springframework.web.servlet.LocaleResolver;

import de.schlund.pfixxml.LanguageInfo;
import de.schlund.pfixxml.Tenant;
import de.schlund.pfixxml.TenantInfo;

public class PustefixLocaleResolver implements LocaleResolver {

    public final static String LOCALE_ATTRIBUTE_NAME = "__PFX_LOCALE__";
    
    private TenantInfo tenantInfo;
    private LanguageInfo langInfo;
    
    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        
        Locale locale = (Locale)request.getAttribute(LOCALE_ATTRIBUTE_NAME);
        if(locale == null) {
            String language = (String)request.getAttribute(AbstractPustefixRequestHandler.REQUEST_ATTR_LANGUAGE);
            if(language != null) {
                locale = LocaleUtils.getLocale(language);
                request.setAttribute(LOCALE_ATTRIBUTE_NAME, locale);
            }
        }
        return locale;
    }
    
    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {

        if(isSupportedLocale(locale, request)) {
            request.setAttribute(LOCALE_ATTRIBUTE_NAME, locale);
            request.setAttribute(AbstractPustefixRequestHandler.REQUEST_ATTR_LANGUAGE, LocaleUtils.getShortString(locale));
        }
    }

    private boolean isSupportedLocale(Locale locale, HttpServletRequest request) {
        String lang = LocaleUtils.getShortString(locale);
        if(tenantInfo != null && !tenantInfo.getTenants().isEmpty()) {
            Tenant tenant = tenantInfo.getTenant(request);
            if(tenant != null) {
                for(String supportedLang : tenant.getSupportedLanguages()) {
                    if(supportedLang.equals(lang)) {
                        return true;
                    }
                }
            }
        } else if(langInfo != null && !langInfo.getSupportedLanguages().isEmpty()) {
            for(String supportedLang : langInfo.getSupportedLanguages()) {
                if(supportedLang.equals(lang)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setTenantInfo(TenantInfo tenantInfo) {
        this.tenantInfo = tenantInfo;
    }

    public void setLanguageInfo(LanguageInfo langInfo) {
        this.langInfo = langInfo;
    }

}
