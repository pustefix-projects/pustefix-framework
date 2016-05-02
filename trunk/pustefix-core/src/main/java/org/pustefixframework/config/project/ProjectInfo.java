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
package org.pustefixframework.config.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pustefixframework.util.LocaleUtils;


/**
 * 
 * @author mleidig@schlund.de
 *
 */
public class ProjectInfo {
    
    private String projectName = "noname";
    private String definingModule;
    private String defaultLanguage;
    private List<String> supportedLanguages = new ArrayList<String>();
    private Map<String, String> languageCodeToLanguage = new HashMap<String, String>();
  
    public ProjectInfo(String definingModule) {
        this.definingModule = definingModule;
    }
    
    public String getProjectName() {
        return projectName;
    }
    
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    
    public String getDefiningModule() {
        return definingModule;
    }
    
    public void addSupportedLanguage(String language) {
        supportedLanguages.add(language);
        languageCodeToLanguage.put(LocaleUtils.getLanguagePart(language), language);
    }
    
    public List<String> getSupportedLanguages() {
        return supportedLanguages;
    }
    
    public String getSupportedLanguageByCode(String languageCode) {
        return languageCodeToLanguage.get(languageCode);
    }
    
    public void setDefaultLanguage(String language) {
        this.defaultLanguage = language;
    }
    
    public String getDefaultLanguage() {
        if(defaultLanguage == null && supportedLanguages.size() > 0) {
            return supportedLanguages.get(0);
        }
        return defaultLanguage;
    }
    
}
