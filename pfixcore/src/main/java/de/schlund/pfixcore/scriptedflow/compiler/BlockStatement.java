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

package de.schlund.pfixcore.scriptedflow.compiler;

import java.util.ArrayList;
import java.util.List;

import de.schlund.pfixcore.scriptedflow.vm.Instruction;
import de.schlund.pfixcore.scriptedflow.vm.NopInstruction;

/**
 * Statement consisting of 1 to n substatements which are executed in order.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class BlockStatement extends AbstractStatement {
    private List<Statement> childs = new ArrayList<Statement>();
    private Instruction nop = new NopInstruction();
    
    public BlockStatement(Statement parent) {
        super(parent);
    }
    
    public void addStatement(Statement stmt) {
        childs.add(stmt);
    }

    public Instruction[] getInstructions() {
        if(childs.size() == 0) {
            return new Instruction[] {nop};
        }
        List<Instruction> temp = new ArrayList<Instruction>();
        for(Statement child : childs) {
            Instruction[] childInstr = child.getInstructions();
            for (int i = 0; i < childInstr.length; i++) {
                temp.add(childInstr[i]);
            }
        }
        Instruction[] temp2 = new Instruction[temp.size()];
        for (int i = 0; i < temp2.length; i++) {
            temp2[i] = temp.get(i);
        }
        return temp2;
    }

}
