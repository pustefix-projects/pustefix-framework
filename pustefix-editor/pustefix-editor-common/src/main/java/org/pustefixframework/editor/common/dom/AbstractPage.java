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

package org.pustefixframework.editor.common.dom;

/**
 * Provides functionality common to all classes implementing Page
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public abstract class AbstractPage implements Page {

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Page page) {
        int ret = this.getHandlerPath().compareTo(page.getHandlerPath());
        if (ret != 0) {
            return ret;
        }
        ret = this.getName().compareTo(page.getName());
        if (ret == 0) {
            if (this.getVariant() == null && page.getVariant() == null) {
                ret = 0;
            } else if (this.getVariant() == null) {
                ret = 1;
            } else if (page.getVariant() == null) {
                ret = -1;
            } else {
                ret = this.getVariant().compareTo(page.getVariant());
            }
        }
        return ret;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Page)) {
            return false;
        }
        Page page = (Page) obj;
        return this.getFullName().equals(page.getFullName())
                && this.getProject().equals(page.getProject());
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return ("PAGE: " + this.toString()).hashCode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.getProject().getName() + "/" + this.getFullName();
    }
    
    /* (non-Javadoc)
     * @see de.schlund.pfixcore.editor2.core.dom.Page#getFullName()
     */
    public String getFullName() {
        if (this.getVariant() == null) {
            return this.getName();
        } else {
            return this.getName() + "::" + this.getVariant().getName();
        }
    }
    
    /* (non-Javadoc)
     * @see de.schlund.pfixcore.editor2.core.dom.Page#hasSubPages()
     */
    public boolean hasSubPages() {
       return (this.getSubPages().size() != 0);
    }
}
