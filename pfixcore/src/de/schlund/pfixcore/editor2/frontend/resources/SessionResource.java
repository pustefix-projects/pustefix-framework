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

import de.schlund.pfixcore.workflow.ContextResource;

/**
 * Provides methods for the user session in the editor
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface SessionResource extends ContextResource {
    /**
     * Try to login a user
     * 
     * @param username
     *            Name identifying user
     * @param password
     *            Password authenticating user
     * @return <code>true</code> on success, <code>false</code> otherwise
     */
    boolean login(String username, String password);

    /**
     * Logout user. If user is already logged out, do nothing.
     */
    void logout();

    /**
     * Checks whether a user is logged in for the current session
     * 
     * @return <code>true</code> if user is logged in, <code>false</code>
     *         otherwise
     */
    boolean isLoggedIn();

    /**
     * Returns wheter users are allowed to login
     * 
     * @return <code>true</code> if users can login, <code>false</code>
     *         otherwise
     */
    boolean isUserLoginsAllowed();

    /**
     * Enables or disables user logins
     * 
     * @param flag
     *            Set to <code>true</code> to enable and <code>false</code>
     *            to disable user logins
     * @return <code>true</code> on success, <code>false</code> otherwise
     */
    boolean setUserLoginsAllowed(boolean flag);
}
