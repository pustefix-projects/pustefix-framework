/*
 * Created on 24.03.2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.schlund.pfixcore.generator.postchecks;

import de.schlund.pfixcore.generator.IWrapperParamPostCheck;
import de.schlund.pfixcore.generator.SimpleCheck;
import de.schlund.util.statuscodes.StatusCode;
import de.schlund.util.statuscodes.StatusCodeFactory;

/**
 * @author <a href="mailto:thomas.braun@schlund.de>Tom Braun</a>
 *
 */
public class StringLength extends SimpleCheck implements IWrapperParamPostCheck {
    int minLength  = 1;
    int maxLength = 64;
    private StatusCode scTooShort;
    private StatusCode scTooLong;
    
    public StringLength () {
        scTooShort = StatusCodeFactory.getInstance().getStatusCode("pfixcore.generator.postcheck.STRING_TOO_SHORT");
        scTooLong = StatusCodeFactory.getInstance().getStatusCode("pfixcore.generator.postcheck.STRING_TOO_LONG");
    }
    
    public void put_scode_too_long(String scode) {
        scTooLong = StatusCodeFactory.getInstance().getStatusCode(scode);
    }
    
    public void put_scode_too_short(String scode) {
        scTooShort = StatusCodeFactory.getInstance().getStatusCode(scode);
    }
    
    public void put_min_length(String minLength) {
        this.minLength = Integer.parseInt(minLength);
    }
    
    public void put_max_length(String maxLength) {
        this.maxLength = Integer.parseInt(maxLength);
    }

    /** @deprecated due to typo in method name.*/
    public void put_max_lentgth(String maxLength) {
        this.maxLength = Integer.parseInt(maxLength);
    }
    
    public void check(Object[] obj) {
        reset();
        for (int i=0; i<obj.length; i++) {
            String str = (String)obj[i];
            if (str.length() > maxLength) {
                addSCode(scTooLong);
                break;
            }
            if (str.length()<minLength) {
                addSCode(scTooShort);
                break;
            }
        }
    }
}
