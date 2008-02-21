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

import java.util.Iterator;
import java.util.TreeSet;

import org.w3c.dom.Element;

import de.schlund.pfixcore.editor2.core.exception.EditorDuplicateUsernameException;
import de.schlund.pfixcore.editor2.core.exception.EditorSecurityException;
import de.schlund.pfixcore.editor2.core.exception.EditorUserNotExistingException;
import de.schlund.pfixcore.editor2.core.spring.UserManagementService;
import de.schlund.pfixcore.editor2.core.vo.EditorUser;
import de.schlund.pfixcore.editor2.frontend.util.SpringBeanLocator;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.ResultDocument;

/**
 * Implementation of UsersResource
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class UsersResourceImpl implements UsersResource {
    private EditorUser selectedUser = null;

    public void init(Context context) throws Exception {
        // Do nothing
    }

    public void insertStatus(ResultDocument resdoc, Element elem)
            throws Exception {
        UserManagementService ums = SpringBeanLocator
                .getUserManagementService();
        TreeSet<EditorUser> users = new TreeSet<EditorUser>(ums.getUsers());
        for (Iterator<EditorUser> i = users.iterator(); i.hasNext();) {
            EditorUser user = i.next();
            Element userNode = resdoc.createSubNode(elem, "user");
            userNode.setAttribute("id", user.getUsername());
            userNode.setAttribute("name", user.getFullname());
            userNode.setAttribute("section", user.getSectionName());
            userNode.setAttribute("phone", user.getPhoneNumber());
            if (this.selectedUser != null
                    && user.getUsername().equals(
                            this.selectedUser.getUsername())) {
                userNode.setAttribute("selected", "true");
            }
        }

        if (this.selectedUser != null) {
            Element currentuser = resdoc.createSubNode(elem, "currentuser");
            currentuser.setAttribute("id", this.selectedUser.getUsername());
        }
    }

    public void reset() throws Exception {
        this.selectedUser = null;
    }

    public void createAndSelectUser(String username)
            throws EditorDuplicateUsernameException {
        UserManagementService ums = SpringBeanLocator
                .getUserManagementService();
        if (ums.hasUser(username)) {
            throw new EditorDuplicateUsernameException(
                    "Cannot create user with already existing name!");
        } else {
            this.selectedUser = new EditorUser(username);
        }
    }

    public void deleteUsers(String[] usernames)
            throws EditorUserNotExistingException, EditorSecurityException {
        UserManagementService ums = SpringBeanLocator
                .getUserManagementService();
        for (int i = 0; i < usernames.length; i++) {
            EditorUser user = ums.getUser(usernames[i]);
            ums.deleteUser(user);
        }
    }

    public void selectUser(String username)
            throws EditorUserNotExistingException {
        UserManagementService ums = SpringBeanLocator
                .getUserManagementService();
        this.selectedUser = ums.getUser(username);
    }

    public void updateSelectedUser() throws EditorUserNotExistingException,
            EditorSecurityException, EditorDuplicateUsernameException {
        UserManagementService ums = SpringBeanLocator
                .getUserManagementService();
        try {
            if (ums.hasUser(this.selectedUser.getUsername())) {
                ums.updateUser(this.selectedUser);
            } else {
                ums.createUser(this.selectedUser);
            }
        } finally {
            this.selectedUser = ums.getUser(this.selectedUser.getUsername());
        }
    }

    public EditorUser getSelectedUser() {
        return this.selectedUser;
    }

    public boolean existsSelectedUser() {
        UserManagementService ums = SpringBeanLocator.getUserManagementService();
        return ums.hasUser(this.selectedUser.getUsername());
    }
}
