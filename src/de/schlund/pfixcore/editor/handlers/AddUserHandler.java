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

import de.schlund.pfixcore.editor.EditorPageUpdater;
import de.schlund.pfixcore.editor.EditorProduct;
import de.schlund.pfixcore.editor.EditorProductFactory;
import de.schlund.pfixcore.editor.EditorUser;
import de.schlund.pfixcore.editor.auth.EditorUserInfo;
import de.schlund.pfixcore.editor.auth.GlobalPermissions;
import de.schlund.pfixcore.editor.auth.NoSuchUserException;
import de.schlund.pfixcore.editor.auth.ProjectPermissions;
import de.schlund.pfixcore.editor.interfaces.AddUser;
import de.schlund.pfixcore.editor.resources.EditorRes;
import de.schlund.pfixcore.editor.resources.EditorSessionStatus;
import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextResourceManager;
import de.schlund.util.statuscodes.StatusCode;
import de.schlund.util.statuscodes.StatusCodeLib;
import org.apache.log4j.Category;

/**
 * Handler for adding new users to the Pustefix CMS. 
 *
 * <br/>
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 */

public class AddUserHandler implements IHandler {
    private static Category CAT = Category.getInstance(EditorPageUpdater.class.getName());

    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
        ContextResourceManager crm     = context.getContextResourceManager();
        AddUser                adduser = (AddUser) wrapper;
        EditorSessionStatus    esess   = EditorRes.getEditorSessionStatus(crm);
        String                 newid   = adduser.getId();

        EditorUserInfo tmp = null;
        try {
            if(CAT.isDebugEnabled())
                CAT.debug("Retrieving user information for user '"+newid+"'.");
            tmp = EditorUser.getUserInfoByLogin(newid);
            
            if(CAT.isDebugEnabled())
                CAT.debug("User '"+newid+"' already exists. Abort.");
            adduser.addSCodeId(StatusCodeLib.PFIXCORE_EDITOR_ADDUSER_USER_EXISTS);
        } catch(NoSuchUserException e) {
            if(CAT.isDebugEnabled()) {
                CAT.debug("Creating new user '"+newid+"' with default permissions");
            }
            tmp = new EditorUserInfo(newid);
            // here we set default permissions for the new user
            GlobalPermissions gp = new GlobalPermissions();
            gp.setAdmin(false);
            gp.setEditDynIncludesDefault(false);
            tmp.setGlobalPermissions(gp);
            EditorProduct[] prods = EditorProductFactory.getInstance().getAllEditorProducts();
            for(int i=0; i<prods.length; i++) {
                ProjectPermissions p = new ProjectPermissions();
                p.setEditDynIncludes(false);
                p.setEditImages(false);
                p.setEditIncludes(false);
                tmp.addProjectPermission(prods[i].getName(), p);
            }
            esess.setUserForEdit(tmp);
        } 
    }
    
    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
        // Never
    }
        
    public boolean prerequisitesMet(Context context) {
        ContextResourceManager crm   = context.getContextResourceManager();
        EditorSessionStatus    esess = EditorRes.getEditorSessionStatus(crm);
        EditorUser             user  = esess.getUser();
        if (user != null && user.getUserInfo().isAdmin()) {
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
