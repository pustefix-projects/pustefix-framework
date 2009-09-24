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

package org.pustefixframework.config.contextxmlservice;

import de.schlund.pfixcore.scriptedflow.compiler.CompilerException;
import de.schlund.pfixcore.scriptedflow.vm.Script;

/**
 * Provides a compiled scripted flow.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface ScriptedFlowProvider {

    /**
     * Returns the name of the scripted flow that is provided
     * by this object.
     * 
     * @return name of the scripted flow
     */
    String getName();

    /**
     * Returns a compiled scripted flow object.
     * 
     * @return compiled scripted flow
     * @throws CompilerException if source code cannot be compiled
     */
    Script getScript() throws CompilerException;
}
