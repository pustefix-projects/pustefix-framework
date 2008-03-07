/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package de.schlund.pfixcore.generator.postchecks;

import de.schlund.pfixcore.generator.IWrapperParamPostCheck;
import de.schlund.pfixcore.generator.SimpleCheck;
import de.schlund.util.statuscodes.StatusCode;
import de.schlund.util.statuscodes.StatusCodeLib;

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
        scTooShort = StatusCodeLib.PFIXCORE_GENERATOR_POSTCHECK_STRING_TOO_SHORT;
        scTooLong  = StatusCodeLib.PFIXCORE_GENERATOR_POSTCHECK_STRING_TOO_LONG;
    }
    
    public void put_scode_too_long(String scode) {
        scTooLong = StatusCodeLib.getStatusCodeByName(scode);
    }
    
    public void put_scode_too_short(String scode) {
        scTooShort = StatusCodeLib.getStatusCodeByName(scode);
    }
    
    public void put_min_length(String minLength) {
        this.minLength = Integer.parseInt(minLength);
    }
    
    public void put_max_length(String maxLength) {
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
