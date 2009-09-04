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

import junit.framework.TestCase;

import org.junit.Assert;
import org.pustefixframework.generated.CoreStatusCodes;

import de.schlund.pfixcore.example.iwrapper.TShirt;
import de.schlund.pfixcore.generator.IWrapperParam;

public class TShirtWrapperTest extends TestCase {
    
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
