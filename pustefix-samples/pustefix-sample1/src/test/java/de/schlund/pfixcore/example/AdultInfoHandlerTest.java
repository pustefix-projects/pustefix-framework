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
package de.schlund.pfixcore.example;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pustefixframework.eventbus.EventBus;

import de.schlund.pfixcore.example.iwrapper.AdultInfo;
import de.schlund.pfixxml.config.GlobalConfig;
import de.schlund.pfixxml.config.GlobalConfigurator;

/**
 * Example test case showing how to test IWrappers and IHandlers.
 * 
 * @author mleidig@schlund.de
 *
 */
public class AdultInfoHandlerTest {

    @Before
    public void setUp() throws Exception {
        if (GlobalConfig.getDocroot() == null) {
            GlobalConfigurator.setDocroot(GlobalConfig.guessDocroot().getAbsolutePath());
        }
    }
    
    @Test
    public void testHandler() throws Exception {
        
        ContextAdultInfo info = new ContextAdultInfo();
        info.eventBus = new EventBus();
        
        //If ContextResources are retrieved via the ContextResourceManager,
        //you have to set up a mock object and add the required ContextResources:
        
        //MockContextResourceManager resourceManager = new MockContextResourceManager();
        //context.setContextResourceManager(resourceManager);
        //resourceManager.addResource(info);
     
        AdultInfoHandler handler = new AdultInfoHandler();
        handler.setContextAdultInfo(info);
        
        Assert.assertTrue(handler.needsData());
        
        AdultInfo iwrapper = new AdultInfo();
        iwrapper.init("info");
        iwrapper.setStringValAdult("false");
        iwrapper.loadFromStringValues();
             
        handler.handleSubmittedData(iwrapper);
        
        Assert.assertFalse(handler.needsData());
    
    }
    
}
