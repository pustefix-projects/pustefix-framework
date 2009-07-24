/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.pustefixframework.extension.support;


/**
 * Stores information about the target extension point of an extension.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ExtensionTargetInfo {

    private String extensionPoint;
    private String version;

    /**
     * Sets the id of the target extension point.
     * 
     * @param extensionPoint identifier of target extension point
     */
    public void setExtensionPoint(String extensionPoint) {
        this.extensionPoint = extensionPoint;
    }

    /**
     * Returns the identifier of the target extension point.
     * 
     * @return target extension point identifier
     */
    public String getExtensionPoint() {
        return extensionPoint;
    }

    /**
     * Sets the target extension point's version or version range.
     * 
     * @param version version or version range
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Returns the target extension point's version or version range.
     * 
     * @return version or version range
     */
    public String getVersion() {
        return version;
    }

}
