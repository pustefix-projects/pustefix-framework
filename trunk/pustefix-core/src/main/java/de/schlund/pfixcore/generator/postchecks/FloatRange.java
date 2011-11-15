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

package de.schlund.pfixcore.generator.postchecks;
import java.util.StringTokenizer;

import org.pustefixframework.generated.CoreStatusCodes;

import de.schlund.pfixcore.generator.IWrapperParamPostCheck;
import de.schlund.pfixcore.generator.SimpleCheck;
import de.schlund.util.statuscodes.StatusCode;
import de.schlund.util.statuscodes.StatusCodeHelper;

/**
 * FloatRange.java
 *
 *
 * Created: Thu Aug 16 17:18:57 2001
 *
 * @author <a href="mailto: "Jens Lautenbacher</a>
 *
 *
 */

public class FloatRange extends SimpleCheck implements IWrapperParamPostCheck {
    private boolean    lower_inc   = true;
    private boolean    upper_inc   = true;
    private StatusCode scode_small;
    private StatusCode scode_big;
    private float      lower;
    private float      upper;
    
    public FloatRange() {
        scode_small = CoreStatusCodes.POSTCHECK_FLOAT_TOO_SMALL;
        scode_big   = CoreStatusCodes.POSTCHECK_FLOAT_TOO_BIG;
    }

    public void setScodeTooSmall(String fqscode) {
        scode_small = StatusCodeHelper.getStatusCodeByName(fqscode);
    }

    public void setScodeTooBig(String fqscode) {
        scode_big = StatusCodeHelper.getStatusCodeByName(fqscode);
    }

    public void setRange(String param) {
        param = param.trim();
        
        if (param.startsWith("("))
        { 
            lower_inc = false;
        }
        
        if (param.endsWith(")")) {
            upper_inc = false;
        }
        
        StringTokenizer tok = new StringTokenizer(param, "()[] :", false);
        if (tok.countTokens() == 2) {
            Float thelower = new Float(tok.nextToken());
            Float theupper = new Float(tok.nextToken());
            lower = thelower.intValue();
            upper = theupper.intValue();
        } else {
            throw new RuntimeException("Range spec '" + param + "' isn't correct");
        }
    }

    public void check(Object[] obj) {
        reset();
        for (int i = 0; i < obj.length; i++) {
            float value = ((Float) obj[i]).floatValue();
            if ((lower_inc && (lower > value)) || (!lower_inc && (lower >= value))) {
                addSCode(scode_small);
                break;
            } else if ((upper_inc && (upper < value)) || (!upper_inc && (upper <= value))) {
                addSCode(scode_big);
                break;
            }
        }
    }


}// FloatRange
