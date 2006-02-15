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
import de.schlund.pfixcore.scriptedflow.vm.JumpCondFalseInstruction;
import de.schlund.pfixcore.scriptedflow.vm.JumpUncondInstruction;
import de.schlund.pfixcore.scriptedflow.vm.NopInstruction;

/**
 * Statement used to branch on different conditions  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ChooseStatement extends AbstractStatement {

    private List<Instruction> jumpInstr = null;
    
    private List<Instruction> jumpEndInstr = null;
    
    private List<Instruction> nopInstr = null;

    private List<String> conditions = new ArrayList<String>();

    private List<Statement> childs = new ArrayList<Statement>();

    public ChooseStatement(Statement parent) {
        super(parent);
    }

    public void addBranch(String condition, Statement child) {
        if (conditions.size() > 0
                && conditions.get(conditions.size() - 1) == null) {
            throw new RuntimeException(
                    "No branch after default branch is allowed");
        }
        conditions.add(condition);
        childs.add(child);
    }

    public Instruction[] getInstructions() {
        if (jumpInstr == null) {
            jumpInstr = new ArrayList<Instruction>();
            nopInstr = new ArrayList<Instruction>();
            for (String condition : conditions) {
                Instruction lastNopInstr = new NopInstruction();
                nopInstr.add(lastNopInstr);
                if (condition != null) {
                    jumpInstr.add(new JumpCondFalseInstruction(condition, lastNopInstr));
                } else {
                    jumpInstr.add(new NopInstruction());
                }
            }
            jumpEndInstr = new ArrayList<Instruction>();
            for (int i = 0; i < jumpInstr.size(); i++) {
                jumpEndInstr.add(new JumpUncondInstruction(nopInstr.get(nopInstr.size() - 1)));
            }
        }
        List<Instruction> temp = new ArrayList<Instruction>();
        for (int i = 0; i < jumpInstr.size(); i++) {
            temp.add(jumpInstr.get(i));
            Instruction[] childInstr = childs.get(i).getInstructions();
            for (int j = 0; j < childInstr.length; j++) {
                temp.add(childInstr[j]);
            }
            temp.add(jumpEndInstr.get(i));
            temp.add(nopInstr.get(i));
        }
        Instruction[] temp2 = new Instruction[temp.size()];
        for (int i = 0; i < temp2.length; i++) {
            temp2[i] = temp.get(i);
        }
        return temp2;
    }
}
