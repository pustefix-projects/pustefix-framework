package de.schlund.pfixxml;

import java.util.Set;

public class IncludePartsInfo {
    
    private Set<String> parts;
    private long lastMod;
    
    public void setParts(Set<String> parts) {
        this.parts = parts;
    }
    
    public Set<String> getParts() {
        return parts;
    }
    
    public void setLastMod(long lastMod) {
        this.lastMod = lastMod;
    }
    
    public long getLastMod() {
        return lastMod;
    }
    
}
