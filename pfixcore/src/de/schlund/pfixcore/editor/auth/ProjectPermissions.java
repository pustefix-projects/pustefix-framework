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
*
*/
package de.schlund.pfixcore.editor.auth;

/**
 * Class representing permissions for a single project used in the Pustefix CMS. 
 *
 *<br/>
 *@author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */
public class ProjectPermissions {

    private boolean imageedit = false;
    private boolean includeedit = false;
    private boolean defaultedit = false;

   
    /**
     * Get the permission to edit images for a project.
     * @return true if granted, else false.
     */
    public boolean isEditImages() {
        return imageedit;
    }

    /**
     * Get the permission to edit includes for a project.
     * @return true if granted, else false.
     */
    public boolean isEditIncludes() {
        return includeedit;
    }

    /**
     * Set the permission to edit images for a project.
     * @param b. 
     */
    public void setEditImages(boolean b) {
        imageedit = b;
    }

    /**
     * Set the permission to edit includes for a project.
     * @param b
     */
    public void setEditIncludes(boolean b) {
        includeedit = b;
    }

    /**
     * Retrieve the permission to edit dynamic includes for a project.
     * @return true if granted, else false.
     */
    public boolean isEditDynIncludes() {
        return defaultedit;
    }

    /**
     * Set the permission to edit dynamic includes for a project.
     * @param b
     */
    public void setEditDynIncludes(boolean b) {
        defaultedit = b;
    }
    
    /**
     * Retrieve String representation.
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("edit Includes     = "+includeedit).append("\n");
        sb.append("edit Images       = "+imageedit).append("\n");
        sb.append("edit dyn includes = "+defaultedit).append("\n");
        return sb.toString();
    }

}
