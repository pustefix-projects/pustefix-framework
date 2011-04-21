package de.schlund.pfixxml;

import java.util.Set;

public class IncludePartsInfo {
    
    private Set<String> parts;
    private Set<String> renderParts;
    private long lastMod;
    
    public void setParts(Set<String> parts) {
        this.parts = parts;
    }
    
    public void setRenderParts(Set<String> renderParts) {
        this.renderParts = renderParts;
    }
    
    public Set<String> getParts() {
        return parts;
    }
    
    public Set<String> getRenderParts() {
        return renderParts;
    }
    
    public void setLastMod(long lastMod) {
        this.lastMod = lastMod;
    }
    
    public long getLastMod() {
        return lastMod;
    }
    
}
