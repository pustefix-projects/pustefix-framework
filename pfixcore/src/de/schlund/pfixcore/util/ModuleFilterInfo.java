package de.schlund.pfixcore.util;

import java.util.HashMap;
import java.util.Map;

import de.schlund.pfixxml.resources.ModuleFilter;

public class ModuleFilterInfo {
    
    private static ModuleFilterInfo instance = new ModuleFilterInfo();
    
    private Map<String, ModuleFilter> applicationToFilter = new HashMap<String, ModuleFilter>();
    
    private ModuleFilterInfo() {
    }
    
    public static ModuleFilterInfo getInstance() {
        return instance;
    }
    
    public void addModuleFilter(String application, String filterExpression) {
        applicationToFilter.put(application, new ModuleFilter(filterExpression));
    }
    
    public ModuleFilter getModuleFilter(String application) {
        return applicationToFilter.get(application);
    }
    
}
