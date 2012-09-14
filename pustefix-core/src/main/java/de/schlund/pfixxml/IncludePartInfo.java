package de.schlund.pfixxml;

import java.util.HashSet;
import java.util.Set;

public class IncludePartInfo {

    private final String name;
    private final boolean render;
    private final Set<String> renderVariants;
    private final Set<String> themes;
    private final String contentType;
    private final boolean contextual;
    
    public IncludePartInfo(String name, boolean render, Set<String> renderVariants, String contentType, boolean contextual) {
        this.name = name;
        this.render = render;
        this.renderVariants = renderVariants;
        this.themes = new HashSet<String>();
        this.contentType = contentType;
        this.contextual = contextual;
    }
    
    public String getName() {
        return name;
    }
    
    public boolean isRender() {
        return render;
    }
    
    public Set<String> getRenderVariants() {
        return renderVariants;
    }
    
    public Set<String> getThemes() {
        return themes;
    }
    
    void addTheme(String theme) {
        themes.add(theme);
    }
    
    public String getMatchingTheme(String[] themeList) {
        for(String theme: themeList) {
            if(themes.contains(theme)) {
                return theme;
            }
        }
        return null;
    }
    
    public String getContentType() {
    	return contentType;
    }
    
    public boolean isContextual() {
    	return contextual;
    }

}
