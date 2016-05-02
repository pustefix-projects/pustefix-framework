package de.schlund.pfixxml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pustefixframework.util.LocaleUtils;

public class LanguageInfo {

	private String defaultLanguage;
    private List<String> supportedLanguages = new ArrayList<String>();
    private Map<String, String> languageCodeToLanguage = new HashMap<String, String>();
	
    public void setSupportedLanguages(List<String> supportedLanguages) {
        this.supportedLanguages = supportedLanguages;
        for(String supportedLanguage: supportedLanguages) {
        	languageCodeToLanguage.put(LocaleUtils.getLanguagePart(supportedLanguage), supportedLanguage);
        }
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
