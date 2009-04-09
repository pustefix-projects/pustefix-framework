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
import de.schlund.pfixcore.scriptedflow.vm.VirtualRequestInstruction;
import de.schlund.pfixcore.scriptedflow.vm.pvo.ParamValueObject;

/**
 * Send a request to the context and use the resulting document for
 * further processing.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class VirtualRequestStatement extends AbstractStatement implements ParameterizedStatement {
    private String pagename = null;
    private boolean dointeractive = false;
    private boolean reuseparams = false;
    private Map<String, List<ParamValueObject>> params = new HashMap<String, List<ParamValueObject>>();

    private Instruction instr = null;

    public VirtualRequestStatement(Statement parent) {
        super(parent);
    }

    public void setPagename(String pagename) {
        this.pagename = pagename;
    }

    public void setDointeractive(String dointer) {
        if (dointer != null && (dointer.equals("true"))) {
            dointeractive = true;
            reuseparams = false;
        } else if (dointer != null && dointer.equals("reuse")) {
            dointeractive = true;
            reuseparams = true;
        } else {
            dointeractive = false;
            reuseparams = false;
        }
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
            instr = new VirtualRequestInstruction(pagename, params, reuseparams, dointeractive);
        }
        return new Instruction[] { instr };
    }

}
