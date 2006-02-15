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
 */

package de.schlund.pfixcore.scriptedflow;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import de.schlund.pfixcore.scriptedflow.compiler.Compiler;
import de.schlund.pfixcore.scriptedflow.compiler.CompilerException;
import de.schlund.pfixcore.scriptedflow.vm.Script;
import de.schlund.pfixcore.util.PropertiesUtils;
import de.schlund.pfixxml.PathFactory;
import de.schlund.pfixxml.PropertyObject;

/**
 * Stores configuration for scripted flows.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ScriptedFlowConfig implements PropertyObject {
    private class Triple {
        long mtime = -1;

        File file = null;

        Script script = null;
    }

    private final static String PROP_SCRIPTEDFLOW = "scriptedflow";

    private Map<String, Triple> scripts;

    public Script getScript(String name) throws CompilerException {
        Triple t = this.scripts.get(name);
        if (t == null) {
            return null;
        }
        synchronized (t) {
            if (t.script == null || t.file.lastModified() > t.mtime) {
                if (!t.file.exists()) {
                    throw new CompilerException(
                            "Scripted flow "
                                    + name
                                    + " is defined but corresponding file does not exist");
                }
                t.mtime = t.file.lastModified();
                t.script = Compiler.compile(t.file);
            }
            return t.script;
        }
    }

    public void init(Properties props) throws Exception {
        scripts = new HashMap<String, Triple>();
        Map<String, String> nameToFile = PropertiesUtils.selectProperties(
                props, PROP_SCRIPTEDFLOW);

        for (String key : nameToFile.keySet()) {
            Triple t = new Triple();
            t.file = PathFactory.getInstance().createPath(nameToFile.get(key))
                    .resolve();
            scripts.put(key, t);
        }
    }
}
