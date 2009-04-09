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

package de.schlund.pfixcore.scriptedflow;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.schlund.pfixcore.scriptedflow.vm.VMState;

/**
 * Stores information about which scripted flow is currently executed and
 * the state of this script.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ScriptedFlowInfo {
    
    private Map<String, String> params = new HashMap<String, String>();
    private VMState state = null;
    private boolean scriptRunning = false;
    
    public void reset() {
        state = null;
        params.clear();
        scriptRunning = false;
    }

    public VMState getState() {
        return state;
    }

    public void setState(VMState state) {
        this.state = state;
    }
    
    public void addParam(String key, String value) {
        this.params.put(key, value);
    }
    
    public Map<String, String> getParams() {
        return Collections.unmodifiableMap(params);
    }
    
    public void isScriptRunning(boolean val) {
        this.scriptRunning = val;
    }
    
    public boolean isScriptRunning() {
        return this.scriptRunning;
    }
}
