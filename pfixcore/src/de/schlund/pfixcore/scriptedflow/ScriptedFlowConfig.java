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

import de.schlund.pfixcore.scriptedflow.compiler.CompilerException;
import de.schlund.pfixcore.scriptedflow.vm.Script;
import de.schlund.pfixxml.ContextXMLServlet;

/**
 * Provides scripted flows for a {@link ContextXMLServlet}.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface ScriptedFlowConfig {

    /**
     * Returns the scripted flow configured for the specified name.
     * 
     * @param name name identifying the scripted flow
     * @return compiled scripted flow or <code>null</code> if there is no 
     * scripted flow for the specified name
     * @throws CompilerException if scripted flow exists but cannot be compiled
     */
    Script getScript(String name) throws CompilerException;

}