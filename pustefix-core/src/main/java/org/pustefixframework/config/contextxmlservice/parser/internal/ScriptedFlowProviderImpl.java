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

package org.pustefixframework.config.contextxmlservice.parser.internal;

import java.io.IOException;
import java.io.InputStream;

import org.pustefixframework.config.contextxmlservice.ScriptedFlowProvider;
import org.pustefixframework.resource.InputStreamResource;

import de.schlund.pfixcore.scriptedflow.compiler.Compiler;
import de.schlund.pfixcore.scriptedflow.compiler.CompilerException;
import de.schlund.pfixcore.scriptedflow.vm.Script;
import de.schlund.pfixxml.util.MD5Utils;


/**
 * Simple scripted flow provider that reads script from an
 * input stream resource and compiles it. Rechecks for changes
 * in the original script, each time the script is requested.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ScriptedFlowProviderImpl implements ScriptedFlowProvider {
    
    private String name;
    private InputStreamResource scriptResource;
    private String lastCompileMD5;
    private Script script;
    private final Object compilerLock = new Object();

    public ScriptedFlowProviderImpl(String name, InputStreamResource scriptResource) {
        this.name = name;
        this.scriptResource = scriptResource;
    }
    
    public String getName() {
        return name;
    }

    public Script getScript() throws CompilerException {
        String fileMD5;
        InputStream is;
        try {
            is = scriptResource.getInputStream();
            fileMD5 = MD5Utils.hex_md5(is);
            is.close();
        } catch (IOException e) {
            throw new CompilerException("Could not read scripted flow from resource " + scriptResource, e);
        }
        synchronized (compilerLock) {
            if (script == null || !fileMD5.equals(lastCompileMD5)) {
                script = Compiler.compile(scriptResource);
            }
            return script;
        }
    }

}
