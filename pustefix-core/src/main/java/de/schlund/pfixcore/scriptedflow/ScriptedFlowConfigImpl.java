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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.pustefixframework.resource.InputStreamResource;

import de.schlund.pfixcore.scriptedflow.compiler.Compiler;
import de.schlund.pfixcore.scriptedflow.compiler.CompilerException;
import de.schlund.pfixcore.scriptedflow.vm.Script;
import de.schlund.pfixxml.util.MD5Utils;

/**
 * Stores configuration for scripted flows.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ScriptedFlowConfigImpl implements ScriptedFlowConfig {
    private class Triple {
        String hexMD5 = "";

        InputStreamResource file = null;

        Script script = null;
    }

    private Map<String, Triple> scripts = new HashMap<String, Triple>();

    /* (non-Javadoc)
     * @see de.schlund.pfixcore.scriptedflow.ScriptedFlowConfig#getScript(java.lang.String)
     */
    public Script getScript(String name) throws CompilerException {
        Triple t = this.scripts.get(name);
        if (t == null) {
            return null;
        }
        synchronized (t) {
            String hexMD5;
            try {
                InputStream is = t.file.getInputStream();
                hexMD5 = MD5Utils.hex_md5(is);
                is.close();
            } catch (IOException e) {
                throw new CompilerException("Could not read scripted flow \"" + name + "\" from file \"" + t.file.getURI().toASCIIString() + "\"", e);
            }
            if (t.script == null || !t.hexMD5.equals(hexMD5)) {
                t.hexMD5 = hexMD5;
                t.script = Compiler.compile(t.file);
            }
            return t.script;
        }
    }

    public void addScript(String name, InputStreamResource resource) {
        Triple t = new Triple();
        t.file = resource;
        scripts.put(name, t);
    }
}
