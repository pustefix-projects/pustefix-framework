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

package de.schlund.pfixcore.editor2.core.spring;

import java.util.Collection;

import org.pustefixframework.editor.common.exception.EditorDuplicateUsernameException;
import org.pustefixframework.editor.common.exception.EditorSecurityException;
import org.pustefixframework.editor.common.exception.EditorUserNotExistingException;

import de.schlund.pfixcore.editor2.core.vo.EditorUser;

/**
 * Service providing methods needed for user management (list users, create
 * user, edit user...).
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface UserManagementService {
    /**
     * Returns a user object for the specified username.
     * 
     * @param username
     *            The username to get the EditorUser object for
     * @return Corresponding EditorUser object or <code>null</code> if there
     *         is no user for the specified username
     * @throws EditorUserNotExistingException
     *             if there is no user with an username matching the username of
     *             the supplied EditorUser object
     */
    EditorUser getUser(String username) throws EditorUserNotExistingException;

    /**
     * Checks wheter a user is existing
     * 
     * @param username
     *            Name of the user
     * @return <code>true</code> if user is existing, <code>false</code>
     *         otherwise
     */
    boolean hasUser(String username);

    /**
     * Updates information for a user.
     * 
     * @param user
     *            EditorUser object containing new settings
     * @throws EditorUserNotExistingException
     *             if there is no user with an username matching the username of
     *             the supplied EditorUser object
     * @throws EditorSecurityException
     */
    void updateUser(EditorUser user) throws EditorUserNotExistingException,
            EditorSecurityException;

    /**
     * Create a new user
     * 
     * @param user
     *            EditorUser object containing the data to use
     * @throws EditorDuplicateUsernameException
     *             If there is already a user with the specified username
     * @throws EditorSecurityException
     */
    void createUser(EditorUser user) throws EditorDuplicateUsernameException,
            EditorSecurityException;

    /**
     * Deletes an user
     * 
     * @param user
     *            User to remove
     * @throws EditorUserNotExistingException
     *             if there is no user with an username matching the username of
     *             the supplied EditorUser object
     * @throws EditorSecurityException
     */
    void deleteUser(EditorUser user) throws EditorUserNotExistingException,
            EditorSecurityException;

    /**
     * Returns all user objects which are stored at the moment.
     * 
     * @return List of all users
     */
    Collection<EditorUser> getUsers();
}
