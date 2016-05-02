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

package de.schlund.pfixcore.scriptedflow.vm;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Stores the state of a scripted flow VM. Can be used to keep the state
 * of a VM between several requests in the same session.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class VMState {
    private Script script;
    private int ip;
    private Map<String, String> variables;

    // We don't want other classes to mess up with a state
    // as this might have funny effects (like having an invalid
    // instruction pointer). So we make all methods visible within
    // this package only.
    
    int getIp() {
        return ip;
    }

    void setIp(int ip) {
        this.ip = ip;
    }

    Script getScript() {
        return script;
    }

    void setScript(Script script) {
        this.script = script;
    }

    public Map<String, String> getVariables() {
        return Collections.unmodifiableMap(variables);
    }
    
    public void setVariables(Map<String, String> vars) {
        this.variables = new HashMap<String, String>();
        this.variables.putAll(vars);
    }
}
