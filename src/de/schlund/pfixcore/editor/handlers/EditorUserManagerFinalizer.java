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
import org.w3c.dom.Element;

import de.schlund.pfixcore.editor.EditorUser;
import de.schlund.pfixcore.editor.auth.EditorUserInfo;
import de.schlund.pfixcore.editor.resources.EditorRes;
import de.schlund.pfixcore.editor.resources.EditorSessionStatus;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextResourceManager;
import de.schlund.pfixcore.workflow.app.IWrapperContainer;
import de.schlund.pfixcore.workflow.app.ResdocSimpleFinalizer;
import de.schlund.pfixxml.ResultDocument;

/**
 * EditorUserManagerFinalizer.java
 *
 *
 * Created: Tue Dec 12 00:29:33 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class EditorUserManagerFinalizer extends ResdocSimpleFinalizer {

    protected void renderDefault(IWrapperContainer container) throws Exception {
        Context                context = container.getAssociatedContext();
        ContextResourceManager crm     = context.getContextResourceManager();
        EditorSessionStatus    esess   = EditorRes.getEditorSessionStatus(crm);
        ResultDocument         resdoc  = container.getAssociatedResultDocument();

        // Render the current status of the editor session
        esess.insertStatus(resdoc, resdoc.createNode("cr_editorsession"));

        // Render all Locks
        Element root = resdoc.createNode("allusers"); 

        EditorUserInfo[]  allusers = EditorUser.getAllEditorUserInfo();
        for (int i = 0; i < allusers.length; i++) {
            EditorUserInfo user = allusers[i];
            Element    elem = resdoc.createSubNode(root, "user");
            elem.setAttribute("count", "" + i);
            elem.setAttribute("id", user.getId());
            elem.setAttribute("name", user.getName());
            elem.setAttribute("phone", user.getPhone());
            elem.setAttribute("sect", user.getSect());
        }
    }
    
}// EditorUserManagerFinalizer
