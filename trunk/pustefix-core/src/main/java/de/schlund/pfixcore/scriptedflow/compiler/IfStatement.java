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

import de.schlund.pfixcore.scriptedflow.vm.Instruction;
import de.schlund.pfixcore.scriptedflow.vm.JumpCondFalseInstruction;
import de.schlund.pfixcore.scriptedflow.vm.NopInstruction;

/**
 * Allows to bind the execution of one or more statements to a condition  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class IfStatement extends AbstractStatement {

    private String condition;
    private Statement child;
    private Instruction jumpInstr = null;
    private Instruction finalInstr = new NopInstruction();

    public IfStatement(Statement parent) {
        super(parent);
    }
    
    public void setCondition(String condition) {
        this.condition = condition;
    }
    
    public void setChild(Statement child) {
        this.child = child;
    }

    public Instruction[] getInstructions() {
        if (jumpInstr == null) {
            jumpInstr = new JumpCondFalseInstruction(condition, finalInstr);
        }
        Instruction[] temp = child.getInstructions();
        Instruction[] temp2 = new Instruction[temp.length + 2];
        temp2[0] = jumpInstr;
        temp2[temp2.length - 1] = finalInstr;
        for (int i = 0; i < temp.length; i++) {
            temp2[i + 1] = temp[i];
        }
        return temp2;
    }
}
