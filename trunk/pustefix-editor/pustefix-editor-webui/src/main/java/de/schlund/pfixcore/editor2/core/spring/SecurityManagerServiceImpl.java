/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package de.schlund.pfixcore.editor2.core.spring;

import java.security.Principal;

import org.pustefixframework.editor.common.dom.Project;
import org.pustefixframework.editor.common.exception.EditorSecurityException;
import org.pustefixframework.editor.common.exception.EditorUserNotExistingException;

import de.schlund.pfixcore.editor2.core.vo.EditorUser;

public class SecurityManagerServiceImpl implements SecurityManagerService {
    private Principal principal;

    private UserManagementService usermanagement;

    public void setUserManagementService(UserManagementService usermanagement) {
        this.usermanagement = usermanagement;
    }

    public void setPrincipal(Principal principal) {
        this.principal = principal;
    }

    public Principal getPrincipal() {
        return this.principal;
    }

    public boolean mayEditIncludes(Project project) {
        if (this.getPrincipal() == null) {
            return false;
        }
        String username = this.getPrincipal().getName();
        if (username == null || username.equals("")) {
            return false;
        }

        try {
            EditorUser user = this.usermanagement.getUser(username);
            if (user.getGlobalPermissions().isAdmin()) {
                return true;
            }
            if (user.getProjectPermissions(project.getName()).isEditIncludes()) {
                return true;
            }
        } catch (EditorUserNotExistingException e) {
            return false;
        }

        return false;
    }

    public void checkEditIncludes(Project project)
            throws EditorSecurityException {
        if (!mayEditIncludes(project)) {
            throw new EditorSecurityException(
                    "Operation editIncludePartThemeVariant not permitted!");
        }
    }

    public boolean mayEditImages(Project project) {
        if (this.getPrincipal() == null) {
            return false;
        }
        String username = this.getPrincipal().getName();
        if (username == null || username.equals("")) {
            return false;
        }

        try {
            EditorUser user = this.usermanagement.getUser(username);
            if (user.getGlobalPermissions().isAdmin()) {
                return true;
            }
            if (user.getProjectPermissions(project.getName()).isEditImages()) {
                return true;
            }
        } catch (EditorUserNotExistingException e) {
            return false;
        }

        return false;
    }

    public void checkEditImages(Project project) throws EditorSecurityException {
        if (!mayEditImages(project)) {
            throw new EditorSecurityException(
                    "Operation editImage not permitted!");
        }
    }

    public boolean mayAdmin() {
        if (this.getPrincipal() == null) {
            return false;
        }
        String username = this.getPrincipal().getName();
        if (username == null || username.equals("")) {
            return false;
        }

        try {
            EditorUser user = this.usermanagement.getUser(username);
            if (user.getGlobalPermissions().isAdmin()) {
                return true;
            } else {
                return false;
            }
        } catch (EditorUserNotExistingException e) {
            return false;
        }
    }

    public void checkAdmin() throws EditorSecurityException {
        if (!mayAdmin()) {
            throw new EditorSecurityException("User is not an admin!");
        }
    }
}
