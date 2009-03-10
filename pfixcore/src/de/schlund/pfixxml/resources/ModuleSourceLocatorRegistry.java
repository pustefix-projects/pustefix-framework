package de.schlund.pfixxml.resources;

import java.util.ArrayList;
import java.util.List;

import de.schlund.pfixcore.util.LiveJarInfo;
import de.schlund.pfixxml.config.BuildTimeProperties;

/**
 * 
 * @author mleidig@schlund.de
 *
 */
public class ModuleSourceLocatorRegistry {
    
    private static ModuleSourceLocatorRegistry instance = new ModuleSourceLocatorRegistry();
    
    private List<ModuleSourceLocator> locators = new ArrayList<ModuleSourceLocator>();
    
    public static ModuleSourceLocatorRegistry getInstance() {
        return instance;
    }
    
    private ModuleSourceLocatorRegistry() {
        registerDefaultLocators();
    }   
    
    private void registerDefaultLocators() {
        //TODO: dynamic/configurable registration
        if(!BuildTimeProperties.getProperties().getProperty("mode").equals("prod")) {
            LiveJarInfo locator = LiveJarInfo.getInstance();
            if(locator.hasEntries()) registerLocators(locator);
        }
    }
    
    public List<ModuleSourceLocator> getLocators() {
        return locators;
    }
    
    public void registerLocators(ModuleSourceLocator locator) {
        locators.add(locator);
    }

}
