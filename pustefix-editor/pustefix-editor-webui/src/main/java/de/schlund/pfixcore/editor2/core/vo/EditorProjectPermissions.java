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
 * Represents the permissions an editor user can have on a specific project.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class EditorProjectPermissions implements Cloneable {
    private boolean editIncludes;

    private boolean editImages;

    /**
     * Default constructor creating object with all permissions disabled.
     */
    public EditorProjectPermissions() {
        this.editImages = false;
        this.editIncludes = false;
    }

    /**
     * Checks whether the user is allowed to edit images for the relevant
     * project.
     * 
     * @return Returns <code>true</code> if user might edit images,
     *         <code>false</code> otherwise
     */
    public boolean isEditImages() {
        return editImages;
    }

    /**
     * Set permission to edit images.
     * 
     * @param editImages
     *            <code>true</code> to set and <code>false</code> to unset
     *            the permission.
     */
    public void setEditImages(boolean editImages) {
        this.editImages = editImages;
    }

    /**
     * Checks whether the user is allowed to edit include parts for the relevant
     * project.
     * 
     * @return Returns <code>true</code> if user might edit include parts,
     *         <code>false</code> otherwise
     */
    public boolean isEditIncludes() {
        return editIncludes;
    }

    /**
     * Set permission to edit include parts.
     * 
     * @param editImages
     *            <code>true</code> to set and <code>false</code> to unset
     *            the permission.
     */
    public void setEditIncludes(boolean editIncludes) {
        this.editIncludes = editIncludes;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {
        EditorProjectPermissions obj = new EditorProjectPermissions();
        obj.setEditImages(this.isEditImages());
        obj.setEditIncludes(this.isEditIncludes());
        return obj;
    }
    
}
