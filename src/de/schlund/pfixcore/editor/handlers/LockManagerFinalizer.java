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
 *
 */

package de.schlund.pfixcore.editor.handlers;
import de.schlund.pfixcore.editor.*;
import de.schlund.pfixcore.editor.resources.*;
import de.schlund.pfixcore.workflow.*;
import de.schlund.pfixcore.workflow.app.*;
import de.schlund.pfixxml.*;
import de.schlund.pfixxml.targets.*;
import org.w3c.dom.*;

/**
 * LockManagerFinalizer.java
 *
 *
 * Created: Tue Dec 11 00:29:33 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class LockManagerFinalizer extends ResdocSimpleFinalizer {

    protected void renderDefault(IWrapperContainer container) throws Exception {
        Context                context = container.getAssociatedContext();
        ContextResourceManager crm     = context.getContextResourceManager();
        EditorSessionStatus    esess   = EditorRes.getEditorSessionStatus(crm);
        ResultDocument         resdoc  = container.getAssociatedResultDocument();

        // Render the current status of the editor session
        esess.insertStatus(resdoc, resdoc.createNode("cr_editorsession"));

        // Render all Locks
        Element root = resdoc.createNode("alllocks"); 

        EditorLockFactory      lfac    = EditorLockFactory.getInstance();
        EditorSessionStatus[]  allsess = lfac.getAllLockingEditorStatusSessions();
        for (int i = 0; i < allsess.length; i++) {
            EditorSessionStatus tmp  = allsess[i];
            EditorUser          user = tmp.getUser();
            EditorProduct       prod = tmp.getProduct();
            if (user != null && prod != null) {
                AuxDependency aux  = lfac.getLockedAuxDependency(tmp);
                Element       elem = resdoc.createSubNode(root, "lock");
                elem.setAttribute("count", "" + i);
                elem.setAttribute("id",   tmp.getEditorSessionId());
                elem.setAttribute("username", user.getUserInfo().getName());
                elem.setAttribute("phone", user.getUserInfo().getPhone());
                elem.setAttribute("sect", user.getUserInfo().getSect());
                elem.setAttribute("product", prod.getComment());
                elem.setAttribute("type", "" + aux.getType());
                elem.setAttribute("auxpath", "" + aux.getPath());
                if (aux.getType() == DependencyType.TEXT) {
                    elem.setAttribute("auxpart", "" + aux.getPart());
                    elem.setAttribute("auxproduct", "" + aux.getProduct());
                }
            } else { // can this ever happen?
                lfac.releaseLock(tmp);
            }
        }
    }
    
}// LockManagerFinalizer
