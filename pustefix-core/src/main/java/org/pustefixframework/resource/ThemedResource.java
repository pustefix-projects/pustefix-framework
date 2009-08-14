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

package org.pustefixframework.resource;


/**
 * Represents a resource that has meta-information about the "theme" version
 * of a resource.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface ThemedResource extends Resource {
    /**
     * Returns the theme this resource provides. A theme is a method to 
     * distinguish different variants of the same resource, which should 
     * be used under different circumstances (e.g. different brands).
     * 
     * @return identifier of the theme
     */
    String getTheme();
}
