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
import de.schlund.pfixcore.editor.auth.EditorUserInfo;
import de.schlund.pfixcore.editor.interfaces.*;
import de.schlund.pfixcore.editor.resources.*;
import de.schlund.pfixcore.generator.*;
import de.schlund.pfixcore.workflow.*;
import de.schlund.util.statuscodes.*;

/**
 * UserForEditHandler.java
 *
 *
 * Created: Sun Nov 25 01:34:24 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class UserForEditHandler implements IHandler {
    
    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
        ContextResourceManager crm    = context.getContextResourceManager();
        EditorSessionStatus    esess  = EditorRes.getEditorSessionStatus(crm);
        EditorUser             curr   = esess.getUser();
        UserForEdit            toedit = (UserForEdit) wrapper;
        String                 user   = toedit.getUser();
        EditorUserInfo          eduser = EditorUser.getUserInfoByLogin(user);
        StatusCodeFactory      sfac   = new StatusCodeFactory("pfixcore.editor.userforedit");
        
        if (curr.getUserInfo().isAdmin()) {
            if (eduser != null) {
                esess.setUserForEdit(eduser);
            } else {
                StatusCode scode = StatusCodeFactory.getInstance().getStatusCode("USER_UNDEF");
                toedit.addSCodeUser(scode);
            }
        } else {
            esess.setUserForEdit(curr.getUserInfo()); // normal users can only edit their own data.
        }
        
    }

    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
        // Never
    }
        
    public boolean prerequisitesMet(Context context) {
        return true;
    }
    
    public boolean isActive(Context context) {
        return true;
    }
    
    public boolean needsData(Context context) {
        ContextResourceManager crm    = context.getContextResourceManager();
        EditorSessionStatus    esess  = EditorRes.getEditorSessionStatus(crm);
        if (esess.getUserForEdit() == null) {
            return true;
        } else {
            return false;
        }
    }
    
}// UserForEditHandler
