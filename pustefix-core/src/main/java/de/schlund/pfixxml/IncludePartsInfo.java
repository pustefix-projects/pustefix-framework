package de.schlund.pfixxml;

import java.util.Collections;
import java.util.Map;

public class IncludePartsInfo {
    
    private final Map<String, IncludePartInfo> parts;
    private long lastMod;
    
    public IncludePartsInfo(Map<String, IncludePartInfo> parts) {
        this.parts = Collections.unmodifiableMap(parts);
    }
    
    public Map<String, IncludePartInfo> getParts() {
        return parts;
    }
    
    public IncludePartInfo getPart(String partName) {
        return parts.get(partName);
    }
    
    public long getLastMod() {
        return lastMod;
    }
    
    void setLastMod(long lastMod) {
        this.lastMod = lastMod;
    }
    
}
