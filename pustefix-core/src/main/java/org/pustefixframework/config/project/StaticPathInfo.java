package org.pustefixframework.config.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Intermediate parsing store for DocrootRequestHandler values 
 *
 */
public class StaticPathInfo {

    private String basePath;
    private boolean i18nBase;
    private String defaultPath;
    private List<String> paths = new ArrayList<String>();
    private Set<String> i18nPaths = new HashSet<String>();
    private Expires defaultExpires = new Expires(Expires.Base.ACCESS, 3600);
    private Map<String, Expires> expiresByType = new HashMap<>();

    public void setBasePath(String basePath, boolean i18nBase) {
        this.basePath = basePath;
        this.i18nBase = i18nBase;
    }
    
    public String getBasePath() {
        return basePath;
    }
    
    public boolean isBaseI18N() {
        return i18nBase;
    }
    
    public void setDefaultPath(String defaultPath) {
        this.defaultPath = defaultPath;
    }
    
    public String getDefaultPath() {
        return defaultPath;
    }
    
    public void addStaticPath(String path, boolean i18n) {
        if(!paths.contains(path)) {
            paths.add(path);
            if(i18n) {
                i18nPaths.add(path);
            }
        }
    }
    
    public List<String> getStaticPaths() {
        return paths;
    }
    
    public Set<String> getI18NPaths() {
        return i18nPaths;
    }

    public void setDefaultExpires(Expires defaultExpires) {
        this.defaultExpires = defaultExpires;
    }

    public Expires getDefaultExpires() {
        return defaultExpires;
    }

    public void addExpires(String type, Expires expires) {
        expiresByType.put(type, expires);
    }

    public Map<String, Expires> getExpiresByType() {
        return expiresByType;
    }

}
