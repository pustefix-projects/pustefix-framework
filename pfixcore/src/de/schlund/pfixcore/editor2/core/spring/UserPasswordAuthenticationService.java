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

import java.security.Principal;

import de.schlund.pfixcore.editor2.core.exception.EditorSecurityException;

public interface UserPasswordAuthenticationService {
    /**
     * Returns the principal for the specified user
     * 
     * @param username
     *            Username for the user
     * @param password
     *            Password for the user
     * @return The {@link Principal} for the user or <code>null</code> if no
     *         user with the specified name can be found or the wrong password
     *         was supplied.
     */
    Principal getPrincipalForUser(String username, String password);

    /**
     * Enables and disables logins for users. Admins are not affected by this
     * setting.
     * @throws EditorSecurityException 
     */
    void setAllowUserLogins(boolean flag) throws EditorSecurityException;

    /**
     * Returns current user login setting
     * 
     * @return <code>true</code> if users are allowed to login,
     *         <code>false</code> otherwise
     */
    boolean isAllowUserLogins();
}
