package de.schlund.pfixcore.example;

import java.io.File;

import junit.framework.TestCase;

import org.junit.Assert;
import org.pustefixframework.generated.CoreStatusCodes;

import de.schlund.pfixcore.example.iwrapper.TShirt;
import de.schlund.pfixcore.generator.IWrapperParam;
import de.schlund.pfixxml.config.GlobalConfig;
import de.schlund.pfixxml.config.GlobalConfigurator;

public class TShirtWrapperTest extends TestCase {
    @Override
    protected void setUp() throws Exception {
        // TODO: becore class
        if (GlobalConfig.getDocroot() == null) {
            GlobalConfigurator.setDocroot(GlobalConfig.guessDocroot().getAbsolutePath());
        }
    }
    
    public void testIWrapper() throws Exception {
        
        TShirt tshirt = new TShirt();
        
        tshirt.init("shirt");
        tshirt.setStringValSize("MX");
        tshirt.loadFromStringValues();
        Assert.assertTrue(tshirt.errorHappened());
        IWrapperParam[] params = tshirt.gimmeAllParamsWithErrors();
        for(IWrapperParam param:params) {
            if(param.getName().equals("Color")) {
                Assert.assertSame(param.getStatusCodeInfos()[0].getStatusCode(), CoreStatusCodes.MISSING_PARAM);
            } else if(param.getName().equals("Size")) {
                Assert.assertSame(param.getStatusCodeInfos()[0].getStatusCode(), CoreStatusCodes.PRECHECK_REGEXP_NO_MATCH);
            }
        }
        
        tshirt.init("shirt");
        tshirt.setStringValSize("XL");
        tshirt.setColor(1);
        tshirt.loadFromStringValues();
        Assert.assertFalse(tshirt.errorHappened());
    }
    
}
