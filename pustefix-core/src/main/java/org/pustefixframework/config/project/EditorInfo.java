package org.pustefixframework.config.project;

public class EditorInfo {
    
    private boolean enabled;
    private boolean includePartsEditableByDefault = true;
    private String location;
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public boolean isEnabled() {
        return enabled;
    }

    public void setIncludePartsEditableByDefault(boolean includePartsEditableByDefault) {
        this.includePartsEditableByDefault = includePartsEditableByDefault;
    }

    public boolean isIncludePartsEditableByDefault() {
        return includePartsEditableByDefault;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getLocation() {
        return location;
    }

}
