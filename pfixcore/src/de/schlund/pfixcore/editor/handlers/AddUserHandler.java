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
import de.schlund.util.*;
import de.schlund.util.statuscodes.*;
import de.schlund.pfixxml.*;
import org.apache.log4j.*;
import java.util.*;

/**
 * AddUserHandler.java
 *
 *
 * Created: Sun Dec 12 03:05:24 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class AddUserHandler implements IHandler {
    private static Category CAT = Category.getInstance(EditorPageUpdater.class.getName());

    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
        ContextResourceManager crm     = context.getContextResourceManager();
        AddUser                adduser = (AddUser) wrapper;
        EditorSessionStatus    esess   = EditorRes.getEditorSessionStatus(crm);
        String                 newid   = adduser.getId();
        StatusCodeFactory      sfac    = new StatusCodeFactory("pfixcore.editor.adduser");

        EditorUser tmp = EditorUserFactory.getInstance().getEditorUser(newid);
        if (tmp == null) {
            tmp = EditorUserFactory.getInstance().createEditorUser(newid);
            esess.setUserForEdit(tmp);
        } else {
            StatusCode scode = sfac.getStatusCode("USER_EXISTS");
            adduser.addSCodeId(scode);
        }
    }
    
    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
        // Never
    }
        
    public boolean prerequisitesMet(Context context) {
        ContextResourceManager crm   = context.getContextResourceManager();
        EditorSessionStatus    esess = EditorRes.getEditorSessionStatus(crm);
        EditorUser             user  = esess.getUser();
        if (user != null && user.isAdmin()) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean isActive(Context context) {
        return true;
    }
    
    public boolean needsData(Context context) {
        return false;
    }
    
}// AddUserHandler
