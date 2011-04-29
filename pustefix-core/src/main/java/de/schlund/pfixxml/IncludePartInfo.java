package de.schlund.pfixxml;

import java.util.Set;

public class IncludePartInfo {

    private String name;
    private boolean render;
    private Set<String> renderVariants;
    
    public IncludePartInfo(String name, boolean render, Set<String> renderVariants) {
        this.name = name;
        this.render = render;
        this.renderVariants = renderVariants;
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
    
}
