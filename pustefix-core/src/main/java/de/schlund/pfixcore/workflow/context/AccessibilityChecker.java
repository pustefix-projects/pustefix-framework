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

package de.schlund.pfixcore.workflow.context;

/**
 * Used by {@link de.schlund.pfixcore.workflow.context.SessionContextImpl} for 
 * checking the availability of a page when generating the navigation map.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface AccessibilityChecker {
    boolean isPageAccessible(String pagename) throws Exception;
    boolean isPageAlreadyVisited(String pagename) throws Exception;
}
