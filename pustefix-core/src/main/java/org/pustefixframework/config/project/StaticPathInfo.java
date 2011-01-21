package org.pustefixframework.config.project;

import java.util.ArrayList;
import java.util.List;

/**
 * Intermediate parsing store for DocrootRequestHandler values 
 * 
 * @author mleidig@schlund.de
 *
 */
public class StaticPathInfo {

    private String basePath;
    private String defaultPath;
    private List<String> paths = new ArrayList<String>();
    
    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }
    
    public String getBasePath() {
        return basePath;
    }
    
    public void setDefaultPath(String defaultPath) {
        this.defaultPath = defaultPath;
    }
    
    public String getDefaultPath() {
        return defaultPath;
    }
    
    public void addStaticPath(String path) {
        if(!paths.contains(path)) paths.add(path);
    }
    
    public List<String> getStaticPaths() {
        return paths;
    }
    
}
