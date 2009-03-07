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

import org.pustefixframework.editor.common.dom.Project;
import org.pustefixframework.editor.common.exception.EditorSecurityException;


/**
 * This service is responsible for handling security issues. It provides methods
 * to check whether a user is allowed to trigger a certain action.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface SecurityManagerService {
    // Login-Handling should be done by another component
    // boolean checkCredentials(String username, String password);
    /**
     * Sets the principal that is identifying the current user. This method is
     * used to "login" a user or do a "logout" (using <code>null</code> as the
     * supplied principal.
     */
    void setPrincipal(Principal auth);

    /**
     * Returns the principal that is identifying the current user.
     * 
     * @return Principal identifying the user currently logged in or
     *         <code>null</code> if no user is currently logged in.
     */
    Principal getPrincipal();

    /**
     * Checks whether the user currently logged in is allowed to edit
     * include parts for the specified {@link Project}.
     * 
     * @param project
     *            the project to do the check for
     * @return <code>true</code> if the user is allowed to edit,
     *         <code>false</code> otherwise
     */
    boolean mayEditIncludes(Project project);

    /**
     * Checks whether the user currently logged in is allowed to edit
     * include parts for the specified {@link Project}.
     * 
     * @param project
     *            the project to do the check for
     * @throws EditorSecurityException
     *             if the current user is not allowed to edit
     */
    void checkEditIncludes(Project project)
            throws EditorSecurityException;

    /**
     * Checks whether the user currently logged in is allowed to edit images
     * for the specified {@link Project}.
     * 
     * @param project
     *            the project to do the check for
     * @return <code>true</code> if the user is allowed to edit,
     *         <code>false</code> otherwise
     */
    boolean mayEditImages(Project project);

    /**
     * Checks whether the user currently logged in is allowed to edit images
     * for the specified {@link Project}.
     * 
     * @param project
     *            the project to do the check for
     * @throws EditorSecurityException
     *             if the current user is not allowed to edit
     */
    void checkEditImages(Project project) throws EditorSecurityException;

    /**
     * Checks whether the user currently logged in is allowed to perform
     * administrative tasks.
     * 
     * @return <code>true</code> if the user is an admin, <code>false</code>
     *         otherwise.
     */
    boolean mayAdmin();

    /**
     * Checks whether the user currently logged in is allowed to perform
     * administrative tasks.
     * 
     * @throws EditorSecurityException
     *             if the current user is no admin
     */
    void checkAdmin() throws EditorSecurityException;
}
