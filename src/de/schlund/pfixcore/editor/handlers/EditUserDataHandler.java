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
import de.schlund.pfixcore.editor.interfaces.*;
import de.schlund.pfixcore.editor.resources.*;
import de.schlund.pfixcore.generator.*;
import de.schlund.pfixcore.workflow.*;
import de.schlund.pfixcore.util.*;
import de.schlund.util.statuscodes.*;
import de.schlund.pfixxml.*;
import org.apache.log4j.*;

/**
 * EditUserDataHandler.java
 *
 *
 * Created: Sun Dec 10 14:34:24 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class EditUserDataHandler implements IHandler {
    private Category CAT = Category.getInstance(this.getClass().getName());

    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
        ContextResourceManager crm    = context.getContextResourceManager();
        EditorSessionStatus    esess  = EditorRes.getEditorSessionStatus(crm);
        EditUserData           data   = (EditUserData) wrapper;
        EditorUser             curr   = esess.getUser();
        EditorUser             euser  = esess.getUserForEdit();
        StatusCodeFactory      sfac   = new StatusCodeFactory("pfixcore.editor.userdata");
        boolean                commit = true;
        
        String name  = data.getName();
        String group = data.getGroup();
        String sect  = data.getSect();
        String phone = data.getPhone();

        String pass1 = data.getPass1();
        String pass2 = data.getPass2();
        String crypt = null;
        
        if (pass1 != null && pass2 != null && pass1.equals(pass2)) {
            crypt = UnixCrypt.crypt(pass1);
        } else if (pass1 != null && !pass1.equals(pass2)) {
            commit = false;
            StatusCode scode = sfac.getStatusCode("PWD_NO_MATCH");
            data.addSCodePass1(scode);
        }

        if (commit) {
            euser.setName(name);
            euser.setSect(sect);
            euser.setPhone(phone);
            if (curr.isAdmin()) {
                euser.setGroup(group);
            }
            if (crypt != null) {
                euser.setPwd(crypt);
            }
            // make sure the user is really added
            EditorUserFactory.getInstance().addEditorUser(euser);
            EditorUserFactory.getInstance().writeFile();
            // reset the selected "user for editing"
            esess.setUserForEdit(null);
        }
    }

    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
        ContextResourceManager crm   = context.getContextResourceManager();
        EditorSessionStatus    esess = EditorRes.getEditorSessionStatus(crm);
        EditUserData           data  = (EditUserData) wrapper;
        EditorUser             euser = esess.getUserForEdit();

        data.setStringValGroup(euser.getGroup());
        data.setStringValName(euser.getName());
        data.setStringValPhone(euser.getPhone());
        data.setStringValSect(euser.getSect());
    }
        
    public boolean prerequisitesMet(Context context) {
        return true;
    }
    
    public boolean isActive(Context context) {
        ContextResourceManager crm   = context.getContextResourceManager();
        EditorSessionStatus    esess = EditorRes.getEditorSessionStatus(crm);
        if (esess.getUserForEdit() != null) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean needsData(Context context) {
        return true;
    }
    
}// EditUserDataHandler
