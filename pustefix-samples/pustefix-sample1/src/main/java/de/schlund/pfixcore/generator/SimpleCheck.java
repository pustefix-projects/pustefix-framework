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

package de.schlund.pfixcore.generator;
import java.util.HashSet;

import org.apache.log4j.Logger;

import de.schlund.util.statuscodes.StatusCode;

/**
 * SimpleCheck.java
 *
 *
 * Created: Thu Aug 16 01:07:36 2001
 *
 * @author <a href="mailto: "Jens Lautenbacher</a>
 *
 *
 */

public abstract class SimpleCheck implements IWrapperParamCheck {
    protected HashSet<StatusCodeInfo>  scodeinfos = new HashSet<StatusCodeInfo>();
    protected Logger   LOG        = Logger.getLogger(this.getClass());
    
    protected void addSCode(StatusCode scode) {
        addScode(scode, null, null);
    }

    protected void addScode(StatusCode scode, String[] args, String level) {
        scodeinfos.add(new StatusCodeInfo(scode, args, level));
    }
    
    protected void reset() {
        scodeinfos = new HashSet<StatusCodeInfo>();
    }
    
    public StatusCodeInfo[] getStatusCodeInfos() {
        synchronized (scodeinfos) {
            return scodeinfos.toArray(new StatusCodeInfo[] {});
        }
    }
    
    public boolean errorHappened() {
        return !scodeinfos.isEmpty();
    }

}
