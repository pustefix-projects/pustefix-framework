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
import java.io.File;
import java.util.ArrayList;

import de.schlund.pfixcore.generator.IWrapperParamCaster;
import de.schlund.pfixcore.generator.SimpleCheck;
import de.schlund.pfixxml.RequestParam;
import de.schlund.pfixxml.RequestParamType;
import de.schlund.pfixxml.multipart.FileData;
import de.schlund.util.statuscodes.StatusCode;
import de.schlund.util.statuscodes.StatusCodeLib;

/**
 * ToFile.java
 *
 *
 * @author <a href="mailto: "Jens Lautenbacher</a>
 *
 *
 */

public class ToFile extends SimpleCheck implements IWrapperParamCaster {
    private File[]  value = null;
    private StatusCode scode;

    public ToFile() {
        scode = StatusCodeLib.PFIXCORE_GENERATOR_CASTER_ERR_TO_FILE;
    }

    public void put_scode_casterror(String fqscode) {
        scode = StatusCodeLib.getStatusCodeByName(fqscode);
    }

    public Object[] getValue() {
        return value;
    }
    
    public void castValue(RequestParam[] param) {
        if(LOG.isDebugEnabled()) {
            for (int i = 0; i < param.length; i++) {
                LOG.debug("*** IN param: " + param[i]);
            }
        }
        reset();
        ArrayList<File> out = new ArrayList<File>();
        File val;
        for (int i = 0; i < param.length; i++) {
            RequestParam tmp = param[i];
            if (tmp.getType().equals(RequestParamType.FILEDATA)) {
                FileData fdata = (FileData) tmp;
                val = fdata.getLocalFile();
                out.add(val);
            } else {
                val = null;
                addSCode(scode);
                break;
            }
        }
        if (!errorHappened()) {
            value = out.toArray(new File[] {});
        }
    }
    
}// ToFile
