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
    private static String EDITOR_PERF = "EDITOR_PERF";
    private static Category PERF_LOGGER = Category.getInstance(EDITOR_PERF);
    
    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
        long start_time = 0;
        if(PERF_LOGGER.isInfoEnabled()) {
            start_time = System.currentTimeMillis();
        }
        
        ContextResourceManager crm     = context.getContextResourceManager();
        EditorSessionStatus    esess   = EditorRes.getEditorSessionStatus(crm);
        UserAuth               auth    = (UserAuth) wrapper;
        String                 user    = auth.getUser();
        String                 pass    = auth.getPass();
        boolean                loginok = esess.getLoginAllowed();
        StatusCodeFactory      sfac    = new StatusCodeFactory("pfixcore.editor.auth");
        
        if(CAT.isDebugEnabled())       
            CAT.debug("Doing login for user '"+user+"'");
        
        EditorUser editor_user = null;
        
        try {
            editor_user = EditorUser.logIn(user, pass);
        } catch (WrongPasswordException e) {
            auth.addSCodeUser(sfac.getStatusCode("WRONG_USER_OR_PASS"));
            if (CAT.isDebugEnabled())
                CAT.debug("Login for user '"+user+"' denied: Wrong password.");
            
            return;
        } catch (NoSuchUserException e) {
            auth.addSCodeUser(sfac.getStatusCode("WRONG_USER_OR_PASS"));
            if(CAT.isDebugEnabled())
                CAT.debug("Login for user '"+user+"' denied: User does not exist.");
            
            return;
        }
        
        /* admins are always allowed to login, regardless of the 'getLoginAllowed' flag.
         * other users must pay attention to this flag.
         */
        if(editor_user.getUserInfo().isAdmin()) {
            esess.setUser(editor_user);
            if(CAT.isDebugEnabled())
                CAT.debug("Login for user '"+user+"' successfull. Saving user in session. User logged in.");
        } else {
            if (loginok ) {
                esess.setUser(editor_user);
                if(CAT.isDebugEnabled())
                    CAT.debug("Login for user '"+user+"' successfull. Saving user in session. User logged in.");
            } else {
                auth.addSCodeUser(sfac.getStatusCode("NO_LOGIN_ALLOWED"));    
                if(CAT.isDebugEnabled())
                    CAT.debug("Login for user '"+user+"' denied: Login currently not allowed.");
            }
        }
        if(PERF_LOGGER.isInfoEnabled()) {
            long length = System.currentTimeMillis() - start_time;
            PERF_LOGGER.info(this.getClass().getName()+"#handleSubmittedData: "+length);
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
