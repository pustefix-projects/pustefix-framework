package de.schlund.pfixxml;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Tenant {

    private String name;
    private String defaultLanguage;
    private List<String> supportedLanguages = new ArrayList<String>();
    private Pattern hostPattern;
    
    public Tenant(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDefaultLanguage() {
        if(defaultLanguage == null && supportedLanguages.size() > 0) {
            return supportedLanguages.get(0);
        }
        return defaultLanguage;
    }
    
    public void setDefaultLanguage(String language) {
        this.defaultLanguage = language;
    }
    
    public List<String> getSupportedLanguages() {
        return supportedLanguages;
    }
    
    public void addSupportedLanguage(String language) {
        supportedLanguages.add(language);
    }
    
    public void setHostPattern(String hostPattern) {
        this.hostPattern = Pattern.compile(hostPattern);
    }
    
    public boolean matches(HttpServletRequest req) {
        if(hostPattern != null) {
            String host = req.getServerName();
            if(!hostPattern.matcher(host).matches()) {
                return false;
            }
        }
        System.out.println("HOST_MATCH: "+ req.getServerName() + " "+hostPattern);
        return true;
    }
    
    public void toXML(Element root) {
        Document doc = root.getOwnerDocument();
        Element varElem = doc.createElement("tenant");
        varElem.setAttribute("name", getName());
        for(String lang : supportedLanguages) {
            Element elem = doc.createElement("lang");
            if(lang.equals(getDefaultLanguage())) {
                elem.setAttribute("default", "true");
            }
            elem.setTextContent(lang);
            varElem.appendChild(elem);
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Tenant) {
            return ((Tenant)obj).getName().equals(name);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
    
}
