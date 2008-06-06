/*
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

package de.schlund.pfixcore.editor2.core.vo;

/**
 * Represents the global (not project specific) permissions a user can have
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 * @see de.schlund.pfixcore.editor.EditorUser
 */
public class EditorGlobalPermissions implements Cloneable {
    private boolean admin;
    private boolean editDynIncludes;
    
    /**
     * Default constructor creating object with all permissions disabled.
     */
    public EditorGlobalPermissions() {
        this.admin = false;
        this.editDynIncludes = false;
    }
    
    /**
     * Checks whether the user is admin or not.
     * 
     * @return Returns <code>true</code> if user is admin,
     *         <code>false</code> otherwise
     */
    public boolean isAdmin() {
        return this.admin;
    }
    
    /**
     * Sets admin flag
     * 
     * @param flag
     *            <code>true</code> to set and <code>false</code> to unset
     *            the admin flag for the user.
     */
    public void setAdmin(boolean flag) {
        this.admin = flag;
    }
    
    /**
     * Checks whether the user is allowed to edit DynIncludes.
     * 
     * @return Returns <code>true</code> if user might edit DynIncludes,
     *         <code>false</code> otherwise
     */
    public boolean isEditDynIncludes() {
        return this.editDynIncludes;
    }
    
    /**
     * Sets permission to edit DynIncludes.
     * 
     * @param flag
     *            <code>true</code> to set and <code>false</code> to unset
     *            the permission.
     */
    public void setEditDynIncludes(boolean flag) {
        this.editDynIncludes = flag;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        EditorGlobalPermissions obj = new EditorGlobalPermissions();
        obj.setAdmin(this.isAdmin());
        obj.setEditDynIncludes(this.isEditDynIncludes());
        return obj;
    }
}
