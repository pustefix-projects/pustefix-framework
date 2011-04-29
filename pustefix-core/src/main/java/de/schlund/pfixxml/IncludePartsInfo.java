package de.schlund.pfixxml;

import java.util.Map;

public class IncludePartsInfo {
    
    private Map<String, IncludePartInfo> parts;
    private long lastMod;
    
    public void setParts(Map<String, IncludePartInfo> parts) {
        this.parts = parts;
    }
    
    public Map<String, IncludePartInfo> getParts() {
        return parts;
    }
    
    public void setLastMod(long lastMod) {
        this.lastMod = lastMod;
    }
    
    public long getLastMod() {
        return lastMod;
    }
    
}
