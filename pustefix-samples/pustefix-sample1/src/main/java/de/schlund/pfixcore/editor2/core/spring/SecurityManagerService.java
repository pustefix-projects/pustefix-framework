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

import de.schlund.pfixcore.editor2.core.dom.Image;
import de.schlund.pfixcore.editor2.core.dom.IncludePart;
import de.schlund.pfixcore.editor2.core.dom.IncludePartThemeVariant;
import de.schlund.pfixcore.editor2.core.dom.Theme;
import de.schlund.pfixcore.editor2.core.exception.EditorSecurityException;

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
     * Checks whether the user currently logged in is allowed to edit the
     * specified {@link IncludePartThemeVariant}.
     * 
     * @param part
     *            The part variant to do the check for
     * @return <code>true</code> if the user is allowed to edit,
     *         <code>false</code> otherwise
     */
    boolean mayEditIncludePartThemeVariant(IncludePartThemeVariant part);

    /**
     * Checks whether the user currently logged in is allowed to edit the
     * specified {@link IncludePartThemeVariant}.
     * 
     * @param part
     *            The part variant to do the check for
     * @throws EditorSecurityException
     *             if the current user is not allowed to edit
     */
    void checkEditIncludePartThemeVariant(IncludePartThemeVariant part)
            throws EditorSecurityException;

    /**
     * Checks whether the user currently logged in is allowed to create a new
     * {@link IncludePartThemeVariant} for the specified {@link IncludePart} and
     * the specified {@link Theme}.
     * 
     * @param part
     *            The part to do the check for
     * @param theme
     *            The theme to do the check for
     * @return <code>true</code> if the user is allowed to create the variant,
     *         <code>false</code> otherwise
     */
    boolean mayCreateIncludePartThemeVariant(IncludePart part, Theme theme);

    /**
     * Checks whether the user currently logged in is allowed to create a new
     * {@link IncludePartThemeVariant} for the specified {@link IncludePart} and
     * the specified {@link Theme}.
     * 
     * @param part
     *            The part to do the check for
     * @param theme
     *            The theme to do the check for
     * @throws EditorSecurityException
     *             if the user is not allowed to create the variant
     */
    void checkCreateIncludePartThemeVariant(IncludePart part, Theme theme)
            throws EditorSecurityException;

    /**
     * Checks whether the user currently logged in is allowed to edit the
     * specified {@link Image}.
     * 
     * @param image
     *            The image to do the check for
     * @return <code>true</code> if the user is allowed to edit,
     *         <code>false</code> otherwise
     */
    boolean mayEditImage(Image image);

    /**
     * Checks whether the user currently logged in is allowed to edit the
     * specified {@link Image}.
     * 
     * @param image
     *            The image to do the check for
     * @throws EditorSecurityException
     *             if the current user is not allowed to edit
     */
    void checkEditImage(Image image) throws EditorSecurityException;

    /**
     * Checks whether the user currently logged in is allowed to edit dynamic
     * includes.
     * 
     * @return <code>true</code> if the user is allowed to edit,
     *         <code>false</code> otherwise.
     */
    boolean mayEditDynInclude();

    /**
     * Checks whether the user currently logged in is allowed to edit dynamic
     * includes.
     * 
     * @throws EditorSecurityException
     *             if the current user is not allowed to edit
     */
    void checkEditDynInclude() throws EditorSecurityException;

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
