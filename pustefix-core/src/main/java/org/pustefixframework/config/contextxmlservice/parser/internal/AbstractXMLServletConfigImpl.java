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

package org.pustefixframework.config.contextxmlservice.parser.internal;

import org.pustefixframework.config.contextxmlservice.AbstractXMLServletConfig;

public class AbstractXMLServletConfigImpl extends ServletManagerConfigImpl implements AbstractXMLServletConfig {

    private String servletName;

    private boolean editMode;

    private boolean editModeSet = false;

    public void setServletName(String value) {
        this.servletName = value;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.IAbstractXMLServletConfig#getServletName()
     */
    public String getServletName() {
        return this.servletName;
    }

    public void setEditMode(boolean b) {
        this.editMode = b;
        this.editModeSet = true;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.IAbstractXMLServletConfig#isEditMode()
     */
    public boolean isEditMode() {
        // We have to take care to handle the case where the editmode is
        // simply not set for the current servlet. Then we should skip
        // to reading the central property.
        if (this.editModeSet) {
            return this.editMode;
        } else {
            String prop = this.getProperties().getProperty(
                    "xmlserver.noeditmodeallowed");
            if (prop != null && prop.equalsIgnoreCase("false")) {
                return true;
            } else {
                return false;
            }
        }
    }

}
