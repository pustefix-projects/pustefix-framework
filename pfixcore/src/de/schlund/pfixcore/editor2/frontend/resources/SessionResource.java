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

package de.schlund.pfixcore.editor2.frontend.resources;

import java.security.Principal;

import org.pustefixframework.container.annotations.Inject;
import org.w3c.dom.Element;

import de.schlund.pfixcore.beans.InitResource;
import de.schlund.pfixcore.beans.InsertStatus;
import de.schlund.pfixcore.editor2.core.exception.EditorSecurityException;
import de.schlund.pfixcore.editor2.core.spring.SecurityManagerService;
import de.schlund.pfixcore.editor2.core.spring.UserManagementService;
import de.schlund.pfixcore.editor2.core.spring.UserPasswordAuthenticationService;
import de.schlund.pfixcore.editor2.core.vo.EditorUser;
import de.schlund.pfixcore.editor2.frontend.util.ContextStore;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.ResultDocument;

/**
 * Generic implementation of SessionResource
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class SessionResource {

    private UserPasswordAuthenticationService upas;
    
    private UserManagementService usermanagement;

    private SecurityManagerService            secman;

    private Context                           context;

    private boolean                           inIncludeEditView = false;

    public boolean login(String username, String password) {
        Principal user = this.upas.getPrincipalForUser(username, password);
        if (user == null) {
            return false;
        }
        this.secman.setPrincipal(user);

        // Register context with ContextStore
        ContextStore.getInstance().registerContext(this.context, username);

        // Set AUTHENTICATED role on context
        context.getAuthentication().addRole("AUTHENTICATED");

        return true;
    }

    public void logout() {
        secman.setPrincipal(null);

        // Unregister context
        ContextStore.getInstance().unregisterContext(this.context);

        // Remove AUTHENTICATED role
        context.getAuthentication().revokeRole("AUTHENTICATED");
    }

    public boolean isLoggedIn() {
        return (secman.getPrincipal() != null);
    }

    public void setInIncludeEditView(boolean flag) {
        this.inIncludeEditView = flag;
    }

    public boolean isInIncludeEditView() {
        return this.inIncludeEditView;
    }

    @InitResource
    public void init(Context context) throws Exception {
        this.context = context;
    }

    @InsertStatus
    public void insertStatus(ResultDocument resdoc, Element elem) throws Exception {
        if (this.upas.isAllowUserLogins()) {
            elem.setAttribute("userLoginsAllowed", "true");
        } else {
            elem.setAttribute("userLoginsAllowed", "false");
        }
        if (this.isLoggedIn()) {
            // Use security manager to render effective permissions
            Element user = resdoc.createSubNode(elem, "user");
            EditorUser userinfo = usermanagement.getUser(this.secman.getPrincipal().getName());
            user.setAttribute("fullname", userinfo.getFullname());
            user.setAttribute("username", userinfo.getUsername());
            Element permissions = resdoc.createSubNode(user, "permissions");
            if (secman.mayAdmin()) {
                permissions.setAttribute("admin", "true");
            } else {
                permissions.setAttribute("admin", "false");
            }
            if (secman.mayEditDynInclude()) {
                permissions.setAttribute("editDynIncludes", "true");
            } else {
                permissions.setAttribute("editDynIncludes", "false");
            }
        }
    }

    public void reset() throws Exception {
        this.logout();
    }

    public boolean isUserLoginsAllowed() {
        return this.upas.isAllowUserLogins();
    }

    public boolean setUserLoginsAllowed(boolean flag) {
        try {
            this.upas.setAllowUserLogins(flag);
        } catch (EditorSecurityException e) {
            return false;
        }
        return true;
    }

    @Inject
    public void setUserPasswordAuthenticationService(UserPasswordAuthenticationService upas) {
        this.upas = upas;
    }

    @Inject
    public void setUserManagementService(UserManagementService usermanagement) {
        this.usermanagement = usermanagement;
    }

    @Inject
    public void setSecurityManagerService(SecurityManagerService secman) {
        this.secman = secman;
    }

}
