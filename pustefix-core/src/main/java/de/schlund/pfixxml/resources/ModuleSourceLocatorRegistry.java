/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
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
