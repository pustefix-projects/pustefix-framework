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
import org.apache.log4j.Category;

import de.schlund.pfixcore.editor.EditorException;
import de.schlund.pfixcore.editor.EditorUser;
import de.schlund.pfixcore.editor.auth.NoSuchUserException;
import de.schlund.pfixcore.editor.auth.WrongPasswordException;
import de.schlund.pfixcore.editor.interfaces.UserAuth;
import de.schlund.pfixcore.editor.resources.EditorRes;
import de.schlund.pfixcore.editor.resources.EditorSessionStatus;
import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextResourceManager;
import de.schlund.util.statuscodes.StatusCode;
import de.schlund.util.statuscodes.StatusCodeFactory;

/**
 * UserAuthHandler.java
 *
 *
 * Created: Sun Nov 25 01:34:24 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class UserAuthHandler implements IHandler {
    private static Category CAT = Category.getInstance(UserAuthHandler.class.getName());
    
    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
        ContextResourceManager crm     = context.getContextResourceManager();
        EditorSessionStatus    esess   = EditorRes.getEditorSessionStatus(crm);
        UserAuth               auth    = (UserAuth) wrapper;
        String                 user    = auth.getUser();
        String                 pass    = auth.getPass();
        boolean                loginok = esess.getLoginAllowed();
        StatusCodeFactory      sfac    = new StatusCodeFactory("pfixcore.editor.auth");
        StatusCode             scode;
 

        /*EditorUser eu = EditorUserFactory.getInstance().getEditorUser(user);
        if (eu != null && UnixCrypt.matches(eu.getPwd(), pass)) {
            if (loginok || eu.isAdmin()) {
                esess.setUser(eu);
            } else {
                auth.addSCodeUser(sfac.getStatusCode("NO_LOGIN_ALLOWED"));
            }
        } else {
            if (eu == null) {
                CAT.debug("Didn't get user for name " + user);
            } else {
                CAT.debug("PWD doesn't match: " + pass );
            }
            auth.addSCodeUser(sfac.getStatusCode("WRONG_USER_OR_PASS"));
        }*/
        
        EditorUser editor_user = null;
        
        try {
            editor_user = EditorUser.logIn(user, pass);
        } catch (WrongPasswordException e) {
            auth.addSCodeUser(sfac.getStatusCode("WRONG_USER_OR_PASS"));
            return;
        } catch (NoSuchUserException e) {
            auth.addSCodeUser(sfac.getStatusCode("WRONG_USER_OR_PASS"));
            return;
        }
        
        if (loginok ) {
            esess.setUser(editor_user);
        } else {
            auth.addSCodeUser(sfac.getStatusCode("NO_LOGIN_ALLOWED"));
        }
    }
        

    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
        // We never want to prefill the userauth dialog.
    }
        
    public boolean prerequisitesMet(Context context) {
        return true;
    }
    
    public boolean isActive(Context context) {
        return true;
    }
    
    public boolean needsData(Context context) {
        ContextResourceManager crm   = context.getContextResourceManager();
        EditorSessionStatus    esess = EditorRes.getEditorSessionStatus(crm);
        if (esess.getUser() == null) {
            return true;
        } else {
            return false;
        }
    }
    
}// UserAuthHandler
