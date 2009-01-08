package de.schlund.pfixcore.example;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pustefixframework.test.MockContext;

import de.schlund.pfixcore.example.iwrapper.AdultInfo;
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
        GlobalConfigurator.setDocroot((new File("projects").getAbsolutePath()));
    }
    
    @Test
    public void testHandler() throws Exception {
        
        MockContext context = new MockContext();
        
        ContextAdultInfo info = new ContextAdultInfo();
        
        //If ContextResources are retrieved via the ContextResourceManager,
        //you have to set up a mock object and add the required ContextResources:
        
        //MockContextResourceManager resourceManager = new MockContextResourceManager();
        //context.setContextResourceManager(resourceManager);
        //resourceManager.addResource(info);
     
        AdultInfoHandler handler = new AdultInfoHandler();
        handler.setContextAdultInfo(info);
        
        Assert.assertTrue(handler.needsData(context));
        
        AdultInfo iwrapper = new AdultInfo();
        iwrapper.init("info");
        iwrapper.setStringValAdult("false");
        iwrapper.loadFromStringValues();
             
        handler.handleSubmittedData(context, iwrapper);
        
        Assert.assertFalse(handler.needsData(context));
    
    }
    
}
