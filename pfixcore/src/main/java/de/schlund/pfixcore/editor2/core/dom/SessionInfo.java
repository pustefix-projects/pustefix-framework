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

package de.schlund.pfixcore.editor2.core.dom;

import java.util.Date;

import de.schlund.pfixcore.editor2.core.vo.EditorUser;

/**
 * Provides information about an editor user session.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface SessionInfo {
    
    /**
     * Returns the user that is logged in to this session or <code>null</code>
     * if no user is logged in.
     * 
     * @return user logged in to this session
     */
    EditorUser getUser();
    
    /**
     * Return the include part currently selected by the user or
     * <code>null</code> if no include part is selected.
     * 
     * @return include part selected by the user
     */
    IncludePartThemeVariant getIncludePart();
    
    /**
     * Returns the last time the session was used.
     * 
     * @return last time the session was used
     */
    Date getLastAccess();
}
