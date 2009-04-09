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
 *
 */

package de.schlund.pfixcore.generator.casters;
import java.util.ArrayList;

import org.pustefixframework.generated.CoreStatusCodes;

import de.schlund.pfixcore.generator.IWrapperParamCaster;
import de.schlund.pfixcore.generator.SimpleCheck;
import de.schlund.pfixxml.RequestParam;
import de.schlund.util.statuscodes.StatusCode;
import de.schlund.util.statuscodes.StatusCodeHelper;

/**
 * ToDouble.java
 *
 *
 * Created: Tue Mar 08 16:17:48 2005
 *
 * @author <a href="mailto:tanjev.stuhr@schlund.de">Tanjev Stuhr</a>
 *
 *
 */

public class ToDouble extends SimpleCheck implements IWrapperParamCaster {
    private Double[]    value = null;
    private StatusCode scode;

    public ToDouble() {
        scode = CoreStatusCodes.CASTER_ERR_TO_DOUBLE;
    }
    
    public void setScodeCastError(String fqscode) {
        scode = StatusCodeHelper.getStatusCodeByName(fqscode);
    }

    public Object[] getValue() {
        return value;
    }
    
    public void castValue(RequestParam[] param) {
        reset();
        ArrayList<Double> out = new ArrayList<Double>();
        String par;
        Double  val;
        for (int i = 0; i < param.length; i++) {
            try {
                par = param[i].getValue().replace(',', '.');
                val = new Double(par);
                out.add(val);
            } catch (NumberFormatException e) {
                addSCode(scode);
                break;
            }
        }
        if (!errorHappened()) {
            value = out.toArray(new Double[] {});
        }
    }
    
}// ToDouble
