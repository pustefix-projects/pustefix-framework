package org.pustefixframework.http;

import org.pustefixframework.util.LocaleUtils;

import de.schlund.pfixcore.workflow.SiteMap;

public class PathMapping {

    public static String getURLPath(String pageName, String altKey, String pageGroup, String pageFlow, 
                             String lang, String defaultPage, String defaultLanguage, SiteMap siteMap) {

        //add language prefix
        String prefix = "";
        if(defaultLanguage != null && !lang.equals(defaultLanguage)) {
            prefix = LocaleUtils.getLanguagePart(lang);
        }
        //add page flow prefix
        if(pageFlow != null) {
            if(!prefix.isEmpty()) {
                prefix += "/";
            }
            prefix += siteMap.getPageFlowAlias(pageFlow, lang);
        }
        if(defaultPage != null && defaultPage.equals(pageName)) {
            return prefix;
        } else {
            //get page alias
            String alias = siteMap.getAlias(pageName, lang, altKey, pageGroup);
            if(!prefix.isEmpty()) {
                return prefix + ( alias.isEmpty() ? "" : "/" + alias );
            } else {
                return alias;
            }
        }
    }

}
