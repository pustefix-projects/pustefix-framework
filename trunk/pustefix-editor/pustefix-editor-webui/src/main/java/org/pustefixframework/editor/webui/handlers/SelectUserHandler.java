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

package org.pustefixframework.editor.webui.handlers;

import org.pustefixframework.container.annotations.Inject;
import org.pustefixframework.editor.common.exception.EditorDuplicateUsernameException;
import org.pustefixframework.editor.generated.EditorStatusCodes;
import org.pustefixframework.editor.webui.resources.UsersResource;
import org.pustefixframework.editor.webui.wrappers.SelectUser;

import de.schlund.pfixcore.editor2.core.spring.SecurityManagerService;
import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;

/**
 * Handles user selection
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class SelectUserHandler implements IHandler {
    private SecurityManagerService securitymanager;
    private UsersResource usersResource;

    public void handleSubmittedData(Context context, IWrapper wrapper)
            throws Exception {
        SelectUser input = (SelectUser) wrapper;
        if (input.getCreate() != null && input.getCreate().booleanValue()) {
            try {
                usersResource.createAndSelectUser(input.getUsername());
            } catch (EditorDuplicateUsernameException e) {
                input.addSCodeUsername(EditorStatusCodes.ADDUSER_USER_EXISTS);
            }
        } else {
            if (securitymanager.mayAdmin()
                    || input.getUsername().equals(securitymanager.getPrincipal().getName())) {
                usersResource.selectUser(input.getUsername());
            }
        }
    }

    public void retrieveCurrentStatus(Context context, IWrapper wrapper)
            throws Exception {
        // Do not insert data
    }

    public boolean prerequisitesMet(Context context) throws Exception {
        // Can always select a user
        return true;
    }

    public boolean isActive(Context context) throws Exception {
        // Always await input
        return true;
    }

    public boolean needsData(Context context) throws Exception {
        // Do not affect pageflow
        return false;
    }

    @Inject
    public void setSecurityManagerService(SecurityManagerService securitymanager) {
        this.securitymanager = securitymanager;
    }

    @Inject
    public void setUsersResource(UsersResource usersResource) {
        this.usersResource = usersResource;
    }

}
