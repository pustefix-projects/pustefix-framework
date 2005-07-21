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

import de.schlund.pfixcore.editor2.core.dom.Image;
import de.schlund.pfixcore.editor2.core.dom.IncludePart;
import de.schlund.pfixcore.editor2.core.dom.IncludePartThemeVariant;
import de.schlund.pfixcore.editor2.core.dom.Theme;
import de.schlund.pfixcore.editor2.core.exception.EditorSecurityException;

/**
 * This service is responsible for handling security issues.
 * It provides methods to check whether a user is allowed to trigger a certain action.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface SecurityManagerService {
    // Login-Handling should be done by another component
    // boolean checkCredentials(String username, String password);
    void setAuthContext(SecurityContext auth);
    SecurityContext getAuthContext();
    
    boolean mayEditIncludePartThemeVariant(IncludePartThemeVariant part);
    void checkEditIncludePartThemeVariant(IncludePartThemeVariant part) throws EditorSecurityException;
    
    boolean mayCreateIncludePartThemeVariant(IncludePart part, Theme theme);
    void checkCreateIncludePartThemeVariant(IncludePart part, Theme theme) throws EditorSecurityException;
    
    boolean mayEditImage(Image image);
    void checkEditImage(Image image) throws EditorSecurityException;
}
