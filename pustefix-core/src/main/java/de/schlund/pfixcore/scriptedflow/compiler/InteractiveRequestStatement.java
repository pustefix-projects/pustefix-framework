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

package de.schlund.pfixcore.scriptedflow.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.schlund.pfixcore.scriptedflow.vm.Instruction;
import de.schlund.pfixcore.scriptedflow.vm.InteractiveRequestInstruction;
import de.schlund.pfixcore.scriptedflow.vm.pvo.ParamValueObject;

/**
 * Return the current document to the browser, but go on with the execution
 * of the script, when the user re-submits.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class InteractiveRequestStatement extends AbstractStatement implements ParameterizedStatement {

    private Map<String, List<ParamValueObject>> params = new HashMap<String, List<ParamValueObject>>();
    
    private Instruction instr = null;
    
    public InteractiveRequestStatement(Statement parent) {
        super(parent);
    }
    
    public void addParam(String param, ParamValueObject value) {
        List<ParamValueObject> list = this.params.get(param);
        if (list == null) {
            list = new ArrayList<ParamValueObject>();
            this.params.put(param, list);
        }
        list.add(value);
    }

    public Instruction[] getInstructions() {
        if (instr == null) {
            instr = new InteractiveRequestInstruction(params);
        }
        return new Instruction[] {instr};
    }

}
