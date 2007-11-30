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

package de.schlund.pfixcore.generator.casters;
import java.util.ArrayList;

import de.schlund.pfixcore.generator.IWrapperParamCaster;
import de.schlund.pfixcore.generator.SimpleCheck;
import de.schlund.pfixxml.RequestParam;
import de.schlund.util.statuscodes.StatusCode;
import de.schlund.util.statuscodes.StatusCodeLib;

/**
 * ToLong.java
 *
 *
 * Created: Thu Aug 16 01:07:36 2001
 *
 * @author <a href="mailto: "Jens Lautenbacher</a>
 *
 *
 */

public class ToLong extends SimpleCheck implements IWrapperParamCaster {
    private Long[]  value = null;
    private StatusCode scode;

    public ToLong() {
        scode = StatusCodeLib.PFIXCORE_GENERATOR_CASTER_ERR_TO_LONG;
    }

    public void put_scode_casterror(String fqscode) {
        scode = StatusCodeLib.getStatusCodeByName(fqscode);
    }

    public Object[] getValue() {
        return value;
    }
    
    public void castValue(RequestParam[] param) {
        for (int i = 0; i < param.length; i++) {
            LOG.debug("*** IN param: " + param[i]);
        }
        reset();
        ArrayList out = new ArrayList();
        Long   val;
        for (int i = 0; i < param.length; i++) {
            try {
                val = new Long(param[i].getValue());
                out.add(val);
            } catch (NumberFormatException e) {
                val = null;
                addSCode(scode);
                break;
            }
        }
        if (!errorHappened()) {
            value = (Long[]) out.toArray(new Long[] {});
        }
    }
    
}// ToLong
